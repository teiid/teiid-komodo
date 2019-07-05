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
package org.komodo.rest;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import org.komodo.spi.repository.KomodoType;
import org.komodo.utils.ArgCheck;
import org.komodo.utils.StringUtils;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents a navigable web link.
 */
public final class RestLink {

    /**
     * The link relationship type.
     */
    public enum LinkType {

        /**
         * A link to its own resource.
         */
        SELF,

        /**
         * A link to the parent resource.
         */
        PARENT,

        /**
         * A link to the children resource.
         */
        CHILDREN,

        /**
         * A link to the reference resources.
         */
        REFERENCE,

        /**
         * A link to a VDB resource
         */
        VDBS(KomodoType.VDB),

        /**
         * A link to a vdb imports resource
         */
        IMPORTS(KomodoType.VDB_IMPORT),

        /**
         * A Link to a vdb models resource
         */
        MODELS(KomodoType.MODEL),

        /**
         * A link to a vdb table resource
         */
        TABLES(KomodoType.TABLE),

        /**
         * A link to a vdb view resource
         */
        VIEWS(KomodoType.VIEW),

        /**
         * A link to a table column resource
         */
        COLUMNS(KomodoType.COLUMN),

        /**
         * A link to a vdb translators resource
         */
        TRANSLATORS(KomodoType.VDB_TRANSLATOR),

        /**
         * A link to a vdb data roles resource
         */
        DATA_ROLES(KomodoType.VDB_DATA_ROLE),

        /**
         * A link to a model source resource
         */
        SOURCES(KomodoType.VDB_MODEL_SOURCE),

        /**
         * A link to a permission resource
         */
        PERMISSIONS(KomodoType.VDB_PERMISSION),

        /**
         * A link to a condition resource
         */
        CONDITIONS(KomodoType.VDB_CONDITION),

        /**
         * A link to a mask resource
         */
        MASKS(KomodoType.VDB_MASK),

        /**
         * Source VDBs matching service source model jndi
         */
        SOURCE_VDB_MATCHES,
        
        /**
         * Service vdb view info
         */
        SERVICE_VIEW_INFO,

        /**
         * A link to a connection template's entries resource
         */
        TEMPLATE_ENTRIES(KomodoType.TEMPLATE_ENTRY);

        private KomodoType kType;

        private LinkType() {
            this.kType = null;
        }

        private LinkType(KomodoType kType) {
            this.kType = kType;
        }

        /**
         * An empty array of link types.
         */
        public static final LinkType[] NO_LINK_TYPES = new LinkType[ 0 ];

        /**
         * @param text
         *        the text whose enum is being requested (can be empty)
         * @return the link type (never <code>null</code>)
         * @throws RuntimeException
         *         if text is empty or does not represent a link type
         */
        public static LinkType fromString( final String text ) {
            for ( final LinkType type : values() ) {
                if ( type.toString().equals( text ) ) {
                    return type;
                }
            }

            throw new RuntimeException( "Unexpected link type of '" + text + '\'' ); //$NON-NLS-1$
        }

        /**
         * @return the name used for the uri target of the link type.
         *                 Prefers the {@link KomodoType} is one is present
         */
        public String uriName() {
            String name = StringUtils.toLowerCamelCase(name());
            if (kType == null)
                return name;

            return kType.getType() + "s"; //$NON-NLS-1$
        }

        /**
         * {@inheritDoc}
         *
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return StringUtils.toLowerCamelCase(name());
        }

    }

    /**
     * An empty array of links.
     */
    public static final Map<LinkType, RestLink> NO_LINKS = Collections.emptyMap();

    private LinkType rel;
    private URI href;

    /**
     * Constructor for use <strong>only</strong> when deserializing.
     */
    public RestLink() {
        // nothing to do
    }

    /**
     * @param rel
     *        the link type (cannot be <code>null</code>)
     * @param href
     *        the link HREF (cannot be <code>null</code>)
     */
    public RestLink( final LinkType rel,
                     final URI href ) {
        ArgCheck.isNotNull( rel, "rel" ); //$NON-NLS-1$
        ArgCheck.isNotNull( href, "href" ); //$NON-NLS-1$

        this.rel = rel;
        this.href = href;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.href == null) ? 0 : this.href.hashCode());
        result = prime * result + ((this.rel == null) ? 0 : this.rel.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RestLink other = (RestLink)obj;
        if (this.href == null) {
            if (other.href != null)
                return false;
        } else
            if (!this.href.equals(other.href))
                return false;
        if (this.rel != other.rel)
            return false;
        return true;
    }

    /**
     * @return the href the HREF (can be <code>null</code>)
     */
    @ApiModelProperty(required=true)
    public URI getHref() {
        return this.href;
    }

    /**
     * @return the rel link type (can be <code>null</code>)
     */
    @ApiModelProperty(required=true)
    public LinkType getRel() {
        return this.rel;
    }

    /**
     * @param newHref
     *        the new HREF (can be <code>null</code>)
     */
    public void setHref( final URI newHref ) {
        this.href = newHref;
    }

    /**
     * @param newRel
     *        the new link type (can be <code>null</code>)
     */
    public void setRel( final LinkType newRel ) {
        this.rel = newRel;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append( "rel = " ).append( this.rel ); //$NON-NLS-1$
        builder.append( ", " ); //$NON-NLS-1$
        builder.append( "href = " ).append( this.href ); //$NON-NLS-1$

        return builder.toString();
    }

}
