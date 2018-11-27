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
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.komodo.core.AbstractLocalRepositoryTest;
import org.komodo.core.KomodoLexicon;
import org.komodo.spi.KException;
import org.komodo.spi.constants.StringConstants;
import org.komodo.spi.lexicon.vdb.VdbLexicon;
import org.komodo.spi.repository.KomodoObject;
import org.komodo.spi.repository.Property;
import org.komodo.spi.repository.Repository;
import org.komodo.spi.repository.Repository.Id;
import org.komodo.spi.repository.Repository.KeywordCriteria;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.komodo.spi.repository.Repository.UnitOfWork.State;
import org.komodo.spi.repository.Repository.UnitOfWorkListener;

@SuppressWarnings( {"javadoc", "nls"} )
public class TestLocalRepository extends AbstractLocalRepositoryTest {

    @Before
    public void assertReachable() {
        assertThat(_repo.ping(), is(true));
        assertThat(_repo.getState(), is(Repository.State.REACHABLE));
    }

    /**
     * Tests to confirm that the transaction awaits the completion of the sequencers
     * prior to calling the given callback when creating an unrelated object.
     *
     * Confirms that the sequencers are called and complete (with nothing to do)
     *
     * @throws Exception
     */
    @Test
    public void shouldRespondWithCallback() throws Exception {
        // Ensure the workspace is created first and in a different transaction
        _repo.komodoWorkspace(getTransaction());
        commit();

        final Boolean[] callbackCalled = new Boolean[1];
        callbackCalled[0] = false;

        //
        // Despite only creating a node the callback should be called
        // and the value of callbackCalled changed to true
        //
        UnitOfWorkListener delegate = new UnitOfWorkListener() {

            @Override
            public void respond(Object results) {
                callbackCalled[0] = true;
            }

            @Override
            public void errorOccurred(Throwable error) {
                // Nothing required since synchronous callback will log the error
            }
        };

        SynchronousCallback callback = new SynchronousNestedCallback(delegate);
        useCustomCallback( callback, false );

        //
        // Create a single test node with no relationship to the sequencers or
        // with any relevant properties
        //
        _repo.add( getTransaction(), RepositoryImpl.komodoWorkspacePath(getTransaction()), "Test1", null );
        commit();

        //
        // Stop the test from completing prior to the callback returning
        //
        assertTrue(callback.await(TIME_TO_WAIT, TimeUnit.MINUTES));
        assertFalse(callback.hasError());

        //
        // The callback should have updated the value of callbackCalled to true
        //
        assertTrue(callbackCalled[0]);

        // Create a single test node with no relationship to the sequencers or
        // with any relevant properties
        //
        _repo.add(getTransaction(), RepositoryImpl.komodoWorkspacePath(getTransaction()), "Test1", null);
        commit();
    }

    /**
     * Tests to confirm that the transaction awaits the completion of the sequencers
     * prior to calling the given callback when setting a property on an unrelated object
     *
     * Confirms that the sequencers are called and complete (with nothing to do)
     *
     * @throws Exception
     */
    @Test
    public void shouldRespondWithCallback2() throws Exception {
        // Ensure the workspace is created first and in a different transaction
        _repo.komodoWorkspace(getTransaction());
        commit();

        // Create the test object to test the addition of a property
        KomodoObject testObject = _repo.add(getTransaction(), RepositoryImpl.komodoWorkspacePath(getTransaction()), "Test1", null);
        assertNotNull(testObject);
        commit();

        final Boolean[] callbackCalled = new Boolean[1];
        callbackCalled[0] = false;

        //
        // Despite setting an unrelated (to the sequencers) property the callback
        // should be called and the value of callbackCalled changed to true
        //
        UnitOfWorkListener delegate = new UnitOfWorkListener() {

            @Override
            public void respond(Object results) {
                callbackCalled[0] = true;
            }

            @Override
            public void errorOccurred(Throwable error) {
                // Nothing required since synchronous callback will log the error
            }
        };

        final SynchronousCallback callback = new SynchronousNestedCallback( delegate );
        useCustomCallback( callback, false );

        //
        // Create a single test node with no relationship to the sequencers or
        // with any relevant properties
        //
        testObject.setProperty(getTransaction(), "TestProperty1", "My property value");
        commit();

        //
        // Stop the test from completing prior to the callback returning
        //
        assertTrue(callback.await(TIME_TO_WAIT, TimeUnit.MINUTES));
        assertFalse(callback.hasError());

        //
        // The callback should have updated the value of callbackCalled to true
        //
        assertTrue(callbackCalled[0]);
    }

