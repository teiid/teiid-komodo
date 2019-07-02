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
package org.komodo.relational.folder;

import org.komodo.core.KomodoLexicon;
import org.komodo.core.repository.ObjectImpl;
import org.komodo.relational.RelationalObject;
import org.komodo.relational.TypeResolver;
import org.komodo.relational.connection.Connection;
import org.komodo.relational.dataservice.Dataservice;
import org.komodo.relational.folder.internal.FolderImpl;
import org.komodo.relational.model.Schema;
import org.komodo.relational.template.Template;
import org.komodo.relational.vdb.Translator;
import org.komodo.relational.vdb.Vdb;
import org.komodo.spi.KException;
import org.komodo.spi.repository.KomodoObject;
import org.komodo.spi.repository.KomodoType;
import org.komodo.spi.repository.Repository.UnitOfWork;

/**
 * A model of a Folder instance
 */
public interface Folder extends RelationalObject {

    /**
     * The type identifier.
     */
    int TYPE_ID = Folder.class.hashCode();

    /**
     * Identifier of this object
     */
    KomodoType IDENTIFIER = KomodoType.FOLDER;

    /**
     * An empty array of folders.
     */
    Folder[] NO_FOLDERS = new Folder[0];

    /**
     * The resolver of a {@link Folder}.
     */
    TypeResolver< Folder > RESOLVER = new TypeResolver< Folder >() {
    
        /**
         * {@inheritDoc}
         *
         * @see org.komodo.relational.TypeResolver#identifier()
         */
        @Override
        public KomodoType identifier() {
            return IDENTIFIER;
        }
    
        /**
         * {@inheritDoc}
         *
         * @see org.komodo.relational.TypeResolver#owningClass()
         */
        @Override
        public Class< FolderImpl > owningClass() {
            return FolderImpl.class;
        }
    
        /**
         * {@inheritDoc}
         *
         * @see org.komodo.relational.TypeResolver#resolvable(org.komodo.spi.repository.Repository.UnitOfWork,
         *      org.komodo.spi.repository.KomodoObject)
         */
        @Override
        public boolean resolvable( final UnitOfWork transaction,
                                   final KomodoObject kobject ) throws KException {
            return ObjectImpl.validateType( transaction, kobject.getRepository(), kobject, KomodoLexicon.Folder.NODE_TYPE );
        }
    
        /**
         * {@inheritDoc}
         *
         * @see org.komodo.relational.TypeResolver#resolve(org.komodo.spi.repository.Repository.UnitOfWork,
         *      org.komodo.spi.repository.KomodoObject)
         */
        @Override
        public Folder resolve( final UnitOfWork transaction,
                               final KomodoObject kobject ) throws KException {
            if ( kobject.getTypeId() == Folder.TYPE_ID ) {
                return ( Folder )kobject;
            }
            return new FolderImpl( transaction, kobject.getRepository(), kobject.getAbsolutePath() );
        }
    
    };

