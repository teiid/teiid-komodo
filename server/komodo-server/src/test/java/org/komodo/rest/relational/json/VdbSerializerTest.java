/*
 * Copyright Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags and
 * the COPYRIGHT.txt file distributed with this work.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.komodo.rest.relational.json;

import static org.junit.Assert.assertEquals;
import java.net.URLDecoder;
import org.junit.Before;
import org.junit.Test;
import org.komodo.relational.vdb.Vdb;
import org.komodo.rest.relational.response.RestVdb;
import org.komodo.spi.repository.KomodoObject;
import org.komodo.spi.repository.KomodoType;
import org.komodo.spi.repository.PropertyDescriptor;
import org.mockito.Mockito;

@SuppressWarnings( { "javadoc", "nls" } )
public final class VdbSerializerTest extends AbstractSerializerTest  {

    private static final String DESCRIPTION = "my description";
    private static final String ORIGINAL_FILE = "/Users/ElvisIsKing/MyVdb.xml";
    private static final KomodoType kType = KomodoType.VDB;
    private static final String CONNECTION_TYPE = "BY_VERSION";
    private static final int VERSION = 1;

    private static final String JSON = OPEN_BRACE + NEW_LINE +
        "  \"" + BASE_URI + "\": \"" + MY_BASE_URI + "\"," + NEW_LINE +
        "  \"keng__id\": \"" + VDB_NAME + "\"," + NEW_LINE +
        "  \"keng__dataPath\": \"" + VDB_DATA_PATH + "\"," + NEW_LINE +
        "  \"keng__kType\": \"Vdb\"," + NEW_LINE +
        "  \"keng__hasChildren\": true," + NEW_LINE +
        "  \"vdb__name\": \"" + VDB_NAME + "\"," + NEW_LINE +
        "  \"vdb__description\": \"my description\"," + NEW_LINE +
        "  \"vdb__originalFile\": \"/Users/ElvisIsKing/MyVdb.xml\"," + NEW_LINE +
        "  \"vdb__preview\": false," + NEW_LINE +
        "  \"vdb__connectionType\": \"BY_VERSION\"," + NEW_LINE +
        "  \"vdb__version\": 1," + NEW_LINE +
        "  \"keng___links\": [" + NEW_LINE +
        "    " + OPEN_BRACE + NEW_LINE +
        "      \"rel\": \"self\"," + NEW_LINE +
        "      \"href\": \"" + BASE_URI_PREFIX + VDB_DATA_PATH + "\"" + NEW_LINE +
        "    " + CLOSE_BRACE + COMMA + NEW_LINE +
        "    " + OPEN_BRACE + NEW_LINE +
        "      \"rel\": \"parent\"," + NEW_LINE +
        "      \"href\": \"" + BASE_URI_PREFIX + "/workspace/vdbs\"" + NEW_LINE +
        "    " + CLOSE_BRACE + COMMA + NEW_LINE +
        "    " + OPEN_BRACE + NEW_LINE +
        "      \"rel\": \"imports\"," + NEW_LINE +
        "      \"href\": \"" + BASE_URI_PREFIX + VDB_DATA_PATH + "/VdbImports\"" + NEW_LINE +
        "    " + CLOSE_BRACE + COMMA + NEW_LINE +
        "    " + OPEN_BRACE + NEW_LINE +
        "      \"rel\": \"models\"," + NEW_LINE +
        "      \"href\": \"" + BASE_URI_PREFIX + VDB_DATA_PATH + "/Models\"" + NEW_LINE +
        "    " + CLOSE_BRACE + COMMA + NEW_LINE +
        "    " + OPEN_BRACE + NEW_LINE +
        "      \"rel\": \"translators\"," + NEW_LINE +
        "      \"href\": \"" + BASE_URI_PREFIX + VDB_DATA_PATH + "/VdbTranslators\"" + NEW_LINE +
        "    " + CLOSE_BRACE + COMMA + NEW_LINE +
        "    " + OPEN_BRACE + NEW_LINE +
        "      \"rel\": \"dataRoles\"," + NEW_LINE +
        "      \"href\": \"" + BASE_URI_PREFIX + VDB_DATA_PATH + "/VdbDataRoles\"" + NEW_LINE +
        "    " + CLOSE_BRACE + NEW_LINE +
        "  " + CLOSE_SQUARE_BRACKET + NEW_LINE +
        CLOSE_BRACE;


    private RestVdb vdb;

    @Before
    public void init() throws Exception {
        KomodoObject workspace = Mockito.mock(KomodoObject.class);
        Mockito.when(workspace.getAbsolutePath()).thenReturn(WORKSPACE_DATA_PATH);

        Vdb theVdb = mockObject(Vdb.class, VDB_NAME, VDB_DATA_PATH, kType, true);
        Mockito.when(theVdb.getPropertyNames(transaction)).thenReturn(new String[0]);
        Mockito.when(theVdb.getPropertyDescriptors(transaction)).thenReturn(new PropertyDescriptor[0]);
        Mockito.when(theVdb.getParent(transaction)).thenReturn(workspace);

        this.vdb = new RestVdb(MY_BASE_URI, theVdb, false, transaction);
        this.vdb.setName(VDB_NAME);
        this.vdb.setDescription(DESCRIPTION);
        this.vdb.setOriginalFilePath(ORIGINAL_FILE);
        this.vdb.setConnectionType(CONNECTION_TYPE);
        this.vdb.setPreview(false);
        this.vdb.setVersion(VERSION);
    }

    @Test
    public void shouldExportJson() throws Exception {
        String json = KomodoJsonMarshaller.marshall( this.vdb );
        json = URLDecoder.decode(json, "UTF-8");
        assertEquals(JSON, json);
    }

    @Test
    public void shouldImportJson() {
        final RestVdb descriptor = KomodoJsonMarshaller.unmarshall( JSON, RestVdb.class );
        assertEquals(VDB_NAME, descriptor.getName());
        assertEquals(DESCRIPTION, descriptor.getDescription());
        assertEquals(ORIGINAL_FILE, descriptor.getOriginalFilePath());
        assertEquals(6, descriptor.getLinks().size());
        assertEquals(true, descriptor.getProperties().isEmpty());
    }

    @Test( expected = Exception.class )
    public void shouldNotExportJsonWhenNameIsMissing() {
        final RestVdb descriptor = new RestVdb();
        KomodoJsonMarshaller.marshall( descriptor );
    }

    @Test( expected = Exception.class )
    public void shouldNotImportJsonWhenIdIsMissing() {
        final String malformed = "{\"description\":\"my description\",\"links\":[{\"rel\":\"self\",\"href\":\"http://localhost:8080/v1/workspace/vdbs/MyVdb\",\"method\":\"GET\"},{\"rel\":\"parent\",\"href\":\"http://localhost:8080/v1/workspace/vdbs\",\"method\":\"GET\"},{\"rel\":\"manifest\",\"href\":\"http://localhost:8080/v1/workspace/vdbs/MyVdb/manifest\",\"method\":\"GET\"}]}";
        KomodoJsonMarshaller.unmarshall( malformed, RestVdb.class );
    }

}