    @Test
    public void shouldAddWorkspaceItemAtRoot() throws Exception {
        // setup
        final String name = this.name.getMethodName();
        final KomodoObject rootNode = _repo.add(getTransaction(), null, name, null);
        commit();

        // tests
        assertThat(rootNode, is(notNullValue()));
        assertThat(rootNode.getName(getTransaction()), is(name));
        assertThat(rootNode.getAbsolutePath(), is(RepositoryImpl.komodoWorkspacePath(getTransaction()) + FORWARD_SLASH + name));
    }

    @Test
    public void shouldCreateRollbackTransaction() throws Exception {
        // setup
        final String name = "elvis";
        final UnitOfWork transaction = _repo.createTransaction(TEST_USER, name, true, null);

        // tests
        assertThat(transaction, is(notNullValue()));
        assertThat(transaction.getName(), is(name));
        assertThat(transaction.getCallback(), is(nullValue()));
        assertThat(transaction.isRollbackOnly(), is(true));

        transaction.commit();
    }

    @Test
    public void shouldCreateUpdateTransaction() throws Exception {
        // setup
        final String name = "elvis";
        final UnitOfWork transaction = _repo.createTransaction(TEST_USER, name, false, null);

        // tests
        assertThat(transaction, is(notNullValue()));
        assertThat(transaction.getName(), is(name));
        assertThat(transaction.getCallback(), is(nullValue()));
        assertThat(transaction.isRollbackOnly(), is(false));

        transaction.commit();
    }

    @Test( expected = KException.class )
    public void shouldFailToAddWorkspaceItemToNonexistingParent() throws Exception {
        _repo.add(getTransaction(), "does-not-exist", "shouldFailToAddWorkspaceItemToNonexistingParent", null);
    }

    @Test( expected = KException.class )
    public void shouldFailToRemoveWorkspaceItemThatDoesNotExist() throws Exception {
        _repo.remove(getTransaction(), "shouldFailToRemoveWorkspaceItemThatDoesNotExist");
    }

    @Test
    public void shouldGetId() {
        final Id id = _repo.getId();
        assertThat(id, is(notNullValue()));
        assertThat(id.getWorkspaceName(), is(StringConstants.DEFAULT_LOCAL_WORKSPACE_NAME));
    }

    @Test
    public void shouldGetNullWhenWorkspaceItemDoesNotExist() throws Exception {
        final KomodoObject doesNotExist = _repo.getFromWorkspace(getTransaction(), "shouldGetNullWhenWorkspaceItemDoesNotExist");
        assertThat(doesNotExist, is(nullValue()));
    }

    @Test
    public void shouldGetType() {
        assertThat(_repo.getType(), is(Repository.Type.LOCAL));
    }

    @Test
    public void shouldGetWorkspaceHomeofTestUser() throws Exception {
        final KomodoObject rootNode = _repo.getFromWorkspace(getTransaction(), null);
        assertThat(rootNode, is(notNullValue()));
        assertThat(rootNode.getName(getTransaction()), is(TEST_USER));
        assertThat(rootNode.getPrimaryType(getTransaction()).getName(), is(KomodoLexicon.Home.NODE_TYPE));
    }

    @Test
    public void shouldDynamicallyCreateWorkspaceHomeofNewUser() throws Exception {
        String newUser = "newUser";

        SynchronousCallback callback = new TestTransactionListener();
        UnitOfWork tx = createTransaction(newUser, (this.name.getMethodName()), false, callback);

        String userWksp = RepositoryImpl.komodoWorkspacePath(tx);

        List<KomodoObject> results = _repo.searchByPath(sysTx(), userWksp);
        sysCommit();
        assertTrue(results.isEmpty());

        //
        // getFromWorkspace dynamically creates user home
        //
        KomodoObject userHome = _repo.getFromWorkspace(tx, userWksp);
        assertNotNull(userHome);
        commit(tx, State.COMMITTED);

        results = _repo.searchByPath(sysTx(), userWksp);
        sysCommit();
        assertFalse(results.isEmpty());
    }

    @Test
    public void shouldDynamicallyCreateWorkspaceHomeofNewUser2() throws Exception {
        String newUser = "newUser";

        SynchronousCallback callback = new TestTransactionListener();
        UnitOfWork tx = createTransaction(newUser, (this.name.getMethodName()), false, callback);

        String userWksp = RepositoryImpl.komodoWorkspacePath(tx);

        List<KomodoObject> results = _repo.searchByPath(sysTx(), userWksp);
        sysCommit();
        assertTrue(results.isEmpty());

        //
        // _repo.checkSettings() dynamically creates user home
        //
        KomodoObject wkspObject = new ObjectImpl(_repo, userWksp, 0);
        KomodoObject vdb = wkspObject.addChild(tx, "testVdb", VdbLexicon.Vdb.VIRTUAL_DATABASE);
        assertNotNull(vdb);
        commit(tx, State.COMMITTED);

        results = _repo.searchByPath(sysTx(), userWksp);
        sysCommit();
        assertFalse(results.isEmpty());
    }

