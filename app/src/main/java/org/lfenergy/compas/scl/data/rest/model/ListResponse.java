// SPDX-FileCopyrightText: 2021 Alliander N.V.
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.compas.scl.data.rest.model;

import org.lfenergy.compas.scl.data.model.Item;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static org.lfenergy.compas.scl.data.Constants.SCL_DATA_SERVICE_NS_URI;

@XmlRootElement(name = "ListResponse", namespace = SCL_DATA_SERVICE_NS_URI)
@XmlAccessorType(XmlAccessType.FIELD)
public class ListResponse {
    @XmlElement(name = "Item", namespace = SCL_DATA_SERVICE_NS_URI)
    private List<Item> items;

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
}
