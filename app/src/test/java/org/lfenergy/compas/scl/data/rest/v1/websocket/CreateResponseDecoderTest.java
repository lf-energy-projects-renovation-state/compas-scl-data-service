// SPDX-FileCopyrightText: 2022 Alliander N.V.
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.compas.scl.data.rest.v1.websocket;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lfenergy.compas.core.commons.exception.CompasException;

import javax.xml.bind.UnmarshalException;

import static org.junit.jupiter.api.Assertions.*;
import static org.lfenergy.compas.core.commons.exception.CompasErrorCode.WEBSOCKET_DECODER_ERROR_CODE;
import static org.lfenergy.compas.scl.data.SclDataServiceConstants.SCL_DATA_SERVICE_V1_NS_URI;

class CreateResponseDecoderTest {
    private CreateResponseDecoder decoder;

    @BeforeEach
    void init() {
        decoder = new CreateResponseDecoder();
        decoder.init(null);
    }

    @Test
    void decode_WhenCalledWithCorrectRequestXML_ThenStringConvertedToObject() {
        var sclData = "Some SCL Data";
        var message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<sds:CreateResponse xmlns:sds=\"" + SCL_DATA_SERVICE_V1_NS_URI + "\">"
                + "<sds:SclData><![CDATA[" + sclData + "]]></sds:SclData>"
                + "</sds:CreateResponse>";

        var result = decoder.decode(message);

        assertNotNull(result);
        assertEquals(sclData, result.getSclData());
    }

    @Test
    void decode_WhenCalledWithWrongXMLType_ThenExceptionThrown() {
        var message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<sds:CreateRequest xmlns:sds=\"" + SCL_DATA_SERVICE_V1_NS_URI + "\">"
                + "</sds:CreateRequest>";

        var exception = assertThrows(CompasException.class, () -> decoder.decode(message));
        assertEquals(WEBSOCKET_DECODER_ERROR_CODE, exception.getErrorCode());
        assertEquals(UnmarshalException.class, exception.getCause().getClass());
    }

    @AfterEach
    void destroy() {
        decoder.destroy();
    }
}
