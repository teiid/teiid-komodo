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
package org.komodo.relational.model;

import org.komodo.core.repository.ObjectImpl;
import org.komodo.relational.TypeResolver;
import org.komodo.relational.model.internal.TabularResultSetImpl;
import org.komodo.spi.KException;
import org.komodo.spi.lexicon.ddl.teiid.TeiidDdlLexicon.CreateProcedure;
import org.komodo.spi.repository.KomodoObject;
import org.komodo.spi.repository.KomodoType;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.komodo.spi.repository.Repository.UnitOfWork.State;

/**
 * Represents a tabular result set.
 */
public interface TabularResultSet extends ProcedureResultSet {

    /**
     * Identifier of this object.
     */
    KomodoType IDENTIFIER = KomodoType.TABULAR_RESULT_SET;

    /**
     * The type identifier.
     */
    int TYPE_ID = TabularResultSet.class.hashCode();

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.repository.KNode#getParent(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    AbstractProcedure getParent( final UnitOfWork transaction ) throws KException;

    /**
     * The resolver of a {@link TabularResultSet}.
     */
    TypeResolver< TabularResultSet > RESOLVER = new TypeResolver< TabularResultSet >() {

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
        public Class< TabularResultSetImpl > owningClass() {
            return TabularResultSetImpl.class;
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
            // must have the right name
            if ( CreateProcedure.RESULT_SET.equals( kobject.getName( transaction ) ) ) {
                return ObjectImpl.validateType( transaction, kobject.getRepository(), kobject, CreateProcedure.RESULT_COLUMNS );
            }

            return false;
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.relational.TypeResolver#resolve(org.komodo.spi.repository.Repository.UnitOfWork,
         *      org.komodo.spi.repository.KomodoObject)
         */
        @Override
        public TabularResultSet resolve( final UnitOfWork transaction,
                                         final KomodoObject kobject ) throws KException {
            if ( kobject.getTypeId() == TabularResultSet.TYPE_ID ) {
                return ( TabularResultSet )kobject;
            }

            return new TabularResultSetImpl( transaction, kobject.getRepository(), kobject.getAbsolutePath() );
        }

    };

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param columnName
     *        the name of the column being added (cannot be empty)
     * @return the new column (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    ResultSetColumn addColumn( final UnitOfWork transaction,
                               final String columnName ) throws KException;

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @return the columns (never <code>null</code> but can be empty)
     * @throws KException
     *         if an error occurs
     */
    ResultSetColumn[] getColumns( final UnitOfWork transaction ) throws KException;

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param columnToRemove
     *        the name of the column being removed (cannot be empty)
     * @throws KException
     *         if an error occurs
     */
    void removeColumn( final UnitOfWork transaction,
                       final String columnToRemove ) throws KException;

    /**
     * <p>
     * <strong><em>Rename is not allowed!!</em></strong>
     *
     * @see org.komodo.spi.repository.KomodoObject#rename(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String)
     * @throws UnsupportedOperationException if called
     */
    @Override
    public void rename( final UnitOfWork transaction,
                        final String newName ) throws UnsupportedOperationException;

}
