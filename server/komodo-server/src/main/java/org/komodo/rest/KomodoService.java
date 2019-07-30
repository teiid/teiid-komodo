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
package org.komodo.rest;

import static org.komodo.rest.Messages.Error.COMMIT_TIMEOUT;
import static org.komodo.rest.Messages.Error.RESOURCE_NOT_FOUND;
import static org.komodo.rest.Messages.General.GET_OPERATION_NAME;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Variant.VariantListBuilder;

import org.komodo.KEngine;
import org.komodo.KException;
import org.komodo.UnitOfWork;
import org.komodo.UnitOfWork.TimeoutException;
import org.komodo.WorkspaceManager;
import org.komodo.datavirtualization.DataVirtualization;
import org.komodo.datavirtualization.ViewDefinition;
import org.komodo.rest.AuthHandlingFilter.OAuthCredentials;
import org.komodo.rest.KomodoRestV1Application.V1Constants;
import org.komodo.rest.RestBasicEntity.ResourceNotFound;
import org.komodo.rest.relational.RelationalMessages;
import org.komodo.rest.relational.json.KomodoJsonMarshaller;
import org.komodo.utils.KLog;
import org.komodo.utils.StringNameValidator;
import org.komodo.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;

/**
 * A Komodo service implementation.
 */
public abstract class KomodoService implements V1Constants {
	
    /**
	 * System user for transactions to be executed internally
	 */
	public static final String SYSTEM_USER_NAME = "SYSTEM";

    public static final String REPO_USER = "anonymous";

	public static final String ENCRYPTED_PREFIX = "ENCRYPTED-";

    protected static final KLog LOGGER = KLog.getLogger();

    protected static final StringNameValidator VALIDATOR = new StringNameValidator();

    protected static final int ALL_AVAILABLE = -1;

    private static final int TIMEOUT = 30;
    private static final TimeUnit UNIT = TimeUnit.SECONDS;

    /**
     * Query parameter keys used by the service methods.
     */
    public interface QueryParamKeys {

        /**
         * A regex expression used when searching. If not present, all objects are returned.
         */
        String PATTERN = "pattern"; //$NON-NLS-1$

        /**
         * The number of objects to return. If not present, all objects are returned.
         */
        String SIZE = "size"; //$NON-NLS-1$

        /**
         * The index of the first object to return. Defaults to zero.
         */
        String START = "start"; //$NON-NLS-1$

		String VIRTUALIZATION = "virtualization";
    }

    private class ErrorResponse {
        private final String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        @SuppressWarnings( "unused" )
        public String getError() {
            return error;
        }
    }

    protected static class SecurityPrincipal {

        private final String userName;

        private final Response errorResponse;

        public SecurityPrincipal(String userName, Response errorResponse) {
            this.userName = userName;
            this.errorResponse = errorResponse;
        }

        public String getUserName() {
            return userName;
        }

        public boolean hasErrorResponse() {
            return errorResponse != null;
        }

        public Response getErrorResponse() {
            return errorResponse;
        }
    }
    
    /**
     * <strong>*** The prefix needs to match the format that Beetle Studio uses. ***</strong>
     *
     * @param vdbName the VDB the view is contained in (cannot be empty)
     * @return the prefix of the view editor state ID (never empty)
     */
    public static String getViewEditorStateIdPrefix( final String vdbName ) {
        assert( !StringUtils.isBlank( vdbName ) );
        return vdbName + '.';
    }

    public final static SecurityPrincipal SYSTEM_USER = new SecurityPrincipal(SYSTEM_USER_NAME, null);

    @Autowired
    protected KEngine kengine;

    @Context
    protected SecurityContext securityContext;

    /**
     * @param value the value
     * @return the value encoded for json
     */
    public static String protectPrefix(String value) {
        if (value == null)
            return null;

        value = value.replaceAll(COLON, PREFIX_SEPARATOR);
        return value;
    }

    /**
     * @param value the value
     * @return the value decoded from json transit
     */
    public static String unprotectPrefix(String value) {
        if (value == null)
            return null;

        value = value.replaceAll(PREFIX_SEPARATOR, COLON);
        return value;
    }

    protected OAuthCredentials getAuthenticationToken() {
        return AuthHandlingFilter.threadOAuthCredentials.get();
    }

