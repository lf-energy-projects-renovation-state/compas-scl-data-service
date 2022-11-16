// SPDX-FileCopyrightText: 2022 Alliander N.V.
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.compas.scl.data.websocket.v1.encoder;

import org.lfenergy.compas.core.websocket.AbstractEncoder;
import org.lfenergy.compas.core.websocket.WebsocketSupport;
import org.lfenergy.compas.scl.data.websocket.v1.model.GetVersionWsRequest;

public class GetVersionWsRequestEncoder extends AbstractEncoder<GetVersionWsRequest> {
    @Override
    public String encode(GetVersionWsRequest jaxbObject) {
        return WebsocketSupport.encode(jaxbObject, GetVersionWsRequest.class);
    }
}
