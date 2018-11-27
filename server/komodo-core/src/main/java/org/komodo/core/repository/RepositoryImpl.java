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
package org.komodo.core.repository;

import static org.komodo.core.repository.Messages.Komodo.ERROR_REPO_HAS_CHANGES;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.komodo.core.KEngine;
import org.komodo.core.KomodoLexicon;
import org.komodo.core.KomodoLexicon.Environment;
import org.komodo.core.KomodoLexicon.Komodo;
import org.komodo.core.KomodoLexicon.LibraryComponent;
import org.komodo.core.KomodoLexicon.Search;
import org.komodo.core.internal.repository.search.ObjectSearcher;
import org.komodo.core.repository.validation.ValidationManagerImpl;
import org.komodo.spi.KClient;
import org.komodo.spi.KEvent;
import org.komodo.spi.KException;
import org.komodo.spi.constants.StringConstants;
import org.komodo.spi.metadata.MetadataInstance;
import org.komodo.spi.query.KQueryManager;
import org.komodo.spi.repository.Artifact;
import org.komodo.spi.repository.ArtifactDescriptor;
import org.komodo.spi.repository.Descriptor;
import org.komodo.spi.repository.KObjectFactory;
import org.komodo.spi.repository.KPropertyFactory;
import org.komodo.spi.repository.UnitOfWorkDelegate;
import org.komodo.spi.repository.KomodoObject;
import org.komodo.spi.repository.Property;
import org.komodo.spi.repository.Repository;
import org.komodo.spi.repository.RepositoryClientEvent;
import org.komodo.spi.repository.RepositoryObserver;
import org.komodo.spi.repository.ValidationManager;
import org.komodo.utils.ArgCheck;
import org.komodo.utils.KLog;

/**
 * A {@link Repository} implementation.
 */
public abstract class RepositoryImpl implements Repository, StringConstants {

    private class ArtifactDescriptorImpl implements ArtifactDescriptor {

        private final String description;
        private final String path;
        private final boolean readOnly;
        private final Repository repository;
        private final String type;
        private final String version;

