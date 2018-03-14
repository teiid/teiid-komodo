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
package org.komodo.rest.service;

import static org.komodo.rest.relational.RelationalMessages.Error.CONNECTION_SERVICE_DEPLOY_CONNECTION_VDB_ERROR;
import static org.komodo.rest.relational.RelationalMessages.Error.CONNECTION_SERVICE_NAME_EXISTS;
import static org.komodo.rest.relational.RelationalMessages.Error.CONNECTION_SERVICE_NAME_VALIDATION_ERROR;
import static org.komodo.rest.relational.RelationalMessages.Error.VDB_DATA_SOURCE_NAME_EXISTS;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.komodo.core.KEngine;
import org.komodo.core.repository.ObjectImpl;
import org.komodo.relational.DeployStatus;
import org.komodo.relational.connection.Connection;
import org.komodo.relational.model.Model;
import org.komodo.relational.vdb.ModelSource;
import org.komodo.relational.vdb.Vdb;
import org.komodo.relational.workspace.WorkspaceManager;
import org.komodo.rest.KRestEntity;
import org.komodo.rest.KomodoRestException;
import org.komodo.rest.KomodoRestV1Application.V1Constants;
import org.komodo.rest.KomodoService;
import org.komodo.rest.relational.KomodoProperties;
import org.komodo.rest.relational.RelationalMessages;
import org.komodo.rest.relational.connection.RestConnection;
import org.komodo.rest.relational.json.KomodoJsonMarshaller;
import org.komodo.rest.relational.request.KomodoConnectionAttributes;
import org.komodo.rest.relational.response.KomodoStatusObject;
import org.komodo.rest.relational.response.RestObjectVdbStatus;
import org.komodo.rest.relational.response.metadata.RestMetadataVdbStatusVdb;
import org.komodo.servicecatalog.TeiidOpenShiftClient;
import org.komodo.spi.KException;
import org.komodo.spi.constants.StringConstants;
import org.komodo.spi.lexicon.datavirt.DataVirtLexicon;
import org.komodo.spi.metadata.MetadataInstance;
import org.komodo.spi.repository.KomodoObject;
import org.komodo.spi.repository.Repository;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.komodo.spi.repository.Repository.UnitOfWork.State;
import org.komodo.spi.runtime.ServiceCatalogDataSource;
import org.komodo.spi.runtime.TeiidDataSource;
import org.komodo.spi.runtime.TeiidVdb;
import org.komodo.utils.StringUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * A Komodo REST service for obtaining Connection information from the workspace.
 */
@Path(V1Constants.WORKSPACE_SEGMENT + StringConstants.FORWARD_SLASH +
           V1Constants.CONNECTIONS_SEGMENT)
@Api(tags = {V1Constants.CONNECTIONS_SEGMENT})
public final class KomodoConnectionService extends KomodoService {

    private static final int ALL_AVAILABLE = -1;
    private TeiidOpenShiftClient openshiftClient;
    private static final String CONNECTION_VDB_SUFFIX = "BtlConn"; //$NON-NLS-1$

    /**
     * Time to wait after deploying/undeploying an artifact from the metadata instance
     */
    private final static int DEPLOYMENT_WAIT_TIME = 10000;

    /**
     * @param engine
     *        the Komodo Engine (cannot be <code>null</code> and must be started)
     * @param openshiftClient OpenShift Client to access service catalog
     * @throws WebApplicationException
     *         if there is a problem obtaining the {@link WorkspaceManager workspace manager}
     */
    public KomodoConnectionService(final KEngine engine, TeiidOpenShiftClient openshiftClient)
            throws WebApplicationException {
        super( engine );
        this.openshiftClient = openshiftClient;
    }

