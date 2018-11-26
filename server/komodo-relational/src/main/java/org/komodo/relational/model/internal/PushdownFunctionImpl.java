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
package org.komodo.relational.model.internal;

import org.komodo.relational.Messages;
import org.komodo.relational.Messages.Relational;
import org.komodo.relational.RelationalModelFactory;
import org.komodo.relational.model.DataTypeResultSet;
import org.komodo.relational.model.Model;
import org.komodo.relational.model.ProcedureResultSet;
import org.komodo.relational.model.PushdownFunction;
import org.komodo.relational.model.TabularResultSet;
import org.komodo.spi.KException;
import org.komodo.spi.repository.KomodoObject;
import org.komodo.spi.repository.KomodoType;
import org.komodo.spi.repository.Repository;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.komodo.spi.repository.Repository.UnitOfWork.State;
import org.komodo.utils.ArgCheck;
import org.komodo.spi.lexicon.ddl.teiid.TeiidDdlLexicon.CreateProcedure;

/**
 * An implementation of a pushdown function.
 */
public final class PushdownFunctionImpl extends FunctionImpl implements PushdownFunction {

    /**
     * The allowed child types.
     */
    private static final KomodoType[] KID_TYPES;

    static {
        KID_TYPES = new KomodoType[ CHILD_TYPES.length + 2 ];
        System.arraycopy( CHILD_TYPES, 0, KID_TYPES, 0, CHILD_TYPES.length );
        KID_TYPES[ CHILD_TYPES.length ] = DataTypeResultSet.IDENTIFIER;
        KID_TYPES[ CHILD_TYPES.length + 1 ] = TabularResultSet.IDENTIFIER;
    }

    /**
     * @param uow
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the relational object exists (cannot be <code>null</code>)
     * @param workspacePath
     *        the workspace relative path (cannot be empty)
     * @throws KException
     *         if an error occurs or if node at specified path is not a procedure
     */
    public PushdownFunctionImpl( final UnitOfWork uow,
                                 final Repository repository,
                                 final String workspacePath ) throws KException {
        super( uow, repository, workspacePath );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.core.repository.ObjectImpl#getChildTypes()
     */
    @Override
    public KomodoType[] getChildTypes() {
        return KID_TYPES;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.PushdownFunction#getResultSet(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public ProcedureResultSet getResultSet( final UnitOfWork transaction ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$

        ProcedureResultSet result = null;

        if ( hasChild( transaction, CreateProcedure.RESULT_SET ) ) {
            final KomodoObject kobject = getChild( transaction, CreateProcedure.RESULT_SET );

            if ( DataTypeResultSet.RESOLVER.resolvable( transaction, kobject ) ) {
                result = DataTypeResultSet.RESOLVER.resolve( transaction, kobject );
            } else if ( TabularResultSet.RESOLVER.resolvable( transaction, kobject ) ) {
                result = TabularResultSet.RESOLVER.resolve( transaction, kobject );
            } else {
                throw new UnsupportedOperationException( Messages.getString( Relational.UNEXPECTED_RESULT_SET_TYPE,
                                                                             kobject.getAbsolutePath() ) );
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.repository.KomodoObject#getTypeId()
     */
    @Override
    public int getTypeId() {
        return TYPE_ID;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.core.repository.ObjectImpl#getTypeIdentifier(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public KomodoType getTypeIdentifier( final UnitOfWork uow ) {
        return PushdownFunction.IDENTIFIER;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.internal.RelationalObjectImpl#getParent(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public Model getParent( final UnitOfWork transaction ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state must be NOT_STARTED" ); //$NON-NLS-1$

        final KomodoObject parent = super.getParent( transaction );
        final Model result = Model.RESOLVER.resolve( transaction, parent );
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.PushdownFunction#removeResultSet(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public void removeResultSet( final UnitOfWork transaction ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$

        // delete existing result set
        final ProcedureResultSet resultSet = getResultSet( transaction );

        if ( resultSet == null ) {
            throw new KException( Messages.getString( Relational.RESULT_SET_NOT_FOUND_TO_REMOVE, getAbsolutePath() ) );
        }

        resultSet.remove( transaction );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.PushdownFunction#setResultSet(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.Class)
     */
    @SuppressWarnings( "unchecked" )
    @Override
    public < T extends ProcedureResultSet > T setResultSet( final UnitOfWork transaction,
                                                            final Class< T > resultSetType ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$

        // delete existing result set (don't call removeResultSet as it throws exception if one does not exist)
        final ProcedureResultSet resultSet = getResultSet( transaction );

        if ( resultSet != null ) {
            resultSet.remove( transaction );
        }

        T result = null;

        if ( resultSetType == TabularResultSet.class ) {
            result = ( T )RelationalModelFactory.createTabularResultSet( transaction, getRepository(), this );
        } else if ( resultSetType == DataTypeResultSet.class ) {
            result = ( T )RelationalModelFactory.createDataTypeResultSet( transaction, getRepository(), this );
        }

        return result;
    }

}