    /**
     * @param uow
     *        the transaction (cannot be <code>null</code> or have a state that is not
     *        {@link org.komodo.spi.repository.Repository.UnitOfWork.State#NOT_STARTED})
     * @param serviceName
     *        the name of the dataservice to create (cannot be empty)
     * @return the Dataservice object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    Dataservice addDataservice( final UnitOfWork uow,
                                final String serviceName ) throws KException;
    /**
     * @param uow
     *        the transaction (cannot be <code>null</code> or have a state that is not
     *        {@link org.komodo.spi.repository.Repository.UnitOfWork.State#NOT_STARTED})
     * @param connectionName
     *        the name of the connection to create (cannot be empty)
     * @return the connection object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    Connection addConnection( final UnitOfWork uow,
                              final String connectionName ) throws KException;

    /**
     * @param uow
     *        the transaction (cannot be <code>null</code> or have a state that is not
     *        {@link org.komodo.spi.repository.Repository.UnitOfWork.State#NOT_STARTED})
     * @param folderName
     *        the name of the folder to create (cannot be empty)
     * @return the Folder object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    Folder addFolder( final UnitOfWork uow,
                      final String folderName ) throws KException;

    /**
     * @param uow
     *        the transaction (cannot be <code>null</code> or have a state that is not
     *        {@link org.komodo.spi.repository.Repository.UnitOfWork.State#NOT_STARTED})
     * @param schemaName
     *        the name of the schema to create (cannot be empty)
     * @return the schema object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    Schema addSchema( final UnitOfWork uow,
                      final String schemaName ) throws KException;

    /**
     * @param uow
     *        the transaction (cannot be <code>null</code> or have a state that is not
     *        {@link org.komodo.spi.repository.Repository.UnitOfWork.State#NOT_STARTED})
     * @param vdbName
     *        the name of the VDB to create (cannot be empty)
     * @param externalFilePath
     *        the VDB file path on the local file system (cannot be empty)
     * @return the VDB (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    Vdb addVdb( final UnitOfWork uow,
                final String vdbName,
                final String externalFilePath ) throws KException;
    
    /**
     * @param uow
     *        the transaction (cannot be <code>null</code> or have a state that is not
     *        {@link org.komodo.spi.repository.Repository.UnitOfWork.State#NOT_STARTED})
     * @param namePatterns
     *        optional name patterns (can be <code>null</code> or empty but cannot have <code>null</code> or empty elements)
     * @return the connections (never <code>null</code> but can be empty)
     * @throws KException
     *         if an error occurs
     */
    Connection[] getConnections( final UnitOfWork uow,
                                 final String... namePatterns ) throws KException;
    /**
     * @param uow
     *        the transaction (cannot be <code>null</code> or have a state that is not
     *        {@link org.komodo.spi.repository.Repository.UnitOfWork.State#NOT_STARTED})
     * @param namePatterns
     *        optional name patterns (can be <code>null</code> or empty but cannot have <code>null</code> or empty elements)
     * @return the data services (never <code>null</code> but can be empty)
     * @throws KException
     *         if an error occurs
     */
    Dataservice[] getDataservices( final UnitOfWork uow,
                                   final String... namePatterns ) throws KException;
    /**
     * @param uow
     *        the transaction (cannot be <code>null</code> or have a state that is not
     *        {@link org.komodo.spi.repository.Repository.UnitOfWork.State#NOT_STARTED})
     * @param namePatterns
     *        optional name patterns (can be <code>null</code> or empty but cannot have <code>null</code> or empty elements)
     * @return the vdbs (never <code>null</code> but can be empty)
     * @throws KException
     *         if an error occurs
     */
    Vdb[] getVdbs( final UnitOfWork uow,
                   final String... namePatterns ) throws KException;
    /**
     * @param uow
     *        the transaction (cannot be <code>null</code> or have a state that is not
     *        {@link org.komodo.spi.repository.Repository.UnitOfWork.State#NOT_STARTED})
     * @param namePatterns
     *        optional name patterns (can be <code>null</code> or empty but cannot have <code>null</code> or empty elements)
     * @return the schemas (never <code>null</code> but can be empty)
     * @throws KException
     *         if an error occurs
     */
    Schema[] getSchemas( final UnitOfWork uow,
                         final String... namePatterns ) throws KException;
    
    /**
     * @param uow
     *        the transaction (cannot be <code>null</code> or have a state that is not
     *        {@link org.komodo.spi.repository.Repository.UnitOfWork.State#NOT_STARTED})
     * @param namePatterns
     *        optional name patterns (can be <code>null</code> or empty but cannot have <code>null</code> or empty elements)
     * @return the translators (never <code>null</code> but can be empty)
     * @throws KException
     *         if an error occurs
     */
    Translator[] getTranslators( final UnitOfWork uow,
                                 final String... namePatterns ) throws KException;

    /**
     * @param uow
     *        the transaction (cannot be <code>null</code> or have a state that is not
     *        {@link org.komodo.spi.repository.Repository.UnitOfWork.State#NOT_STARTED})
     * @param namePatterns
     *        optional name patterns (can be <code>null</code> or empty but cannot have <code>null</code> or empty elements)
     * @return the templates (never <code>null</code> but can be empty)
     * @throws KException
     *         if an error occurs
     */
    Template[] getTemplates(final UnitOfWork transaction,
                         final String... namePatterns) throws KException;

    /**
     * @param uow
     *        the transaction (cannot be <code>null</code> or have a state that is not
     *        {@link org.komodo.spi.repository.Repository.UnitOfWork.State#NOT_STARTED})
     * @param namePatterns
     *        optional name patterns (can be <code>null</code> or empty but cannot have <code>null</code> or empty elements)
     * @return the folders (never <code>null</code> but can be empty)
     * @throws KException
     *         if an error occurs
     */
    Folder[] getFolders( final UnitOfWork uow,
                         final String... namePatterns ) throws KException;
}