    /**
     * Get the Connections from the komodo repository
     * @param headers
     *        the request headers (never <code>null</code>)
     * @param uriInfo
     *        the request URI information (never <code>null</code>)
     * @return a JSON document representing all the Connections in the Komodo workspace (never <code>null</code>)
     * @throws KomodoRestException
     *         if there is a problem constructing the Connection JSON document
     */
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    @ApiOperation(value = "Return the collection of connections",
                            response = RestConnection[].class)
    @ApiResponses(value = {
        @ApiResponse(code = 403, message = "An error has occurred.")
    })
    public Response getConnections( final @Context HttpHeaders headers,
                                    final @Context UriInfo uriInfo ) throws KomodoRestException {

        SecurityPrincipal principal = checkSecurityContext(headers);
        if (principal.hasErrorResponse())
            return principal.getErrorResponse();

        List<MediaType> mediaTypes = headers.getAcceptableMediaTypes();
        UnitOfWork uow = null;

        try {
            final String searchPattern = uriInfo.getQueryParameters().getFirst( QueryParamKeys.PATTERN );

            // find connections
            uow = createTransaction(principal, "getConnections", true ); //$NON-NLS-1$
            Connection[] connections = null;

            if ( StringUtils.isBlank( searchPattern ) ) {
                connections = getWorkspaceManager(uow).findConnections( uow );
                LOGGER.debug( "getConnections:found '{0}' Connections", connections.length ); //$NON-NLS-1$
            } else {
                final String[] connectionPaths = getWorkspaceManager(uow).findByType( uow, DataVirtLexicon.Connection.NODE_TYPE, null, searchPattern, false );

                if ( connectionPaths.length == 0 ) {
                    connections = Connection.NO_CONNECTIONS;
                } else {
                    connections = new Connection[ connectionPaths.length ];
                    int i = 0;

                    for ( final String path : connectionPaths ) {
                        connections[ i++ ] = getWorkspaceManager(uow).resolve( uow, new ObjectImpl( getWorkspaceManager(uow).getRepository(), path, 0 ), Connection.class );
                    }

                    LOGGER.debug( "getConnections:found '{0}' Connections using pattern '{1}'", connections.length, searchPattern ); //$NON-NLS-1$
                }
            }

            int start = 0;

            { // start query parameter
                final String qparam = uriInfo.getQueryParameters().getFirst( QueryParamKeys.START );

                if ( qparam != null ) {

                    try {
                        start = Integer.parseInt( qparam );

                        if ( start < 0 ) {
                            start = 0;
                        }
                    } catch ( final Exception e ) {
                        start = 0;
                    }
                }
            }

            int size = ALL_AVAILABLE;

            { // size query parameter
                final String qparam = uriInfo.getQueryParameters().getFirst( QueryParamKeys.SIZE );

                if ( qparam != null ) {

                    try {
                        size = Integer.parseInt( qparam );

                        if ( size <= 0 ) {
                            size = ALL_AVAILABLE;
                        }
                    } catch ( final Exception e ) {
                        size = ALL_AVAILABLE;
                    }
                }
            }

            final List< RestConnection > entities = new ArrayList< >();
            int i = 0;

            KomodoProperties properties = new KomodoProperties();
            for ( final Connection connection : connections ) {
                if ( ( start == 0 ) || ( i >= start ) ) {
                    if ( ( size == ALL_AVAILABLE ) || ( entities.size() < size ) ) {                        
                        RestConnection entity = entityFactory.create(connection, uriInfo.getBaseUri(), uow, properties);
                        entities.add(entity);
                        LOGGER.debug("getConnections:Connection '{0}' entity was constructed", connection.getName(uow)); //$NON-NLS-1$
                    } else {
                        break;
                    }
                }

                ++i;
            }

            // create response
            return commit( uow, mediaTypes, entities );

        } catch ( final Exception e ) {
            if ( ( uow != null ) && ( uow.getState() != State.ROLLED_BACK ) ) {
                uow.rollback();
            }

            if ( e instanceof KomodoRestException ) {
                throw ( KomodoRestException )e;
            }

            return createErrorResponseWithForbidden(mediaTypes, e, RelationalMessages.Error.CONNECTION_SERVICE_GET_CONNECTIONS_ERROR);
        }
    }

