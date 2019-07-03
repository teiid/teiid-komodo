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
package org.komodo.relational.connection.internal;

import java.util.Collection;
import java.util.Properties;

import org.komodo.core.KomodoLexicon;
import org.komodo.relational.DeployStatus;
import org.komodo.relational.Messages;
import org.komodo.relational.Messages.Relational;
import org.komodo.relational.connection.Connection;
import org.komodo.relational.internal.RelationalObjectImpl;
import org.komodo.spi.KException;
import org.komodo.spi.constants.StringConstants;
import org.komodo.spi.metadata.MetadataInstance;
import org.komodo.spi.repository.KomodoType;
import org.komodo.spi.repository.Property;
import org.komodo.spi.repository.PropertyValueType;
import org.komodo.spi.repository.Repository;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.komodo.spi.repository.Repository.UnitOfWork.State;
import org.komodo.spi.runtime.EventManager;
import org.komodo.spi.runtime.ExecutionConfigurationEvent;
import org.komodo.spi.runtime.ExecutionConfigurationListener;
import org.komodo.spi.runtime.TeiidDataSource;
import org.komodo.spi.runtime.TeiidPropertyDefinition;
import org.komodo.utils.ArgCheck;
import org.komodo.utils.StringUtils;
import org.teiid.modeshape.sequencer.dataservice.lexicon.DataVirtLexicon;

/**
 * Implementation of connection instance model
 */
public class ConnectionImpl extends RelationalObjectImpl implements Connection, EventManager {

    /**
     * @param uow
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository
     * @param path
     *        the path
     * @throws KException
     *         if error occurs
     */
    public ConnectionImpl( final UnitOfWork uow,
                      final Repository repository,
                      final String path ) throws KException {
        super(uow, repository, path);

        // Update filters based on JDBC
        updatePropFilters(uow);
    }

