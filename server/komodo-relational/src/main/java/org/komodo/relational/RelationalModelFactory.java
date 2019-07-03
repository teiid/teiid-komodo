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

import java.util.Map;

import org.komodo.core.KomodoLexicon;
import org.komodo.core.repository.RepositoryTools;
import org.komodo.relational.connection.Connection;
import org.komodo.relational.connection.internal.ConnectionImpl;
import org.komodo.relational.dataservice.ConnectionEntry;
import org.komodo.relational.dataservice.Dataservice;
import org.komodo.relational.dataservice.ServiceVdbEntry;
import org.komodo.relational.dataservice.VdbEntry;
import org.komodo.relational.dataservice.VdbEntryContainer;
import org.komodo.relational.dataservice.internal.ConnectionEntryImpl;
import org.komodo.relational.dataservice.internal.DataserviceImpl;
import org.komodo.relational.dataservice.internal.ServiceVdbEntryImpl;
import org.komodo.relational.dataservice.internal.VdbEntryImpl;
import org.komodo.relational.model.AbstractProcedure;
import org.komodo.relational.model.AccessPattern;
import org.komodo.relational.model.Column;
import org.komodo.relational.model.DataTypeResultSet;
import org.komodo.relational.model.ForeignKey;
import org.komodo.relational.model.Index;
import org.komodo.relational.model.Model;
import org.komodo.relational.model.OptionContainer;
import org.komodo.relational.model.Parameter;
import org.komodo.relational.model.PrimaryKey;
import org.komodo.relational.model.PushdownFunction;
import org.komodo.relational.model.ResultSetColumn;
import org.komodo.relational.model.Schema;
import org.komodo.relational.model.SchemaElement.SchemaElementType;
import org.komodo.relational.model.StatementOption;
import org.komodo.relational.model.StoredProcedure;
import org.komodo.relational.model.Table;
import org.komodo.relational.model.TabularResultSet;
import org.komodo.relational.model.UniqueConstraint;
import org.komodo.relational.model.UserDefinedFunction;
import org.komodo.relational.model.View;
import org.komodo.relational.model.VirtualProcedure;
import org.komodo.relational.model.internal.AccessPatternImpl;
import org.komodo.relational.model.internal.ColumnImpl;
import org.komodo.relational.model.internal.DataTypeResultSetImpl;
import org.komodo.relational.model.internal.ForeignKeyImpl;
import org.komodo.relational.model.internal.IndexImpl;
import org.komodo.relational.model.internal.ModelImpl;
import org.komodo.relational.model.internal.ParameterImpl;
import org.komodo.relational.model.internal.PrimaryKeyImpl;
import org.komodo.relational.model.internal.PushdownFunctionImpl;
import org.komodo.relational.model.internal.ResultSetColumnImpl;
import org.komodo.relational.model.internal.SchemaImpl;
import org.komodo.relational.model.internal.StatementOptionImpl;
import org.komodo.relational.model.internal.StoredProcedureImpl;
import org.komodo.relational.model.internal.TableImpl;
import org.komodo.relational.model.internal.TabularResultSetImpl;
import org.komodo.relational.model.internal.UniqueConstraintImpl;
import org.komodo.relational.model.internal.UserDefinedFunctionImpl;
import org.komodo.relational.model.internal.ViewImpl;
import org.komodo.relational.model.internal.VirtualProcedureImpl;
import org.komodo.relational.profile.Profile;
import org.komodo.relational.profile.SqlComposition;
import org.komodo.relational.profile.SqlProjectedColumn;
import org.komodo.relational.profile.StateCommand;
import org.komodo.relational.profile.StateCommandAggregate;
import org.komodo.relational.profile.ViewDefinition;
import org.komodo.relational.profile.ViewEditorState;
import org.komodo.relational.profile.internal.SqlCompositionImpl;
import org.komodo.relational.profile.internal.SqlProjectedColumnImpl;
import org.komodo.relational.profile.internal.StateCommandAggregateImpl;
import org.komodo.relational.profile.internal.ViewDefinitionImpl;
import org.komodo.relational.profile.internal.ViewEditorStateImpl;
import org.komodo.relational.template.Template;
import org.komodo.relational.template.TemplateEntry;
import org.komodo.relational.template.internal.TemplateEntryImpl;
import org.komodo.relational.template.internal.TemplateImpl;
import org.komodo.relational.vdb.Condition;
import org.komodo.relational.vdb.DataRole;
import org.komodo.relational.vdb.Entry;
import org.komodo.relational.vdb.Mask;
import org.komodo.relational.vdb.ModelSource;
import org.komodo.relational.vdb.Permission;
import org.komodo.relational.vdb.Translator;
import org.komodo.relational.vdb.Vdb;
import org.komodo.relational.vdb.VdbImport;
import org.komodo.relational.vdb.internal.ConditionImpl;
import org.komodo.relational.vdb.internal.DataRoleImpl;
import org.komodo.relational.vdb.internal.EntryImpl;
import org.komodo.relational.vdb.internal.MaskImpl;
import org.komodo.relational.vdb.internal.ModelSourceImpl;
import org.komodo.relational.vdb.internal.PermissionImpl;
import org.komodo.relational.vdb.internal.TranslatorImpl;
import org.komodo.relational.vdb.internal.VdbImpl;
import org.komodo.relational.vdb.internal.VdbImportImpl;
import org.komodo.spi.KException;
import org.komodo.spi.repository.KomodoObject;
import org.komodo.spi.repository.Repository;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.komodo.spi.repository.Repository.UnitOfWork.State;
import org.komodo.utils.ArgCheck;
import org.komodo.utils.StringUtils;
import org.teiid.modeshape.sequencer.dataservice.lexicon.DataVirtLexicon;
import org.teiid.modeshape.sequencer.ddl.StandardDdlLexicon;
import org.teiid.modeshape.sequencer.ddl.TeiidDdlLexicon.Constraint;
import org.teiid.modeshape.sequencer.ddl.TeiidDdlLexicon.CreateProcedure;
import org.teiid.modeshape.sequencer.ddl.TeiidDdlLexicon.CreateTable;
import org.teiid.modeshape.sequencer.ddl.TeiidDdlLexicon.SchemaElement;
import org.teiid.modeshape.sequencer.vdb.lexicon.VdbLexicon;

