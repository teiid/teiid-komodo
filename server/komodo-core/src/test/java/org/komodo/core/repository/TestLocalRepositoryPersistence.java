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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.komodo.core.AbstractLoggingTest;
import org.komodo.core.KomodoLexicon;
import org.komodo.core.repository.LocalRepository.LocalRepositoryId;
import org.komodo.spi.KClient;
import org.komodo.spi.KEvent;
import org.komodo.spi.KException;
import org.komodo.spi.constants.SystemConstants;
import org.komodo.spi.lexicon.LexiconConstants.JcrLexicon;
import org.komodo.spi.lexicon.LexiconConstants.NTLexicon;
import org.komodo.spi.lexicon.datavirt.DataVirtLexicon;
import org.komodo.spi.repository.ApplicationProperties;
import org.komodo.spi.repository.KomodoObject;
import org.komodo.spi.repository.PersistenceType;
import org.komodo.spi.repository.Property;
import org.komodo.spi.repository.Repository.State;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.komodo.spi.repository.RepositoryClientEvent;
import org.komodo.test.utils.TestUtilities;
import org.komodo.utils.FileUtils;
import org.komodo.utils.TestKLog;
import org.komodo.utils.observer.KLatchRepositoryObserver;

/**
 * Tests the persistence storage of the repository.
 *
 * Note: The PGSQL tests cannot be run automated since they require an external PGSQL DB. As a result,
 * these tests will be skipped if a connection outside of modeshape cannot be first established to a DB. Thus,
 * care should be taken when modifying ApplicationProperties are other classes that these tests are executed
 * manually and their results checked to ensure that they not just ran but produced satisfactory executions.
 *
 */
@SuppressWarnings( {"javadoc", "nls"} )
public class TestLocalRepositoryPersistence extends AbstractLoggingTest implements SystemConstants {

    protected static final long TIME_TO_WAIT = 3; // in minutes

    private static final String CLEANUP_PSQL = EMPTY_STRING +
                                                    "drop table if exists modeshape_repository cascade;" +
                                                    "drop table if exists content_store cascade;";

    private String PRODUCTION_REPOSITORY_DB;

    private final String PRODUCTION_REPOSITORY_CONFIG = "local-repository-config.json";

    protected LocalRepository _repo = null;

    private Connection connection = null;

    private String komodoTestDir() throws IOException{
        return TestKLog.createEngineDirectory().toAbsolutePath().toString();
    }

    private String komodoPersistenceHost() {
        String host = "localhost";
        ApplicationProperties.setRepositoryPersistenceHost(host);
        return host;
    }

    private String komodoConnectionUrl(PersistenceType persistenceType) {
        String connUrl = persistenceType.getConnUrl();
        ApplicationProperties.setRepositoryPersistenceURL(connUrl);
        return connUrl;
    }

    private String komodoBinaryUrl(PersistenceType persistenceType) {
        String binaryStoreUrl = persistenceType.getBinaryStoreUrl();
        ApplicationProperties.setRepositoryPersistenceBinaryStoreURL(binaryStoreUrl);
        return binaryStoreUrl;
    }

    private String komodoConnectionDriver(PersistenceType persistenceType) {
        String driver = persistenceType.getDriver();
        ApplicationProperties.setRepositoryPersistenceDriver(driver);
        return driver;
    }

    private String komodoUser(PersistenceType persistenceType) {
        String user = EMPTY_STRING;
        if (persistenceType.isExternal())
            user = ApplicationProperties.getRepositoryPersistenceDefaultUser();

        ApplicationProperties.setRepositoryPersistenceUser(user);
        return user;
    }

    private String komodoPassword(PersistenceType persistenceType) {
        String password = EMPTY_STRING;
        if (persistenceType.isExternal()) {
            password = ApplicationProperties.getRepositoryPersistenceDefaultPassword();
        }

        ApplicationProperties.setRepositoryPersistencePassword(password);
        return password;
    }

