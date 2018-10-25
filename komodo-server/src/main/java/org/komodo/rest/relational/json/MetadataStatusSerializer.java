/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */
package org.komodo.rest.relational.json;

import static org.komodo.rest.relational.json.KomodoJsonMarshaller.BUILDER;
import java.io.IOException;
import org.komodo.rest.relational.response.RestConnectionDriver;
import org.komodo.rest.relational.response.metadata.RestMetadataStatus;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class MetadataStatusSerializer extends AbstractEntitySerializer<RestMetadataStatus> {

    @Override
    protected RestMetadataStatus createEntity() {
        return new RestMetadataStatus();
    }

    @Override
    protected String readExtension(String name, RestMetadataStatus status, JsonReader in) {
        if (RestMetadataStatus.DATA_SOURCE_DRIVERS_LABEL.equals(name)) {
            RestConnectionDriver[] drivers = BUILDER.fromJson(in, RestConnectionDriver[].class);
            status.setDataSourceDrivers(drivers);
            return name;
        }

        return null;
    }

    @Override
    protected void writeExtensions(JsonWriter out, RestMetadataStatus entity) throws IOException {
        out.name(RestMetadataStatus.DATA_SOURCE_DRIVERS_LABEL);
        BUILDER.toJson(entity.getDataSourceDrivers().toArray(new RestConnectionDriver[0]), RestConnectionDriver[].class, out);
    }

    @Override
    protected boolean isComplete(RestMetadataStatus entity) {
        return entity.getVersion() != null;
    }
}