/**
 * A factory for {@link RelationalObject relational model objects}.
 */
public final class RelationalModelFactory {

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param parentTable
     *        the parent of the model object being created (cannot be <code>null</code>)
     * @param accessPatternName
     *        the name of the access pattern to create (cannot be empty)
     * @return the access pattern model object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static AccessPattern createAccessPattern( final UnitOfWork transaction,
                                                     final Repository repository,
                                                     final Table parentTable,
                                                     final String accessPatternName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotNull( parentTable, "parentTable" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( accessPatternName, "accessPatternName" ); //$NON-NLS-1$

        final KomodoObject kobject = repository.add( transaction, parentTable.getAbsolutePath(), accessPatternName, null );
        kobject.addDescriptor( transaction, Constraint.TABLE_ELEMENT );
        kobject.setProperty( transaction, Constraint.TYPE, AccessPattern.CONSTRAINT_TYPE.toValue() );

        final AccessPattern result = new AccessPatternImpl( transaction, repository, kobject.getAbsolutePath() );
        return result;
    }

    /**
     * Wraps the given exception in a {@link KException}
     *
     * @param e
     *        the exception
     * @return return a {@link KException} from the given {@link Exception}
     */
    public static KException handleError( final Exception e ) {
        assert ( e != null );

        if ( e instanceof KException ) {
            return ( KException )e;
        }

        return new KException( e );
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param table
     *        the table where the column is being created (cannot be <code>null</code>)
     * @param columnName
     *        the name of the column to create (cannot be empty)
     * @return the column model object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static Column createColumn( final UnitOfWork transaction,
                                       final Repository repository,
                                       final Table table,
                                       final String columnName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotNull( table, "table" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( columnName, "columnName" ); //$NON-NLS-1$

        final KomodoObject kobject = repository.add( transaction, table.getAbsolutePath(), columnName, null );
        kobject.addDescriptor( transaction, CreateTable.TABLE_ELEMENT );

        final Column result = new ColumnImpl( transaction, repository, kobject.getAbsolutePath() );
        return result;
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param parentPermission
     *        the permission where the condition model object is being created (cannot be <code>null</code>)
     * @param conditionName
     *        the name of the condition to create (cannot be empty)
     * @return the condition model object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static Condition createCondition( final UnitOfWork transaction,
                                             final Repository repository,
                                             final Permission parentPermission,
                                             final String conditionName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotNull( parentPermission, "parentPermission" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( conditionName, "conditionName" ); //$NON-NLS-1$

        try {
            final KomodoObject grouping = RepositoryTools.findOrCreateChild( transaction,
                                                                             parentPermission,
                                                                             VdbLexicon.DataRole.Permission.CONDITIONS,
                                                                             VdbLexicon.DataRole.Permission.CONDITIONS );
            final KomodoObject kobject = grouping.addChild( transaction,
                                                            conditionName,
                                                            VdbLexicon.DataRole.Permission.Condition.CONDITION );
            final Condition result = new ConditionImpl( transaction, repository, kobject.getAbsolutePath() );
            return result;
        } catch ( final Exception e ) {
            throw handleError( e );
        }
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> and must have a state of {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param dataService
     *        the data service where the connection entry is being created (cannot be <code>null</code>)
     * @param connectionEntryName
     *        the name of the connection entry to create (cannot be empty)
     * @return the connection entry model object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static ConnectionEntry createConnectionEntry( final UnitOfWork transaction,
                                                         final Repository repository,
                                                         final Dataservice dataService,
                                                         final String connectionEntryName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotNull( dataService, "dataService" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( connectionEntryName, "connectionEntryName" ); //$NON-NLS-1$

        final KomodoObject kobject = repository.add( transaction,
                                                     dataService.getAbsolutePath(),
                                                     connectionEntryName,
                                                     DataVirtLexicon.ConnectionEntry.NODE_TYPE );
        final ConnectionEntry result = new ConnectionEntryImpl( transaction, repository, kobject.getAbsolutePath() );
        return result;
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param parentVdb
     *        the VDB where the data role model object is being created (cannot be <code>null</code>)
     * @param dataRoleName
     *        the name of the data role to create (cannot be empty)
     * @return the data role model object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static DataRole createDataRole( final UnitOfWork transaction,
                                           final Repository repository,
                                           final Vdb parentVdb,
                                           final String dataRoleName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotNull( parentVdb, "parentVdb" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( dataRoleName, "dataRoleName" ); //$NON-NLS-1$

        try {
            final KomodoObject grouping = RepositoryTools.findOrCreateChild( transaction,
                                                                             parentVdb,
                                                                             VdbLexicon.Vdb.DATA_ROLES,
                                                                             VdbLexicon.Vdb.DATA_ROLES );
            final KomodoObject kobject = grouping.addChild( transaction, dataRoleName, VdbLexicon.DataRole.DATA_ROLE );
            final DataRole result = new DataRoleImpl( transaction, repository, kobject.getAbsolutePath() );
            return result;
        } catch ( final Exception e ) {
            throw handleError( e );
        }
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param parentWorkspacePath
     *        the parent path (can be empty)
     * @param serviceName
     *        the name of the dataservice fragment to create (cannot be empty)
     * @return the Dataservice model object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static Dataservice createDataservice( final UnitOfWork transaction,
                                                 final Repository repository,
                                                 final String parentWorkspacePath,
                                                 final String serviceName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( serviceName, "serviceName" ); //$NON-NLS-1$

        // make sure path is in the library
        String parentPath = parentWorkspacePath;
        final String workspacePath = repository.komodoWorkspace( transaction ).getAbsolutePath();

        if ( StringUtils.isBlank( parentWorkspacePath ) ) {
            parentPath = workspacePath;
        } else if ( !parentPath.startsWith( workspacePath ) ) {
            parentPath = ( workspacePath + parentPath );
        }

        final KomodoObject kobject = repository.add( transaction, parentPath, serviceName, DataVirtLexicon.DataService.NODE_TYPE );
        final Dataservice result = new DataserviceImpl( transaction, repository, kobject.getAbsolutePath() );
        return result;
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param parentProcedure
     *        the procedure where the procedure result set model object is being created (cannot be <code>null</code>)
     * @return the procedure result set model object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static DataTypeResultSet createDataTypeResultSet( final UnitOfWork transaction,
                                                             final Repository repository,
                                                             final AbstractProcedure parentProcedure ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotNull( parentProcedure, "parentProcedure" ); //$NON-NLS-1$

        final KomodoObject kobject = repository.add( transaction,
                                                     parentProcedure.getAbsolutePath(),
                                                     CreateProcedure.RESULT_SET,
                                                     null );
        kobject.addDescriptor( transaction, CreateProcedure.RESULT_DATA_TYPE );
        final DataTypeResultSet result = new DataTypeResultSetImpl( transaction, repository, kobject.getAbsolutePath() );
        return result;
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param parentVdb
     *        the VDB where the entry model object is being created (cannot be <code>null</code>)
     * @param entryName
     *        the name of the VDB entry to create (cannot be empty)
     * @param entryPath
     *        the imported VDB path (cannot be empty)
     * @return the VDB entry model object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static Entry createEntry( final UnitOfWork transaction,
                                     final Repository repository,
                                     final Vdb parentVdb,
                                     final String entryName,
                                     final String entryPath ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotNull( parentVdb, "parentVdb" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( entryName, "entryName" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( entryPath, "entryPath" ); //$NON-NLS-1$

        try {
            final KomodoObject grouping = RepositoryTools.findOrCreateChild( transaction,
                                                                             parentVdb,
                                                                             VdbLexicon.Vdb.ENTRIES,
                                                                             VdbLexicon.Vdb.ENTRIES );
            final KomodoObject kobject = grouping.addChild( transaction, entryName, VdbLexicon.Entry.ENTRY );
            final Entry result = new EntryImpl( transaction, repository, kobject.getAbsolutePath() );
            result.setPath( transaction, entryPath );
            return result;
        } catch ( final Exception e ) {
            throw handleError( e );
        }
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param parentTable
     *        the table model object where the foreign key is being created (cannot be <code>null</code>)
     * @param foreignKeyName
     *        the name of the foreign key to create (cannot be empty)
     * @param tableReference
     *        the table referenced by this constraint (cannot be <code>null</code>)
     * @return the foreign key model object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static ForeignKey createForeignKey( final UnitOfWork transaction,
                                               final Repository repository,
                                               final Table parentTable,
                                               final String foreignKeyName,
                                               final Table tableReference ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotNull( parentTable, "parentTable" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( foreignKeyName, "foreignKeyName" ); //$NON-NLS-1$
        ArgCheck.isNotNull( tableReference, "tableReference" ); //$NON-NLS-1$

        final KomodoObject kobject = repository.add( transaction, parentTable.getAbsolutePath(), foreignKeyName, null );
        kobject.addDescriptor( transaction, Constraint.FOREIGN_KEY_CONSTRAINT );
        kobject.setProperty( transaction, Constraint.TYPE, ForeignKey.CONSTRAINT_TYPE.toValue() );

        final ForeignKey fk = new ForeignKeyImpl( transaction, repository, kobject.getAbsolutePath() );
        fk.setReferencesTable( transaction, tableReference );
        return fk;
    }

    /**
     * This is Teiid's <code>Create Foreign Function</code> command.
     *
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param parentModel
     *        the model where the function is being created (cannot be <code>null</code>)
     * @param functionName
     *        the name of the function to create (cannot be empty)
     * @return the function model object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static PushdownFunction createPushdownFunction( final UnitOfWork transaction,
                                                           final Repository repository,
                                                           final Model parentModel,
                                                           final String functionName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotNull( parentModel, "parentModel" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( functionName, "functionName" ); //$NON-NLS-1$

        final KomodoObject kobject = repository.add( transaction, parentModel.getAbsolutePath(), functionName, null );
        kobject.addDescriptor( transaction, CreateProcedure.FUNCTION_STATEMENT );
        kobject.setProperty( transaction, SchemaElement.TYPE, SchemaElementType.FOREIGN.name() );
        setCreateStatementProperties( transaction, kobject );

        final PushdownFunction result = new PushdownFunctionImpl( transaction, repository, kobject.getAbsolutePath() );
        return result;
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param parentTable
     *        the table where the index model object is being created (cannot be <code>null</code>)
     * @param indexName
     *        the name of the index to create (cannot be empty)
     * @return the index model object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static Index createIndex( final UnitOfWork transaction,
                                     final Repository repository,
                                     final Table parentTable,
                                     final String indexName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotNull( parentTable, "parentTable" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( indexName, "indexName" ); //$NON-NLS-1$

        final KomodoObject kobject = repository.add( transaction, parentTable.getAbsolutePath(), indexName, null );
        kobject.addDescriptor( transaction, Constraint.INDEX_CONSTRAINT );
        kobject.setProperty( transaction, Constraint.TYPE, Index.CONSTRAINT_TYPE.toValue() );

        final Index index = new IndexImpl( transaction, repository, kobject.getAbsolutePath() );
        return index;
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param parentPermission
     *        the permission where the mask model object is being created (cannot be <code>null</code>)
     * @param maskName
     *        the name of the mask to create (cannot be empty)
     * @return the mask model object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static Mask createMask( final UnitOfWork transaction,
                                   final Repository repository,
                                   final Permission parentPermission,
                                   final String maskName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotNull( parentPermission, "parentPermission" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( maskName, "maskName" ); //$NON-NLS-1$

        try {
            final KomodoObject grouping = RepositoryTools.findOrCreateChild( transaction,
                                                                             parentPermission,
                                                                             VdbLexicon.DataRole.Permission.MASKS,
                                                                             VdbLexicon.DataRole.Permission.MASKS );
            final KomodoObject kobject = grouping.addChild( transaction, maskName, VdbLexicon.DataRole.Permission.Mask.MASK );
            final Mask result = new MaskImpl( transaction, repository, kobject.getAbsolutePath() );
            return result;
        } catch ( final Exception e ) {
            throw handleError( e );
        }
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param vdb
     *        the VDB where the model is being created (cannot be <code>null</code>)
     * @param modelName
     *        the name of the model to create (cannot be empty)
     * @return the VDB import model object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static Model createModel( final UnitOfWork transaction,
                                     final Repository repository,
                                     final Vdb vdb,
                                     final String modelName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotNull( vdb, "vdb" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( modelName, "modelName" ); //$NON-NLS-1$

        final KomodoObject kobject = vdb.addChild( transaction, modelName, VdbLexicon.Vdb.DECLARATIVE_MODEL );
        final Model result = new ModelImpl( transaction, repository, kobject.getAbsolutePath() );

        return result;
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param parentModel
     *        the model where the model source is being created (cannot be <code>null</code>)
     * @param sourceName
     *        the name of the model source to create (cannot be empty)
     * @return the model source object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static ModelSource createModelSource( final UnitOfWork transaction,
                                                 final Repository repository,
                                                 final Model parentModel,
                                                 final String sourceName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotNull( parentModel, "parentModel" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( sourceName, "sourceName" ); //$NON-NLS-1$

        try {
            final KomodoObject grouping = RepositoryTools.findOrCreateChild( transaction,
                                                                             parentModel,
                                                                             VdbLexicon.Vdb.SOURCES,
                                                                             VdbLexicon.Vdb.SOURCES );
            final KomodoObject kobject = grouping.addChild( transaction, sourceName, VdbLexicon.Source.SOURCE );
            final ModelSource result = new ModelSourceImpl( transaction, repository, kobject.getAbsolutePath() );
            return result;
        } catch ( final Exception e ) {
            throw handleError( e );
        }
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param parentProcedure
     *        the procedure where the parameter model object is being created (cannot be <code>null</code>)
     * @param parameterName
     *        the name of the parameter to create (cannot be empty)
     * @return the parameter model object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static Parameter createParameter( final UnitOfWork transaction,
                                             final Repository repository,
                                             final AbstractProcedure parentProcedure,
                                             final String parameterName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotNull( parentProcedure, "parentProcedure" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( parameterName, "parameterName" ); //$NON-NLS-1$

        try {
            final KomodoObject kobject = repository.add( transaction, parentProcedure.getAbsolutePath(), parameterName, null );
            kobject.addDescriptor( transaction, CreateProcedure.PARAMETER );

            final Parameter result = new ParameterImpl( transaction, repository, kobject.getAbsolutePath() );
            return result;
        } catch ( final Exception e ) {
            throw handleError( e );
        }
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param parentDataRole
     *        the data role where the permission model object is being created (cannot be <code>null</code>)
     * @param permissionName
     *        the name of the permission to create (cannot be empty)
     * @return the permission model object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static Permission createPermission( final UnitOfWork transaction,
                                               final Repository repository,
                                               final DataRole parentDataRole,
                                               final String permissionName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotNull( parentDataRole, "parentDataRole" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( permissionName, "permissionName" ); //$NON-NLS-1$

        try {
            final KomodoObject grouping = RepositoryTools.findOrCreateChild( transaction,
                                                                             parentDataRole,
                                                                             VdbLexicon.DataRole.PERMISSIONS,
                                                                             VdbLexicon.DataRole.PERMISSIONS );
            final KomodoObject kobject = grouping.addChild( transaction,
                                                            permissionName,
                                                            VdbLexicon.DataRole.Permission.PERMISSION );
            final Permission result = new PermissionImpl( transaction, repository, kobject.getAbsolutePath() );
            return result;
        } catch ( final Exception e ) {
            throw handleError( e );
        }
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param parentTable
     *        the parent of the model object being created (cannot be <code>null</code>)
     * @param primaryKeyName
     *        the name of the primary key to create (cannot be empty)
     * @return the primary key model object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static PrimaryKey createPrimaryKey( final UnitOfWork transaction,
                                               final Repository repository,
                                               final Table parentTable,
                                               final String primaryKeyName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotNull( parentTable, "parentTable" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( primaryKeyName, "primaryKeyName" ); //$NON-NLS-1$

        final KomodoObject kobject = repository.add( transaction, parentTable.getAbsolutePath(), primaryKeyName, null );
        kobject.addDescriptor( transaction, Constraint.TABLE_ELEMENT );
        kobject.setProperty( transaction, Constraint.TYPE, PrimaryKey.CONSTRAINT_TYPE.toValue() );

        final PrimaryKey result = new PrimaryKeyImpl( transaction, repository, kobject.getAbsolutePath() );
        return result;
    }

    /**
     * This is Teiid's <code>Create Foreign Procedure</code> command.
     *
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param parentModel
     *        the model where the procedure is being created (cannot be <code>null</code>)
     * @param procedureName
     *        the name of the procedure to create (cannot be empty)
     * @return the procedure model object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static StoredProcedure createStoredProcedure( final UnitOfWork transaction,
                                                         final Repository repository,
                                                         final Model parentModel,
                                                         final String procedureName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotNull( parentModel, "parentModel" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( procedureName, "procedureName" ); //$NON-NLS-1$

        final KomodoObject kobject = repository.add( transaction, parentModel.getAbsolutePath(), procedureName, null );
        kobject.addDescriptor( transaction, CreateProcedure.PROCEDURE_STATEMENT );
        kobject.setProperty( transaction, SchemaElement.TYPE, SchemaElementType.FOREIGN.name() );
        setCreateStatementProperties( transaction, kobject );

        final StoredProcedure result = new StoredProcedureImpl( transaction, repository, kobject.getAbsolutePath() );
        return result;
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param resultSet
     *        the tabular result set where the column is being created (cannot be <code>null</code>)
     * @param columnName
     *        the name of the column to create (cannot be empty)
     * @return the result set column model object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static ResultSetColumn createResultSetColumn( final UnitOfWork transaction,
                                                         final Repository repository,
                                                         final TabularResultSet resultSet,
                                                         final String columnName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotNull( resultSet, "resultSet" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( columnName, "columnName" ); //$NON-NLS-1$

        final KomodoObject kobject = repository.add( transaction, resultSet.getAbsolutePath(), columnName, null );
        kobject.addDescriptor( transaction, CreateProcedure.RESULT_COLUMN );

        final ResultSetColumn result = new ResultSetColumnImpl( transaction, repository, kobject.getAbsolutePath() );
        return result;
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param parentWorkspacePath
     *        the parent path (can be empty)
     * @param schemaName
     *        the name of the schema fragment to create (cannot be empty)
     * @return the Schema model object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static Schema createSchema( final UnitOfWork transaction,
                                       final Repository repository,
                                       final String parentWorkspacePath,
                                       final String schemaName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( schemaName, "schemaName" ); //$NON-NLS-1$

        // make sure path is in the library
        String parentPath = parentWorkspacePath;
        final String workspacePath = repository.komodoWorkspace( transaction ).getAbsolutePath();

        if ( StringUtils.isBlank( parentWorkspacePath ) ) {
            parentPath = workspacePath;
        } else if ( !parentPath.startsWith( workspacePath ) ) {
            parentPath = ( workspacePath + parentPath );
        }

        final KomodoObject kobject = repository.add( transaction, parentPath, schemaName, KomodoLexicon.Schema.NODE_TYPE );
        final Schema result = new SchemaImpl( transaction, repository, kobject.getAbsolutePath() );
        return result;
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> and must have a state of {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param dataService
     *        the data service where the VDB file entry is being created (cannot be <code>null</code>)
     * @param serviceVdbEntryName
     *        the name of the service VDB entry to create (cannot be empty)
     * @return the service VDB entry model object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static ServiceVdbEntry createServiceVdbEntry( final UnitOfWork transaction,
                                                         final Repository repository,
                                                         final Dataservice dataService,
                                                         final String serviceVdbEntryName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotNull( dataService, "dataService" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( serviceVdbEntryName, "serviceVdbEntryName" ); //$NON-NLS-1$

        final KomodoObject kobject = repository.add( transaction,
                                                     dataService.getAbsolutePath(),
                                                     serviceVdbEntryName,
                                                     DataVirtLexicon.ServiceVdbEntry.NODE_TYPE );
        final ServiceVdbEntry result = new ServiceVdbEntryImpl( transaction, repository, kobject.getAbsolutePath() );
        return result;
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param optionContainer
     *        the parent where the option is being created (can be empty if created at the root of the workspace)
     * @param optionName
     *        the name of the statement option to create (cannot be empty)
     * @param optionValue
     *        the option value (cannot be empty)
     * @return the statement option model object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static StatementOption createStatementOption( final UnitOfWork transaction,
                                                         final Repository repository,
                                                         final OptionContainer optionContainer,
                                                         final String optionName,
                                                         final String optionValue ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotNull( optionContainer, "optionContainer" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( optionName, "optionName" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( optionValue, "optionValue" ); //$NON-NLS-1$

        final KomodoObject kobject = repository.add( transaction, optionContainer.getAbsolutePath(), optionName, null );
        kobject.addDescriptor( transaction, StandardDdlLexicon.TYPE_STATEMENT_OPTION );

        final StatementOption result = new StatementOptionImpl( transaction, repository, kobject.getAbsolutePath() );
        result.setOption( transaction, optionValue );
        return result;
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param parentModel
     *        the model where the table is being created (cannot be <code>null</code>)
     * @param tableName
     *        the name of the table to create (cannot be empty)
     * @return the table model object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static Table createTable( final UnitOfWork transaction,
                                     final Repository repository,
                                     final Model parentModel,
                                     final String tableName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotNull( parentModel, "parentModel" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( tableName, "tableName" ); //$NON-NLS-1$

        final KomodoObject kobject = repository.add( transaction, parentModel.getAbsolutePath(), tableName, null );
        kobject.addDescriptor( transaction, CreateTable.TABLE_STATEMENT );
        setCreateStatementProperties( transaction, kobject );

        final Table result = new TableImpl( transaction, repository, kobject.getAbsolutePath() );
        return result;
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param parentProcedure
     *        the procedure where the procedure result set model object is being created (cannot be <code>null</code>)
     * @return the procedure result set model object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static TabularResultSet createTabularResultSet( final UnitOfWork transaction,
                                                           final Repository repository,
                                                           final AbstractProcedure parentProcedure ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotNull( parentProcedure, "parentProcedure" ); //$NON-NLS-1$

        final KomodoObject kobject = repository.add( transaction,
                                                     parentProcedure.getAbsolutePath(),
                                                     CreateProcedure.RESULT_SET,
                                                     null );
        kobject.addDescriptor( transaction, CreateProcedure.RESULT_COLUMNS );
        final TabularResultSet result = new TabularResultSetImpl( transaction, repository, kobject.getAbsolutePath() );
        return result;
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param parentVdb
     *        the VDB where the VDB import model object is being created (cannot be <code>null</code>)
     * @param translatorName
     *        the name of the VDB translator to create (cannot be empty)
     * @param translatorType
     *        the type of translator (cannot be empty)
     * @return the VDB translator model object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static Translator createTranslator( final UnitOfWork transaction,
                                               final Repository repository,
                                               final Vdb parentVdb,
                                               final String translatorName,
                                               final String translatorType ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotNull( parentVdb, "parentVdb" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( translatorName, "translatorName" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( translatorType, "translatorType" ); //$NON-NLS-1$

        try {
            final KomodoObject grouping = RepositoryTools.findOrCreateChild( transaction,
                                                                             parentVdb,
                                                                             VdbLexicon.Vdb.TRANSLATORS,
                                                                             VdbLexicon.Vdb.TRANSLATORS );
            final KomodoObject kobject = grouping.addChild( transaction, translatorName, VdbLexicon.Translator.TRANSLATOR );
            final Translator result = new TranslatorImpl( transaction, grouping.getRepository(), kobject.getAbsolutePath() );
            result.setType( transaction, translatorType );
            return result;
        } catch ( final Exception e ) {
            throw handleError( e );
        }
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param parent
     *        the parent of the template created (cannot be <code>null</code>)
     * @param templateName
     *        the name of the template to create (cannot be empty)
     *
     * @return the Template object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static Template createTemplate( final UnitOfWork transaction,
                                               final Repository repository,
                                               final KomodoObject parent,
                                               final String templateName) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotNull( parent, "parent" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( templateName, "templateName" ); //$NON-NLS-1$

        try {
            final KomodoObject kobject = parent.addChild( transaction, templateName, DataVirtLexicon.Template.NODE_TYPE);
            final Template result = new TemplateImpl(transaction, repository, kobject.getAbsolutePath());
            return result;
        } catch ( final Exception e ) {
            throw handleError( e );
        }
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param parent
     *        the Template where the templateEntry is being created (cannot be <code>null</code>)
     * @param entryName
     *        the name of the templateEntry to create (cannot be empty)
     *
     * @return the TemplateEntry object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static TemplateEntry createTemplateEntry( final UnitOfWork transaction,
                                               final Repository repository,
                                               final Template parent,
                                               final String entryName) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotNull( parent, "parent" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( entryName, "templateName" ); //$NON-NLS-1$

        try {
            final KomodoObject kobject = parent.addChild( transaction, entryName, DataVirtLexicon.TemplateEntry.NODE_TYPE);
            final TemplateEntry result = new TemplateEntryImpl(transaction, repository, kobject.getAbsolutePath());
            return result;
        } catch ( final Exception e ) {
            throw handleError( e );
        }
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param parentTable
     *        the parent of the model object being created (cannot be <code>null</code>)
     * @param uniqueConstraintName
     *        the name of the unique constraint to create (cannot be empty)
     * @return the unique constraint model object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static UniqueConstraint createUniqueConstraint( final UnitOfWork transaction,
                                                           final Repository repository,
                                                           final Table parentTable,
                                                           final String uniqueConstraintName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotNull( parentTable, "parentTable" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( uniqueConstraintName, "uniqueConstraintName" ); //$NON-NLS-1$

        final KomodoObject kobject = repository.add( transaction, parentTable.getAbsolutePath(), uniqueConstraintName, null );
        kobject.addDescriptor( transaction, Constraint.TABLE_ELEMENT );
        kobject.setProperty( transaction, Constraint.TYPE, UniqueConstraint.CONSTRAINT_TYPE.toValue() );

        final UniqueConstraint result = new UniqueConstraintImpl( transaction, repository, kobject.getAbsolutePath() );
        return result;
    }

    /**
     * This is Teiid's <code>Create Virtual Function</code> command.
     *
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param parentModel
     *        the model where the function is being created (cannot be <code>null</code>)
     * @param functionName
     *        the name of the function to create (cannot be empty)
     * @return the function model object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static UserDefinedFunction createUserDefinedFunction( final UnitOfWork transaction,
                                                                 final Repository repository,
                                                                 final Model parentModel,
                                                                 final String functionName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotNull( parentModel, "parentModel" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( functionName, "functionName" ); //$NON-NLS-1$

        final KomodoObject kobject = repository.add( transaction, parentModel.getAbsolutePath(), functionName, null );
        kobject.addDescriptor( transaction, CreateProcedure.FUNCTION_STATEMENT );
        kobject.setProperty( transaction, SchemaElement.TYPE, SchemaElementType.VIRTUAL.name() );
        setCreateStatementProperties( transaction, kobject );

        final UserDefinedFunction result = new UserDefinedFunctionImpl( transaction, repository, kobject.getAbsolutePath() );
        return result;
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param parentWorkspacePath
     *        the parent path (can be empty)
     * @param vdbName
     *        the name of the VDB to create (cannot be empty)
     * @param externalFilePath
     *        the VDB file path on the local file system (cannot be empty)
     * @return the VDB model object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static Vdb createVdb( final UnitOfWork transaction,
                                 final Repository repository,
                                 final String parentWorkspacePath,
                                 final String vdbName,
                                 final String externalFilePath ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( vdbName, "vdbName" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( externalFilePath, "externalFilePath" ); //$NON-NLS-1$

        // make sure path is in the library
        String parentPath = parentWorkspacePath;
        final String workspacePath = repository.komodoWorkspace( transaction ).getAbsolutePath();

        if ( StringUtils.isBlank( parentWorkspacePath ) ) {
            parentPath = workspacePath;
        } else if ( !parentPath.startsWith( workspacePath ) ) {
            parentPath = ( workspacePath + parentPath );
        }

        final KomodoObject kobject = repository.add( transaction, parentPath, vdbName, VdbLexicon.Vdb.VIRTUAL_DATABASE );
        final Vdb result = new VdbImpl( transaction, repository, kobject.getAbsolutePath() );
        result.setOriginalFilePath( transaction, externalFilePath );
        result.setVdbName( transaction, vdbName );
        return result;
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param container
     *        the container where the VDB file entry is being created (cannot be <code>null</code>)
     * @param vdbEntryName
     *        the name of the VDB entry to create (cannot be empty)
     * @return the VDB entry model object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static VdbEntry createVdbEntry( final UnitOfWork transaction,
                                           final Repository repository,
                                           final VdbEntryContainer container,
                                           final String vdbEntryName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotNull( container, "container" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( vdbEntryName, "vdbEntryName" ); //$NON-NLS-1$

        final KomodoObject kobject = repository.add( transaction,
                                                     container.getAbsolutePath(),
                                                     vdbEntryName,
                                                     DataVirtLexicon.VdbEntry.NODE_TYPE );
        final VdbEntry result = new VdbEntryImpl( transaction, repository, kobject.getAbsolutePath() );
        return result;
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param parentVdb
     *        the VDB where the VDB import model object is being created (cannot be <code>null</code>)
     * @param vdbName
     *        the name of the VDB import to create (cannot be empty)
     * @return the VDB import model object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static VdbImport createVdbImport( final UnitOfWork transaction,
                                             final Repository repository,
                                             final Vdb parentVdb,
                                             final String vdbName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotNull( parentVdb, "parentVdb" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( vdbName, "vdbName" ); //$NON-NLS-1$

        try {
            final KomodoObject grouping = RepositoryTools.findOrCreateChild( transaction,
                                                                             parentVdb,
                                                                             VdbLexicon.Vdb.IMPORT_VDBS,
                                                                             VdbLexicon.Vdb.IMPORT_VDBS );
            final KomodoObject kobject = grouping.addChild( transaction, vdbName, VdbLexicon.ImportVdb.IMPORT_VDB );
            final VdbImport result = new VdbImportImpl( transaction, repository, kobject.getAbsolutePath() );
            result.setVersion( transaction, Vdb.DEFAULT_VERSION );
            return result;
        } catch ( final Exception e ) {
            throw handleError( e );
        }
    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param parentModel
     *        the model where the view is being created (cannot be <code>null</code>)
     * @param viewName
     *        the name of the view to create (cannot be empty)
     * @return the view model object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static View createView( final UnitOfWork transaction,
                                   final Repository repository,
                                   final Model parentModel,
                                   final String viewName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotNull( parentModel, "parentModel" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( viewName, "viewName" ); //$NON-NLS-1$

        final KomodoObject kobject = repository.add( transaction, parentModel.getAbsolutePath(), viewName, null );
        kobject.addDescriptor( transaction, CreateTable.VIEW_STATEMENT );
        setCreateStatementProperties( transaction, kobject );

        final View result = new ViewImpl( transaction, repository, kobject.getAbsolutePath() );
        return result;
    }

    /**
     *
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param profile
     *        the parent profile object
     * @param stateId
     *        the id of the view editor state
     * @return the view editor state object
     * @throws KException
     *        if an error occurs
     */
    public static ViewEditorState createViewEditorState(UnitOfWork transaction, Repository repository, Profile profile,
                                                                                                   String stateId) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotNull( stateId, "stateId" ); //$NON-NLS-1$

        try {
            final KomodoObject grouping = RepositoryTools.findOrCreateChild( transaction,
                                                                             profile,
                                                                             KomodoLexicon.Profile.VIEW_EDITOR_STATES,
                                                                             KomodoLexicon.Profile.VIEW_EDITOR_STATES );
            final KomodoObject kobject = grouping.addChild( transaction, stateId, KomodoLexicon.ViewEditorState.NODE_TYPE );
            final ViewEditorState result = new ViewEditorStateImpl( transaction, repository, kobject.getAbsolutePath() );
            return result;
        } catch ( final Exception e ) {
            throw handleError( e );
        }
    }
    
    /**
    *
    * @param transaction
    *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
    * @param repository
    *        the repository where the model object will be created (cannot be <code>null</code>)
    * @param profile
    *        the parent profile object
    * @param stateId
    *        the id of the view definition
    * @return the view definition object
    * @throws KException
    *        if an error occurs
    */
   public static ViewDefinition createViewDefinition(UnitOfWork transaction, Repository repository, ViewEditorState viewEditorState) throws KException {
       ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
       ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
       ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$

       try {
   		   // If a ViewDefinition already exists, remove it first
           final ViewDefinition viewDefn = viewEditorState.getViewDefinition(transaction);
           if (viewDefn != null) {
           	viewDefn.remove(transaction);
           }

    	   final KomodoObject kobject = viewEditorState.addChild(transaction, KomodoLexicon.ViewEditorState.VIEW_DEFINITION, KomodoLexicon.ViewEditorState.VIEW_DEFINITION);
           final ViewDefinition result = new ViewDefinitionImpl( transaction, repository, kobject.getAbsolutePath() );
           return result;
       } catch ( final Exception e ) {
           throw handleError( e );
       }
   }
   
   /**
   *
   * @param transaction
   *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
   * @param repository
   *        the repository where the model object will be created (cannot be <code>null</code>)
   * @param viewDefinition
   *        the parent view definition
   * @param compositionName
   *        the sql composition name
   * @return the sql composition object
   * @throws KException
   *        if an error occurs
   */
	public static SqlComposition createSqlComposition(UnitOfWork transaction, 
			                                          Repository repository, 
			                                          ViewDefinition viewDefinition, 
			                                          String compositionName) throws KException {
		
	       ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
	       ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
	       ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
	       ArgCheck.isNotNull( compositionName, "compositionName" ); //$NON-NLS-1$
	       
	       try {
	           final KomodoObject grouping = RepositoryTools.findOrCreateChild( transaction, viewDefinition, KomodoLexicon.ViewDefinition.SQL_COMPOSITIONS,
	                                                                                                         KomodoLexicon.ViewDefinition.SQL_COMPOSITIONS );
	           final KomodoObject kobject = grouping.addChild( transaction, compositionName, KomodoLexicon.SqlComposition.NODE_TYPE );
	           final SqlComposition result = new SqlCompositionImpl( transaction, repository, kobject.getAbsolutePath() );
	           return result;
	       } catch ( final Exception e ) {
	           throw handleError( e );
	       }
	}

	/**
	 *
	 * @param transaction
	 *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
	 * @param repository
	 *        the repository where the model object will be created (cannot be <code>null</code>)
	 * @param viewDefinition
	 *        the parent view definition
	 * @param columnName
	 *        the sql projected column name
	 * @return the sql projected column object
	 * @throws KException
	 *        if an error occurs
	 */
	public static SqlProjectedColumn createSqlProjectedColumn(UnitOfWork transaction, 
	                                                          Repository repository, 
	                                                          ViewDefinition viewDefinition, 
	                                                          String columnName) throws KException {

	    ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
	    ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
	    ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
	    ArgCheck.isNotNull( columnName, "columnName" ); //$NON-NLS-1$

	    try {
	        final KomodoObject grouping = RepositoryTools.findOrCreateChild( transaction, viewDefinition, KomodoLexicon.ViewDefinition.SQL_PROJECTED_COLUMNS,
	                                                                         KomodoLexicon.ViewDefinition.SQL_PROJECTED_COLUMNS );
	        final KomodoObject kobject = grouping.addChild( transaction, columnName, KomodoLexicon.SqlProjectedColumn.NODE_TYPE );
	        final SqlProjectedColumn result = new SqlProjectedColumnImpl( transaction, repository, kobject.getAbsolutePath() );
	        return result;
	    } catch ( final Exception e ) {
	        throw handleError( e );
	    }
	}

    /**
     *
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param viewEditorState
     *        the parent view editor state object
     * @return the view editor state command object
     * @throws KException
     *        if an error occurs
     */
    public static StateCommandAggregate createStateCommandAggregate (UnitOfWork transaction, Repository repository,
                                                                     ViewEditorState viewEditorState) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$

        try {
            int cmdSize = viewEditorState.getCommands(transaction).length;
            int index = cmdSize > 0 ? cmdSize - 1 : 0;
            String aggName = KomodoLexicon.StateCommandAggregate.NAME_PREFIX + index;
            final KomodoObject kobject = viewEditorState.addChild(transaction, aggName,
                                                                  KomodoLexicon.StateCommandAggregate.NODE_TYPE);
            final StateCommandAggregate result = new StateCommandAggregateImpl(transaction, repository, kobject.getAbsolutePath());
            return result;
        } catch ( final Exception e ) {
            throw handleError( e );
        }
    }

    /**
    *
    * @param transaction
    *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
    * @param repository
    *        the repository where the model object will be created (cannot be <code>null</code>)
    * @param stateCommandAgg
    *        the parent state command aggregate
    * @param stateCommandType
    *        the state command type (either 'undo' or 'redo')
    * @param commandId
    *        the id of the command
    * @param arguments
    *        the command arguments
    * @return the view editor state command object
    * @throws KException
    *        if an error occurs
    */
   public static StateCommand createStateCommand(UnitOfWork transaction, Repository repository,
                                                 StateCommandAggregate stateCommandAgg,
                                                 String stateCommandType,
                                                 String commandId,
                                                 Map<String, String> arguments) throws KException {
       ArgCheck.isNotNull(transaction, "transaction"); //$NON-NLS-1$
       ArgCheck.isTrue((transaction.getState() == State.NOT_STARTED), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
       ArgCheck.isNotNull(repository, "repository"); //$NON-NLS-1$
       ArgCheck.isNotNull(commandId, "commandId"); //$NON-NLS-1$
       ArgCheck.isNotNull(arguments, "arguments"); //$NON-NLS-1$

       
       try {
           if (stateCommandAgg.hasChild(transaction, stateCommandType))
               stateCommandAgg.removeChild(transaction, stateCommandType);

           KomodoObject stateCmdObject = stateCommandAgg.addChild(transaction, stateCommandType,
                                                                              KomodoLexicon.StateCommand.NODE_TYPE);
           StateCommand stateCmd = StateCommand.RESOLVER.resolve(transaction, stateCmdObject);
           stateCmd.setId(transaction, commandId);
           stateCmd.setArguments(transaction, arguments);
           return stateCmd;
       } catch ( final Exception e ) {
           throw handleError( e );
       }
   }

    /**
     * This is Teiid's <code>Create Virtual Procedure</code> command.
     *
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the model object will be created (cannot be <code>null</code>)
     * @param parentModel
     *        the model where the procedure is being created (cannot be <code>null</code>)
     * @param procedureName
     *        the name of the procedure to create (cannot be empty)
     * @return the procedure model object (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    public static VirtualProcedure createVirtualProcedure( final UnitOfWork transaction,
                                                           final Repository repository,
                                                           final Model parentModel,
                                                           final String procedureName ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state is not NOT_STARTED" ); //$NON-NLS-1$
        ArgCheck.isNotNull( repository, "repository" ); //$NON-NLS-1$
        ArgCheck.isNotNull( parentModel, "parentModel" ); //$NON-NLS-1$
        ArgCheck.isNotEmpty( procedureName, "procedureName" ); //$NON-NLS-1$

        final KomodoObject kobject = repository.add( transaction, parentModel.getAbsolutePath(), procedureName, null );
        kobject.addDescriptor( transaction, CreateProcedure.PROCEDURE_STATEMENT );
        kobject.setProperty( transaction, SchemaElement.TYPE, SchemaElementType.VIRTUAL.name() );
        setCreateStatementProperties( transaction, kobject );

        final VirtualProcedure result = new VirtualProcedureImpl( transaction, repository, kobject.getAbsolutePath() );
        return result;
    }

    private static void setCreateStatementProperties( final UnitOfWork transaction,
                                                      final KomodoObject kobject ) throws KException {
        assert ( transaction != null );

        /*

        - ddl:expression (string) mandatory             // The string fragment encompassing the statement expression.
        - ddl:originalExpression (string)               // The string fragment encompassing the original statement expression.
        - ddl:startLineNumber (long) mandatory          // The starting line number for the statement
        - ddl:startColumnNumber (long) mandatory        // The starting column number for the statement
        - ddl:startCharIndex (long) mandatory           // The starting content character index for the statement
        - ddl:length (long)  mandatory                  // The string length

         */

        kobject.setProperty( transaction, StandardDdlLexicon.DDL_EXPRESSION, "komodo created model" ); //$NON-NLS-1$
        kobject.setProperty( transaction, StandardDdlLexicon.DDL_ORIGINAL_EXPRESSION, "komodo created model" ); //$NON-NLS-1$
        kobject.setProperty( transaction, StandardDdlLexicon.DDL_START_LINE_NUMBER, 0L );
        kobject.setProperty( transaction, StandardDdlLexicon.DDL_START_COLUMN_NUMBER, 0L );
        kobject.setProperty( transaction, StandardDdlLexicon.DDL_START_CHAR_INDEX, 0L );
        kobject.setProperty( transaction, StandardDdlLexicon.DDL_LENGTH, 0L );
    }

    private RelationalModelFactory() {
        // nothing to do
    }
}
