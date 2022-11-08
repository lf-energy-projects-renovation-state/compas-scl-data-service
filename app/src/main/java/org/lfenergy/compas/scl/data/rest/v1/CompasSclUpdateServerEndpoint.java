// SPDX-FileCopyrightText: 2022 Alliander N.V.
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.compas.scl.data.rest.v1;

import io.quarkus.security.Authenticated;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.lfenergy.compas.core.websocket.ErrorResponseEncoder;
import org.lfenergy.compas.scl.data.rest.UserInfoProperties;
import org.lfenergy.compas.scl.data.rest.v1.event.UpdateEventRequest;
import org.lfenergy.compas.scl.data.rest.v1.model.UpdateRequest;
import org.lfenergy.compas.scl.data.rest.v1.websocket.UpdateRequestDecoder;
import org.lfenergy.compas.scl.data.rest.v1.websocket.UpdateResponseEncoder;
import org.lfenergy.compas.scl.extensions.model.SclFileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.UUID;

import static org.lfenergy.compas.scl.data.rest.Constants.ID_PATH_PARAM;
import static org.lfenergy.compas.scl.data.rest.Constants.TYPE_PATH_PARAM;

@Authenticated
@ApplicationScoped
@ServerEndpoint(value = "/scl-ws/v1/{" + TYPE_PATH_PARAM + "}/update/{" + ID_PATH_PARAM + "}",
        decoders = {UpdateRequestDecoder.class},
        encoders = {UpdateResponseEncoder.class, ErrorResponseEncoder.class})
public class CompasSclUpdateServerEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompasSclUpdateServerEndpoint.class);

    private final EventBus eventBus;

    @Inject
    JsonWebToken jsonWebToken;

    @Inject
    UserInfoProperties userInfoProperties;

    @Inject
    public CompasSclUpdateServerEndpoint(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam(TYPE_PATH_PARAM) String type) {
        LOGGER.debug("Starting session {} for type {}.", session.getId(), type);
    }

    @OnMessage
    public void onUpdateMessage(Session session, UpdateRequest request,
                                @PathParam(TYPE_PATH_PARAM) String type,
                                @PathParam(ID_PATH_PARAM) String id) {
        LOGGER.info("Message from session {} for type {}.", session.getId(), type);

        String who = jsonWebToken.getClaim(userInfoProperties.who());
        LOGGER.trace("Username used for Who {}", who);

        eventBus.send("update-ws", new UpdateEventRequest(
                session, SclFileType.valueOf(type), UUID.fromString(id), request.getChangeSetType(),
                who, request.getComment(), request.getSclData()));
    }

    @OnError
    public void onError(Session session, @PathParam(TYPE_PATH_PARAM) String type, Throwable throwable) {
        LOGGER.warn("Error with session {} for type {}.", session.getId(), type, throwable);
    }

    @OnClose
    public void onClose(Session session, @PathParam(TYPE_PATH_PARAM) String type) {
        LOGGER.debug("Closing session {} for type {}.", session.getId(), type);
    }
}