    private void setKomodoDataDir(PersistenceType persistenceType) throws Exception {
        komodoTestDir();
        komodoPersistenceHost();
        komodoConnectionUrl(persistenceType);
        komodoBinaryUrl(persistenceType);
        komodoConnectionDriver(persistenceType);
        komodoUser(persistenceType);
        komodoPassword(persistenceType);
        PRODUCTION_REPOSITORY_DB = komodoTestDir() + File.separator + "komododb";
    }

    private File testDb(String dbPath) {
        return new File(dbPath);
    }

    private boolean dbExists(String dbPath) {
        File testDb = testDb(dbPath);
        return testDb.exists();
    }

    private void checkRepoObserverErrors(KLatchRepositoryObserver repoObserver) throws Exception {
        Throwable startupError = repoObserver.getError();
        if (startupError != null) {
            startupError.printStackTrace();
            fail("Repository error occurred on startup: " + startupError.getMessage());
        }
    }

    private void initLocalRepository(Class<?> loaderClass, String configFile) throws Exception {
        URL configUrl = loaderClass.getResource(configFile);
        assertNotNull(configUrl);

        LocalRepositoryId id = new LocalRepositoryId(configUrl, DEFAULT_LOCAL_WORKSPACE_NAME);
        _repo = new LocalRepository(id);
        assertThat(_repo.getState(), is(State.NOT_REACHABLE));
        assertThat(_repo.ping(), is(false));

        KLatchRepositoryObserver _repoStartedObserver = new KLatchRepositoryObserver(KEvent.Type.REPOSITORY_STARTED);
        _repo.addObserver(_repoStartedObserver);

        // Start the repository
        final KClient client = mock(KClient.class);
        final RepositoryClientEvent event = RepositoryClientEvent.createStartedEvent(client);
        _repo.notify(event);

        try {
            // Wait for the starting of the repository or timeout of 1 minute
            if (!_repoStartedObserver.getLatch().await(1, TimeUnit.MINUTES)) {
                checkRepoObserverErrors(_repoStartedObserver);
                fail("Test timed-out waiting for local repository to start");
            }

            checkRepoObserverErrors(_repoStartedObserver);
        } finally {
            _repo.removeObserver(_repoStartedObserver);
        }
    }

    private void initLocalRepository(String configFile) throws Exception {
        initLocalRepository(TestLocalRepositoryPersistence.class, configFile);
    }

    /**
     * Shutdown and destroy repo
     *
     * @throws Exception
     */
    private void destroyLocalRepository() throws Exception {
        if (_repo == null)
            return; // nothing to do

        KLatchRepositoryObserver _repoStoppedObserver = new KLatchRepositoryObserver(KEvent.Type.REPOSITORY_STOPPED);
        _repo.addObserver(_repoStoppedObserver);

        KClient client = mock(KClient.class);
        RepositoryClientEvent event = RepositoryClientEvent.createShuttingDownEvent(client);
        _repo.notify(event);

        try {
            if (! _repoStoppedObserver.getLatch().await(1, TimeUnit.MINUTES))
                fail("Local repository was not stopped");
        } finally {
            _repo.removeObserver(_repoStoppedObserver);
            _repo = null;
        }
    }

    private void deleteDbDir(String dbPath) {
        File testDb = testDb(dbPath);
        if (testDb.exists())
            FileUtils.removeDirectoryAndChildren(testDb);
    }