        ArtifactDescriptorImpl( final String artifactType,
                                final String artifactDescription,
                                final String artifactPath,
                                final Repository artifactRepository,
                                final String artifactVersion,
                                final boolean artifactReadOnly ) {
            this.type = artifactType;
            this.description = artifactDescription;
            this.path = artifactPath;
            this.repository = artifactRepository;
            this.version = artifactVersion;
            this.readOnly = true;
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.spi.repository.ArtifactDescriptor#getArtifactType()
         */
        @Override
        public String getArtifactType() {
            return this.type;
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.spi.repository.ArtifactDescriptor#getDescription()
         */
        @Override
        public String getDescription() {
            return this.description;
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.spi.repository.ArtifactDescriptor#getPath()
         */
        @Override
        public String getPath() {
            return this.path;
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.spi.repository.ArtifactDescriptor#getRepository()
         */
        @Override
        public Repository getRepository() {
            return this.repository;
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.spi.repository.ArtifactDescriptor#getVersion()
         */
        @Override
        public String getVersion() {
            return this.version;
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.spi.repository.ArtifactDescriptor#isReadOnly()
         */
        @Override
        public boolean isReadOnly() {
            return this.readOnly;
        }

    }

    private class ArtifactImpl implements Artifact {

        private final ArtifactDescriptor descriptor;
        private final KomodoObject komodoObject;

        ArtifactImpl( final ArtifactDescriptor descriptor,
                      final KomodoObject komodoObject ) {
            this.descriptor = descriptor;
            this.komodoObject = komodoObject;
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.spi.repository.Artifact#get()
         */
        @Override
        public KomodoObject get() {
            return this.komodoObject;
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.spi.repository.Artifact#getDescriptor()
         */
        @Override
        public ArtifactDescriptor getDescriptor() {
            return this.descriptor;
        }

    }

    /**
     * A unit of work analogous to a transaction.
     */
    public static class UnitOfWorkImpl implements UnitOfWork {

        protected final UnitOfWorkListener callback;
        protected KException error;
        protected final String userName;
        protected final String name;
        protected final boolean rollbackOnly;
        protected UnitOfWorkDelegate uowDelegate;
        protected State state = State.NOT_STARTED;

        /**
         * @param userName
         *        the user who initiated the transaction
         * @param uowName
         *        the transaction name (cannot be empty)
         * @param uowDelegate
         *        the repository session this unit of work will be using (cannot be <code>null</code>)
         * @param uowRollbackOnly
         *        <code>true</code> if only a rollback can be done (i.e., commit not allowed)
         * @param listener
         *        the callback (can be <code>null</code>)
         */
        public UnitOfWorkImpl(final String userName,
                               final String uowName,
                               final UnitOfWorkDelegate uowDelegate,
                               final boolean uowRollbackOnly,
                               final UnitOfWorkListener listener) {
            ArgCheck.isNotEmpty(userName, "userName"); //$NON-NLS-1$
            ArgCheck.isNotEmpty(uowName, "uowName"); //$NON-NLS-1$
            ArgCheck.isNotNull(uowDelegate, "uowSession"); //$NON-NLS-1$

            this.userName = userName;
            this.name = uowName;
            this.uowDelegate = uowDelegate;
            this.rollbackOnly = uowRollbackOnly;
            this.callback = listener;
        }

        @Override
        public UnitOfWorkDelegate getDelegate() {
            return uowDelegate;
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.spi.repository.Repository.UnitOfWork#commit()
         */
        @Override
        public void commit() {
            if (this.state != State.NOT_STARTED) {
                this.error = new KException( Messages.getString( Messages.Komodo.ERROR_TRANSACTION_FINISHED,
                                                                 this.name,
                                                                 this.state ) );
                this.state = State.ERROR;
            } else {
                LOGGER.debug( "commit transaction {0}", getName() ); //$NON-NLS-1$

                if (this.rollbackOnly) {
                    rollback();
                } else {
                    this.state = State.RUNNING;

                    try {
                        if (this.uowDelegate == null) {
                            this.state = State.ERROR;
                            this.error = new KException( Messages.getString( Messages.Komodo.ERROR_SESSION_IS_CLOSED, this.name ) );
                        } else {
                            this.uowDelegate.save();

                            this.state = State.COMMITTED;
                            LOGGER.debug( "transaction {0} saved", getName() ); //$NON-NLS-1$

                            if (this.callback != null) {
                                this.callback.respond( this );
                            }
                        }
                    } catch (final Exception e) {
                        this.state = State.ERROR;
                        this.error = new KException( e );

                        if (this.callback == null) {
                            LOGGER.error( Messages.getString( Messages.Komodo.ERROR_TRYING_TO_COMMIT, getName(), e ) );
                            rollback();
                            this.state = State.ERROR;
                        } else {
                            this.callback.errorOccurred( e );
                        }
                    } finally {
                        if (uowDelegate.isLive()) this.uowDelegate.complete();
                        this.uowDelegate = null;
                    }
                }
            }
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.spi.repository.Repository.UnitOfWork#getCallback()
         */
        @Override
        public UnitOfWorkListener getCallback() {
            return this.callback;
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.spi.repository.Repository.UnitOfWork#getError()
         */
        @Override
        public KException getError() {
            return this.error;
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.spi.repository.Repository.UnitOfWork#getUserName()
         */
        @Override
        public String getUserName() {
            return userName;
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.spi.repository.Repository.UnitOfWork#getName()
         */
        @Override
        public String getName() {
            return this.name;
        }

        /**
         * @return the session used during the transaction (never <code>null</code>)
         */
        UnitOfWorkDelegate getSession() {
            return this.uowDelegate;
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.spi.repository.Repository.UnitOfWork#getState()
         */
        @Override
        public State getState() {
            return this.state;
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.spi.repository.Repository.UnitOfWork#hasChanges()
         */
        @Override
        public boolean hasChanges() throws KException {
            if ( this.state == State.NOT_STARTED ) {
                try {
                    return ( ( this.uowDelegate != null ) && this.uowDelegate.isLive() && this.uowDelegate.hasPendingChanges() );
                } catch ( final Exception e ) {
                    throw new KException( Messages.getString( ERROR_REPO_HAS_CHANGES, this.name ), e );
                }
            }

            return false;
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.spi.repository.Repository.UnitOfWork#isRollbackOnly()
         */
        @Override
        public boolean isRollbackOnly() {
            return this.rollbackOnly;
        }

        /**
         * {@inheritDoc}
         *
         * @see org.komodo.spi.repository.Repository.UnitOfWork#rollback()
         */
        @Override
        public void rollback() {
            if (this.state != State.NOT_STARTED) {
                this.error = new KException( Messages.getString( Messages.Komodo.ERROR_TRANSACTION_FINISHED,
                                                                 this.name,
                                                                 this.state ) );
                this.state = State.ERROR;
            } else {
                this.state = State.RUNNING;
                LOGGER.debug( "rollback transaction {0}", getName() ); //$NON-NLS-1$

                try {
                    if (this.uowDelegate == null) {
                        this.state = State.ERROR;
                        this.error = new KException( Messages.getString( Messages.Komodo.ERROR_SESSION_IS_CLOSED, this.name ) );
                    } else {
                        this.uowDelegate.refresh( false );
                        this.state = State.ROLLED_BACK;
                        LOGGER.debug( "transaction {0} rolled back", getName() ); //$NON-NLS-1$

                        if (this.callback != null) {
                            this.callback.respond( null );
                        }
                    }
                } catch (final Exception e) {
                    this.state = State.ERROR;
                    this.error = new KException( e );

                    if (this.callback == null) {
                        LOGGER.error( Messages.getString( Messages.Komodo.ERROR_TRYING_TO_ROLLBACK, e, getName() ) );
                    } else {
                        this.callback.errorOccurred( e );
                    }
                } finally {
                    if (uowDelegate.isLive()) this.uowDelegate.complete();
                    this.uowDelegate = null;
                }
            }
        }
    }

    protected static final KLog LOGGER = KLog.getLogger();

    /**
     * The root path of the repository.
     */
    public static final String REPO_ROOT = FORWARD_SLASH;

    /**
     * The root path of the Komodo repository.
     */
    public static final String KOMODO_ROOT = (REPO_ROOT + Komodo.NODE_TYPE);

    /**
     * The root path of the Komodo repository environment area.
     */
    public static final String ENV_ROOT = ( KOMODO_ROOT + FORWARD_SLASH + Komodo.ENVIRONMENT );

    /**
     * The root path of the Komodo repository library area.
     */
    public static final String LIBRARY_ROOT = (KOMODO_ROOT + FORWARD_SLASH + Komodo.LIBRARY);

    /**
     * The root path of the Komodo repository environment validation rules area
     */
    public static final String VALIDATION_ROOT = ENV_ROOT + FORWARD_SLASH + Environment.VALIDATION;

    /**
     * The root path of the Komodo repository environment profiles area
     */
    public static final String PROFILES_ROOT = ENV_ROOT + FORWARD_SLASH + Environment.PROFILES;

    /**
     * The root path of the Komodo repository workspace area.
     * This should remain private as clients should use
     * {@link #komodoWorkspacePath(org.komodo.spi.repository.Repository.UnitOfWork)}
     * in preference, allowing for the creation of the user home directory
     */
    private static final String WORKSPACE_ROOT = (KOMODO_ROOT + FORWARD_SLASH + Komodo.WORKSPACE);

    /**
     * The root path of the Komodo repository workspace searches area.
     * This remains a sibling of the user home directories so that searches can be shared by users.
     */
    public static final String SEARCHES_ROOT = WORKSPACE_ROOT + FORWARD_SLASH + Search.GROUP_NODE;

    /**
     * @param transaction
     *       the transaction (cannot be <code>null</code> or have a state that is not
    *        {@link org.komodo.spi.repository.Repository.UnitOfWork.State#NOT_STARTED})
    *
     * @return true if the given transaction is a system transaction
     */
    public static boolean isSystemTx(UnitOfWork transaction) {
        ArgCheck.isNotNull(transaction, "Transaction cannot be null");
        ArgCheck.isNotNull(transaction.getUserName(), "Transaction must contain a user name");

        //
        // Transactions should always have a user name but just in case one sneaked through
        //
        return SYSTEM_USER.equals(transaction.getUserName());
    }

    /**
     * The komodo user's workspace in the repository, ie. /tko:komodo/tko:workspace/${user}
     * where ${user} is the user owning the given transaction
     *
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not
     *        {@link org.komodo.spi.repository.Repository.UnitOfWork.State#NOT_STARTED})
     *
     * @return the workspace path for the user who owns the transaction
     */
    public static String komodoWorkspacePath(final UnitOfWork uow) {
        if(uow == null)
            return WORKSPACE_ROOT;

        String userName = uow.getUserName();
        if (userName == null || isSystemTx(uow))
            return WORKSPACE_ROOT;

        return WORKSPACE_ROOT + FORWARD_SLASH + userName;
    }

    /**
     * The komodo user's profile in the repository, ie. /tko:komodo/tko:environment/tko:profiles/${user}
     * where ${user} is the user owning the given transaction
     *
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not
     *        {@link org.komodo.spi.repository.Repository.UnitOfWork.State#NOT_STARTED})
     *
     * @return the profile path for the user who owns the transaction
     */
    public static String komodoProfilePath(final UnitOfWork uow) {
        if(uow == null)
            return PROFILES_ROOT;

        String userName = uow.getUserName();
        if (userName == null || isSystemTx(uow))
            return PROFILES_ROOT;

        return PROFILES_ROOT + FORWARD_SLASH + userName;
    }

    /**
     * @param path the path to test
     *
     * @return true if the path is a reserved path
     */
    public static boolean isReservedPath(String path) {
        if (path == null)
            return false;

        //
        // Ensure path has no trailing slash
        //
        if (path.endsWith(FORWARD_SLASH))
            path = path.substring(0, path.length() - 1);

        if (KOMODO_ROOT.equals(path) || LIBRARY_ROOT.equals(path) || ENV_ROOT.equals(path)
                || SEARCHES_ROOT.equals(path) || VALIDATION_ROOT.equals(path) || PROFILES_ROOT.equals(path)
                || WORKSPACE_ROOT.equals(path))
            return true;

        path = path.replace(WORKSPACE_ROOT + FORWARD_SLASH, EMPTY_STRING);
        if (! path.contains(FORWARD_SLASH))
            // If no slash then this is a home directory
            return true;

        return false;
    }

    /**
     * @param transaction
     * @return the group of reserved paths including the home directory for the owner of the transaction
     */
    public static String[] getReservedPaths(UnitOfWork transaction) {
        List<String> paths = new ArrayList<>();

        paths.add(KOMODO_ROOT);
        paths.add(LIBRARY_ROOT);
        paths.add(ENV_ROOT);
        paths.add(SEARCHES_ROOT);
        paths.add(VALIDATION_ROOT);
        paths.add(PROFILES_ROOT);
        paths.add(WORKSPACE_ROOT);
        paths.add(komodoWorkspacePath(transaction));

        return paths.toArray(new String[0]);
    }

    private final Set< KClient > clients = new HashSet< >();
    private final Id id;
    private final Set< RepositoryObserver > observers = new HashSet< >();
    private final Type type;
    private ValidationManager validationMgr;
    protected KEngine kEngine;

    /**
     * @param type
     *        the repository type (cannot be <code>null</code>)
     * @param id
     *        the repository identifier (cannot be <code>null</code>)
     */
    public RepositoryImpl( final Type type,
                           final Id id) {
        ArgCheck.isNotNull(type, "type"); //$NON-NLS-1$
        ArgCheck.isNotNull(id, "id"); //$NON-NLS-1$

        this.type = type;
        this.id = id;
    }

    @Override
    public abstract KObjectFactory getObjectFactory();

    @Override
    public abstract KPropertyFactory getPropertyFactory();

    protected abstract KQueryManager getQueryManager();

    public void registerKEngine(KEngine engine) {
    	this.kEngine = engine;
    }
    
    public KEngine getKEngine() {
    	return this.kEngine;
    }    
    
    @Override
    public MetadataInstance getMetadataInstance() throws KException {
    	if (this.kEngine != null) {
    		return this.kEngine.getMetadataInstance();
    	}
    	return null;
    }
    
    /**
     * Called prior to each external API method. Prepares the object at the given nodePath
     * to be acted upon by the transaction, including testing if such operation violates any
     * security constraints and ensuring that a user-space is available in the workspace.
     *
     * @param transaction
     *        the transaction (cannot be <code>null</code>)
     * @param nodePath the path to a repository node
     * @param operationType the type of the operation to be performed
     * @throws KException if an error occurs
     */
    protected void provision(UnitOfWork transaction, String nodePath, OperationType operationType) throws KException {
        String userWksp = komodoWorkspacePath(transaction);
        String userProfile = komodoProfilePath(transaction);

        if (isSystemTx(transaction))
            return; // System can do what it wishes

        /*
         * Ensures that a user workspace is always available so truly dynamic
         * and guarantees that the user space is available to the current tx.
         */
        komodoWorkspace(transaction);

        switch (operationType) {
            case READ_OPERATION:
                if (isReservedPath(nodePath)) {
                  /*
                   * Reserved paths can be read but not written to
                   * allowing for absolute paths to be broken down into segments
                   * and each segment read, eg. DefaultLabelProvider.getPath();
                   *
                   * However, this does not mean that these paths will return anything
                   * useful, eg. property descriptors, as individual API methods may stop
                   * their reading.
                   */
                  return;
                }

                if (nodePath.startsWith(VALIDATION_ROOT))
                    return; // Read the group of validation rules

                if (nodePath.startsWith(LIBRARY_ROOT))
                    return; // Read the contents of the library

                if(nodePath.startsWith(userWksp))
                    return; // Read the contents of the user's workspace

                if (nodePath.startsWith(userProfile)) {
                    return; // Read the contents of the user's profile
                }

                if (nodePath.startsWith(SEARCHES_ROOT))
                    return; // Read the contents of the searches

                throw new KException(Messages.getString(
                                                        Messages.Komodo.READ_NOT_ALLOWED,
                                                        nodePath, transaction.getUserName() ));

            case CHILD_OPERATION:
                if (nodePath.startsWith(VALIDATION_ROOT))
                    return; // Add/Remove validation rules from both the validation root and its children

                // Only system can add/remove library objects through the check-in/out framework

                if (nodePath.startsWith(userWksp))
                    return; // Add/Remove children in the user's workspace

                if (nodePath.startsWith(userProfile))
                    return; // Add/Remove children in the user's profile

                if (nodePath.startsWith(SEARCHES_ROOT))
                    return; // Add/Remove searches from the searches location

                throw new KException(Messages.getString(
                                                        Messages.Komodo.ADD_REMOVE_CHILD_NOT_ALLOWED,
                                                        nodePath, transaction.getUserName() ));

            case MODIFY_OPERATION:
                if (nodePath.startsWith(VALIDATION_ROOT) && ! VALIDATION_ROOT.equals(nodePath))
                    return; // Can modify validation rules

                if(nodePath.startsWith(userWksp) && ! userWksp.equals(nodePath))
                    return; // Can modify contents of workspace

                if(nodePath.startsWith(userProfile) && ! userProfile.equals(nodePath))
                    return; // Can modify contents of user profile

                if (nodePath.startsWith(SEARCHES_ROOT))
                    return; // Can modify searches in the search location

                throw new KException(Messages.getString(
                                                             Messages.Komodo.SET_PROPERTY_NOT_ALLOWED,
                                                             nodePath, transaction.getUserName() ));

            case REMOVE_OPERATION:
                if (nodePath.startsWith(VALIDATION_ROOT) && ! VALIDATION_ROOT.equals(nodePath))
                    return; // Can remove validation rules

                if(nodePath.startsWith(userWksp) && ! userWksp.equals(nodePath))
                    return; // Can remove contents of workspace

                if(nodePath.startsWith(userProfile) && ! userProfile.equals(nodePath))
                    return; // Can remove contents of user profile

                if (nodePath.startsWith(SEARCHES_ROOT))
                    return; // Can remove searches in the search location

                throw new KException(Messages.getString(
                                                        Messages.Komodo.REMOVE_NOT_ALLOWED,
                                                        nodePath, transaction.getUserName() ));
        }
    }

    @Override
    public void provision(UnitOfWork transaction, KomodoObject object, OperationType operationType) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == org.komodo.spi.repository.Repository.UnitOfWork.State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull(object, "object not found");

        String nodePath = object.getAbsolutePath();
        provision(transaction, nodePath, operationType);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.repository.Repository#add(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public KomodoObject add( final UnitOfWork transaction,
                             final String parentPath,
                             final String name,
                             final String primaryType ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == org.komodo.spi.repository.Repository.UnitOfWork.State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty(name, "name"); //$NON-NLS-1$

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("add: transaction = {0}, parentPath = {1}, name = {2}", //$NON-NLS-1$
                         transaction.getName(),
                         parentPath,
                         name);
        }

        final String workspacePath = getAbsoluteWorkspacePath(transaction, parentPath, OperationType.CHILD_OPERATION);

        try {
            String komodoWorkspacePath = komodoWorkspacePath(transaction);
            if (komodoWorkspacePath.equals(workspacePath) && ! getObjectFactory().hasNode(transaction, komodoWorkspacePath)) {
                komodoWorkspace(transaction);
            }

            KomodoObject parent = getObjectFactory().getNode(transaction, this, workspacePath);
            KomodoObject result = parent.addChild(transaction, name, primaryType);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug( "RepositoryImpl.add: transaction = {0}, node name = {1}", //$NON-NLS-1$
                              transaction.getName(),
                              name );
            }

            return result;
        } catch (final Exception e) {
            if (e instanceof KException) {
                throw (KException)e;
            }

            throw new KException(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.repository.Repository#addClient(org.komodo.spi.KClient)
     */
    @Override
    public void addClient( final KClient client ) {
        ArgCheck.isNotNull(client, "client"); //$NON-NLS-1$
        this.clients.add(client);
        addObserver(client);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.repository.Repository#addObserver(org.komodo.spi.repository.RepositoryObserver)
     */
    @Override
    public void addObserver( final RepositoryObserver observer ) {
        ArgCheck.isNotNull(observer, "observer"); //$NON-NLS-1$
        this.observers.add(observer);
    }

    private void copy( final UnitOfWork uow,
                       final KomodoObject src,
                       final KomodoObject target) throws Exception {
        copyProperties(uow, src, target);

        for (final KomodoObject child : src.getChildren(uow)) {
            final KomodoObject childNode = target.addChild(uow, child.getName(uow), child.getPrimaryType(uow).getName());
            copy(uow, child, childNode);
        }
    }

    private void copyProperties( final UnitOfWork uow,
                                 final KomodoObject src,
                                 final KomodoObject target) throws Exception {
        for (final String name : src.getPropertyNames(uow)) {
            final Property prop = src.getProperty(uow, name);

            if (prop.getDescriptor(uow).isMultiple()) {
                Object[] values = prop.getValues(uow);
                target.setProperty(uow, name, values);
            } else {
                Object value = prop.getValue(uow);
                target.setProperty(uow, name, value);
            }
        }
    }

    private KomodoObject create( final UnitOfWork transaction,
                                 final String absolutePath, final String nodeType ) throws KException {
        try {
            return getObjectFactory().create(transaction, this, absolutePath, nodeType);
        } catch (final Exception e) {
            if (e instanceof KException) {
                throw (KException)e;
            }

            throw new KException(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.repository.Repository#query(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String)
     */
    @Override
    public List< KomodoObject > query( final UnitOfWork transaction,
                                       final String queryStatement ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == org.komodo.spi.repository.Repository.UnitOfWork.State.NOT_STARTED ),
        "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty(queryStatement, "Query statement cannot be empty"); //$NON-NLS-1$

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("find: transaction = {0}, query = {1}", //$NON-NLS-1$
                         transaction.getName(),
                         queryStatement);
        }

        List<KomodoObject> results;
        try {
            KQueryManager queryMgr = getQueryManager();
            results = queryMgr.execute(transaction, this, queryStatement);
            return results;
        } catch (final Exception e) {
            if (e instanceof KException) {
                throw (KException)e;
            }

            throw new KException(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.repository.Repository#searchByKeyword(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String, java.lang.String, org.komodo.spi.repository.Repository.KeywordCriteria, java.lang.String[])
     */
    @Override
    public List< KomodoObject > searchByKeyword( final UnitOfWork transaction,
                                                 final String type,
                                                 final String property,
                                                 KeywordCriteria keywordCriteria,
                                                 final String... keywords ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == org.komodo.spi.repository.Repository.UnitOfWork.State.NOT_STARTED ),
        "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty(keywords, "Search by keyword requires keywords"); //$NON-NLS-1$

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("find: transaction = {0}, keywords = {1}", //$NON-NLS-1$
                         transaction.getName(),
                         keywords);
        }

        if (keywordCriteria == null)
            keywordCriteria = KeywordCriteria.getDefault();

        ObjectSearcher searcher = new ObjectSearcher(this);
        String typeAlias = "k1"; // where clauses need an alias so assign one to the type //$NON-NLS-1$
        searcher.setFromType(type, typeAlias);
        searcher.addWhereContainsClause(null, typeAlias, property, keywordCriteria, keywords);
        List<KomodoObject> searchObjects = searcher.searchObjects(transaction);

        return searchObjects;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.repository.Repository#searchByType(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String[])
     */
    @Override
    public List< KomodoObject > searchByType( final UnitOfWork transaction,
                                              final String... types ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == org.komodo.spi.repository.Repository.UnitOfWork.State.NOT_STARTED ),
        "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty(types, "types"); //$NON-NLS-1$

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("find: transaction = {0}, types = {1}", //$NON-NLS-1$
                         transaction.getName(),
                         types);
        }

        ObjectSearcher searcher = new ObjectSearcher(this);
        for (String type : types) {
            searcher.setFromType(type);
        }

        List<KomodoObject> searchObjects = searcher.searchObjects(transaction);
        return searchObjects;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.repository.Repository#searchByPath(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String)
     */
    @Override
    public List< KomodoObject > searchByPath( final UnitOfWork transaction,
                                              final String path ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == org.komodo.spi.repository.Repository.UnitOfWork.State.NOT_STARTED ),
        "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty(path, "path"); //$NON-NLS-1$

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("find: transaction = {0}, path = {1}", //$NON-NLS-1$
                         transaction.getName(),
                         path);
        }

        ObjectSearcher searcher = new ObjectSearcher(this);
        String typeAlias = "k1"; // where clauses need an alias so assign one to the type //$NON-NLS-1$
        searcher.setFromType("nt:base", typeAlias);
        searcher.addWherePathClause(null, typeAlias, path);

        List<KomodoObject> searchObjects = searcher.searchObjects(transaction);
        return searchObjects;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.repository.Repository#getFromWorkspace(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String)
     */
    @Override
    public KomodoObject getFromWorkspace( final UnitOfWork transaction,
                                          final String path ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == org.komodo.spi.repository.Repository.UnitOfWork.State.NOT_STARTED ),
        "transaction state is not NOT_STARTED" ); //$NON-NLS-1$

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("get: transaction = {0}, path = {1}", transaction.getName(), path); //$NON-NLS-1$
        }

        KomodoObject result = null;
        final String workspacePath = getAbsoluteWorkspacePath(transaction, path, OperationType.READ_OPERATION);

        try {
            if (getObjectFactory().hasNode(transaction, workspacePath)) {
                KomodoObject node = getObjectFactory().getNode(transaction, this, workspacePath);
                result = new ObjectImpl(this, workspacePath, node.getIndex());
            } else if (komodoWorkspacePath(transaction).equals(workspacePath)) {
                result = komodoWorkspace(transaction);
            }

            return result;
        } catch (final Exception e) {
            if (e instanceof KException) {
                throw (KException)e;
            }

            throw new KException(e);
        }
    }

    private String getAbsoluteLibraryPath( final String path ) {
        // return root if empty
        if ((path == null) || path.trim().isEmpty()) {
            return LIBRARY_ROOT;
        }

        String nodePath = path.trim();

        if (!nodePath.startsWith(LIBRARY_ROOT)) {
            if (REPO_ROOT.equals(path)) {
                return LIBRARY_ROOT;
            }

            if (nodePath.charAt(0) == FORWARD_SLASH.charAt(0)) {
                nodePath = LIBRARY_ROOT + FORWARD_SLASH + nodePath.substring(1); // remove leading slash
            } else {
                nodePath = LIBRARY_ROOT + FORWARD_SLASH + nodePath;
            }
        }

        return nodePath;
    }

    private String getAbsoluteWorkspacePath(final UnitOfWork transaction, final String path,
                                                                                    OperationType operationType ) throws KException {
        // return root if empty
        String userWksp = komodoWorkspacePath(transaction);
        if ((path == null) || path.trim().isEmpty()) {
            return userWksp;
        }

        String nodePath = path.trim();

        // return workspace root if path is forward slash
        if (REPO_ROOT.equals(nodePath)) {
            return userWksp;
        }

        // if path does not start with workspace root assume a relative path so insert workspace root
        if (!nodePath.startsWith(WORKSPACE_ROOT)) {
            if (nodePath.charAt(0) == FORWARD_SLASH.charAt(0)) {
                nodePath = userWksp + FORWARD_SLASH + nodePath.substring(1); // remove leading slash
            } else {
                nodePath = userWksp + FORWARD_SLASH + nodePath;
            }
        }

        provision(transaction, nodePath, operationType);

        return nodePath;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.repository.Repository#getId()
     */
    @Override
    public Id getId() {
        return this.id;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.repository.Repository#getType()
     */
    @Override
    public Type getType() {
        return this.type;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.repository.Repository#getUsingId(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String)
     */
    @Override
    public KomodoObject getUsingId( final UnitOfWork transaction,
                                    final String jcrUuid ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == org.komodo.spi.repository.Repository.UnitOfWork.State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty(jcrUuid, "jcrUuid"); //$NON-NLS-1$

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("getUsingId: transaction = {0}, uuid = {1}", //$NON-NLS-1$
                         transaction.getName(),
                         jcrUuid);
        }

        try {
            KomodoObject node = getObjectFactory().getNodeById(transaction, this, jcrUuid);
            if (node == null) {
                return null;
            }

            return node;
        } catch (final Exception e) {
            if (e instanceof KException) {
                throw (KException)e;
            }

            throw new KException(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.repository.Repository#getValidationManager()
     */
    @Override
    public ValidationManager getValidationManager() throws KException {
        if ( this.validationMgr == null ) {
            // the ValidationManager loads validation rules when it is constructed so we need a transaction to save those rules
            final SynchronousCallback callback = new SynchronousCallback();
            final UnitOfWork transaction = createTransaction(SYSTEM_USER, "getValidationManager", false, callback ); //$NON-NLS-1$
            this.validationMgr = new ValidationManagerImpl( transaction, this );
            transaction.commit();

            try {
                // wait for transaction to commit before returning
                if ( !callback.await( 30, TimeUnit.SECONDS ) ) {
                    throw new KException( Messages.getString( Messages.Komodo.ERROR_CONSTRUCTING_VALIDATION_MANAGER ) );
                }
            } catch ( final Exception e ) {
                if ( !( e instanceof KException ) ) {
                    throw new KException( e );
                }

                throw ( KException )e;
            }
        }

        return this.validationMgr;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.repository.Repository#notify(org.komodo.spi.repository.RepositoryClientEvent)
     */
    @Override
    public void notify( final RepositoryClientEvent event ) {
        // nothing to do
    }

    protected void notifyObservers(KEvent<?> event) {
        final Set<RepositoryObserver> copy = new HashSet<>(this.observers);

        for (final RepositoryObserver observer : copy) {
            try {
                // Ensure all observers are informed even if one throws an exception
                observer.eventOccurred(event);
            } catch (final Exception ex) {
                observer.errorOccurred(ex);
            }
        }
    }

    protected void errorObservers(Throwable e) {
        final Set<RepositoryObserver> copy = new HashSet<>(this.observers);

        for (final RepositoryObserver observer : copy) {
            try {
                // Ensure all observers are informed even if one throws an exception
                observer.errorOccurred(e);
            } catch (final Exception ex) {
                this.kEngine.getErrorHandler().error(Messages.getString(Messages.LocalRepository.General_Exception), ex);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.repository.Repository#publish(org.komodo.spi.repository.Repository.UnitOfWork, boolean,
     *      org.komodo.spi.repository.ArtifactDescriptor, org.komodo.spi.repository.KomodoObject)
     */
    @Override
    public void publish( final UnitOfWork transaction,
                         final boolean overwrite,
                         final ArtifactDescriptor descriptor,
                         final KomodoObject komodoObject ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == org.komodo.spi.repository.Repository.UnitOfWork.State.NOT_STARTED ),
        "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull(descriptor, "descriptor"); //$NON-NLS-1$
        ArgCheck.isNotNull(komodoObject, "komodoObject"); //$NON-NLS-1$

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("publish: overwrite = {0}, name = {1}, library path = {2}, transaction = {3}", //$NON-NLS-1$
                         overwrite,
                         komodoObject,
                         descriptor.getPath(),
                         transaction.getName());
        }

        try {
            final boolean exists = getObjectFactory().hasNode(transaction, descriptor.getPath());
            KomodoObject node;

            if (exists) {
                if (!overwrite) {
                    throw new KException(Messages.getString(Messages.Komodo.ARTIFACT_EXISTS_ERROR, descriptor.getPath()));
                }

                node = getObjectFactory().getNode(transaction, this, descriptor.getPath());
                node.remove(transaction);
            }

            node = getObjectFactory().create(transaction, this, descriptor.getPath(), descriptor.getArtifactType());
            node.addDescriptor(transaction, LibraryComponent.MIXIN_TYPE);

            node.setProperty(transaction, LibraryComponent.DESCRIPTION, descriptor.getDescription());
            node.setPrimaryType(transaction, descriptor.getArtifactType());

            // TODO not sure how version works??

            // copy node
            copy(transaction, komodoObject, node);
        } catch (final Exception e) {
            if (e instanceof KException) {
                throw (KException)e;
            }

            throw new KException(Messages.getString(Messages.Komodo.ERROR_ADDING_ARTIFACT, komodoObject, descriptor.getPath()), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.repository.Repository#remove(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String[])
     */
    @Override
    public void remove( final UnitOfWork transaction,
                        final String... paths ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == org.komodo.spi.repository.Repository.UnitOfWork.State.NOT_STARTED ),
        "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( paths, "paths" ); //$NON-NLS-1$

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("remove: paths = {0}, transaction = {1}", //$NON-NLS-1$
                         Arrays.asList(paths),
                         transaction.getName());
        }

        // first make sure all exist
        for ( final String path : paths ) {
            String absPath = null;

            try {
                ArgCheck.isNotNull( path, "path" ); //$NON-NLS-1$
                absPath = getAbsoluteWorkspacePath(transaction, path, OperationType.READ_OPERATION);

                if ( !getObjectFactory().hasNode(transaction, absPath ) ) {
                    throw new KException( Messages.getString( Messages.Komodo.UNABLE_TO_REMOVE_NON_EXISTENT_WORKSPACE_ITEM,
                                                              absPath ) );
                }
            } catch ( final Exception e ) {
                if ( e instanceof KException ) {
                    throw ( KException )e;
                }

                throw new KException( Messages.getString( Messages.Komodo.REMOVE_WORKSPACE_OBJECT_ERROR, absPath ), e );
            }
        }

        // all exist so now do the deletes
        for ( final String path : paths ) {
            final String absPath = getAbsoluteWorkspacePath(transaction, path, OperationType.REMOVE_OPERATION);

            try {
                KomodoObject node = getObjectFactory().getNode(transaction, this, absPath);
                node.remove(transaction);
                LOGGER.debug( "removed workspace node at path {0} in transaction {1}", absPath, transaction.getName() ); //$NON-NLS-1$
            } catch ( final Exception e ) {
                if ( e instanceof KException ) {
                    throw ( KException )e;
                }

                throw new KException( Messages.getString( Messages.Komodo.REMOVE_WORKSPACE_OBJECT_ERROR, absPath ), e );
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.repository.Repository#removeClient(org.komodo.spi.KClient)
     */
    @Override
    public void removeClient( final KClient client ) {
        ArgCheck.isNotNull(client, "client"); //$NON-NLS-1$
        this.clients.remove(client);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.repository.Repository#removeObserver(org.komodo.spi.repository.RepositoryObserver)
     */
    @Override
    public void removeObserver( final RepositoryObserver observer ) {
        ArgCheck.isNotNull(observer, "observer"); //$NON-NLS-1$
        this.observers.remove(observer);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.repository.Repository#retrieve(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String[])
     */
    @Override
    public Artifact[] retrieve( final UnitOfWork transaction,
                                final String... artifactPaths ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == org.komodo.spi.repository.Repository.UnitOfWork.State.NOT_STARTED ),
        "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty(artifactPaths, "artifactPaths"); //$NON-NLS-1$

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("retrieve: paths = {0}, transaction = {1}", //$NON-NLS-1$
                         Arrays.asList(artifactPaths),
                         transaction.getName());
        }

        final Artifact[] artifacts = new Artifact[artifactPaths.length];
        int i = 0;

        try {
            for (final String path : artifactPaths) {
                ArgCheck.isNotNull(path, "path"); //$NON-NLS-1$
                final String absPath = getAbsoluteLibraryPath(path);

                if (getObjectFactory().hasNode(transaction, absPath)) {
                    final KomodoObject node = getObjectFactory().getNode(transaction, this, absPath);
                    final String description = node.getProperty(transaction, LibraryComponent.DESCRIPTION).getStringValue(transaction);
                    Descriptor type = getObjectFactory().getType(transaction, node);
                    final ArtifactDescriptor descriptor = new ArtifactDescriptorImpl(type.getName(),
                                                                                     description, path, this, "1", // TODO figure out version //$NON-NLS-1$
                                                                                     true); // TODO figure out how to tell if readonly
                    final KomodoObject komodoObject = new ObjectImpl(this, path, node.getIndex());
                    artifacts[i++] = new ArtifactImpl(descriptor, komodoObject);
                } else {
                    throw new KException(Messages.getString(Messages.Komodo.ARTIFACT_DOES_NOT_EXIST_ERROR, path));
                }
            }

            return artifacts;
        } catch (final Exception e) {
            if (e instanceof KException) {
                throw (KException)e;
            }

            throw new KException(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.repository.Repository#unpublish(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String[])
     */
    @Override
    public void unpublish( final UnitOfWork transaction,
                           final String... artifactPaths ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == org.komodo.spi.repository.Repository.UnitOfWork.State.NOT_STARTED ),
        "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty(artifactPaths, "artifactPaths"); //$NON-NLS-1$

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("unpublish: artifact paths = {0}, transaction = {1}", //$NON-NLS-1$
                         Arrays.asList(artifactPaths),
                         transaction.getName());
        }

        for (final String path : artifactPaths) {
            ArgCheck.isNotNull(path, "path"); //$NON-NLS-1$
            final String absPath = getAbsoluteLibraryPath(path);

            try {
                if (getObjectFactory().hasNode(transaction, absPath)) {
                    KomodoObject node = getObjectFactory().getNode(transaction, this, absPath);
                    node.remove(transaction);
                    LOGGER.debug("removed library node at path {0} in transaction {1}", absPath, transaction.getName()); //$NON-NLS-1$
                } else {
                    throw new KException(Messages.getString(Messages.Komodo.UNABLE_TO_UNPUBLISH_NON_EXISTENT_ARTIFACT, absPath));
                }
            } catch (final Exception e) {
                if (e instanceof KException) {
                    throw (KException)e;
                }

                throw new KException(Messages.getString(Messages.Komodo.UNPUBLISH_ARTIFACT_ERROR, absPath), e);
            }
        }
    }

    /**
     * The komodo root in the repository, eg. /tko:komodo
     *
     * @param uow
     *        the transaction (can be <code>null</code> if operation should be automatically committed)
     *
     * @return the root komodo object
     * @throws KException if an error occurs
     */
    protected KomodoObject komodoRoot(final UnitOfWork uow) throws KException {
        return create(uow, KOMODO_ROOT, KomodoLexicon.Komodo.NODE_TYPE);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.repository.Repository#komodoEnvironment(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public KomodoObject komodoEnvironment( final UnitOfWork transaction ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == org.komodo.spi.repository.Repository.UnitOfWork.State.NOT_STARTED ),
        "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        komodoRoot( transaction );
        return create( transaction, ENV_ROOT, KomodoLexicon.Environment.NODE_TYPE );
    }

    @Override
    public KomodoObject komodoLibrary(final UnitOfWork uow) throws KException {
        komodoRoot(uow);
        return create(uow, LIBRARY_ROOT, KomodoLexicon.Library.NODE_TYPE);
    }

    @Override
    public KomodoObject komodoWorkspace(final UnitOfWork uow) throws KException {
        komodoRoot(uow);
        create(uow, WORKSPACE_ROOT, KomodoLexicon.Workspace.NODE_TYPE);
        return create(uow, komodoWorkspacePath(uow), KomodoLexicon.Home.NODE_TYPE);
    }

    @Override
    public KomodoObject komodoProfile(final UnitOfWork uow) throws KException {
        komodoProfiles(uow);
        return create(uow, komodoProfilePath(uow), KomodoLexicon.Profile.NODE_TYPE);
    }

    @Override
    public KomodoObject komodoSearches(UnitOfWork transaction) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == org.komodo.spi.repository.Repository.UnitOfWork.State.NOT_STARTED ),
        "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        komodoWorkspace( transaction );
        return create( transaction, SEARCHES_ROOT, KomodoLexicon.Search.GROUP_NODE );
    }

    @Override
    public KomodoObject komodoProfiles(UnitOfWork transaction) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == org.komodo.spi.repository.Repository.UnitOfWork.State.NOT_STARTED ),
        "transaction state is not NOT_STARTED" ); //$NON-NLS-1$

        komodoEnvironment(transaction);
        return create(transaction, PROFILES_ROOT, KomodoLexicon.Profile.GROUP_NODE);
    }

    @Override
    public KomodoObject komodoValidationRoot(final UnitOfWork transaction) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == org.komodo.spi.repository.Repository.UnitOfWork.State.NOT_STARTED ),
        "transaction state is not NOT_STARTED" ); //$NON-NLS-1$

        komodoEnvironment(transaction);
        return create(transaction, VALIDATION_ROOT, Environment.VALIDATION);
    }
}