    protected SecurityPrincipal checkSecurityContext(HttpHeaders headers) {
        OAuthCredentials oAuthCredentials = AuthHandlingFilter.threadOAuthCredentials.get();

        //
        // Without oauth proxy running oAuthCredentials is not null but its user is.
        // This will allow the default to the 'komodo' user but the catalog-service resource methods
        // will not be available.
        //
        if (oAuthCredentials != null && oAuthCredentials.getUser() != null) {
            return new SecurityPrincipal(oAuthCredentials.getUser(), null);
        }

		return new SecurityPrincipal(
		                             "komodo",
		                             createErrorResponse(Status.UNAUTHORIZED,
		                             headers.getAcceptableMediaTypes(), RelationalMessages.Error.SECURITY_FAILURE_ERROR));
    }

    protected WorkspaceManager getWorkspaceManager() throws KException {
    	return this.kengine.getWorkspaceManager();
    }

    protected boolean removeViewDefinition(String viewDefinitionName) throws Exception {
        return getWorkspaceManager().deleteViewDefinition(viewDefinitionName);
    }

    protected boolean removeViewDefinition( final ViewDefinition viewDefinition ) throws Exception {
        return removeViewDefinition(viewDefinition.getName( ) );
    }

    protected ViewDefinition[] getViewDefinitions(final String namePrefix ) throws Exception {
    	return getWorkspaceManager().getViewDefinitions( namePrefix );
    }

    protected Object createErrorResponseEntity(List<MediaType> acceptableMediaTypes, String errorMessage) {
        Object responseEntity = null;

        if (acceptableMediaTypes.contains(MediaType.APPLICATION_JSON_TYPE)) {
            Gson gson = new Gson();
            responseEntity = gson.toJson(new ErrorResponse(errorMessage));
        } else if (acceptableMediaTypes.contains(MediaType.APPLICATION_XML_TYPE)) {
        	return "<error>"+errorMessage+"</error>";
        } else
            responseEntity = errorMessage;

        return responseEntity;
    }

    protected Response createErrorResponse(Status returnCode, List<MediaType> mediaTypes, Throwable ex,
                                           RelationalMessages.Error errorType, Object... errorMsgInputs) {
        String errorMsg = ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : ex.getClass().getSimpleName();

        //
        // Allow for splitting the message into actual message & stack trace by
        // dividing them with -----
        //
        StringBuffer buf = new StringBuffer(errorMsg).append(NEW_LINE).append("-----").append(NEW_LINE);
        String stackTrace = StringUtils.exceptionToString(ex);
        buf.append(stackTrace).append(NEW_LINE);

        String resultMsg = null;
        if (errorMsgInputs == null || errorMsgInputs.length == 0)
            resultMsg = RelationalMessages.getString(errorType, buf.toString());
        else
            resultMsg = RelationalMessages.getString(errorType, errorMsgInputs, buf.toString());

        return createErrorResponse(returnCode, mediaTypes, resultMsg);
    }

    protected Response createErrorResponse(Status returnCode, List<MediaType> mediaTypes,
                                           RelationalMessages.Error errorType, Object... errorMsgInputs) {
        String resultMsg = null;
        if (errorMsgInputs == null || errorMsgInputs.length == 0)
            resultMsg = RelationalMessages.getString(errorType);
        else
            resultMsg = RelationalMessages.getString(errorType, errorMsgInputs);

        return createErrorResponse(returnCode, mediaTypes, resultMsg);
    }
    
    protected Response createErrorResponse(List<MediaType> mediaTypes, Throwable ex,
            RelationalMessages.Error errorType, Object... errorMsgInputs) {
		if (ex != null) {
			LOGGER.error(errorType.toString(), ex);
		}
		
		return createErrorResponse(Status.INTERNAL_SERVER_ERROR, mediaTypes, ex, errorType, errorMsgInputs);
	}

    protected Response createErrorResponseWithForbidden(List<MediaType> mediaTypes, Throwable ex,
                                                        RelationalMessages.Error errorType, Object... errorMsgInputs) {
        if (ex != null) {
        	LOGGER.error(errorType.toString(), ex);
        }
    	
    	return createErrorResponse(Status.FORBIDDEN, mediaTypes, ex, errorType, errorMsgInputs);
    }

