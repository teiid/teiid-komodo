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
package org.komodo.rest.relational.response;

import java.net.URI;
import java.util.Properties;
import org.komodo.relational.vdb.DataRole;
import org.komodo.relational.vdb.Mask;
import org.komodo.relational.vdb.Permission;
import org.komodo.relational.vdb.Vdb;
import org.komodo.rest.KomodoService;
import org.komodo.rest.RestBasicEntity;
import org.komodo.rest.RestLink;
import org.komodo.rest.RestLink.LinkType;
import org.komodo.rest.relational.KomodoRestUriBuilder.SettingNames;
import org.komodo.spi.KException;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.komodo.utils.ArgCheck;
import org.teiid.modeshape.sequencer.vdb.lexicon.VdbLexicon;

/**
 * A condition that can be used by GSON to build a JSON document representation.
 */
public final class RestVdbMask extends RestBasicEntity {

    /**
     * Label used to describe name
     */
    public static final String NAME_LABEL = KomodoService.protectPrefix(VdbLexicon.DataRole.Permission.Mask.MASK);

    /**
     * Label used to describe order
     */
    public static final String ORDER_LABEL = KomodoService.protectPrefix(VdbLexicon.DataRole.Permission.Mask.ORDER);

    /**
     * An empty array of masks.
     */
    public static final RestVdbMask[] NO_MASKS = new RestVdbMask[ 0 ];

    /**
     * Constructor for use <strong>only</strong> when deserializing.
     */
    public RestVdbMask() {
        // nothing to do
    }

    /**
     * Constructor for use when serializing.
     * @param baseUri the base uri of the REST request
     * @param mask the mask
     * @param uow the transaction
     * @throws KException if error occurs
     */
    public RestVdbMask(URI baseUri, Mask mask, UnitOfWork uow) throws KException {
        super(baseUri, mask, uow);

        setName(mask.getName(uow));
        setOrder(mask.getOrder(uow));

        Permission permission = ancestor(mask, Permission.class, uow);
        ArgCheck.isNotNull(permission);
        String permName = permission.getName(uow);

        DataRole dataRole = ancestor(permission, DataRole.class, uow);
        ArgCheck.isNotNull(dataRole);
        String dataRoleName = dataRole.getName(uow);

        Vdb vdb = ancestor(dataRole, Vdb.class, uow);
        ArgCheck.isNotNull(vdb);
        String vdbName = vdb.getName(uow);

        Properties settings = getUriBuilder().createSettings(SettingNames.VDB_NAME, vdbName);
        getUriBuilder().addSetting(settings, SettingNames.VDB_PARENT_PATH, getUriBuilder().vdbParentUri(vdb, uow));
        getUriBuilder().addSetting(settings, SettingNames.DATA_ROLE_ID, dataRoleName);
        getUriBuilder().addSetting(settings, SettingNames.PERMISSION_ID, permName);
        getUriBuilder().addSetting(settings, SettingNames.PERMISSION_CHILD_TYPE, LinkType.MASKS.uriName());
        getUriBuilder().addSetting(settings, SettingNames.PERMISSION_CHILD_ID, getId());

        addLink(new RestLink(LinkType.SELF, getUriBuilder()
                             .vdbPermissionChildUri(LinkType.SELF, settings)));
        addLink(new RestLink(LinkType.PARENT, getUriBuilder()
                             .vdbPermissionChildUri(LinkType.PARENT, settings)));
    }

    /**
     * @return the name (can be empty)
     */
    public String getName() {
        Object value = tuples.get(NAME_LABEL);
        return value != null ? value.toString() : null;
    }

    /**
     * @param newName
     *        the new mask name (can be empty)
     */
    public void setName( final String newName ) {
        tuples.put(NAME_LABEL, newName);
    }

    /**
     * @return the order
     */
    public String getOrder() {
        Object value = tuples.get(ORDER_LABEL);
        return value != null ? value.toString() : null;
    }

    /**
     * @param order the order to set
     */
    public void setOrder(String order) {
        tuples.put(ORDER_LABEL, order);
    }
}