    @Test
    public void shouldNotRemoveExistingItemsIfTryingToRemoveItemThatDoesNotExist() throws Exception {
        final String item1 = "shouldNotRemoveExistingItemsIfTryingToRemoveItemThatDoesNotExist-1";
        final String item2 = "shouldNotRemoveExistingItemsIfTryingToRemoveItemThatDoesNotExist-2";

        // setup
        _repo.add( getTransaction(), null, item1, null );
        commit();

        try {
            _repo.remove(getTransaction(),
                         item1,
                         item2,
                         "shouldNotRemoveExistingItemsIfTryingToRemoveItemThatDoesNotExist-doesNotExist");
            fail();
        } catch (final KException e) {
            // tests
            assertThat(_repo.getFromWorkspace(getTransaction(), item1), is(notNullValue()));
            assertThat(_repo.getFromWorkspace(getTransaction(), item2), is(nullValue()));
        }
    }

    @Test
    public void shouldRemoveMultipleWorkspaceRootItems() throws Exception {
        // setup
        final String item1 = "shouldRemoveMultipleWorkspaceRootItems-1";
        _repo.add(getTransaction(), null, item1, null);
        final String item2 = "shouldRemoveMultipleWorkspaceRootItems-2";
        _repo.add(getTransaction(), null, item2, null);
        commit();

        _repo.remove(getTransaction(), item1, item2);
        commit();

        // tests
        assertThat(_repo.getFromWorkspace(getTransaction(), item1), is(nullValue()));
        assertThat(_repo.getFromWorkspace(getTransaction(), item2), is(nullValue()));
    }

    @Test
    public void shouldRemoveWorkspaceRootItem() throws Exception {
        // setup
        final String name = this.name.getMethodName();
        _repo.add(getTransaction(), null, name, null);
        _repo.remove(getTransaction(), name);

        // tests
        assertThat(_repo.getFromWorkspace(getTransaction(), name), is(nullValue()));
    }

    @Test
    public void shouldTraverseUserWorkspace() throws Exception {
        UnitOfWork sysTx = sysTx();
        KomodoObject komodoWksp = _repo.komodoWorkspace(sysTx);
        assertNotNull(komodoWksp);
        String komodoRootPath = FORWARD_SLASH + KomodoLexicon.Komodo.NODE_TYPE;
        String komodoWkspPath = komodoRootPath + FORWARD_SLASH + KomodoLexicon.Komodo.WORKSPACE;

        assertEquals(komodoWkspPath, komodoWksp.getAbsolutePath());

        KomodoObject komodoRoot = komodoWksp.getParent(sysTx);
        assertNotNull(komodoRoot);
        assertEquals(komodoRootPath, komodoRoot.getAbsolutePath());

        final KomodoObject nullParent = komodoRoot.getParent(sysTx);
        assertThat( nullParent, is( nullValue() ) );
    }

    @Test
    public void shouldSearchForPrimaryType() throws Exception {
        KomodoObject komodoWksp = _repo.komodoWorkspace(getTransaction());
        assertNotNull(komodoWksp);

        // Setup up 10 nodes to find
        for (int i = 1; i < 6; ++i) {
            KomodoObject child = komodoWksp.addChild(getTransaction(), "test" + i, KomodoLexicon.VdbModel.NODE_TYPE);
            child.setProperty(getTransaction(), KomodoLexicon.VdbModel.MODEL_DEFINITION, "DDL");
        }

        for (int i = 6; i < 11; ++i) {
            KomodoObject child = komodoWksp.addChild(getTransaction(), "test" + i, KomodoLexicon.VdbModel.NODE_TYPE);
            child.setProperty(getTransaction(), KomodoLexicon.VdbModel.MODEL_DEFINITION, "TEIIDSQL");
        }

        KomodoObject[] testNodes = komodoWksp.getChildrenOfType(getTransaction(), KomodoLexicon.VdbModel.NODE_TYPE);
        assertEquals(10, testNodes.length);
        for (KomodoObject testKO : testNodes) {
            Property property = testKO.getProperty(getTransaction(), KomodoLexicon.VdbModel.MODEL_DEFINITION);
            assertTrue(property.getStringValue(getTransaction()).equals("DDL") || property.getStringValue(getTransaction()).equals("TEIIDSQL"));
        }

        commit(); // session save needed before query

        // Perform the search
        List<KomodoObject> results = _repo.searchByType(getTransaction(), KomodoLexicon.VdbModel.NODE_TYPE);

        // Validate the results are as exepcted
        assertEquals(testNodes.length, results.size());
        for (KomodoObject searchObject : results) {
            String name = searchObject.getName(getTransaction());
            assertTrue(name.startsWith("test"));

            String indexStr = name.substring(4);
            int index = Integer.parseInt(indexStr);
            assertTrue(index > 0 && index < 11);

            Property property = searchObject.getProperty(getTransaction(), KomodoLexicon.VdbModel.MODEL_DEFINITION);
            if (index < 6)
                assertEquals("DDL", property.getStringValue(getTransaction()));
            else
                assertEquals("TEIIDSQL", property.getStringValue(getTransaction()));
        }
    }

