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
package org.komodo.relational.dataservice.internal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.komodo.core.repository.ObjectImpl;
import org.komodo.relational.Messages;
import org.komodo.relational.TypeResolver;
import org.komodo.relational.dataservice.DataServiceEntry;
import org.komodo.relational.dataservice.Dataservice;
import org.komodo.relational.dataservice.ServiceVdbEntry;
import org.komodo.relational.internal.RelationalModelFactory;
import org.komodo.relational.internal.RelationalObjectImpl;
import org.komodo.relational.model.Model;
import org.komodo.relational.model.Model.Type;
import org.komodo.relational.profile.Profile;
import org.komodo.relational.profile.ViewDefinition;
import org.komodo.relational.profile.ViewEditorState;
import org.komodo.relational.vdb.Vdb;
import org.komodo.relational.vdb.internal.VdbImpl;
import org.komodo.relational.workspace.WorkspaceManager;
import org.komodo.spi.KException;
import org.komodo.spi.repository.KomodoObject;
import org.komodo.spi.repository.KomodoType;
import org.komodo.spi.repository.PropertyValueType;
import org.komodo.spi.repository.Repository;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.komodo.spi.repository.Repository.UnitOfWork.State;
import org.komodo.utils.ArgCheck;
import org.teiid.modeshape.sequencer.dataservice.lexicon.DataVirtLexicon;

/**
 * Implementation of data service instance model.
 */
public class DataserviceImpl extends RelationalObjectImpl implements Dataservice {
	
