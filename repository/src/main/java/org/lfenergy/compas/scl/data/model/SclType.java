// SPDX-FileCopyrightText: 2021 Alliander N.V.
//
// SPDX-License-Identifier: Apache-2.0

package org.lfenergy.compas.scl.data.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Type of SCL stored. Used for filtering later.")
public enum SclType {
    SSD("Substation Specification Description"),
    IID("IED Instance Description"),
    ICD("IED Capability Description"),
    SCD("Substation Configuration Description"),
    CID("Configured IED Description"),
    SED("System Exchange Description"),
    ISD("IED Specification Description");

    private final String description;

    SclType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
