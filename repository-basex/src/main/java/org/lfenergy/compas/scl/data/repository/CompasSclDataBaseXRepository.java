// SPDX-FileCopyrightText: 2021 Alliander N.V.
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.compas.scl.data.repository;

import org.lfenergy.compas.scl.data.basex.BaseXClient;
import org.lfenergy.compas.scl.data.basex.BaseXClientFactory;
import org.lfenergy.compas.scl.data.model.Item;
import org.lfenergy.compas.scl.data.model.SclType;
import org.lfenergy.compas.scl.data.model.Version;
import org.lfenergy.compas.scl.data.util.SclDataModelMarshaller;
import org.lfenergy.compas.scl.data.util.SclElementConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static org.lfenergy.compas.scl.data.Constants.*;

@ApplicationScoped
public class CompasSclDataBaseXRepository implements CompasSclDataRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompasSclDataBaseXRepository.class);

    private static final String DECLARE_SCL_NS_URI = "declare namespace scl=\"" + SCL_NS_URI + "\";\n";
    private static final String DECLARE_COMPAS_NAMESPACE = "declare namespace compas=\"" + COMPAS_EXTENSION_NS_URI + "\";\n";
    private static final String DECLARE_LATEST_VERSION_FUNC =
            "declare function local:latest-version($db as xs:string, $id as xs:string)\n" +
                    "   as document-node() { \n" +
                    "      let $doc :=\n" +
                    "          (for $resource in db:open($db, $id)\n" +
                    "            let $parts := tokenize($resource/scl:SCL/scl:Header/@version, '\\.')\n" +
                    "            let $majorVersion := xs:int($parts[1])\n" +
                    "            let $minorVersion := xs:int($parts[2])\n" +
                    "            let $patchVersion := xs:int($parts[3])\n" +
                    "            order by $majorVersion descending, $minorVersion descending, $patchVersion descending\n" +
                    "            return $resource\n" +
                    "          )[1]\n" +
                    "      return $doc\n" +
                    "};\n";
    private static final String DECLARE_DB_VARIABLE = "declare variable $db := '%s';\n";
    private static final String DECLARE_ID_VARIABLE = "declare variable $id := '%s';\n";

    private final BaseXClientFactory baseXClientFactory;
    private final SclDataModelMarshaller sclDataMarshaller;
    private final SclElementConverter sclElementConverter;

    @Inject
    public CompasSclDataBaseXRepository(BaseXClientFactory baseXClientFactory) {
        this.baseXClientFactory = baseXClientFactory;
        
        this.sclDataMarshaller = new SclDataModelMarshaller();
        this.sclElementConverter = new SclElementConverter();

        // At startup create all needed databases.
        Arrays.stream(SclType.values()).forEach(type ->
                executeCommand(client -> {
                    client.executeXQuery(
                            format(DECLARE_DB_VARIABLE, type) +
                                    "if (not(db:exists($db)))\n" +
                                    "   then db:create($db)");
                    return true;
                }));
    }

    @Override
    public List<Item> list(SclType type) {
        return executeQuery(type, DECLARE_SCL_NS_URI +
                        DECLARE_COMPAS_NAMESPACE +
                        DECLARE_LATEST_VERSION_FUNC +
                        format(DECLARE_DB_VARIABLE, type) +
                        "for $resource in db:open($db)\n" +
                        "   let $id := $resource/scl:SCL/scl:Header/@id\n" +
                        "   group by $id\n" +
                        "   let $latestScl := local:latest-version($db, $id)\n" +
                        "   let $version := $latestScl//scl:SCL/scl:Header/@version\n" +
                        "   let $name := $latestScl//scl:SCL/scl:Private[@type='" + COMPAS_SCL_EXTENSION_TYPE + "']/compas:" + COMPAS_SCL_NAME_EXTENSION + "\n" +
                        "   return '<Item xmlns=\"" + SCL_DATA_SERVICE_NS_URI + "\"><Id>' || $id || '</Id><Name>' || $name || '</Name><Version>' || $version || '</Version></Item>'",
                sclDataMarshaller::unmarshal
        );
    }

    @Override
    public List<Item> listVersionsByUUID(SclType type, UUID id) {
        return executeQuery(type, DECLARE_SCL_NS_URI +
                        DECLARE_COMPAS_NAMESPACE +
                        format(DECLARE_DB_VARIABLE, type) +
                        format(DECLARE_ID_VARIABLE, id) +
                        "for $resource in db:open($db, $id)\n" +
                        "   let $id := $resource/scl:SCL/scl:Header/@id\n" +
                        "   let $name := $resource/scl:SCL/scl:Private[@type='" + COMPAS_SCL_EXTENSION_TYPE + "']/compas:" + COMPAS_SCL_NAME_EXTENSION + "\n" +
                        "   let $version := $resource/scl:SCL/scl:Header/@version\n" +
                        "   let $parts := tokenize($version, '\\.')\n" +
                        "   let $majorVersion := xs:int($parts[1])\n" +
                        "   let $minorVersion := xs:int($parts[2])\n" +
                        "   let $patchVersion := xs:int($parts[3])\n" +
                        "   order by $majorVersion, $minorVersion, $patchVersion\n" +
                        "   return '<Item xmlns=\"" + SCL_DATA_SERVICE_NS_URI + "\"><Id>' || $id || '</Id><Name>' || $name || '</Name><Version>' || $version || '</Version></Item>' ",
                sclDataMarshaller::unmarshal
        );
    }

    @Override
    public Element findByUUID(SclType type, UUID id) {
        // This find method always searches for the latest version.
        // Retrieve all versions using db:list-details function.
        // Sort the result descending, this way the last version is the first.
        // Use this path to retrieve the document with the doc function.
        var result = executeQuery(type, DECLARE_SCL_NS_URI +
                        DECLARE_LATEST_VERSION_FUNC +
                        format(DECLARE_DB_VARIABLE, type) +
                        format(DECLARE_ID_VARIABLE, id) +
                        "local:latest-version($db, $id)",
                sclElementConverter::convertToElement);
        return result.get(0);
    }

    @Override
    public Element findByUUID(SclType type, UUID id, Version version) {
        // This find method searches for a specific version.
        var result = executeQuery(type,
                "doc('" + type + "/" + createDocumentPath(id, version) + "')",
                sclElementConverter::convertToElement);
        return result.get(0);
    }

    @Override
    public void create(SclType type, UUID id, Element scl, Version version) {
        var inputStream = new ByteArrayInputStream(sclElementConverter.convertToString(scl).getBytes(StandardCharsets.UTF_8));
        executeCommand(client -> {
            openDatabase(client, type);
            client.add(createDocumentPath(id, version) + "/scl.xml", inputStream);
            closeDatabase(client);
            return true;
        });
    }

    @Override
    public void delete(SclType type, UUID id) {
        executeCommand(client -> {
            client.executeXQuery("db:delete('" + type + "', '" + id + "')");
            return true;
        });
    }

    @Override
    public void delete(SclType type, UUID id, Version version) {
        executeCommand(client -> {
            client.executeXQuery("db:delete('" + type + "', '" + createDocumentPath(id, version) + "')");
            return true;
        });
    }

    private String createDocumentPath(UUID uuid, Version version) {
        return uuid
                + "/" + version.getMajorVersion()
                + "/" + version.getMinorVersion()
                + "/" + version.getPatchVersion();
    }

    private <R> List<R> executeQuery(SclType type, String query, ResultRowMapper<R> mapper) {
        return executeCommand(client -> {
            try {
                var response = new ArrayList<R>();
                openDatabase(client, type);
                LOGGER.debug("Executing Query:\n{}", query);
                try (var queryToRun = client.query(query)) {
                    while (queryToRun.more()) {
                        response.add(mapper.map(queryToRun.next()));
                    }
                }
                closeDatabase(client);
                return response;
            } catch (IOException exception) {
                final var exceptionMessage = exception.getLocalizedMessage();
                LOGGER.error("executeCommand: {}", exceptionMessage);
                throw new SclDataRepositoryException("Error executing command!", exception);
            }
        });
    }

    private void openDatabase(BaseXClient client, SclType type) throws IOException {
        client.execute("OPEN " + type);
    }

    private void closeDatabase(BaseXClient client) throws IOException {
        client.execute("CLOSE");
    }

    private <R> R executeCommand(ClientExecutor<R> command) {
        try (var client = baseXClientFactory.createClient()) {
            return command.execute(client);
        } catch (IOException exception) {
            final var exceptionMessage = exception.getLocalizedMessage();
            LOGGER.error("executeCommand: {}", exceptionMessage);
            throw new SclDataRepositoryException("Error executing command!", exception);
        }
    }

    private interface ClientExecutor<R> {
        R execute(BaseXClient client) throws IOException;
    }

    private interface ResultRowMapper<R> {
        R map(String row);
    }
}
