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
import org.komodo.relational.vdb.internal.ConditionImpl;
import org.komodo.spi.KException;
import org.komodo.spi.repository.KomodoObject;
import org.komodo.spi.repository.KomodoType;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.komodo.spi.repository.Repository.UnitOfWork.State;
import org.komodo.spi.lexicon.vdb.VdbLexicon;

/**
 * Represents a VDB permission condition.
 */
public interface Condition extends RelationalObject {

    /**
     * The type identifier.
     */
    int TYPE_ID = Condition.class.hashCode();

    /**
     * Identifier of this object
     */
    KomodoType IDENTIFIER = KomodoType.VDB_CONDITION;

    /**
     * The default value indicating if this condition is a constraint. Value is {@value} .
     */
    boolean DEFAULT_CONSTRAINT = true;

    /**
     * An empty array of conditions.
     */
    Condition[] NO_CONDITIONS = new Condition[0];

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.repository.KNode#getParent(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    Permission getParent( final UnitOfWork transaction ) throws KException;

    /**
     * The resolver of a {@link Condition}.
     */
    TypeResolver< Condition > RESOLVER = new TypeResolver< Condition >() {

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
        public Class< ConditionImpl > owningClass() {
            return ConditionImpl.class;
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
            return ObjectImpl.validateType( transaction,
                                            kobject.getRepository(),
                                            kobject,
                                            VdbLexicon.DataRole.Permission.Condition.CONDITION );
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.relational.TypeResolver#resolve(org.komodo.spi.repository.Repository.UnitOfWork,
         *      org.komodo.spi.repository.KomodoObject)
         */
        @Override
        public Condition resolve( final UnitOfWork transaction,
                                  final KomodoObject kobject ) throws KException {
            if ( kobject.getTypeId() == Condition.TYPE_ID ) {
                return ( Condition )kobject;
            }

            return new ConditionImpl( transaction, kobject.getRepository(), kobject.getAbsolutePath() );
        }

    };

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @return <code>true</code> if this condition is a constraint
     * @throws KException
     *         if an error occurs
     * @see #DEFAULT_CONSTRAINT
     */
    boolean isConstraint( final UnitOfWork transaction ) throws KException;

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param newConstraint
     *        the new value for the <code>constraint</code> property
     * @throws KException
     *         if an error occurs
     * @see #DEFAULT_CONSTRAINT
     */
    void setConstraint( final UnitOfWork transaction,
                        final boolean newConstraint ) throws KException;

}
