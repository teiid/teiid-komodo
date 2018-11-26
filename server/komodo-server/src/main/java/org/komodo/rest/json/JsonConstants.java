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
package org.komodo.rest.json;

import org.komodo.spi.constants.StringConstants;

/**
 * Identifiers used in Komodo REST object JSON representations.
 */
public interface JsonConstants extends StringConstants {

    /**
     * Json-safe separator between prefix and attribute name
     */
    String PREFIX_SEPARATOR = UNDERSCORE + UNDERSCORE;

    /**
     * RegExp pattern for determining a property's prefix and name
     */
    String PREFIX_PATTERN = "([a-zA-Z]+):(.*)"; //$NON-NLS-1$

    /**
     * KEngine prefix
     */
    String KENGINE_PREFIX = "keng" + PREFIX_SEPARATOR; //$NON-NLS-1$

    /**
     * id property
     */
    String ID = KENGINE_PREFIX + "id"; //$NON-NLS-1$

    /**
     * id property
     */
    String BASE_URI = KENGINE_PREFIX + "baseUri"; //$NON-NLS-1$

    /**
     * path property
     */
    String DATA_PATH = KENGINE_PREFIX + "dataPath"; //$NON-NLS-1$

    /**
     * type property
     */
    String TYPE = KENGINE_PREFIX + "type"; //$NON-NLS-1$

    /**
     * kengine type property
     */
    String KTYPE = KENGINE_PREFIX + "kType"; //$NON-NLS-1$

    /**
     * has-children property
     */
    String HAS_CHILDREN = KENGINE_PREFIX + "hasChildren"; //$NON-NLS-1$

    /**
     * links property
     */
    String LINKS = KENGINE_PREFIX + "_links"; //$NON-NLS-1$

    /**
     * default property
     */
    String DEFAULT_VALUE = KENGINE_PREFIX + "defaultValue"; //$NON-NLS-1$

    /**
     * description property
     */
    String DESCRIPTION = KENGINE_PREFIX + "description"; //$NON-NLS-1$

    /**
     * properties attribute
     */
    String PROPERTIES = KENGINE_PREFIX + "properties"; //$NON-NLS-1$

    /**
     * Required property
     */
    String REQUIRED = KENGINE_PREFIX + "required"; //$NON-NLS-1$

    /**
     * Repeatable property
     */
    String REPEATABLE = KENGINE_PREFIX + "repeatable"; //$NON-NLS-1$

    /**
     * Limit property
     */
    String LIMIT = KENGINE_PREFIX + "limit"; //$NON-NLS-1$

    /**
     * Values property
     */
    String VALUES = KENGINE_PREFIX + "values"; //$NON-NLS-1$

    /**
     * Children property
     */
    String CHILDREN = KENGINE_PREFIX + "children"; //$NON-NLS-1$

    /**
     * DDL property
     */
    String DDL_ATTRIBUTE = KENGINE_PREFIX + "ddl"; //$NON-NLS-1$

    /**
     * relational property
     */
    String REL = "rel"; //$NON-NLS-1$

    /**
     * self link value
     */
    String SELF = "self"; //$NON-NLS-1$

    /**
     * href value
     */
    String HREF = "href"; //$NON-NLS-1$

    /**
     * parent link value
     */
    String PARENT = "parent"; //$NON-NLS-1$

    /**
     * null value
     */
    String NULL = "null"; //$NON-NLS-1$
}
