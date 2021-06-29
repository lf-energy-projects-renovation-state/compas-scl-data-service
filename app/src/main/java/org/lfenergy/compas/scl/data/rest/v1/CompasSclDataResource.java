// SPDX-FileCopyrightText: 2021 Alliander N.V.
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.compas.scl.data.rest.v1;

import org.lfenergy.compas.scl.SCL;
import org.lfenergy.compas.scl.data.model.SclType;
import org.lfenergy.compas.scl.data.model.Version;
import org.lfenergy.compas.scl.data.rest.model.*;
import org.lfenergy.compas.scl.data.service.CompasSclDataService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import java.util.UUID;

import static org.lfenergy.compas.scl.data.rest.Constants.*;

@Path("/scl/v1/{" + TYPE_PATH_PARAM + "}")
public class CompasSclDataResource {
    private CompasSclDataService compasSclDataService;

    @Inject
    public CompasSclDataResource(CompasSclDataService compasSclDataService) {
        this.compasSclDataService = compasSclDataService;
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public CreateResponse create(@PathParam(TYPE_PATH_PARAM) SclType type,
                                 JAXBElement<CreateRequest> request) {
        var response = new CreateResponse();
        response.setId(compasSclDataService.create(type, request.getValue().getName(), request.getValue().getScl()));
        return response;
    }

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_XML)
    public ListResponse list(@PathParam(TYPE_PATH_PARAM) SclType type) {
        var response = new ListResponse();
        response.setItems(compasSclDataService.list(type));
        return response;
    }

    @GET
    @Path("/{" + ID_PATH_PARAM + "}/versions")
    @Produces(MediaType.APPLICATION_XML)
    public VersionsResponse listVersionsByUUID(@PathParam(TYPE_PATH_PARAM) SclType type,
                                               @PathParam(ID_PATH_PARAM) UUID id) {
        var response = new VersionsResponse();
        response.setItems(compasSclDataService.listVersionsByUUID(type, id));
        return response;
    }

    @GET
    @Path("/{" + ID_PATH_PARAM + "}")
    @Produces(MediaType.APPLICATION_XML)
    public GetResponse findByUUID(@PathParam(TYPE_PATH_PARAM) SclType type,
                                  @PathParam(ID_PATH_PARAM) UUID id) {
        var response = new GetResponse();
        response.setScl(compasSclDataService.findByUUID(type, id));
        return response;
    }

    @GET
    @Path("/{" + ID_PATH_PARAM + "}/{" + VERSION_PATH_PARAM + "}")
    @Produces(MediaType.APPLICATION_XML)
    public GetResponse findByUUIDAndVersion(@PathParam(TYPE_PATH_PARAM) SclType type,
                                            @PathParam(ID_PATH_PARAM) UUID id,
                                            @PathParam(VERSION_PATH_PARAM) Version version) {
        var response = new GetResponse();
        response.setScl(compasSclDataService.findByUUID(type, id, version));
        return response;
    }

    @GET
    @Path("/{" + ID_PATH_PARAM + "}/scl")
    @Produces(MediaType.APPLICATION_XML)
    public SCL findRawSCLByUUID(@PathParam(TYPE_PATH_PARAM) SclType type,
                                @PathParam(ID_PATH_PARAM) UUID id) {
        return compasSclDataService.findByUUID(type, id);
    }

    @GET
    @Path("/{" + ID_PATH_PARAM + "}/{" + VERSION_PATH_PARAM + "}/scl")
    @Produces(MediaType.APPLICATION_XML)
    public SCL findRawSCLByUUIDAndVersion(@PathParam(TYPE_PATH_PARAM) SclType type,
                                          @PathParam(ID_PATH_PARAM) UUID id,
                                          @PathParam(VERSION_PATH_PARAM) Version version) {
        return compasSclDataService.findByUUID(type, id, version);
    }

    @PUT
    @Path("/{" + ID_PATH_PARAM + "}")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public void update(@PathParam(TYPE_PATH_PARAM) SclType type,
                       @PathParam(ID_PATH_PARAM) UUID id,
                       JAXBElement<UpdateRequest> request) {
        compasSclDataService.update(type, id, request.getValue().getChangeSetType(), request.getValue().getScl());
    }

    @DELETE
    @Path("/{" + ID_PATH_PARAM + "}")
    @Produces(MediaType.APPLICATION_XML)
    public void deleteAll(@PathParam(TYPE_PATH_PARAM) SclType type,
                          @PathParam(ID_PATH_PARAM) UUID id) {
        compasSclDataService.delete(type, id);
    }

    @DELETE
    @Path("/{" + ID_PATH_PARAM + "}/{" + VERSION_PATH_PARAM + "}")
    @Produces(MediaType.APPLICATION_XML)
    public void deleteVersion(@PathParam(TYPE_PATH_PARAM) SclType type,
                              @PathParam(ID_PATH_PARAM) UUID id,
                              @PathParam(VERSION_PATH_PARAM) Version version) {
        compasSclDataService.delete(type, id, version);
    }
}