    /**
     * The resolver of a {@link Dataservice}.
     */
    public static final TypeResolver< Dataservice > RESOLVER = new TypeResolver< Dataservice >() {

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
        public Class< DataserviceImpl > owningClass() {
            return DataserviceImpl.class;
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
                                            DataVirtLexicon.DataService.NODE_TYPE );
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.relational.TypeResolver#resolve(org.komodo.spi.repository.Repository.UnitOfWork,
         *      org.komodo.spi.repository.KomodoObject)
         */
        @Override
        public Dataservice resolve( final UnitOfWork transaction,
                                    final KomodoObject kobject ) throws KException {
            if ( kobject.getTypeId() == Dataservice.TYPE_ID ) {
                return ( Dataservice )kobject;
            }
            return new DataserviceImpl( transaction, kobject.getRepository(), kobject.getAbsolutePath() );
        }

    };


    /**
     * The allowed child types.
     */
    private static final KomodoType[] CHILD_TYPES = new KomodoType[] { ServiceVdbEntry.IDENTIFIER };

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository
     * @param path
     *        the path
     * @throws KException
     *         if error occurs
     */
    public DataserviceImpl(final UnitOfWork transaction,
                      final Repository repository,
                      final String path ) throws KException {
        super(transaction, repository, path);
    }

    @Override
    public KomodoType getTypeIdentifier(UnitOfWork transaction) {
        return Dataservice.IDENTIFIER;
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
     * @see org.komodo.relational.internal.RelationalObjectImpl#getChild(org.komodo.spi.repository.Repository.UnitOfWork,
     *      java.lang.String)
     */
    @Override
    public DataServiceEntry< ? > getChild( final UnitOfWork transaction,
                                           final String name ) throws KException {
        // check service VDB
        final ServiceVdbEntry entry = getServiceVdbEntry( transaction );

        if ( ( entry != null ) && name.equals( entry.getName( transaction ) ) ) {
            return entry;
        }

        // child does not exist
        throw new KException( Messages.getString( org.komodo.core.repository.Messages.Komodo.CHILD_NOT_FOUND,
                                                  name,
                                                  getAbsolutePath() ) );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.internal.RelationalObjectImpl#getChild(org.komodo.spi.repository.Repository.UnitOfWork,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public DataServiceEntry< ? > getChild( final UnitOfWork transaction,
                                           final String name,
                                           final String typeName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state must be NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( name, "name" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( typeName, "typeName" ); //$NON-NLS-1$

        if ( DataVirtLexicon.ServiceVdbEntry.NODE_TYPE.equals( typeName ) ) {
            return getServiceVdbEntry( transaction );
        }

        // child does not exist
        throw new KException( Messages.getString( org.komodo.core.repository.Messages.Komodo.CHILD_NOT_FOUND,
                                                  name,
                                                  getAbsolutePath() ) );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.internal.RelationalObjectImpl#getChildren(org.komodo.spi.repository.Repository.UnitOfWork,
     *      java.lang.String[])
     */
    @Override
    public DataServiceEntry< ? >[] getChildren( final UnitOfWork transaction,
                                                final String... namePatterns ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$

        final ServiceVdbEntry serviceVdb = getServiceVdbEntry( transaction );

        final DataServiceEntry< ? >[] result = new DataServiceEntry< ? >[ ( ( serviceVdb == null ) ? 0 : 1 )];

        if ( serviceVdb != null ) {
            result[ result.length - 1 ] = serviceVdb;
        }

        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.internal.RelationalObjectImpl#getChildrenOfType(org.komodo.spi.repository.Repository.UnitOfWork,
     *      java.lang.String, java.lang.String[])
     */
    @Override
    public DataServiceEntry< ? >[] getChildrenOfType( final UnitOfWork transaction,
                                                      final String type,
                                                      final String... namePatterns ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$

        if ( DataVirtLexicon.ServiceVdbEntry.NODE_TYPE.equals( type ) ) {
            final ServiceVdbEntry entry = getServiceVdbEntry( transaction );

            if ( entry == null ) {
                return ServiceVdbEntry.NO_ENTRIES;
            }

            return new ServiceVdbEntry[] { entry };
        }

        return ServiceVdbEntry.NO_ENTRIES;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.internal.RelationalObjectImpl#hasChild(org.komodo.spi.repository.Repository.UnitOfWork,
     *      java.lang.String)
     */
    @Override
    public boolean hasChild( final UnitOfWork transaction,
                             final String name ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state must be NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( name, "name" ); //$NON-NLS-1$

        return ( ( ( getServiceVdbEntry( transaction ) != null )
                   && name.equals( getServiceVdbEntry( transaction ).getName( transaction ) ) ));
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.internal.RelationalObjectImpl#hasChild(org.komodo.spi.repository.Repository.UnitOfWork,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public boolean hasChild( final UnitOfWork transaction,
                             final String name,
                             final String typeName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state must be NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( name, "name" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( typeName, "typeName" ); //$NON-NLS-1$

        if ( DataVirtLexicon.ServiceVdbEntry.NODE_TYPE.equals( typeName ) ) {
            return ( ( getServiceVdbEntry( transaction ) != null )
                     && name.equals( getServiceVdbEntry( transaction ).getName( transaction ) ) );
        }

        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.internal.RelationalObjectImpl#hasChildren(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public boolean hasChildren( final UnitOfWork transaction ) throws KException {
        return ( ( getServiceVdbEntry( transaction ) != null ));
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.dataservice.Dataservice#getServiceVdb(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public Vdb getServiceVdb( final UnitOfWork transaction ) throws KException {
        final ServiceVdbEntry entry = getServiceVdbEntry( transaction );
        return ( ( entry == null ) ? null : entry.getReference( transaction ) );
    }

    /* (non-Javadoc)
     * @see org.komodo.relational.dataservice.Dataservice#getViewDefinitionNames(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public String[] getViewDefinitionNames(UnitOfWork uow) throws KException {
    	return this.getViewDefnNames(uow);
    }

    /* (non-Javadoc)
     * @see org.komodo.relational.dataservice.Dataservice#getDataserviceViewModel(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public String getServiceViewModelName(UnitOfWork uow) throws KException {
        String viewModelName = null;
        // Only ONE virtual model should exist in the dataservice vdb.
        // The returned view model is the first virtual model found - or null if none found.
        Vdb serviceVdb = getServiceVdb(uow);
        if( serviceVdb != null ) {
            Model[] models = serviceVdb.getModels(uow);
            for(Model model : models) {
                Model.Type modelType = model.getModelType(uow);
                if(modelType == Type.VIRTUAL) {
                    viewModelName = model.getName(uow);
                    break;
                }
            }
        }
        return viewModelName;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.dataservice.Dataservice#getDescription(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public String getDescription( final UnitOfWork transaction ) throws KException {
        return getObjectProperty( transaction,
                                  PropertyValueType.STRING,
                                  "getDescription", //$NON-NLS-1$
                                  DataVirtLexicon.DataService.DESCRIPTION );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.dataservice.Dataservice#getLastModified(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public Calendar getLastModified( final UnitOfWork transaction ) throws KException {
        return getObjectProperty( transaction,
                                  PropertyValueType.DATE,
                                  "getLastModified", //$NON-NLS-1$
                                  DataVirtLexicon.DataService.LAST_MODIFIED );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.dataservice.Dataservice#getModifiedBy(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public String getModifiedBy( final UnitOfWork transaction ) throws KException {
        return getObjectProperty( transaction,
                                  PropertyValueType.STRING,
                                  "getModifiedBy", //$NON-NLS-1$
                                  DataVirtLexicon.DataService.MODIFIED_BY );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.dataservice.Dataservice#getServiceVdbEntry(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public ServiceVdbEntry getServiceVdbEntry( final UnitOfWork uow ) throws KException {
        ArgCheck.isNotNull( uow, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( uow.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$

        final KomodoObject[] kobjects = super.getChildrenOfType( uow, DataVirtLexicon.ServiceVdbEntry.NODE_TYPE );

        if ( kobjects.length == 0 ) {
            return null;
        }

        return new ServiceVdbEntryImpl( uow, getRepository(), kobjects[ 0 ].getAbsolutePath() );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.dataservice.Dataservice#setDescription(org.komodo.spi.repository.Repository.UnitOfWork,
     *      java.lang.String)
     */
    @Override
    public void setDescription( final UnitOfWork transaction,
                                final String newDescription ) throws KException {
        setObjectProperty( transaction,
                           "setDescription", //$NON-NLS-1$
                           DataVirtLexicon.DataService.DESCRIPTION,
                           newDescription );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.dataservice.Dataservice#setLastModified(org.komodo.spi.repository.Repository.UnitOfWork,
     *      java.util.Calendar)
     */
    @Override
    public void setLastModified( final UnitOfWork transaction,
                                 final Calendar newLastModified ) throws KException {
        setObjectProperty( transaction,
                           "setLastModified", //$NON-NLS-1$
                           DataVirtLexicon.DataService.LAST_MODIFIED,
                           newLastModified );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.dataservice.Dataservice#setModifiedBy(org.komodo.spi.repository.Repository.UnitOfWork,
     *      java.lang.String)
     */
    @Override
    public void setModifiedBy( final UnitOfWork transaction,
                               final String newModifiedBy ) throws KException {
        setObjectProperty( transaction,
                           "setModifiedBy", //$NON-NLS-1$
                           DataVirtLexicon.DataService.MODIFIED_BY,
                           newModifiedBy );
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.dataservice.Dataservice#setServiceVdb(org.komodo.spi.repository.Repository.UnitOfWork,
     *      org.komodo.relational.vdb.Vdb)
     */
    @Override
    public Vdb setServiceVdb( final UnitOfWork uow,
                              final Vdb serviceVdb ) throws KException {
        Vdb oldServiceVdb = null;
        final KomodoObject[] kobjects = getChildrenOfType( uow, DataVirtLexicon.ServiceVdbEntry.NODE_TYPE );

        if ( kobjects.length != 0 ) {
            if ( kobjects[ 0 ].hasProperty( uow, DataVirtLexicon.ServiceVdbEntry.VDB_REF ) ) {
                final String refId = kobjects[ 0 ].getProperty( uow, DataVirtLexicon.ServiceVdbEntry.VDB_REF )
                                                  .getStringValue( uow );
                final KomodoObject kobj = getRepository().getUsingId( uow, refId );

                if ( kobj != null ) {
                    oldServiceVdb = new VdbImpl( uow, getRepository(), kobj.getAbsolutePath() );
                }
            }

            // always delete current
            kobjects[ 0 ].remove( uow );
        }

        // add new service VDB if necessary
        if ( serviceVdb != null ) {
            final ServiceVdbEntry entry = RelationalModelFactory.createServiceVdbEntry( uow,
                                                                                        getRepository(),
                                                                                        this,
                                                                                        serviceVdb.getName( uow ) );
            entry.setVdbName( uow, serviceVdb.getName( uow ) );
            entry.setVdbVersion( uow, Integer.toString( serviceVdb.getVersion( uow ) ) );
            entry.setReference( uow, serviceVdb );
        }

        return oldServiceVdb;
    }

    /**
     *  get the ViewDefinition names for the dataservice
     */
    private String[] getViewDefnNames( final UnitOfWork uow ) throws KException {
        ArgCheck.isNotNull( uow, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( uow.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$

        KomodoObject userProfileObj = getRepository().komodoProfile(uow);
        WorkspaceManager wkspMgr = WorkspaceManager.getInstance(getRepository(), uow);
        
        Profile userProfile = wkspMgr.resolve(uow, userProfileObj, Profile.class);
        ViewEditorState[] editorStates = null;
        if (userProfile != null) {
        	String svcVdbName = this.getServiceVdb(uow).getName(uow).toLowerCase();
        	String pattern = svcVdbName + "*";
            editorStates = userProfile.getViewEditorStates(uow, pattern);
        }

        List<String> viewNames = new ArrayList<String>();
        for (ViewEditorState editorState: editorStates) {
        	ViewDefinition viewDefn = editorState.getViewDefinition(uow);
        	viewNames.add(viewDefn.getViewName(uow));
        }
        
        return viewNames.toArray(new String[0]);
    }

}