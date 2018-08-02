/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */
package org.komodo.relational.profile.internal;

import java.util.ArrayList;
import java.util.List;

import org.komodo.core.KomodoLexicon;
import org.komodo.relational.Messages;
import org.komodo.relational.Messages.Relational;
import org.komodo.relational.RelationalModelFactory;
import org.komodo.relational.internal.RelationalObjectImpl;
import org.komodo.relational.profile.SqlComposition;
import org.komodo.relational.profile.ViewDefinition;
import org.komodo.relational.profile.ViewEditorState;
import org.komodo.spi.KException;
import org.komodo.spi.constants.StringConstants;
import org.komodo.spi.repository.KomodoObject;
import org.komodo.spi.repository.KomodoType;
import org.komodo.spi.repository.Property;
import org.komodo.spi.repository.PropertyValueType;
import org.komodo.spi.repository.Repository;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.komodo.spi.repository.Repository.UnitOfWork.State;
import org.komodo.utils.ArgCheck;

/**
 * An implementation of an view definition
 */
public class ViewDefinitionImpl extends RelationalObjectImpl implements ViewDefinition {
    /**
     * The allowed child types.
     */
    private static final KomodoType[] CHILD_TYPES = new KomodoType[] { SqlComposition.IDENTIFIER};

    /**
     * @param uow
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the relational object exists (cannot be <code>null</code>)
     * @param path
     *        the path (cannot be empty)
     * @throws KException
     *         if an error occurs or if node at specified path is not a model
     */
	public ViewDefinitionImpl(UnitOfWork uow, Repository repository, String path) throws KException {
		super(uow, repository, path);
	}

    @Override
    public KomodoType getTypeIdentifier(UnitOfWork uow) {
        return ViewDefinition.IDENTIFIER;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.internal.RelationalObjectImpl#getParent(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public ViewEditorState getParent(final UnitOfWork transaction) throws KException {
        ArgCheck.isNotNull(transaction, "transaction"); //$NON-NLS-1$
        ArgCheck.isTrue((transaction.getState() == State.NOT_STARTED), "transaction state must be NOT_STARTED"); //$NON-NLS-1$

        final KomodoObject grouping = super.getParent(transaction);
        final ViewEditorState result = ViewEditorState.RESOLVER.resolve(transaction, grouping.getParent(transaction));
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
     * @see org.komodo.core.repository.ObjectImpl#getChildTypes()
     */
    @Override
    public KomodoType[] getChildTypes() {
        return CHILD_TYPES;
    }
    
    
    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.profile.ViewDefinition#addSqlComposition(UnitOfWork, String, String, String, String, String, String, String, String)
     */
	@Override
	public SqlComposition addSqlComposition(UnitOfWork transaction, String compositionName, String description,
			String leftSourcePath, String rightSourcePath, 
            String leftColumnCriteria, String rightColumnCriteria, 
            String type, String operator) throws KException {
		// TODO Auto-generated method stub
		return RelationalModelFactory.createSqlComposition( transaction, getRepository(), this, 
				compositionName, description, leftSourcePath, rightSourcePath, type, operator);
	}
    
    private KomodoObject getSqlCompositionsGroupingNode( final UnitOfWork transaction ) {
        try {
            final KomodoObject[] groupings = getRawChildren( transaction, KomodoLexicon.ViewDefinition.SQL_COMPOSITIONS );

            if ( groupings.length == 0 ) {
                return null;
            }

            return groupings[ 0 ];
        } catch ( final KException e ) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.profile.ViewDefinition#getSqlCompositions(UnitOfWork, String...)
     */
    @Override
    public SqlComposition[] getSqlCompositions(final UnitOfWork transaction, final String... namePatterns) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$

        final KomodoObject grouping = getSqlCompositionsGroupingNode( transaction);

        if ( grouping != null ) {
            final List< SqlComposition > temp = new ArrayList<>();

            for ( final KomodoObject kobject : grouping.getChildren( transaction, namePatterns ) ) {
                final SqlComposition gitRepo = new SqlCompositionImpl( transaction, getRepository(), kobject.getAbsolutePath() );
                temp.add( gitRepo );
            }

            return temp.toArray( new SqlComposition[ temp.size() ] );
        }

        return SqlComposition.NO_SQL_COMPOSITIONS;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.profile.ViewDefinition#getSqlCompositions(UnitOfWork, String...)
     */
    @Override
    public void removeSqlComposition(UnitOfWork transaction, String sqlCompositionToRemove) throws KException {
        ArgCheck.isNotNull(transaction, "transaction"); //$NON-NLS-1$
        ArgCheck.isTrue((transaction.getState() == State.NOT_STARTED), "transaction state is not NOT_STARTED"); //$NON-NLS-1$
        ArgCheck.isNotEmpty(sqlCompositionToRemove, "sqlCompositionToRemove"); //$NON-NLS-1$

        final SqlComposition[] sqlCompositions = getSqlCompositions(transaction, sqlCompositionToRemove);

        if (sqlCompositions.length == 0) {
            throw new KException(Messages.getString(Relational.SQL_COMPOSITION_NOT_FOUND_TO_REMOVE, sqlCompositionToRemove));
        }

        // remove first occurrence
        sqlCompositions[0].remove(transaction);
    }

	@Override
	public String getDescription(UnitOfWork transaction) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        
        String desc = getObjectProperty(transaction, PropertyValueType.STRING, "getDescription", //$NON-NLS-1$
                KomodoLexicon.ViewDefinition.DESCRIPTION );
		return desc;
	}

	@Override
	public String[] getSourcePaths(UnitOfWork transaction,
            final String... namePatterns ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$

        final Property property = getProperty( transaction, KomodoLexicon.ViewDefinition.SOURCE_PATHS );

        if ( property == null ) {
            return StringConstants.EMPTY_ARRAY;
        }

        final boolean matchPattern = ( ( namePatterns != null ) && ( namePatterns.length != 0 ) );
        final List< String > paths = new ArrayList< >();

        for ( final String value : property.getStringValues( transaction ) ) {
            if ( matchPattern ) {
                for ( final String pattern : namePatterns ) {
                    // convert pattern to a regex
                    final String regex = pattern.replace( "*", ".*" ); //$NON-NLS-1$ //$NON-NLS-2$

                    if ( value.matches( regex ) ) {
                    	paths.add( value );
                    }
                }
            } else {
            	paths.add( value );
            }
        }

        return paths.toArray( new String[ paths.size() ] );
	}

	@Override
	public String getViewName(UnitOfWork transaction) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        
        String name = getObjectProperty(transaction, PropertyValueType.STRING, "getViewName", //$NON-NLS-1$
                KomodoLexicon.ViewDefinition.VIEW_NAME );
		return name;
	}
	
