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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import java.net.URLDecoder;
import org.junit.Before;
import org.junit.Test;
import org.komodo.relational.vdb.DataRole;
import org.komodo.relational.vdb.Permission;
import org.komodo.relational.vdb.Vdb;
import org.komodo.rest.relational.response.RestVdbPermission;
import org.komodo.spi.repository.KomodoType;
import org.mockito.Mockito;

@SuppressWarnings( { "javadoc", "nls" } )
public final class VdbPermissionSerializerTest extends AbstractSerializerTest {

    private static final String DATA_ROLE_NAME = "MyDataRole";
    private static final String DATA_ROLE_DATA_PATH = VDB_DATA_PATH + "/vdbDataRoles/MyDataRole";

    private static final String NAME = "MyPermission";
    private static final String PERM_DATA_PATH = DATA_ROLE_DATA_PATH + "/permissions/" + NAME;

    private static final boolean ALLOW_ALTER = true;
    private static final boolean ALLOW_CREATE = false;
    private static final boolean ALLOW_DELETE = true;
    private static final boolean ALLOW_EXECUTE = false;
    private static final boolean ALLOW_LANGUAGE = true;
    private static final boolean ALLOW_READ = false;
    private static final boolean ALLOW_UPDATE = true;

    private static final String JSON = EMPTY_STRING +
    OPEN_BRACE + NEW_LINE +
    "  \"" + BASE_URI + "\": \"" + MY_BASE_URI + "\"," + NEW_LINE +
    "  \"" + ID + "\": \"" + NAME + "\"," + NEW_LINE +
    "  \"" + DATA_PATH + "\": \"" + PERM_DATA_PATH + "\"," + NEW_LINE +
    "  \"" + KTYPE + "\": \"" + KomodoType.VDB_PERMISSION.getType() + "\"," + NEW_LINE +
    "  \"" + HAS_CHILDREN + "\": true," + NEW_LINE +
    "  \"vdb__permission\": \"" + NAME + "\"," + NEW_LINE +
    "  \"vdb__allowAlter\": true," + NEW_LINE +
    "  \"vdb__allowCreate\": false," + NEW_LINE +
    "  \"vdb__allowDelete\": true," + NEW_LINE +
    "  \"vdb__allowExecute\": false," + NEW_LINE +
    "  \"vdb__allowLanguage\": true," + NEW_LINE +
    "  \"vdb__allowRead\": false," + NEW_LINE +
    "  \"vdb__allowUpdate\": true," + NEW_LINE +
    "  \"" + LINKS + "\": " + OPEN_SQUARE_BRACKET + NEW_LINE +
    "    " + OPEN_BRACE + NEW_LINE +
    "      \"rel\": \"self\"," + NEW_LINE +
    "      \"href\": \"" + BASE_URI_PREFIX + VDB_DATA_PATH + "/VdbDataRoles/MyDataRole/VdbPermissions/MyPermission\"" + NEW_LINE +
    "    " + CLOSE_BRACE + COMMA + NEW_LINE +
    "    " + OPEN_BRACE + NEW_LINE +
    "      \"rel\": \"parent\"," + NEW_LINE +
    "      \"href\": \"" + BASE_URI_PREFIX + VDB_DATA_PATH + "/VdbDataRoles/MyDataRole\"" + NEW_LINE +
    "    " + CLOSE_BRACE + COMMA + NEW_LINE +
    "    " + OPEN_BRACE + NEW_LINE +
    "      \"rel\": \"children\"," + NEW_LINE +
    "      \"href\": \"" + BASE_URI_PREFIX + SEARCH + "parent\\u003d" + PERM_DATA_PATH + "\"" + NEW_LINE +
    "    " + CLOSE_BRACE + COMMA + NEW_LINE +
    "    " + OPEN_BRACE + NEW_LINE +
    "      \"rel\": \"conditions\"," + NEW_LINE +
    "      \"href\": \""+ BASE_URI_PREFIX + VDB_DATA_PATH + "/VdbDataRoles/MyDataRole/VdbPermissions/MyPermission/VdbConditions\"" + NEW_LINE +
    "    " + CLOSE_BRACE + COMMA + NEW_LINE +
    "    " + OPEN_BRACE + NEW_LINE +
    "      \"rel\": \"masks\"," + NEW_LINE +
    "      \"href\": \"" + BASE_URI_PREFIX + VDB_DATA_PATH + "/VdbDataRoles/MyDataRole/VdbPermissions/MyPermission/VdbMasks\"" + NEW_LINE +
    "    " + CLOSE_BRACE + NEW_LINE +
    "  " + CLOSE_SQUARE_BRACKET + NEW_LINE +
    CLOSE_BRACE;

