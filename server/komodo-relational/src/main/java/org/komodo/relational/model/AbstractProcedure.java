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

import org.komodo.relational.Messages;
import org.komodo.relational.Messages.Relational;
import org.komodo.relational.RelationalObject;
import org.komodo.spi.KException;
import org.komodo.spi.repository.KomodoObject;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.komodo.spi.repository.Repository.UnitOfWork.State;

/**
 * Represents a relational model procedure or function.
 */
public interface AbstractProcedure extends OptionContainer, RelationalObject, SchemaElement {

    /**
     * Utils for accessing procedure type
     */
    class Utils {
        static Class< ? extends AbstractProcedure > getProcedureType( final UnitOfWork transaction,
                                                                      final KomodoObject kobject ) throws KException {
            Class< ? extends AbstractProcedure > clazz = null;

            if (PushdownFunction.RESOLVER.resolvable( transaction, kobject )) {
                clazz = PushdownFunction.class;
            } else if (UserDefinedFunction.RESOLVER.resolvable( transaction, kobject )) {
                clazz = UserDefinedFunction.class;
            } else if (StoredProcedure.RESOLVER.resolvable( transaction, kobject )) {
                clazz = StoredProcedure.class;
            } else if (VirtualProcedure.RESOLVER.resolvable( transaction, kobject )) {
                clazz = VirtualProcedure.class;
            } else {
                throw new KException( Messages.getString( Relational.UNEXPECTED_PROCEDURE_TYPE, kobject.getAbsolutePath() ) );
            }

            return clazz;
        }
    }

    /**
     * The default value of this table's update count. Value is {@value} .
     */
    int DEFAULT_UPDATE_COUNT = 0;

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param parameterName
     *        the name of the parameter being added (cannot be <code>null</code>)
     * @return the new parameter (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    Parameter addParameter( final UnitOfWork transaction,
                            final String parameterName ) throws KException;

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @return the value of the <code>annotation</code> option (can be empty)
     * @throws KException
     *         if an error occurs
     */
    String getDescription( final UnitOfWork transaction ) throws KException;

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @return the value of the <code>name in source</code> option (can be empty)
     * @throws KException
     *         if an error occurs
     */
    String getNameInSource( final UnitOfWork transaction ) throws KException;

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param namePatterns
     *        optional name patterns of the child(ren) being requested (can be <code>null</code> or empty but cannot have
     *        <code>null</code> or empty elements)
     * @return the input parameters (never <code>null</code> but can be empty)
     * @throws KException
     *         if an error occurs
     */
    Parameter[] getParameters( final UnitOfWork transaction,
                               final String... namePatterns ) throws KException;

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.repository.KNode#getParent(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    Model getParent( final UnitOfWork transaction ) throws KException;

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @return the value of the <code>update count</code> option
     * @throws KException
     *         if an error occurs
     */
    int getUpdateCount( final UnitOfWork transaction ) throws KException;

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @return the value of the <code>UUID</code> option (can be empty)
     * @throws KException
     *         if an error occurs
     */
    String getUuid( final UnitOfWork transaction ) throws KException;

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param parameterName
     *        the name of the parameter to remove (cannot be empty)
     * @throws KException
     *         if an error occurs
     */
    void removeParameter( final UnitOfWork transaction,
                          final String parameterName ) throws KException;

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param newDescription
     *        the new value of the <code>annotation</code> option (can only be empty when removing)
     * @throws KException
     *         if an error occurs
     */
    void setDescription( final UnitOfWork transaction,
                         final String newDescription ) throws KException;

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param newNameInSource
     *        the new name in source option (can only be empty when removing)
     * @throws KException
     *         if an error occurs
     */
    void setNameInSource( final UnitOfWork transaction,
                          final String newNameInSource ) throws KException;

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param newUpdateCount
     *        the new value of the <code>update count</code> option
     * @throws KException
     *         if an error occurs
     */
    void setUpdateCount( final UnitOfWork transaction,
                         final long newUpdateCount ) throws KException;

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param newUuid
     *        the new value of the <code>UUID</code> option (can only be empty when removing)
     * @throws KException
     *         if an error occurs
     */
    void setUuid( final UnitOfWork transaction,
                  final String newUuid ) throws KException;

}