	@Override
	public void setViewName(UnitOfWork transaction, String name) throws KException {
		setObjectProperty( transaction, "setViewName", KomodoLexicon.ViewDefinition.VIEW_NAME, name ); //$NON-NLS-1$
	}

	@Override
	public void setDescription(UnitOfWork transaction, String description) throws KException {
		setObjectProperty( transaction, "setDescription", KomodoLexicon.ViewDefinition.DESCRIPTION, description ); //$NON-NLS-1$
	}

	@Override
	public String[] removeSourcePath(UnitOfWork transaction, String sourcePathToRemove) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( sourcePathToRemove, "roleNameToRemove" ); //$NON-NLS-1$

        final String[] current = getSourcePaths( transaction );

        if ( current.length == 0 ) {
            throw new KException( Messages.getString( Relational.MAPPED_ROLE_NOT_FOUND_TO_REMOVE, sourcePathToRemove ) );
        }

        final String[] result = new String[ current.length - 1 ];
        boolean found = false;
        int i = 0;

        for ( final String sourcePath : current ) {
            if ( sourcePath.equals( sourcePathToRemove ) ) {
                found = true;
            } else {
                result[i++] = sourcePath;
            }
        }

        if ( !found ) {
            throw new KException( Messages.getString( Relational.SOURCE_PATH_NOT_FOUND_TO_REMOVE, sourcePathToRemove ) );
        }

        final Object[] newValue = ( ( result.length == 0 ) ? null : result );
        setProperty( transaction, KomodoLexicon.ViewDefinition.SOURCE_PATHS, newValue );

        return result;
		
	}

	@Override
	public String[] addSourcePath(UnitOfWork transaction, String sourcePathToAdd) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( sourcePathToAdd, "sourcePathToAdd" ); //$NON-NLS-1$

        String[] result = null;
        final String[] current = getSourcePaths( transaction );
        int i = 0;

        if ( current.length == 0 ) {
            // this is first mapped role name
            result = new String[ 1 ];
        } else {
            // add to existing (make sure it doesn't already exist)
            result = new String[ current.length + 1 ];

            for ( final String sourcePath : current ) {
                if ( sourcePath.equals( sourcePathToAdd ) ) {
                    throw new KException( Messages.getString( Relational.DUPLICATE_SOURCE_PATH, sourcePathToAdd ) );
                }

                result[i++] = sourcePath;
            }
        }

        result[i] = sourcePathToAdd;
        setProperty( transaction, KomodoLexicon.ViewDefinition.SOURCE_PATHS, ( Object[] )result );

        return result;
	}

	@Override
	public void setComplete(UnitOfWork transaction, boolean complete) throws KException {
		setObjectProperty( transaction, "setDescription", KomodoLexicon.ViewDefinition.DESCRIPTION, complete ); //$NON-NLS-1$
	}

	@Override
	public boolean isComplete(UnitOfWork transaction) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        
        String value = getObjectProperty(transaction, PropertyValueType.BOOLEAN, "isComplete", //$NON-NLS-1$
                KomodoLexicon.ViewDefinition.IS_COMPLETE );
		return Boolean.getBoolean(value);
	}
}
