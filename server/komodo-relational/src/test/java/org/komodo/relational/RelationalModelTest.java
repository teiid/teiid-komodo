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
package org.komodo.relational;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.komodo.core.AbstractLocalRepositoryTest;
import org.komodo.core.KomodoLexicon;
import org.komodo.relational.connection.Connection;
import org.komodo.relational.dataservice.Dataservice;
import org.komodo.relational.folder.Folder;
import org.komodo.relational.model.Model;
import org.komodo.relational.model.Schema;
import org.komodo.relational.model.Table;
import org.komodo.relational.vdb.Vdb;
import org.komodo.relational.workspace.WorkspaceManager;
import org.komodo.spi.lexicon.datavirt.DataVirtLexicon;
import org.komodo.spi.lexicon.vdb.VdbLexicon;
import org.komodo.spi.repository.KomodoObject;

@SuppressWarnings( { "javadoc", "nls" } )
public class RelationalModelTest extends AbstractLocalRepositoryTest {

    protected static final String VDB_PATH = "/vdb/path/vdb.vdb";

    protected Model createModel() throws Exception {
        return createModel( this.name.getMethodName() + "-VDB", VDB_PATH, this.name.getMethodName() + "-Model" );
    }

    protected Model createModel( final String vdbName,
                                 final String vdbPath,
                                 final String modelName ) throws Exception {
        final WorkspaceManager mgr = WorkspaceManager.getInstance(_repo, getTransaction());
        final Vdb vdb = mgr.createVdb( getTransaction(), null, vdbName, vdbPath );
        final Model model = vdb.addModel( getTransaction(), modelName );

        assertThat( model.getPrimaryType( getTransaction() ).getName(), is( VdbLexicon.Vdb.DECLARATIVE_MODEL ) );
        assertThat( model.getName( getTransaction() ), is( modelName ) );
        return model;
    }

    protected Table createTable() throws Exception {
        return createTable( getDefaultVdbName(), VDB_PATH, getDefaultModelName(), getDefaultTableName() );
    }

    protected Table createTable( final String vdbName,
                                 final String vdbPath,
                                 final String modelName,
                                 final String tableName ) throws Exception {
        final WorkspaceManager mgr = WorkspaceManager.getInstance(_repo, getTransaction());
        final Vdb vdb = mgr.createVdb( getTransaction(), null, vdbName, vdbPath );
        final Model model = vdb.addModel( getTransaction(), modelName );
        return model.addTable( getTransaction(), tableName );
    }

    protected Vdb createVdb() throws Exception {
        return createVdb( getDefaultVdbName(), VDB_PATH );
    }

    protected Vdb createVdb( final String vdbName ) throws Exception {
        return createVdb( vdbName, VDB_PATH );
    }

    protected Vdb createVdb( final String vdbName,
                             final String originalFilePath ) throws Exception {
        return createVdb(vdbName, null, originalFilePath);
    }

    protected Vdb createVdb( final String vdbName,
                             final KomodoObject parent,
                             final String originalFilePath ) throws Exception {
        final WorkspaceManager mgr = WorkspaceManager.getInstance(_repo, getTransaction());
        final Vdb vdb = mgr.createVdb( getTransaction(), parent, vdbName, originalFilePath );

        assertThat( vdb.getPrimaryType( getTransaction() ).getName(), is( VdbLexicon.Vdb.VIRTUAL_DATABASE ) );
        assertThat( vdb.getName( getTransaction() ), is( vdbName ) );
        assertThat( vdb.getOriginalFilePath( getTransaction() ), is( originalFilePath ) );
        return vdb;
    }

    protected Schema createSchema() throws Exception {
        return createSchema( getDefaultSchemaName() );
    }

    protected Schema createSchema( final String schemaName ) throws Exception {
        return createSchema( schemaName, null );
    }

    protected Schema createSchema( final String schemaName,
                                   final KomodoObject parent ) throws Exception {
        final WorkspaceManager mgr = WorkspaceManager.getInstance(_repo, getTransaction());
        final Schema schema = mgr.createSchema( getTransaction(), parent, schemaName );

        assertThat( schema.getPrimaryType( getTransaction() ).getName(), is( KomodoLexicon.Schema.NODE_TYPE ) );
        assertThat( schema.getName( getTransaction() ), is( schemaName ) );
        return schema;
    }

    protected Dataservice createDataservice() throws Exception {
        return createDataservice( getDefaultDataserviceName() );
    }

    protected Dataservice createDataservice( final String serviceName ) throws Exception {
        return createDataservice( serviceName, null );
    }

    protected Dataservice createDataservice( final String serviceName,
                                             final KomodoObject parent ) throws Exception {
        final WorkspaceManager mgr = WorkspaceManager.getInstance(_repo, getTransaction());
        final Dataservice ds = mgr.createDataservice( getTransaction(), parent, serviceName );

        assertThat( ds.getPrimaryType( getTransaction() ).getName(), is( DataVirtLexicon.DataService.NODE_TYPE ) );
        assertThat( ds.getName( getTransaction() ), is( serviceName ) );
        return ds;
    }

    protected Connection createConnection() throws Exception {
        return createConnection( getDefaultConnectionName() );
    }

    protected Connection createConnection( final String dsName ) throws Exception {
        return createConnection( dsName, null );
    }

    protected Connection createConnection( final String dsName,
                                           final KomodoObject parent ) throws Exception {
        final WorkspaceManager mgr = WorkspaceManager.getInstance(_repo, getTransaction());
        final Connection ds = mgr.createConnection( getTransaction(), parent, dsName );

        assertThat( ds.getPrimaryType( getTransaction() ).getName(), is( DataVirtLexicon.Connection.NODE_TYPE ) );
        assertThat( ds.getName( getTransaction() ), is( dsName ) );
        return ds;
    }

    protected Folder createFolder() throws Exception {
        return createFolder( getDefaultFolderName() );
    }

    protected Folder createFolder( final String folderName ) throws Exception {
        return createFolder( folderName, null );
    }

    protected Folder createFolder( final String folderName,
                                             final KomodoObject parent ) throws Exception {
        final WorkspaceManager mgr = WorkspaceManager.getInstance(_repo, getTransaction());
        final Folder folder = mgr.createFolder( getTransaction(), parent, folderName );

        assertThat( folder.getPrimaryType( getTransaction() ).getName(), is( KomodoLexicon.Folder.NODE_TYPE ) );
        assertThat( folder.getName( getTransaction() ), is( folderName ) );
        return folder;
    }

    protected String getDefaultModelName() {
        return ( this.name.getMethodName() + "-Model" );
    }

    protected String getDefaultTableName() {
        return ( this.name.getMethodName() + "-Table" );
    }

    protected String getDefaultVdbName() {
        return ( this.name.getMethodName() + "-Vdb" );
    }

    protected String getDefaultSchemaName() {
        return ( this.name.getMethodName() + "-Schema" );
    }

    protected String getDefaultTeiidName() {
        return ( this.name.getMethodName() + "-Teiid" );
    }

    protected String getDefaultDataserviceName() {
        return ( this.name.getMethodName() + "-Dataservice" );
    }

    protected String getDefaultConnectionName() {
        return ( this.name.getMethodName() + "-Connection" );
    }

    protected String getDefaultFolderName() {
        return ( this.name.getMethodName() + "-Folder" );
    }
    
    protected String getDefaultViewEditorStateName() {
        return ( this.name.getMethodName() + "-ViewEditorState" );
    }

}