    private RestVdbPermission permission;

    @Before
    public void init() throws Exception {
        Vdb theVdb = mockObject(Vdb.class, VDB_NAME, VDB_DATA_PATH, KomodoType.VDB, true);

        DataRole theDataRole = mockObject(DataRole.class, DATA_ROLE_NAME, DATA_ROLE_DATA_PATH, KomodoType.VDB_DATA_ROLE, true);
        Mockito.when(theDataRole.getParent(transaction)).thenReturn(theVdb);

        Permission thePermission = mockObject(Permission.class, NAME, PERM_DATA_PATH, KomodoType.VDB_PERMISSION, true);
        Mockito.when(thePermission.getParent(transaction)).thenReturn(theDataRole);

        this.permission = new RestVdbPermission(MY_BASE_URI, thePermission, transaction);
        this.permission.setName(NAME);
        this.permission.setAllowAlter( ALLOW_ALTER );
        this.permission.setAllowCreate( ALLOW_CREATE );
        this.permission.setAllowDelete( ALLOW_DELETE );
        this.permission.setAllowExecute( ALLOW_EXECUTE );
        this.permission.setAllowLanguage( ALLOW_LANGUAGE );
        this.permission.setAllowRead( ALLOW_READ );
        this.permission.setAllowUpdate( ALLOW_UPDATE );
    }

    @Test
    public void shouldExportJson() throws Exception {
        String json = KomodoJsonMarshaller.marshall( this.permission );
        json = URLDecoder.decode(json, "UTF-8");
        assertEquals(JSON, json);
    }

    @Test
    public void shouldImportJson() {
        final RestVdbPermission permission = KomodoJsonMarshaller.unmarshall( JSON, RestVdbPermission.class );
        assertThat( permission.getName(), is( NAME ) );
        assertThat( permission.isAllowAlter(), is( ALLOW_ALTER ) );
        assertThat( permission.isAllowCreate(), is( ALLOW_CREATE ) );
        assertThat( permission.isAllowDelete(), is( ALLOW_DELETE ) );
        assertThat( permission.isAllowExecute(), is( ALLOW_EXECUTE ) );
        assertThat( permission.isAllowLanguage(), is( ALLOW_LANGUAGE ) );
        assertThat( permission.isAllowRead(), is( ALLOW_READ ) );
        assertThat( permission.isAllowUpdate(), is( ALLOW_UPDATE ) );
        assertThat( permission.getLinks().size(), is( 5 ) );
        assertThat( permission.getProperties().isEmpty(), is( true ) );
    }

    @Test( expected = Exception.class )
    public void shouldNotExportWhenNameIsMissing() {
        final RestVdbPermission incomplete = new RestVdbPermission();
        KomodoJsonMarshaller.marshall( incomplete );
    }

    @Test( expected = Exception.class )
    public void shouldNotImportJsonWhenIdIsMissing() {
        final String malformed = "{\"allowAlter\":true,\"allowCreate\":false,\"allowDelete\":true,\"allowExecute\":false,\"allowLanguage\":true,\"allowRead\":false,\"allowUpdate\":true,\"conditions\":{\"over\":true,\"the\":false,\"rainbow\":true},\"masks\":{\"this\":\"that\",\"either\":\"or\",\"sixofone\":\"halfdozenofanother\"}}";
        KomodoJsonMarshaller.unmarshall( malformed, RestVdbPermission.class );
    }

}