    private void clearPostgresDb() {
        if (connection == null)
            return;

        try {
            PreparedStatement stmt = connection.prepareStatement(CLEANUP_PSQL);
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    // nothing to do
                }
            }
        }

        connection = null;
    }

    private Connection postgresDBConnection() {
        Connection connection = null;
        //
        // Check we have a postgres db handy. If not then skip this test
        //

        try {
            Class.forName(PersistenceType.PGSQL.getDriver());
            
            //
            // PGSQL, unlike H2, requires credentials. This will both set them for use by modeshape later
            // and also return them for checking now
            //
            String komodoUser = komodoUser(PersistenceType.PGSQL);
            String komodoPassword = komodoPassword(PersistenceType.PGSQL);
            String connUrl = PersistenceType.PGSQL.getEvaluatedConnUrl();
            connection = DriverManager.getConnection(connUrl, komodoUser, komodoPassword);
        } catch (Exception ex) {
            // Nothing to do
        }

        return connection;
    }

    @Before
    public void setup() throws Exception {
        setKomodoDataDir(PersistenceType.H2);

        deleteDbDir(PRODUCTION_REPOSITORY_DB);
        assertFalse(dbExists(PRODUCTION_REPOSITORY_DB));
    }

    @After
    public void cleanup() throws Exception {
        destroyLocalRepository();
        deleteDbDir(komodoTestDir());
        clearPostgresDb();
    }

    /**
     * Test to ensure that the production repository
     * configuration files are valid and the repository
     * can be successfully started
     *
     * @throws Exception
     */
    @Test
    public void testProductionRepositoryConfiguration() throws Exception {
        initLocalRepository(LocalRepository.class, PRODUCTION_REPOSITORY_CONFIG);
        assertNotNull(_repo);

        UnitOfWork uow = _repo.createTransaction(RepositoryImpl.SYSTEM_USER, "test-search-type", true, null);
        List<KomodoObject> results = _repo.searchByType(uow, NTLexicon.NT_UNSTRUCTURED);
        assertTrue(results.size() > 0);

        boolean foundRoot = false;
        boolean foundWksp = false;
        boolean foundLibrary = false;
        for (KomodoObject ko : results) {
        	String path = ko.getAbsolutePath();
        	assertNotNull(path);

        	if (RepositoryImpl.KOMODO_ROOT.equals(path))
        		foundRoot = true;
        	else if (RepositoryImpl.LIBRARY_ROOT.equals(path))
        		foundLibrary = true;
        	else if(RepositoryImpl.komodoWorkspacePath(null).equals(path))
        		foundWksp = true;
        }

        assertTrue(foundRoot);
        assertTrue(foundLibrary);
        assertTrue(foundWksp);
    }

    private void helpTestPersistenceWorkspace(String dbPath, String config) throws Exception {
        // Ensure the file store does not already exist
        assertFalse(dbExists(dbPath));
        assertNull(_repo);
    
        // Initialise the repository
        initLocalRepository(config);
        assertNotNull(_repo);
    
        SynchronousCallback callback = new SynchronousCallback();
        UnitOfWork uow = _repo.createTransaction(TEST_USER, "test-persistence-workspace", false, callback);
    
        // Create the komodo workspace
        KomodoObject workspace = _repo.komodoWorkspace(uow);
        assertNotNull(workspace);
    
        //
        // Commit the transaction and await the response of the callback
        //
        uow.commit();
    
        //
        // Wait for the latch to countdown
        //
        assertTrue(callback.await(TIME_TO_WAIT, TimeUnit.MINUTES));
        assertFalse(callback.hasError());
    
        // Find the workspace to confirm what we expect to happen
        uow = _repo.createTransaction(RepositoryImpl.SYSTEM_USER, "test-search-type", true, null);
        List<KomodoObject> results = _repo.searchByType(uow, KomodoLexicon.Workspace.NODE_TYPE);
        assertEquals(1, results.size());
        assertEquals(RepositoryImpl.komodoWorkspacePath(uow), results.iterator().next().getAbsolutePath());
        uow.commit();
    
        // Shutdown the repository
        destroyLocalRepository();
    
        // Restart the repository
        initLocalRepository(config);
        assertNotNull(_repo);
    
        // Find the root and workspace to confirm repo was persisted
        uow = _repo.createTransaction(RepositoryImpl.SYSTEM_USER, "test-search-type", true, null);
        results = _repo.searchByType(uow, KomodoLexicon.Komodo.NODE_TYPE);
        assertEquals(1, results.size());
    
        results = _repo.searchByType(uow, KomodoLexicon.Workspace.NODE_TYPE);
        assertEquals(1, results.size());
        assertEquals(RepositoryImpl.komodoWorkspacePath(uow), results.iterator().next().getAbsolutePath());
        uow.commit();
    }

    @Test
    public void testProductionRepositoryPersistenceWorkspaceH2() throws Exception {
        helpTestPersistenceWorkspace(PRODUCTION_REPOSITORY_DB, PRODUCTION_REPOSITORY_CONFIG);
    }

    @Test
    public void testProductionRepositoryPersistenceWorkspacePSQL() throws Exception {
        connection  = postgresDBConnection();
        Assume.assumeNotNull(connection);

        setKomodoDataDir(PersistenceType.PGSQL);
        helpTestPersistenceWorkspace(PRODUCTION_REPOSITORY_DB, PRODUCTION_REPOSITORY_CONFIG);
    }

    private void helpTestPersistenceObjects(String dbPath, String config) throws Exception, KException {
        int testObjectCount = 5;
    
        // Ensure the file store does not already exist
        assertFalse(dbExists(dbPath));
        assertNull(_repo);
    
        // Initialise the repository
        initLocalRepository(config);
        assertNotNull(_repo);
    
        SynchronousCallback callback = new SynchronousCallback();
        UnitOfWork uow = _repo.createTransaction(TEST_USER, "test-persistence-objects", false, callback);
    
        // Create the komodo workspace
        KomodoObject workspace = _repo.komodoWorkspace(uow);
        assertNotNull(workspace);
    
        for (int i = 1; i <= testObjectCount; ++i) {
            String name = "test" + i;
            if (workspace.hasChild(uow, name))
                continue;

            KomodoObject child = workspace.addChild(uow, name, KomodoLexicon.VdbModel.NODE_TYPE);
            child.setProperty(uow, KomodoLexicon.VdbModel.METADATA_TYPE, "DDL");
        }
    
        //
        // Commit the transaction and await the response of the callback
        //
        uow.commit();
    
        //
        // Wait for the latch to countdown
        //
        assertTrue(callback.await(TIME_TO_WAIT, TimeUnit.MINUTES));
        assertFalse(callback.hasError());
    
        // Find the objects to confirm what we expect to happen
        uow = _repo.createTransaction(TEST_USER, "test-search-type", true, null);
        List<KomodoObject> results = _repo.searchByType(uow, KomodoLexicon.VdbModel.NODE_TYPE);
        assertEquals(testObjectCount, results.size());
        uow.commit();
    
        // Shutdown the repository
        destroyLocalRepository();
    
        // Restart the repository
        initLocalRepository(config);
        assertNotNull(_repo);
    
        // Find the test nodes to confirm repo was persisted
        uow = _repo.createTransaction(TEST_USER, "test-search-type", true, null);
        results = _repo.searchByType(uow, KomodoLexicon.VdbModel.NODE_TYPE);
        assertEquals(testObjectCount, results.size());
    
        for (KomodoObject result : results) {
            String name = result.getName(uow);
            assertTrue(name.startsWith("test"));
    
            Property property = result.getProperty(uow, KomodoLexicon.VdbModel.METADATA_TYPE);
            assertEquals("DDL", property.getStringValue(uow));
        }
    
        uow.commit();
    }

    @Test
    public void testProductionRepositoryPersistenceObjectsH2() throws Exception {
        helpTestPersistenceObjects(PRODUCTION_REPOSITORY_DB, PRODUCTION_REPOSITORY_CONFIG);
    }

    @Test
    public void testProductionRepositoryPersistenceObjectsPSQL() throws Exception {
        connection = postgresDBConnection();
        Assume.assumeNotNull(connection);

        setKomodoDataDir(PersistenceType.PGSQL);
        helpTestPersistenceObjects(PRODUCTION_REPOSITORY_DB, PRODUCTION_REPOSITORY_CONFIG);
    }

    private KomodoObject createMySqlDriver(UnitOfWork uow, KomodoObject parent, String name) throws Exception {
        KomodoObject driver = parent.addChild(uow, name, DataVirtLexicon.ResourceFile.DRIVER_FILE_NODE_TYPE);
        InputStream contentStream = TestUtilities.mySqlDriver();
        assertNotNull(contentStream);
        byte[] content = FileUtils.write(contentStream);

        KomodoObject fileNode;
        if (! driver.hasChild(uow, JcrLexicon.JCR_CONTENT))
            fileNode = driver.addChild(uow, JcrLexicon.JCR_CONTENT, null);
        else
            fileNode = driver.getChild(uow, JcrLexicon.JCR_CONTENT);

        ByteArrayInputStream stream = new ByteArrayInputStream(content);
        fileNode.setProperty(uow, JcrLexicon.JCR_DATA, stream);

        return driver;
    }

    private void helpTestBinaryValuePersistence() throws Exception, IOException, KException {
        //
        // Get control values for testing the created properties against
        //
        InputStream contentStream = TestUtilities.mySqlDriver();
        assertNotNull(contentStream);
        byte[] content = FileUtils.write(contentStream);
        int contentAvailable = content.length;
        long mySqlChkSum = TestUtilities.checksum(content);

        int testObjectCount = 5;

        // Initialise the repository
        initLocalRepository(LocalRepository.class, PRODUCTION_REPOSITORY_CONFIG);
        assertNotNull(_repo);

        SynchronousCallback callback = new SynchronousCallback();
        UnitOfWork uow = _repo.createTransaction(TEST_USER, "test-persistence-binary-values", false, callback);

        // Create the komodo workspace
        KomodoObject workspace = _repo.komodoWorkspace(uow);
        assertNotNull(workspace);

        //
        // Create objects with binary properties
        //
        for (int i = 1; i <= testObjectCount; ++i) {
            createMySqlDriver(uow, workspace, "test" + i);
        }

        //
        // Commit the transaction and await the response of the callback
        //
        uow.commit();

        //
        // Wait for the latch to countdown
        //
        assertTrue(callback.await(TIME_TO_WAIT, TimeUnit.MINUTES));
        assertFalse(callback.hasError());

        // Find the objects to confirm what we expect to happen
        uow = _repo.createTransaction(TEST_USER, "test-search-type", true, null);
        List<KomodoObject> results = _repo.searchByType(uow, DataVirtLexicon.ResourceFile.DRIVER_FILE_NODE_TYPE);
        assertEquals(testObjectCount, results.size());
        uow.commit();

        // Shutdown the repository
        destroyLocalRepository();

        // Restart the repository
        initLocalRepository(LocalRepository.class, PRODUCTION_REPOSITORY_CONFIG);
        assertNotNull(_repo);

        // Find the test nodes to confirm repo was persisted
        uow = _repo.createTransaction(TEST_USER, "test-search-type", true, null);
        results = _repo.searchByType(uow, DataVirtLexicon.ResourceFile.DRIVER_FILE_NODE_TYPE);
        assertEquals(testObjectCount, results.size());

        //
        // Interrogate each of the drivers to confirm the
        // child file nodes still have their binary content
        //
        for (KomodoObject driver : results) {
            String name = driver.getName(uow);
            assertTrue(name.startsWith("test"));
            assertTrue(driver.hasChild(uow, JcrLexicon.JCR_CONTENT));

            KomodoObject fileNode = driver.getChild(uow, JcrLexicon.JCR_CONTENT);

            Property property = fileNode.getProperty(uow, JcrLexicon.JCR_DATA);

            InputStream binaryStream = property.getBinaryValue(uow);
            byte[] binaryBytes = FileUtils.write(binaryStream);
            assertEquals(contentAvailable, binaryBytes.length);
            assertEquals(mySqlChkSum, TestUtilities.checksum(binaryBytes));
        }

        uow.commit();
    }

    @Test
    public void testBinaryValuePersistenceH2() throws Exception {
        helpTestBinaryValuePersistence();
    }

    @Test
    public void testBinaryValuePersistencePSQL() throws Exception {
        connection = postgresDBConnection();
        Assume.assumeNotNull(connection);

        setKomodoDataDir(PersistenceType.PGSQL);
        helpTestBinaryValuePersistence();
    }
}