    @Override
    public KomodoType getTypeIdentifier(UnitOfWork uow) {
        return Connection.IDENTIFIER;
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @return value of teiid id property (never empty)
     * @throws KException
     *         if error occurs
     */
    @Override
    public String getId( final UnitOfWork transaction ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$

        final Property prop = this.getObjectFactory().getId(transaction, this); 
        final String result = prop.getStringValue( transaction );
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
     * @see org.komodo.relational.connection.Connection#getJndiName(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public String getJndiName( final UnitOfWork uow ) throws KException {
        return getObjectProperty( uow, PropertyValueType.STRING, "getJndiName", //$NON-NLS-1$
                                  DataVirtLexicon.Connection.JNDI_NAME);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.connection.Connection#getDescription(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public String getDescription( final UnitOfWork uow ) throws KException {
        return getObjectProperty( uow, PropertyValueType.STRING, "getDescription", //$NON-NLS-1$
                                  KomodoLexicon.LibraryComponent.DESCRIPTION);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.connection.Connection#getExternalLocation(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public String getExternalLocation( final UnitOfWork uow ) throws KException {
        return getObjectProperty( uow, PropertyValueType.STRING, "getExternalLocation", //$NON-NLS-1$
                                  KomodoLexicon.WorkspaceItem.EXT_LOC);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.connection.Connection#getDriverName(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public String getDriverName( final UnitOfWork uow ) throws KException {
        return getObjectProperty( uow, PropertyValueType.STRING, "getDriverName", //$NON-NLS-1$
                                  DataVirtLexicon.Connection.DRIVER_NAME);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.connection.Connection#getClassName(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public String getClassName(UnitOfWork uow) throws KException {
        return getObjectProperty( uow, PropertyValueType.STRING, "getClassName", //$NON-NLS-1$
                                  DataVirtLexicon.Connection.CLASS_NAME);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.connection.Connection#setDriverName(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String)
     */
    @Override
    public void setDriverName( final UnitOfWork uow,
                               final String driverName ) throws KException {
        setObjectProperty( uow, "setDriverName", DataVirtLexicon.Connection.DRIVER_NAME, driverName ); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.connection.Connection#setJndiName(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String)
     */
    @Override
    public void setJndiName( final UnitOfWork uow,
                             final String jndiName ) throws KException {
        setObjectProperty( uow, "setJndiName", DataVirtLexicon.Connection.JNDI_NAME, jndiName ); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.connection.Connection#setDescription(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String)
     */
    @Override
    public void setDescription( final UnitOfWork uow,
                                final String description ) throws KException {
        setObjectProperty( uow, "setDescription", KomodoLexicon.LibraryComponent.DESCRIPTION, description ); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.connection.Connection#setExternalLocation(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String)
     */
    @Override
    public void setExternalLocation( final UnitOfWork uow,
                                     final String extLoc ) throws KException {
        setObjectProperty( uow, "setExternalLocation", KomodoLexicon.WorkspaceItem.EXT_LOC, extLoc ); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.connection.Connection#setClassName(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String)
     */
    @Override
    public void setClassName(UnitOfWork uow,
                             String className) throws KException {
        setObjectProperty( uow, "setClassName", DataVirtLexicon.Connection.CLASS_NAME, className ); //$NON-NLS-1$
    }

    private void updatePropFilters(UnitOfWork uow) throws KException {
        setFilters( DEFAULT_FILTERS );
    }

    /* (non-Javadoc)
     * @see org.komodo.relational.connection.Connection#getPropertiesForServerDeployment(org.komodo.spi.repository.Repository.UnitOfWork, org.komodo.spi.runtime.TeiidInstance)
     */
    @Override
    public Properties getPropertiesForServerDeployment(UnitOfWork transaction) throws Exception {
        Properties sourceProps = new Properties();

        MetadataInstance metadata = getRepository().getMetadataInstance();            
        // Get the Property Defns for this type of source.
        Collection<TeiidPropertyDefinition> templatePropDefns = metadata.getTemplatePropertyDefns(getDriverName(transaction));

        // Connection driverName and jndiName must be defined.
        String driverName = getDriverName(transaction);
        if(StringUtils.isBlank(driverName)) {
            throw new Exception( Messages.getString( Relational.CONNECTION_DRIVERNAME_NOT_DEFINED ) );
        }
        sourceProps.setProperty(TeiidDataSource.DATASOURCE_DRIVERNAME,driverName);
        String jndiName = getJndiName(transaction);
        if(StringUtils.isBlank(jndiName)) {
            throw new Exception( Messages.getString( Relational.CONNECTION_JNDINAME_NOT_DEFINED ) );
        }
        sourceProps.setProperty(TeiidDataSource.DATASOURCE_JNDINAME, jndiName);

        String className = getClassName(transaction);
        if(StringUtils.isBlank(className)) {
            throw new Exception( Messages.getString( Relational.CONNECTION_CLASSNAME_NOT_DEFINED ) );
        }
        sourceProps.setProperty(TeiidDataSource.DATASOURCE_CLASSNAME, className);

        // Iterate the datasource properties.  Compare them against the valid properties for the server source type.
        String[] propNames = getPropertyNames(transaction);
        for(String propName : propNames) {
            TeiidPropertyDefinition propDefn = getTemplatePropertyDefn(templatePropDefns,propName);
            if(propDefn!=null) {
                boolean hasDefault = propDefn.getDefaultValue()!=null ? true : false;
                String sourcePropValue = getProperty(transaction, propName).getStringValue(transaction);
                // Template has no default - set the property
                if(!hasDefault) {
                    if(sourcePropValue!=null) {
                        sourceProps.setProperty(propName, sourcePropValue);
                    }
                    // Template has default - if source property matches it, no need to provide it.
                } else {
                    String templateDefaultValue = propDefn.getDefaultValue().toString();
                    if(!templateDefaultValue.equals(sourcePropValue)) {
                        if(sourcePropValue!=null) {
                            sourceProps.setProperty(propName, sourcePropValue);
                        }
                    }
                }
            }
        }

        return sourceProps;
    }

    private TeiidPropertyDefinition getTemplatePropertyDefn(Collection<TeiidPropertyDefinition> templatePropDefns, String propName) {
        TeiidPropertyDefinition propDefn = null;
        for(TeiidPropertyDefinition aDefn : templatePropDefns) {
            if(propName.equals(aDefn.getName())) {
                propDefn = aDefn;
                break;
            }
        }
        return propDefn;
    }

    /* (non-Javadoc)
     * @see org.komodo.spi.repository.Exportable#export(org.komodo.spi.repository.Repository.UnitOfWork, java.util.Properties)
     */
    @Override
    public byte[] export(UnitOfWork transaction,
                         Properties exportProperties) throws KException {

        // Get the XML result
        ConnectionNodeVisitor visitor = new ConnectionNodeVisitor(transaction, this, exportProperties);
        String xmlResult = visitor.getXml();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("ConnectionImpl#export: transaction = {0}, xml = {1}", //$NON-NLS-1$
                         transaction.getName(),
                         xmlResult);
        }

        return xmlResult.getBytes();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.core.repository.ObjectImpl#setProperty(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String,
     *      java.lang.Object[])
     */
    @Override
    public void setProperty( final UnitOfWork transaction,
                             final String propertyName,
                             final Object... values ) throws KException {

        int nsPrefixLength = (KomodoLexicon.Namespace.PREFIX+StringConstants.COLON).length();

        if( propertyName.equals(DataVirtLexicon.Connection.CLASS_NAME.substring(nsPrefixLength)) ) {
            setClassName(transaction, (String)values[0]);
        } else if ( propertyName.equals(DataVirtLexicon.Connection.DRIVER_NAME.substring(nsPrefixLength)) ) {
            setDriverName(transaction, (String)values[0]);
        } else if ( propertyName.equals(DataVirtLexicon.Connection.JNDI_NAME.substring(nsPrefixLength)) ) {
            setJndiName(transaction, (String)values[0]);
        } else {
            super.setProperty(transaction, propertyName, values);
        }
    }

    @Override
    public DeployStatus deploy(UnitOfWork uow) {
        ArgCheck.isNotNull( uow, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( uow.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$

        DeployStatus status = new DeployStatus();

        try {
            String connName = getName(uow);
            status.addProgressMessage("Starting deployment of connection " + connName); //$NON-NLS-1$

            String jndiName = getJndiName(uow);
            String sourceType = getDriverName(uow);
            Properties properties = getPropertiesForServerDeployment(uow);

            status.addProgressMessage("Attempting to deploy connection " + connName + " to teiid"); //$NON-NLS-1$ //$NON-NLS-2$
            MetadataInstance metadata = getRepository().getMetadataInstance();
            TeiidDataSource teiidDataSrc = metadata.getOrCreateDataSource(connName,
                                                                               jndiName,
                                                                               sourceType,
                                                                               properties);
            if (teiidDataSrc == null) {
                status.addErrorMessage("Connection " + connName + " failed to deploy"); //$NON-NLS-1$ //$NON-NLS-2$
                return status;
            }

            status.addProgressMessage("Data source deployed " + connName + " to teiid"); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (Exception ex) {
            status.addErrorMessage(ex);
        }

        return status;
    }

    @Override
    public boolean addListener( ExecutionConfigurationListener listener ) {
        return false;
    }

    @Override
    public void permitListeners( boolean enable ) {
        // TODO
        // Consider whether this is still required.
    }

    @Override
    public void notifyListeners( ExecutionConfigurationEvent event ) {
        // TODO
        // Consider whether this is still required.
    }

    @Override
    public boolean removeListener( ExecutionConfigurationListener listener ) {
        return false;
    }

}
