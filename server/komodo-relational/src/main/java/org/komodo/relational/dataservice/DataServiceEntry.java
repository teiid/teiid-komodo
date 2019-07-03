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
package org.komodo.relational.dataservice;

import java.util.Properties;

import org.komodo.relational.Messages;
import org.komodo.relational.Messages.Relational;
import org.komodo.relational.RelationalObject;
import org.komodo.spi.KException;
import org.komodo.spi.constants.StringConstants;
import org.komodo.spi.repository.DocumentType;
import org.komodo.spi.repository.Exportable;
import org.komodo.spi.repository.Property;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.komodo.spi.repository.Repository.UnitOfWork.State;
import org.komodo.utils.StringUtils;
import org.teiid.modeshape.sequencer.dataservice.lexicon.DataVirtLexicon;

/**
 * Represents an entry in a data service archive.
 *
 * @param <T>
 *        the entry type
 */
public interface DataServiceEntry< T extends Exportable & RelationalObject > extends Exportable, RelationalObject {

    /**
     * Indicates how the entry should uploaded and/or deployed.
     */
    public enum PublishPolicy {

        /**
         * Always publish the file.
         */
        ALWAYS( "always" ),

        /**
         * Only publish if not already published.
         */
        IF_MISSING( "ifMissing" ),

        /**
         * Never publish the file.
         */
        NEVER( "never" );

        /**
         * The default policy.
         *
         * @see #IF_MISSING
         */
        public static final PublishPolicy DEFAULT = IF_MISSING;

        /**
         * @param xml the XML value whose type is being requested (can be <code>null</code> or empty)
         * @return the appropriate type or <code>null</code> if not found
         */
        public static PublishPolicy fromXml( final String xml ) {
            for ( final PublishPolicy type : values() ) {
                if ( type.xml.equals( xml ) ) {
                    return type;
                }
            }

            return null;
        }

        private final String xml;

        private PublishPolicy( final String xmlValue ) {
            this.xml = xmlValue;
        }

        /**
         * @return the value appropriate for an XML document (never <code>null</code> or empty)
         */
        public String toXml() {
            return this.xml;
        }

    }

    /**
     * Empty resource content.
     */
    byte[] NO_CONTENT = new byte[0];

    /**
     * @return the archive path segment where the resource should be archived (can be <code>null</code> or empty if the resource
     *         should be located at the archive root)
     */
    String getArchiveFolder();

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.repository.Exportable#export(org.komodo.spi.repository.Repository.UnitOfWork, java.util.Properties)
     */
    @Override
    default byte[] export( final UnitOfWork transaction,
                           final Properties properties ) throws KException {
        final T resource = getReference( transaction );

        if ( resource == null ) {
            if ( getPublishPolicy( transaction ) != PublishPolicy.NEVER ) {
                throw new KException( Messages.getString( Relational.EXPORT_FAILED_NO_CONTENT, getAbsolutePath() ) );
            }

            return NO_CONTENT;
        }

        return resource.export( transaction, properties );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.repository.Exportable#getDocumentType(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    default DocumentType getDocumentType( final UnitOfWork transaction ) throws KException {
        final T ref = getReference( transaction );

        if ( ref == null ) {
            return DocumentType.UNKNOWN;
        }

        return ref.getDocumentType( transaction );
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> and must have a state of {@link State#NOT_STARTED})
     * @return the entry path (can be <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    default String getEntryPath( final UnitOfWork transaction ) throws KException {
        if ( hasProperty( transaction, DataVirtLexicon.ResourceEntry.PATH ) ) {
            return getProperty( transaction, DataVirtLexicon.ResourceEntry.PATH ).getStringValue( transaction );
        }

        final T file = getReference( transaction );
        String folder = getArchiveFolder();

        if ( StringUtils.isBlank( folder ) ) {
            if ( folder == null ) {
                folder = StringConstants.EMPTY_STRING;
            }
        } else if ( !folder.endsWith( StringConstants.FORWARD_SLASH ) ) {
            folder += StringConstants.FORWARD_SLASH;
        }

        if ( file != null ) {
            return ( folder + file.getDocumentType( transaction ).fileName( file.getName( transaction ) ) );
        }

        return ( folder + getName( transaction ) );
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> and must have a state of {@link State#NOT_STARTED})
     * @return the entry's publish policy (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    default PublishPolicy getPublishPolicy( final UnitOfWork transaction ) throws KException {
        if ( hasProperty( transaction, DataVirtLexicon.DataServiceEntry.PUBLISH_POLICY ) ) {
            final String value = getProperty( transaction,
                                              DataVirtLexicon.DataServiceEntry.PUBLISH_POLICY ).getStringValue( transaction );
            return PublishPolicy.valueOf( value );
        }

        return PublishPolicy.DEFAULT;
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> and must have a state of {@link State#NOT_STARTED})
     * @return the referenced object or <code>null</code> if none exists
     * @throws KException
     *         if an error occurs
     */
    T getReference( final UnitOfWork transaction ) throws KException;

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> and must have a state of {@link State#NOT_STARTED})
     * @param newEntryPath
     *        the new entry path (can be <code>null</code> or empty)
     * @throws KException
     *         if an error occurs
     */
    default void setEntryPath( final UnitOfWork transaction,
                               final String newEntryPath ) throws KException {
        setProperty( transaction, DataVirtLexicon.DataServiceEntry.PATH, newEntryPath );
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> and must have a state of {@link State#NOT_STARTED})
     * @param newPublishPolicy
     *        the new publish policy (can be <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    default void setPublishPolicy( final UnitOfWork transaction,
                                   final PublishPolicy newPublishPolicy ) throws KException {
        String value = ( ( newPublishPolicy == null ) ? null : newPublishPolicy.name() );
        setProperty( transaction, DataVirtLexicon.DataServiceEntry.PUBLISH_POLICY, value );
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> and must have a state of {@link State#NOT_STARTED})
     * @param reference
     *        the referenced object or <code>null</code> if removing an existing reference
     * @throws KException
     *         if an error occurs
     */
    default void setReference( final UnitOfWork transaction,
                               final T reference ) throws KException {
        String refId = null;

        if ( reference != null ) {
            Property uuidProperty = getObjectFactory().getId(transaction, reference);
            if (uuidProperty == null) {
                String msg = Messages.getString(Messages.Relational.NO_UUID_PROPERTY, reference.getName(transaction));
                throw new KException(msg);
            }

            refId = uuidProperty.getStringValue( transaction );
        }

        setProperty( transaction, DataVirtLexicon.DataServiceEntry.SOURCE_RESOURCE, refId );
    }

}