    protected Response createErrorResponseWithForbidden(List<MediaType> mediaTypes,
                                                        RelationalMessages.Error errorType, Object... errorMsgInputs) {
        return createErrorResponse(Status.FORBIDDEN, mediaTypes, errorType, errorMsgInputs);
    }

    protected Response createErrorResponse(Status returnCode, List<MediaType> mediaTypes, String resultMsg) {
        Object responseEntity = createErrorResponseEntity(mediaTypes, resultMsg);

        //
        // Log the error in the komodo log for future reference
        //
        KLog.getLogger().error(Messages.getString(Messages.Error.RESPONSE_ERROR, returnCode, resultMsg));

        return Response.status(returnCode).entity(responseEntity).build();
    }

    protected ResponseBuilder notAcceptableMediaTypesBuilder() {
        List<Variant> variants = VariantListBuilder.newInstance()
                                                                   .mediaTypes(MediaType.APPLICATION_XML_TYPE,
                                                                                       MediaType.APPLICATION_JSON_TYPE)
                                                                   .build();

        return Response.notAcceptable(variants);
    }

    protected boolean isAcceptable(List<MediaType> acceptableTypes, MediaType candidate) {
        if (acceptableTypes == null || acceptableTypes.isEmpty())
            return false;

        if (candidate == null)
            return false;

        for (MediaType acceptableType : acceptableTypes) {
            if (candidate.isCompatible(acceptableType))
                return true;
        }

        return false;
    }

    protected Response toResponse(List<MediaType> acceptableMediaTypes, final KRestEntity entity) throws Exception {
    	if (entity == null) {
            return Response.ok().build();
        }
    	
        ResponseBuilder builder = null;

        if ( entity == RestBasicEntity.NO_CONTENT ) {
            builder = Response.noContent();
        } else if ( entity instanceof ResourceNotFound ) {
            final ResourceNotFound resourceNotFound = ( ResourceNotFound )entity;

            String notFoundMsg = Messages.getString( RESOURCE_NOT_FOUND,
                                                     resourceNotFound.getResourceName(),
                                                     resourceNotFound.getOperationName() );
            Object responseEntity = createErrorResponseEntity(acceptableMediaTypes, notFoundMsg);
            builder = Response.status( Status.NOT_FOUND ).entity(responseEntity);
        } else {

            //
            // Json will always be preferred over XML if both or the wildcard are present in the header
            //
            if (isAcceptable(acceptableMediaTypes, MediaType.APPLICATION_JSON_TYPE))
                builder = Response.ok( KomodoJsonMarshaller.marshall( entity ), MediaType.APPLICATION_JSON );
            else if (isAcceptable(acceptableMediaTypes, MediaType.APPLICATION_XML_TYPE) && entity.supports(MediaType.APPLICATION_XML_TYPE))
                builder = Response.ok( entity.getXml(), MediaType.APPLICATION_XML );
            else {
                builder = notAcceptableMediaTypesBuilder();
            }
        }

        return builder.build();
    }
    
    protected <T> T runInTransaction(SecurityPrincipal principal, String txnName, boolean rollbackOnly, Callable<T> callable) throws Exception {
		UnitOfWork uow = null;

        try {
            uow = createTransaction(principal, txnName, rollbackOnly ); //$NON-NLS-1$
            T result = callable.call();
            commit(uow);
            return result;
        } catch ( final Exception e ) {
            if ( ( uow != null ) && !uow.isCompleted()) {
                uow.rollback();
            }
            if ( e instanceof KomodoRestException ) {
                throw ( KomodoRestException )e;
            }
            throw e;
        }
	}    

    
    protected void commit(UnitOfWork transaction) throws Exception {
        boolean rollbackOnly = false;
    	if (transaction.isRollbackOnly()) {
    		rollbackOnly = true;
    		transaction.rollback();
    	} else {
    		transaction.commit();
    	}

        LOGGER.debug( "commit: successfully committed '{0}', rollbackOnly = '{1}'", //$NON-NLS-1$
                transaction.getName(),
                rollbackOnly);
    }