    /**
     * @param headers
     *        the request headers (never <code>null</code>)
     * @param uriInfo
     *        the request URI information (never <code>null</code>)
     * @param connectionName
     *        the id of the Connection being retrieved (cannot be empty)
     * @return the JSON representation of the Connection (never <code>null</code>)
     * @throws KomodoRestException
     *         if there is a problem finding the specified workspace Connection or constructing the JSON representation
     */
    @GET
    @Path( V1Constants.CONNECTION_PLACEHOLDER )
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
    @ApiOperation(value = "Find connection by name", response = RestConnection.class)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "No Connection could be found with name"),
        @ApiResponse(code = 406, message = "Only JSON or XML is returned by this operation"),
        @ApiResponse(code = 403, message = "An error has occurred.")
    })
    public Response getConnection( final @Context HttpHeaders headers,
                                   final @Context UriInfo uriInfo,
                                   @ApiParam(
                                             value = "Name of the connection",
                                             required = true
                                   )
                                   final @PathParam( "connectionName" ) String connectionName) throws KomodoRestException {

        SecurityPrincipal principal = checkSecurityContext(headers);
        if (principal.hasErrorResponse())
            return principal.getErrorResponse();

        List<MediaType> mediaTypes = headers.getAcceptableMediaTypes();
        UnitOfWork uow = null;

        try {
            uow = createTransaction(principal, "getConnection", true ); //$NON-NLS-1$

            Connection connection = findConnection(uow, connectionName);
            if (connection == null)
                return commitNoConnectionFound(uow, mediaTypes, connectionName);

            KomodoProperties properties = new KomodoProperties();
            final RestConnection restConnection = entityFactory.create(connection, uriInfo.getBaseUri(), uow, properties);
            LOGGER.debug("getConnection:Connection '{0}' entity was constructed", connection.getName(uow)); //$NON-NLS-1$
            return commit( uow, mediaTypes, restConnection );

        } catch ( final Exception e ) {
            if ( ( uow != null ) && ( uow.getState() != State.ROLLED_BACK ) ) {
                uow.rollback();
            }

            if ( e instanceof KomodoRestException ) {
                throw ( KomodoRestException )e;
            }

            return createErrorResponseWithForbidden(mediaTypes, e, RelationalMessages.Error.CONNECTION_SERVICE_GET_CONNECTION_ERROR, connectionName);
        }
    }
    
    /**
     * Create a new Connection in the komodo repository, using a service catalogSource
     * @param headers
     *        the request headers (never <code>null</code>)
     * @param uriInfo
     *        the request URI information (never <code>null</code>)
     * @param connectionName
     *        the connection name (cannot be empty)
     * @param connectionJson
     *        the connection JSON representation (cannot be <code>null</code>)
     * @return a JSON representation of the new connection (never <code>null</code>)
     * @throws KomodoRestException
     *         if there is an error creating the Connection
     */
    @POST
    @Path( StringConstants.FORWARD_SLASH + V1Constants.CONNECTION_PLACEHOLDER )
    @Produces( MediaType.APPLICATION_JSON )
    @ApiOperation(value = "Create a connection in the workspace, using ServiceCatalogSource")
    @ApiResponses(value = {
        @ApiResponse(code = 406, message = "Only JSON is returned by this operation"),
        @ApiResponse(code = 403, message = "An error has occurred.")
    })
    public Response createConnection( final @Context HttpHeaders headers,
                                      final @Context UriInfo uriInfo,
                                      @ApiParam(
                                              value = "Name of the connection",
                                              required = true
                                      )
                                      final @PathParam( "connectionName" ) String connectionName,
                                      @ApiParam(
                                              value = "" + 
                                                      "Properties for the new connection:<br>" +
                                                      OPEN_PRE_TAG +
                                                      OPEN_BRACE + BR +
                                                      NBSP + "description: \"description for the connection\"" + COMMA + BR +
                                                      NBSP + "serviceCatalogSource: \"serviceCatalog source for the connection\"" + BR +
                                                      CLOSE_BRACE +
                                                      CLOSE_PRE_TAG,
                                              required = true
                                      )
                                      final String connectionJson) throws KomodoRestException {

        SecurityPrincipal principal = checkSecurityContext(headers);
        if (principal.hasErrorResponse())
            return principal.getErrorResponse();

        List<MediaType> mediaTypes = headers.getAcceptableMediaTypes();
        if (! isAcceptable(mediaTypes, MediaType.APPLICATION_JSON_TYPE))
            return notAcceptableMediaTypesBuilder().build();

        // Error if the connection name is missing
        if (StringUtils.isBlank( connectionName )) {
            return createErrorResponseWithForbidden(mediaTypes, RelationalMessages.Error.CONNECTION_SERVICE_MISSING_NAME);
        }

        // Get the attributes - ensure valid attributes provided
        KomodoConnectionAttributes rcAttr;
        try {
        	rcAttr = KomodoJsonMarshaller.unmarshall(connectionJson, KomodoConnectionAttributes.class);
            
            Response response = checkConnectionAttributes(rcAttr, mediaTypes);
            if (response.getStatus() != Status.OK.getStatusCode())
                return response;

        } catch (Exception ex) {
            return createErrorResponseWithForbidden(mediaTypes, ex, RelationalMessages.Error.CONNECTION_SERVICE_REQUEST_PARSING_ERROR);
        }
        
        ServiceCatalogDataSource serviceCatalogSource = null;
        
        RestConnection restConnection = new RestConnection();
        restConnection.setId(connectionName);

        try {
            // Add properties for the description and serviceCatalogSource
            restConnection.addProperty("description", rcAttr.getDescription());
            restConnection.addProperty("serviceCatalogSource", rcAttr.getServiceCatalogSource());
            restConnection.setJdbc(true);
            
            // Get the specified ServiceCatalogDataSource from the metadata instance
            Collection<ServiceCatalogDataSource> dataSources = openshiftClient.getServiceCatalogSources();
			for(ServiceCatalogDataSource ds: dataSources) {
				if(ds.getName().equals(rcAttr.getServiceCatalogSource())) {
					serviceCatalogSource = ds;
					break;
				}
			}
			// If catalogSource is not found, exit with error
			if (serviceCatalogSource == null) {
				return createErrorResponseWithForbidden(mediaTypes,
						RelationalMessages.Error.CONNECTION_SERVICE_CATALOG_SOURCE_DNE_ERROR);
			}
        } catch (Exception ex) {
            throw new KomodoRestException(ex);
        }

        UnitOfWork uow = null;
        try {
            uow = createTransaction(principal, "createConnection", false ); //$NON-NLS-1$

            // Error if the repo already contains a connection with the supplied name.
            if ( getWorkspaceManager(uow).hasChild( uow, connectionName ) ) {
                return createErrorResponseWithForbidden(mediaTypes, RelationalMessages.Error.CONNECTION_SERVICE_CREATE_ALREADY_EXISTS);
            }

			// Ensures service catalog is bound, and creates the corresponding datasource in wildfly
			openshiftClient.bindToServiceCatalogSource(serviceCatalogSource.getName());
			
			// Get the connection from the wildfly instance (should be available after binding)
            TeiidDataSource dataSource = getMetadataInstance().getDataSource(serviceCatalogSource.getName());
            if (dataSource == null)
                return commitNoConnectionFound(uow, mediaTypes, connectionName);
			
            // Add the jndi and driver to the komodo connection to be created
            restConnection.setJndiName(dataSource.getJndiName());
            restConnection.setDriverName(dataSource.getType());

            // create new Connection
            return doAddConnection( uow, uriInfo.getBaseUri(), mediaTypes, restConnection );

        } catch (final Exception e) {
            if ((uow != null) && (uow.getState() != State.ROLLED_BACK)) {
                uow.rollback();
            }

            if (e instanceof KomodoRestException) {
                throw (KomodoRestException)e;
            }

            return createErrorResponseWithForbidden(mediaTypes, e, RelationalMessages.Error.CONNECTION_SERVICE_CREATE_CONNECTION_ERROR, connectionName);
        }
    }

    /**
     * Clone a Connection in the komodo repository
     * @param headers
     *        the request headers (never <code>null</code>)
     * @param uriInfo
     *        the request URI information (never <code>null</code>)
     * @param connectionName
     *        the connection name (cannot be empty)
     * @param newConnectionName
     *        the new connection name (cannot be empty)
     * @return a JSON representation of the new connection (never <code>null</code>)
     * @throws KomodoRestException
     *         if there is an error creating the Connection
     */
    @POST
    @Path( StringConstants.FORWARD_SLASH + V1Constants.CLONE_SEGMENT + StringConstants.FORWARD_SLASH + V1Constants.CONNECTION_PLACEHOLDER )
    @Produces( MediaType.APPLICATION_JSON )
    @ApiOperation(value = "Clone a connection in the workspace")
    @ApiResponses(value = {
        @ApiResponse(code = 406, message = "Only JSON is returned by this operation"),
        @ApiResponse(code = 403, message = "An error has occurred.")
    })
    public Response cloneConnection( final @Context HttpHeaders headers,
                                     final @Context UriInfo uriInfo,
                                     @ApiParam(
                                               value = "Name of the connection",
                                               required = true
                                     )
                                     final @PathParam( "connectionName" ) String connectionName,
                                     @ApiParam(
                                               value = "The new name of the connection",
                                               required = true
                                     )
                                     final String newConnectionName) throws KomodoRestException {

        SecurityPrincipal principal = checkSecurityContext(headers);
        if (principal.hasErrorResponse())
            return principal.getErrorResponse();

        List<MediaType> mediaTypes = headers.getAcceptableMediaTypes();
        if (! isAcceptable(mediaTypes, MediaType.APPLICATION_JSON_TYPE))
            return notAcceptableMediaTypesBuilder().build();

        // Error if the connection name is missing
        if (StringUtils.isBlank( connectionName )) {
            return createErrorResponseWithForbidden(mediaTypes, RelationalMessages.Error.CONNECTION_SERVICE_MISSING_NAME);
        }

        // Error if the new connection name is missing
        if ( StringUtils.isBlank( newConnectionName ) ) {
            return createErrorResponseWithForbidden(mediaTypes, RelationalMessages.Error.CONNECTION_SERVICE_CLONE_MISSING_NEW_NAME);
        }

        // Error if the name parameter and new name are the same
        final boolean namesMatch = connectionName.equals( newConnectionName );
        if ( namesMatch ) {
            return createErrorResponseWithForbidden(mediaTypes, RelationalMessages.Error.CONNECTION_SERVICE_CLONE_SAME_NAME_ERROR, newConnectionName);
        }

        UnitOfWork uow = null;

        try {
            uow = createTransaction(principal, "cloneConnection", false ); //$NON-NLS-1$

            // Error if the repo already contains a connection with the supplied name.
            if ( getWorkspaceManager(uow).hasChild( uow, newConnectionName ) ) {
                return createErrorResponseWithForbidden(mediaTypes, RelationalMessages.Error.CONNECTION_SERVICE_CLONE_ALREADY_EXISTS);
            }

            // create new Connection
            // must be an update
            final KomodoObject kobject = getWorkspaceManager(uow).getChild( uow, connectionName, DataVirtLexicon.Connection.NODE_TYPE );
            final Connection oldConnection = getWorkspaceManager(uow).resolve( uow, kobject, Connection.class );
            final RestConnection oldEntity = entityFactory.create(oldConnection, uriInfo.getBaseUri(), uow );
            
            final Connection connection = getWorkspaceManager(uow).createConnection( uow, null, newConnectionName);

            setProperties( uow, connection, oldEntity );

            final RestConnection entity = entityFactory.create(connection, uriInfo.getBaseUri(), uow );
            final Response response = commit( uow, mediaTypes, entity );
            return response;
        } catch (final Exception e) {
            if ((uow != null) && (uow.getState() != State.ROLLED_BACK)) {
                uow.rollback();
            }

            if (e instanceof KomodoRestException) {
                throw (KomodoRestException)e;
            }

            return createErrorResponseWithForbidden(mediaTypes, e, RelationalMessages.Error.CONNECTION_SERVICE_CLONE_CONNECTION_ERROR);
        }
    }

    /**
     * Update a Connection in the komodo repository, using service catalog source
     * @param headers
     *        the request headers (never <code>null</code>)
     * @param uriInfo
     *        the request URI information (never <code>null</code>)
     * @param connectionName
     *        the connection name (cannot be empty)
     * @param connectionJson
     *        the connection JSON representation (cannot be <code>null</code>)
     * @return a JSON representation of the updated connection (never <code>null</code>)
     * @throws KomodoRestException
     *         if there is an error updating the VDB
     */
    @PUT
    @Path( StringConstants.FORWARD_SLASH + V1Constants.CONNECTION_PLACEHOLDER )
    @Produces( MediaType.APPLICATION_JSON )
    @ApiOperation(value = "Update a connection in the workspace")
    @ApiResponses(value = {
        @ApiResponse(code = 406, message = "Only JSON is returned by this operation"),
        @ApiResponse(code = 403, message = "An error has occurred.")
    })
    public Response updateConnection( final @Context HttpHeaders headers,
                                      final @Context UriInfo uriInfo,
                                      @ApiParam(
                                                value = "Name of the connection",
                                                required = true
                                      )
                                      final @PathParam( "connectionName" ) String connectionName,
                                      @ApiParam(
                                                value = "" + 
                                                        "Properties for the connection update:<br>" +
                                                        OPEN_PRE_TAG +
                                                        OPEN_BRACE + BR +
                                                        NBSP + "description: \"description for the connection\"" + COMMA + BR +
                                                        NBSP + "serviceCatalogSource: \"serviceCatalog source for the connection\"" + BR +
                                                        CLOSE_BRACE +
                                                        CLOSE_PRE_TAG,
                                                required = true
                                      )
                                      final String connectionJson) throws KomodoRestException {

        SecurityPrincipal principal = checkSecurityContext(headers);
        if (principal.hasErrorResponse())
            return principal.getErrorResponse();

        List<MediaType> mediaTypes = headers.getAcceptableMediaTypes();
        if (! isAcceptable(mediaTypes, MediaType.APPLICATION_JSON_TYPE))
            return notAcceptableMediaTypesBuilder().build();

        // Error if the connection name is missing
        if (StringUtils.isBlank( connectionName )) {
            return createErrorResponseWithForbidden(mediaTypes, RelationalMessages.Error.CONNECTION_SERVICE_MISSING_NAME);
        }
        
        // Get the attributes - ensure valid attributes provided
        KomodoConnectionAttributes rcAttr;
        try {
        	rcAttr = KomodoJsonMarshaller.unmarshall(connectionJson, KomodoConnectionAttributes.class);
            
            Response response = checkConnectionAttributes(rcAttr, mediaTypes);
            if (response.getStatus() != Status.OK.getStatusCode())
                return response;

        } catch (Exception ex) {
            return createErrorResponseWithForbidden(mediaTypes, ex, RelationalMessages.Error.CONNECTION_SERVICE_REQUEST_PARSING_ERROR);
        }

        ServiceCatalogDataSource serviceCatalogSource = null;

        RestConnection restConnection = new RestConnection();
        restConnection.setId(connectionName);

        try {
            // Add properties for the description and serviceCatalogSource
            restConnection.addProperty("description", rcAttr.getDescription());
            restConnection.addProperty("serviceCatalogSource", rcAttr.getServiceCatalogSource());
            restConnection.setJdbc(true);
            
            // Get the specified ServiceCatalogDataSource from the metadata instance
            Collection<ServiceCatalogDataSource> dataSources = openshiftClient.getServiceCatalogSources();
			for(ServiceCatalogDataSource ds: dataSources) {
				if(ds.getName().equals(rcAttr.getServiceCatalogSource())) {
					serviceCatalogSource = ds;
					break;
				}
			}
			// If catalogSource is not found, exit with error
			if(serviceCatalogSource == null) {
                return createErrorResponseWithForbidden(mediaTypes, RelationalMessages.Error.CONNECTION_SERVICE_CATALOG_SOURCE_DNE_ERROR);
			}
        } catch (Exception ex) {
            throw new KomodoRestException(ex);
        }

        UnitOfWork uow = null;
        try {
            uow = createTransaction(principal, "updateConnection", false ); //$NON-NLS-1$

            final boolean exists = getWorkspaceManager(uow).hasChild( uow, connectionName );
            // Error if the specified connection does not exist
            if ( !exists ) {
                return createErrorResponseWithForbidden(mediaTypes, RelationalMessages.Error.CONNECTION_SERVICE_UPDATE_CONNECTION_DNE);
            }

            // Update deletes the existing connection and recreates it.
            final KomodoObject kobject = getWorkspaceManager(uow).getChild( uow, connectionName, DataVirtLexicon.Connection.NODE_TYPE );
            getWorkspaceManager(uow).delete(uow, kobject);

			// Ensures service catalog is bound, and creates the corresponding datasource in wildfly
			openshiftClient.bindToServiceCatalogSource(serviceCatalogSource.getName());
			
			// Get the connection from the wildfly instance (should be available after binding)
            TeiidDataSource dataSource = getMetadataInstance().getDataSource(serviceCatalogSource.getName());
            if (dataSource == null)
                return commitNoConnectionFound(uow, mediaTypes, connectionName);
			
            // Add the jndi and driver to the komodo connection to be created
            restConnection.setJndiName(dataSource.getJndiName());
            restConnection.setDriverName(dataSource.getType());

            // Create the connection
            Response response = doAddConnection( uow, uriInfo.getBaseUri(), mediaTypes, restConnection );

            LOGGER.debug("updateConnection: connection '{0}' entity was updated", connectionName); //$NON-NLS-1$

            return response;
        } catch (final Exception e) {
            if ((uow != null) && (uow.getState() != State.ROLLED_BACK)) {
                uow.rollback();
            }

            if (e instanceof KomodoRestException) {
                throw (KomodoRestException)e;
            }

            return createErrorResponseWithForbidden(mediaTypes, e, RelationalMessages.Error.CONNECTION_SERVICE_UPDATE_CONNECTION_ERROR);
        }
    }

    private Response doAddConnection( final UnitOfWork uow,
                                      final URI baseUri,
                                      final List<MediaType> mediaTypes,
                                      final RestConnection restConnection ) throws KomodoRestException {
        assert( !uow.isRollbackOnly() );
        assert( uow.getState() == State.NOT_STARTED );
        assert( restConnection != null );

        final String connectionName = restConnection.getId();
        try {
            final Connection connection = getWorkspaceManager(uow).createConnection( uow, null, connectionName);

            // Transfers the properties from the rest object to the created komodo service.
            setProperties(uow, connection, restConnection);

            final RestConnection entity = entityFactory.create(connection, baseUri, uow );
            final Response response = commit( uow, mediaTypes, entity );
            return response;
        } catch ( final Exception e ) {
            if ((uow != null) && (uow.getState() != State.ROLLED_BACK)) {
                uow.rollback();
            }

            if (e instanceof KomodoRestException) {
                throw (KomodoRestException)e;
            }

            throw new KomodoRestException( RelationalMessages.getString( RelationalMessages.Error.CONNECTION_SERVICE_CREATE_CONNECTION_ERROR, connectionName ), e );
        }
    }

    /**
     * Delete the specified Connection from the komodo repository
     * @param headers
     *        the request headers (never <code>null</code>)
     * @param uriInfo
     *        the request URI information (never <code>null</code>)
     * @param connectionName
     *        the name of the connection to remove (cannot be <code>null</code>)
     * @return a JSON document representing the results of the removal
     * @throws KomodoRestException
     *         if there is a problem performing the delete
     */
    @DELETE
    @Path("{connectionName}")
    @Produces( MediaType.APPLICATION_JSON )
    @ApiOperation(value = "Delete a connection from the workspace")
    @ApiResponses(value = {
        @ApiResponse(code = 406, message = "Only JSON is returned by this operation"),
        @ApiResponse(code = 403, message = "An error has occurred.")
    })
    public Response deleteConnection( final @Context HttpHeaders headers,
                                      final @Context UriInfo uriInfo,
                                      @ApiParam(
                                                value = "Name of the connection",
                                                required = true
                                      )
                                      final @PathParam( "connectionName" ) String connectionName) throws KomodoRestException {
        SecurityPrincipal principal = checkSecurityContext(headers);
        if (principal.hasErrorResponse())
            return principal.getErrorResponse();

        List<MediaType> mediaTypes = headers.getAcceptableMediaTypes();

        UnitOfWork uow = null;
        try {
            uow = createTransaction(principal, "removeConnectionFromWorkspace", false); //$NON-NLS-1$
            Repository repo = this.kengine.getDefaultRepository();
            final WorkspaceManager mgr = WorkspaceManager.getInstance( repo, uow );
            KomodoObject connection = mgr.getChild(uow, connectionName, DataVirtLexicon.Connection.NODE_TYPE);

            if (connection == null)
                return Response.noContent().build();

            mgr.delete(uow, connection);

            KomodoStatusObject kso = new KomodoStatusObject("Delete Status"); //$NON-NLS-1$
            if (mgr.hasChild(uow, connectionName))
                kso.addAttribute(connectionName, "Deletion failure"); //$NON-NLS-1$
            else
                kso.addAttribute(connectionName, "Successfully deleted"); //$NON-NLS-1$

            return commit(uow, mediaTypes, kso);
        } catch (final Exception e) {
            if ((uow != null) && (uow.getState() != State.ROLLED_BACK)) {
                uow.rollback();
            }

            if (e instanceof KomodoRestException) {
                throw (KomodoRestException)e;
            }

            return createErrorResponseWithForbidden(mediaTypes, e, RelationalMessages.Error.CONNECTION_SERVICE_DELETE_CONNECTION_ERROR);
        }
    }

    /**
     * @param headers
     *            the request headers (never <code>null</code>)
     * @param uriInfo
     *            the request URI information (never <code>null</code>)
     * @param connectionName
     *            the Connection name being validated (cannot be empty)
     * @return the response (never <code>null</code>) with an entity that is
     *         either an empty string, when the name is valid, or an error
     *         message
     * @throws KomodoRestException
     *             if there is a problem validating the name or constructing
     *             the response
     */
    @GET
    @Path( V1Constants.NAME_VALIDATION_SEGMENT + StringConstants.FORWARD_SLASH + V1Constants.CONNECTION_PLACEHOLDER )
    @Produces( { MediaType.TEXT_PLAIN } )
    @ApiOperation( value = "Returns an error message if the Connection name is invalid" )
    @ApiResponses( value = {
            @ApiResponse( code = 400, message = "The URI cannot contain encoded slashes or backslashes." ),
            @ApiResponse( code = 403, message = "An unexpected error has occurred." ),
            @ApiResponse( code = 500, message = "The Connection name cannot be empty." )
    } )
    public Response validateConnectionName( final @Context HttpHeaders headers,
                                     final @Context UriInfo uriInfo,
                                     @ApiParam( value = "The Connection name being checked", required = true )
                                     final @PathParam( "connectionName" ) String connectionName ) throws KomodoRestException {

        final SecurityPrincipal principal = checkSecurityContext( headers );

        if ( principal.hasErrorResponse() ) {
            return principal.getErrorResponse();
        }

        final String errorMsg = VALIDATOR.checkValidName( connectionName );
        
        // a name validation error occurred
        if ( errorMsg != null ) {
            return Response.ok().entity( errorMsg ).build();
        }

        UnitOfWork uow = null;

        try {
            uow = createTransaction( principal, "validateConnectionName", true ); //$NON-NLS-1$

            // make sure an existing Connection does not have that name
            final Connection connection = findConnection( uow, connectionName );

            if ( connection == null ) {
                // make sure an existing vdb does not have the same name
                final Vdb ds = findVdb( uow, connectionName );

                if ( ds == null ) {
                    // name is valid
                    return Response.ok().build();
                }

                // name is the same as an existing connection
                return Response.ok()
                               .entity( RelationalMessages.getString( VDB_DATA_SOURCE_NAME_EXISTS ) )
                               .build();
            }

            // name is the same as an existing connection
            return Response.ok()
                           .entity( RelationalMessages.getString( CONNECTION_SERVICE_NAME_EXISTS ) )
                           .build();
        } catch ( final Exception e ) {
            if ( ( uow != null ) && ( uow.getState() != State.ROLLED_BACK ) ) {
                uow.rollback();
            }

            if ( e instanceof KomodoRestException ) {
                throw ( KomodoRestException )e;
            }

            return createErrorResponseWithForbidden( headers.getAcceptableMediaTypes(), 
                                                     e, 
                                                     CONNECTION_SERVICE_NAME_VALIDATION_ERROR );
        }
    }
    
    /**
     * Deploy a VDB for the specified workspace connection
     * @param headers
     *            the request headers (never <code>null</code>)
     * @param uriInfo
     *            the request URI information (never <code>null</code>)
     * @param connectionName
     *            the Connection name being deployed (cannot be empty)
     * @return a JSON document representing the results of the removal
     * @throws KomodoRestException
     *             if there is a problem deploying the vdb
     */
    @POST
    @Path( V1Constants.CONNECTION_DEPLOY_VDB_SEGMENT + StringConstants.FORWARD_SLASH + V1Constants.CONNECTION_PLACEHOLDER )
    @Produces( { MediaType.APPLICATION_JSON } )
    @ApiOperation( value = "Deploy a VDB for the workspace connection" )
    @ApiResponses(value = {
            @ApiResponse(code = 406, message = "Only JSON is returned by this operation"),
            @ApiResponse(code = 403, message = "An error has occurred.")
        })
    public Response deployVdbForConnection( final @Context HttpHeaders headers,
                                     final @Context UriInfo uriInfo,
                                     @ApiParam( value = "Name of the connection", required = true )
                                     final @PathParam( "connectionName" ) String connectionName ) throws KomodoRestException {

        SecurityPrincipal principal = checkSecurityContext(headers);
        if (principal.hasErrorResponse())
            return principal.getErrorResponse();

        List<MediaType> mediaTypes = headers.getAcceptableMediaTypes();
        if (! isAcceptable(mediaTypes, MediaType.APPLICATION_JSON_TYPE))
            return notAcceptableMediaTypesBuilder().build();

        // Error if the connection name is missing
        if (StringUtils.isBlank( connectionName )) {
            return createErrorResponseWithForbidden(mediaTypes, RelationalMessages.Error.CONNECTION_SERVICE_MISSING_NAME);
        }

        UnitOfWork uow = null;

        try {
            uow = createTransaction( principal, "deployVdbForConnection", false ); //$NON-NLS-1$

            // Get the connection
            Connection connection = findConnection(uow, connectionName);
            if (connection == null)
                return commitNoConnectionFound(uow, mediaTypes, connectionName);
            String jndiName = connection.getJndiName(uow);
            String driverName = connection.getDriverName(uow);
            
            // Name of VDB to be created, based on connection
            String vdbName = connectionName + CONNECTION_VDB_SUFFIX;
            
            // If VDB with name already exists, delete it
            Repository repo = this.kengine.getDefaultRepository();
            final WorkspaceManager mgr = WorkspaceManager.getInstance( repo, uow );
            String repoPath = repo.komodoWorkspace( uow ).getAbsolutePath();
            
            final Vdb existingVdb = findVdb( uow, vdbName );

            if ( existingVdb != null ) {
                mgr.delete(uow, existingVdb);
            }
            
            // Create new VDB
            String vdbPath = repoPath + "/" + vdbName;
            final Vdb vdb = getWorkspaceManager(uow).createVdb( uow, null, vdbName, vdbPath );
            vdb.setDescription(uow, "Vdb for connection "+connectionName);
                        
            // Add model to the VDB
            Model model = vdb.addModel(uow, connectionName);
            model.setModelType(uow, Model.Type.PHYSICAL);
            model.setProperty(uow, "importer.TableTypes", "TABLE,VIEW");
            model.setProperty(uow, "importer.UseQualifiedName", "true");
            model.setProperty(uow, "importer.UseCatalogName", "false");
            model.setProperty(uow, "importer.UseFullSchemaName", "false");
            
            // Add model source to the model
            ModelSource modelSource = model.addSource(uow, connectionName);
            modelSource.setJndiName(uow, jndiName);
            modelSource.setTranslatorName(uow, driverName);
            
            // Deploy the VDB
            DeployStatus deployStatus = vdb.deploy(uow);
            
            // Await the deployment to end
            Thread.sleep(DEPLOYMENT_WAIT_TIME);

            String title = RelationalMessages.getString(RelationalMessages.Info.VDB_DEPLOYMENT_STATUS_TITLE);
            KomodoStatusObject status = new KomodoStatusObject(title);

            List<String> progressMessages = deployStatus.getProgressMessages();
            for (int i = 0; i < progressMessages.size(); ++i) {
                status.addAttribute("ProgressMessage" + (i + 1), progressMessages.get(i));
            }

            if (deployStatus.ok()) {
                status.addAttribute("deploymentSuccess", Boolean.TRUE.toString());
                status.addAttribute(vdb.getName(uow),
                                    RelationalMessages.getString(RelationalMessages.Info.VDB_SUCCESSFULLY_DEPLOYED));
            } else {
                status.addAttribute("deploymentSuccess", Boolean.FALSE.toString());
                List<String> errorMessages = deployStatus.getErrorMessages();
                for (int i = 0; i < errorMessages.size(); ++i) {
                    status.addAttribute("ErrorMessage" + (i + 1), errorMessages.get(i));
                }

                status.addAttribute(vdb.getName(uow),
                                    RelationalMessages.getString(RelationalMessages.Info.VDB_DEPLOYED_WITH_ERRORS));
            }

           return commit(uow, mediaTypes, status);
        } catch ( final Exception e ) {
            if ( ( uow != null ) && ( uow.getState() != State.ROLLED_BACK ) ) {
                uow.rollback();
            }

            if ( e instanceof KomodoRestException ) {
                throw ( KomodoRestException )e;
            }

            return createErrorResponseWithForbidden( headers.getAcceptableMediaTypes(), 
                                                     e, 
                                                     CONNECTION_SERVICE_DEPLOY_CONNECTION_VDB_ERROR );
        }
    }
    
    /**
     * Gets the VDBStatus for all connections in the workspace.  If no VDB found for a connection,
     * no status is returned.
     * @param headers
     *        the request headers (never <code>null</code>)
     * @param uriInfo
     *        the request URI information (never <code>null</code>)
     * @return a JSON document the name of all connections and their VDB status in the local server (never <code>null</code>)
     * @throws KomodoRestException
     *         if there is a problem constructing the VDBs JSON document
     */
    @GET
    @Path(V1Constants.VDB_STATUS_SEGMENT)
    @Produces( MediaType.APPLICATION_JSON )
    @ApiOperation(value = "Display the VDBStatus of all connections in the workspace",
                  response = RestObjectVdbStatus.class)
    @ApiResponses(value = {
        @ApiResponse(code = 403, message = "An error has occurred.")
    })
    public Response getVdbStatuses(final @Context HttpHeaders headers,
                                   final @Context UriInfo uriInfo) throws KomodoRestException {

        SecurityPrincipal principal = checkSecurityContext(headers);
        if (principal.hasErrorResponse())
            return principal.getErrorResponse();

        List<MediaType> mediaTypes = headers.getAcceptableMediaTypes();
        UnitOfWork uow = null;

        List<KRestEntity> statusList = new ArrayList<KRestEntity>();
        try {
            uow = createTransaction(principal, "getConnectionVdbStatuses", true ); //$NON-NLS-1$
            
            // Get all workspace connections and all metadata instance VDBs
            Connection[] connections = getWorkspaceManager(uow).findConnections( uow );
            Collection<TeiidVdb> vdbs = getMetadataInstance().getVdbs();
            
            for(Connection connection: connections) {
            	String connectionName = connection.getName(uow);
            	String connVdbName = connectionName+CONNECTION_VDB_SUFFIX;
            	
            	// Find VDB for the connection and add status
            	TeiidVdb connVdb = null;
                for(TeiidVdb vdb : vdbs) {
                	if(vdb.getName().equals(connVdbName)) {
                		connVdb = vdb;
                		break;
                	}
                }
        		RestMetadataVdbStatusVdb vdbStatus = null;
                if(connVdb!=null) {
            		vdbStatus = new RestMetadataVdbStatusVdb(connVdb);
                } else {
            		vdbStatus = RestMetadataVdbStatusVdb.emptyVdb();
                }
        		statusList.add(new RestObjectVdbStatus(connectionName,vdbStatus));
            }

            // create response
            return commit( uow, mediaTypes, statusList );

        } catch (Throwable e) {

            if ( e instanceof KomodoRestException ) {
                throw ( KomodoRestException )e;
            }

            return createErrorResponseWithForbidden(mediaTypes, e, RelationalMessages.Error.CONNECTION_SERVICE_VDBS_STATUS_ERROR);
        }
    }

    private synchronized MetadataInstance getMetadataInstance() throws KException {
        return this.kengine.getMetadataInstance();
    }
    
    /*
     * Checks the supplied attributes for create and update of connections
     *  - serviceCatalogSource is required
     *  - description is optional
     */
    private Response checkConnectionAttributes(KomodoConnectionAttributes attr,
                                               List<MediaType> mediaTypes) throws Exception {

        if ( attr == null || attr.getServiceCatalogSource() == null ) {
            return createErrorResponseWithForbidden(mediaTypes, RelationalMessages.Error.CONNECTION_SERVICE_MISSING_PARAMETER_ERROR);
        }

        return Response.ok().build();
    }
    
}
