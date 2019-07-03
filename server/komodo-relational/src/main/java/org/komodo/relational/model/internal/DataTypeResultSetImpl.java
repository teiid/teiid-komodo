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
import org.komodo.relational.model.AbstractProcedure;
import org.komodo.relational.model.DataTypeResultSet;
import org.komodo.spi.KException;
import org.komodo.spi.repository.KomodoType;
import org.komodo.spi.repository.PropertyValueType;
import org.komodo.spi.repository.Repository;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.komodo.spi.repository.Repository.UnitOfWork.State;
import org.komodo.utils.ArgCheck;
import org.komodo.utils.StringUtils;
import org.teiid.modeshape.sequencer.ddl.StandardDdlLexicon;

/**
 * An implementation of a relational model procedure data type result set.
 */
public final class DataTypeResultSetImpl extends ResultSetColumnImpl implements DataTypeResultSet {

    private static final String ARRAY_SUFFIX = "[]"; //$NON-NLS-1$

    /**
     * @param uow
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the relational object exists (cannot be <code>null</code>)
     * @param workspacePath
     *        the workspace relative path (cannot be empty)
     * @throws KException
     *         if an error occurs or if node at specified path is not a procedure result set
     */
    public DataTypeResultSetImpl( final UnitOfWork uow,
                                  final Repository repository,
                                  final String workspacePath ) throws KException {
        super( uow, repository, workspacePath );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.DataTypeResultSet#getDisplayString(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public String getDisplayString( final UnitOfWork transaction ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$

        final StringBuilder result = new StringBuilder( getType( transaction ).toString() );
        final long length = getLength( transaction );

        if ( length != 0 ) {
            result.append( '(' ).append( length ).append( ')' );
        }

        if ( isArray( transaction ) ) {
            result.append( "[]" ); //$NON-NLS-1$
        }

        return result.toString();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.DataTypeResultSet#getType(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public Type getType( final UnitOfWork uow ) throws KException {
        String value = getObjectProperty( uow, PropertyValueType.STRING, "getDataType", StandardDdlLexicon.DATATYPE_NAME ); //$NON-NLS-1$

        if (StringUtils.isBlank( value )) {
            return Type.DEFAULT_VALUE;
        }

        final int index = value.indexOf( ARRAY_SUFFIX );

        if (index != -1) {
            value = value.substring( 0, index );
        }

        final Type result = Type.valueOf( value );

        if (result == null) {
            return Type.DEFAULT_VALUE;
        }

        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.repository.KomodoObject#getTypeIdentifier(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public KomodoType getTypeIdentifier( final UnitOfWork uow ) {
        return DataTypeResultSet.IDENTIFIER;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.internal.RelationalObjectImpl#getParent(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public AbstractProcedure getParent( final UnitOfWork transaction ) throws KException {
        return (AbstractProcedure) super.getParent(transaction);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.DataTypeResultSet#isArray(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public boolean isArray( final UnitOfWork uow ) throws KException {
        final String value = getObjectProperty( uow, PropertyValueType.STRING, "getDataType", StandardDdlLexicon.DATATYPE_NAME ); //$NON-NLS-1$

        if (StringUtils.isBlank( value )) {
            return false;
        }

        return ( value.indexOf( ARRAY_SUFFIX ) != -1 );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.core.repository.ObjectImpl#rename(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String)
     */
    @Override
    public final void rename( final UnitOfWork transaction,
                              final String newName ) throws UnsupportedOperationException {
        throw new UnsupportedOperationException( Messages.getString( Relational.RENAME_NOT_ALLOWED, getAbsolutePath() ) );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.DataTypeResultSet#setArray(org.komodo.spi.repository.Repository.UnitOfWork, boolean)
     */
    @Override
    public void setArray( final UnitOfWork uow,
                          final boolean newArrayIndicator ) throws KException {
        final String value = getObjectProperty( uow, PropertyValueType.STRING, "setArray", StandardDdlLexicon.DATATYPE_NAME ); //$NON-NLS-1$

        if ( StringUtils.isBlank( value ) ) {
            // add suffix to default datatype if necessary
            final String newValue = Type.DEFAULT_VALUE.name() + ( newArrayIndicator ? ARRAY_SUFFIX : EMPTY_STRING );
            setObjectProperty( uow, "setArray", StandardDdlLexicon.DATATYPE_NAME, newValue ); //$NON-NLS-1$
        } else if ( value.endsWith( ARRAY_SUFFIX ) && !newArrayIndicator ) {
            // remove suffix
            setObjectProperty( uow,
                               "setArray", //$NON-NLS-1$
                               StandardDdlLexicon.DATATYPE_NAME,
                               value.substring( 0, value.length() - ARRAY_SUFFIX.length() ) );
        } else if ( !value.endsWith( ARRAY_SUFFIX ) && newArrayIndicator ) {
            // add suffix
            setObjectProperty( uow, "setArray", StandardDdlLexicon.DATATYPE_NAME, ( value + ARRAY_SUFFIX ) ); //$NON-NLS-1$
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.internal.ResultSetColumnImpl#setDatatypeName(org.komodo.spi.repository.Repository.UnitOfWork,
     *      java.lang.String)
     */
    @Override
    public void setDatatypeName( final UnitOfWork uow,
                                 final String newTypeName ) throws KException {
        Type newType = null;

        if ( !StringUtils.isBlank( newTypeName ) ) {
            try {
                newType = Type.valueOf( newTypeName );
            } catch ( final Exception e ) {
                // not a valid type name so ignore
            }
        }

        setType( uow, newType );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.model.DataTypeResultSet#setType(org.komodo.spi.repository.Repository.UnitOfWork,
     *      org.komodo.relational.model.DataTypeResultSet.Type)
     */
    @Override
    public void setType( final UnitOfWork uow,
                         final Type newType ) throws KException {
        String newValue = null;

        if (newType == null) {
            newValue = Type.DEFAULT_VALUE.name();
        } else {
            newValue = newType.name();
        }

        if (isArray( uow )) {
            newValue += ARRAY_SUFFIX;
        }

        setObjectProperty( uow, "setType", StandardDdlLexicon.DATATYPE_NAME, newValue ); //$NON-NLS-1$
    }

}