    protected Response commit(UnitOfWork transaction, List<MediaType> acceptableMediaTypes, final KRestEntity entity) throws Exception {
        final int timeout = TIMEOUT;
        final TimeUnit unit = UNIT;

        try {
        	commit(transaction);
        } catch (TimeoutException e) {
            // callback timeout occurred
            String errorMessage = Messages.getString( COMMIT_TIMEOUT, transaction.getName(), timeout, unit );
            Object responseEntity = createErrorResponseEntity(acceptableMediaTypes, errorMessage);
            return Response.status( Status.INTERNAL_SERVER_ERROR )
                           .type( MediaType.TEXT_PLAIN )
                           .entity(responseEntity)
                           .build();
        } catch (Throwable e) {
            // callback was called because of an error condition
            Object responseEntity = createErrorResponseEntity(acceptableMediaTypes, e.getLocalizedMessage());
            return Response.status( Status.INTERNAL_SERVER_ERROR )
                            .entity(responseEntity)
                            .build();
        }

        if (entity != null) {
        	return toResponse(acceptableMediaTypes, entity);
        }

        return Response.ok().build();
    }

    protected Response commit( final UnitOfWork transaction, List<MediaType> acceptableMediaTypes,
                               final List<? extends KRestEntity> entities ) throws Exception {

        commit(transaction, acceptableMediaTypes, (KRestEntity)null);

        ResponseBuilder builder = null;

        KRestEntity entity;
        if ( entities.size() == 1 && (entity = entities.iterator().next()) instanceof ResourceNotFound ) {
            final ResourceNotFound resourceNotFound = ( ResourceNotFound )entity;

            String notFoundMessage = Messages.getString( RESOURCE_NOT_FOUND,
                                                         resourceNotFound.getResourceName(),
                                                         resourceNotFound.getOperationName() );
            Object responseEntity = createErrorResponseEntity(acceptableMediaTypes, notFoundMessage);
            builder = Response.status( Status.NOT_FOUND ).entity(responseEntity);
        } else {

            if (isAcceptable(acceptableMediaTypes, MediaType.APPLICATION_JSON_TYPE))
                builder = Response.ok( KomodoJsonMarshaller.marshallArray(entities.toArray(new KRestEntity[0]), true), MediaType.APPLICATION_JSON );
            else {
                builder = notAcceptableMediaTypesBuilder();
            }
        }

        return builder.build();
    }

    /**
     * @param user
     *        the user initiating the transaction
     * @param name
     *        the name of the transaction (cannot be empty)
     * @param rollbackOnly
     *        <code>true</code> if transaction must be rolled back
     * @param callback the callback to fire when the transaction is committed
     * @return the new transaction (never <code>null</code>)
     * @throws KException
     *         if there is an error creating the transaction
     */
    protected UnitOfWork createTransaction(final SecurityPrincipal user, final String name,
                                            final boolean rollbackOnly) throws KException {
    	final UnitOfWork result = this.kengine.createTransaction( user.getUserName(),
                                                               (getClass().getSimpleName() + COLON + name + COLON + System.currentTimeMillis()),
                                                               rollbackOnly, REPO_USER);
        LOGGER.debug( "createTransaction:created '{0}', rollbackOnly = '{1}'", result.getName(), result.isRollbackOnly() ); //$NON-NLS-1$
        return result;
    }

    protected UnitOfWork systemTx(String description, boolean rollback) throws KException {
        return createTransaction(SYSTEM_USER, description, rollback); //$NON-NLS-1$
    }

    protected DataVirtualization findDataservice(String dataserviceName) throws KException {
    	return getWorkspaceManager().findDataVirtualization(dataserviceName);
    }

    protected Response commitNoDataserviceFound(UnitOfWork uow, List<MediaType> mediaTypes, String dataserviceName) throws Exception {
        LOGGER.debug( "Dataservice '{0}' was not found", dataserviceName ); //$NON-NLS-1$
        return commit( uow, mediaTypes, new ResourceNotFound( dataserviceName, Messages.getString( GET_OPERATION_NAME ) ) );
    }

    protected Response commitNoConnectionFound(UnitOfWork uow, List<MediaType> mediaTypes, String connectionName) throws Exception {
        LOGGER.debug( "Connection '{0}' was not found", connectionName ); //$NON-NLS-1$
        return commit( uow, mediaTypes, new ResourceNotFound( connectionName, Messages.getString( GET_OPERATION_NAME ) ) );
    }

}