    @Test
    public void shouldSearchForKeyword() throws Exception {
        KomodoObject komodoWksp = _repo.komodoWorkspace(getTransaction());
        assertNotNull(komodoWksp);

        // Setup up 10 nodes to find
        for (int i = 1; i < 6; ++i) {
            KomodoObject child = komodoWksp.addChild(getTransaction(), "test" + i, KomodoLexicon.VdbModel.NODE_TYPE);
            child.setProperty(getTransaction(), KomodoLexicon.VdbModel.MODEL_DEFINITION, "DDL");
        }

        for (int i = 6; i < 11; ++i) {
            KomodoObject child = komodoWksp.addChild(getTransaction(), "test" + i, KomodoLexicon.VdbModel.NODE_TYPE);
            child.setProperty(getTransaction(), KomodoLexicon.VdbModel.MODEL_DEFINITION, "TEIIDSQL");
        }

        KomodoObject[] testNodes = komodoWksp.getChildrenOfType(getTransaction(), KomodoLexicon.VdbModel.NODE_TYPE);
        assertEquals(10, testNodes.length);
        for (KomodoObject testKO : testNodes) {
            Property property = testKO.getProperty(getTransaction(), KomodoLexicon.VdbModel.MODEL_DEFINITION);
            assertTrue(property.getStringValue(getTransaction()).equals("DDL") || property.getStringValue(getTransaction()).equals("TEIIDSQL"));
        }

        commit(); // session save needed before query

        // Perform the search
        List<KomodoObject> results = _repo.searchByKeyword(
                                                           getTransaction(), KomodoLexicon.VdbModel.NODE_TYPE,
                                                           KomodoLexicon.VdbModel.MODEL_DEFINITION,
                                                           KeywordCriteria.ANY,
                                                           "DDL");

        // Validate the results are as expected
        assertEquals(5, results.size());
        for (KomodoObject searchObject : results) {
            String name = searchObject.getName(getTransaction());
            assertTrue(name.startsWith("test"));

            String indexStr = name.substring(4);
            int index = Integer.parseInt(indexStr);
            assertTrue(index > 0 && index < 11);

            Property property = searchObject.getProperty(getTransaction(), KomodoLexicon.VdbModel.MODEL_DEFINITION);
            if (index < 6)
                assertEquals("DDL", property.getStringValue(getTransaction()));
        }
    }

    @Test
    public void shouldSearchForPath() throws Exception {
        KomodoObject komodoWksp = _repo.komodoWorkspace(getTransaction());
        assertNotNull(komodoWksp);

        // Setup up 10 nodes to find
        for (int i = 1; i <= 5; ++i) {
            KomodoObject child = komodoWksp.addChild(getTransaction(), "test" + i, KomodoLexicon.VdbModel.NODE_TYPE);
            child.setProperty(getTransaction(), KomodoLexicon.VdbModel.MODEL_DEFINITION, "DDL");
        }

        KomodoObject[] testNodes = komodoWksp.getChildrenOfType(getTransaction(), KomodoLexicon.VdbModel.NODE_TYPE);
        assertEquals(5, testNodes.length);
        for (KomodoObject testKO : testNodes) {
            Property property = testKO.getProperty(getTransaction(), KomodoLexicon.VdbModel.MODEL_DEFINITION);
            assertTrue(property.getStringValue(getTransaction()).equals("DDL") || property.getStringValue(getTransaction()).equals("TEIIDSQL"));
        }

        commit(); // session save needed before query

        // Perform the search
        for (int i = 1; i <= 5; ++i) {
            List<KomodoObject> results = _repo.searchByPath(getTransaction(),
                                                           komodoWksp.getAbsolutePath() + FORWARD_SLASH + "test" + i);
            // Validate the results are as expected
            assertEquals(1, results.size());
            KomodoObject searchObject = results.iterator().next();
            String name = searchObject.getName(getTransaction());
            assertEquals("test" + i, name);

            Property property = searchObject.getProperty(getTransaction(), KomodoLexicon.VdbModel.MODEL_DEFINITION);
            assertEquals("DDL", property.getStringValue(getTransaction()));
        }
    }
}
