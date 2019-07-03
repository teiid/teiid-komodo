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
package org.komodo.relational.vdb;

import org.komodo.core.repository.ObjectImpl;
import org.komodo.relational.RelationalObject;
import org.komodo.relational.TypeResolver;
import org.komodo.relational.connection.Connection;
import org.komodo.relational.model.Model;
import org.komodo.relational.vdb.internal.ModelSourceImpl;
import org.komodo.spi.KException;
import org.komodo.spi.repository.KomodoObject;
import org.komodo.spi.repository.KomodoType;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.komodo.spi.repository.Repository.UnitOfWork.State;
import org.teiid.modeshape.sequencer.vdb.lexicon.VdbLexicon;

/**
 * Represents a VDB model source.
 */
public interface ModelSource extends RelationalObject {

    /**
     * The type identifier.
     */
    int TYPE_ID = ModelSource.class.hashCode();

    /**
     * Identifier of this object
     */
    KomodoType IDENTIFIER = KomodoType.VDB_MODEL_SOURCE;

    /**
     * An empty array of model sources.
     */
    ModelSource[] NO_SOURCES = new ModelSource[0];

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.repository.KNode#getParent(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    Model getParent( final UnitOfWork transaction ) throws KException;

    /**
     * The resolver of a {@link ModelSource}.
     */
    TypeResolver< ModelSource > RESOLVER = new TypeResolver< ModelSource >() {

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
        public Class< ModelSourceImpl > owningClass() {
            return ModelSourceImpl.class;
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
            return ObjectImpl.validateType( transaction, kobject.getRepository(), kobject, VdbLexicon.Source.SOURCE );
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.relational.TypeResolver#resolve(org.komodo.spi.repository.Repository.UnitOfWork,
         *      org.komodo.spi.repository.KomodoObject)
         */
        @Override
        public ModelSource resolve( final UnitOfWork transaction,
                                    final KomodoObject kobject ) throws KException {
            if ( kobject.getTypeId() == ModelSource.TYPE_ID ) {
                return ( ModelSource )kobject;
            }

            return new ModelSourceImpl( transaction, kobject.getRepository(), kobject.getAbsolutePath() );
        }

    };

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @return the value of the <code>JNDI name</code> property (can be empty)
     * @throws KException
     *         if an error occurs
     */
    String getJndiName( final UnitOfWork transaction ) throws KException;

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @return the value of the <code>translator name</code> property (can be empty)
     * @throws KException
     *         if an error occurs
     */
    String getTranslatorName( final UnitOfWork transaction ) throws KException;

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @return the Connection referenced by this model source or null
     * @throws KException
     *         if an error occurs
     */
    Connection getOriginConnection( final UnitOfWork transaction ) throws KException;

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param newJndiName
     *        the new value of the <code>JNDI name</code> property (can only be empty when removing)
     * @throws KException
     *         if an error occurs
     */
    void setJndiName( final UnitOfWork transaction,
                      final String newJndiName ) throws KException;

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param newTranslatorName
     *        the new value of the <code>translator name</code> property (can only be empty when removing)
     * @throws KException
     *         if an error occurs
     */
    void setTranslatorName( final UnitOfWork transaction,
                            final String newTranslatorName ) throws KException;

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param connection
     *        the connection that this model source references
     * @throws KException
     *         if an error occurs
     */
    void setAssociatedConnection( final UnitOfWork transaction,
                            final Connection connection ) throws KException;
}
