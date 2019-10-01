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

package org.komodo.openshift;

import static org.junit.Assert.*;

import java.util.concurrent.Callable;

import org.junit.Test;
import org.komodo.KEngine;
import org.komodo.KException;
import org.komodo.WorkspaceManager;
import org.komodo.datasources.DefaultSyndesisDataSource;
import org.komodo.metadata.MetadataInstance;
import org.komodo.rest.KomodoConfigurationProperties;
import org.mockito.Mockito;

public class TeiidOpenShiftClientTest {

    @Test public void testSetKomodoName() throws Exception {
        MetadataInstance metadata = Mockito.mock(MetadataInstance.class);

        TeiidOpenShiftClient client = new TeiidOpenShiftClient(metadata, new EncryptionComponent("blah"), new KomodoConfigurationProperties(), new KEngine() {

            @Override
            public void start() throws Exception {

            }

            @Override
            public <T> T runInTransaction(boolean rollbackOnly, Callable<T> callable) throws Exception {
                return callable.call();
            }

            @Override
            public WorkspaceManager getWorkspaceManager() throws KException {
                return Mockito.mock(WorkspaceManager.class);
            }
        }, null);

        DefaultSyndesisDataSource dsd = new DefaultSyndesisDataSource();

        String name = client.getUniqueKomodoName(dsd, "sys");

        assertTrue(name.startsWith("sys_"));

        name = client.getUniqueKomodoName(dsd, "View");

        assertEquals("View", name);

        name = client.getUniqueKomodoName(dsd, "?syS.");

        assertTrue(name.startsWith("syS_"));
    }

}
