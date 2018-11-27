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
package org.komodo.relational.model.internal;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.komodo.relational.RelationalModelFactory;
import org.komodo.relational.RelationalModelTest;
import org.komodo.relational.RelationalObject.Filter;
import org.komodo.relational.internal.RelationalObjectImpl;
import org.komodo.relational.model.Column;
import org.komodo.relational.model.ForeignKey;
import org.komodo.relational.model.Model;
import org.komodo.relational.model.Table;
import org.komodo.relational.model.TableConstraint;
import org.komodo.relational.vdb.Vdb;
import org.komodo.spi.KException;
import org.komodo.spi.lexicon.ddl.teiid.TeiidDdlLexicon;
import org.komodo.spi.lexicon.ddl.teiid.TeiidDdlLexicon.Constraint;
import org.komodo.spi.repository.KomodoType;

@SuppressWarnings( { "javadoc", "nls" } )
public final class ForeignKeyImplTest extends RelationalModelTest {

    private static final String NAME = "foreignKey";

    private ForeignKey foreignKey;
    private Table parentTable;
    private Table refTable;

    @Before
    public void init() throws Exception {
        final Vdb vdb = createVdb();
        final Model model = createModel();
        this.parentTable = model.addTable( getTransaction(), "parentTable" );

        final Model refModel = vdb.addModel( getTransaction(), "refModel" );
        this.refTable = refModel.addTable( getTransaction(), "refTable" );

        this.foreignKey = this.parentTable.addForeignKey( getTransaction(), NAME, this.refTable );
        commit();
    }

    @Test
    public void shouldAddReferencesColumns() throws Exception {
        final Column columnA = RelationalModelFactory.createColumn( getTransaction(), _repo, this.refTable, "columnRefA" );
        this.foreignKey.addReferencesColumn( getTransaction(), columnA );

        final Column columnB = RelationalModelFactory.createColumn( getTransaction(), _repo, this.refTable, "columnRefB" );
        this.foreignKey.addReferencesColumn( getTransaction(), columnB );

        commit(); // must commit so that query used in getReferencesColumns will work

        assertThat( this.foreignKey.getReferencesColumns( getTransaction() ).length, is( 2 ) );
        assertThat( Arrays.asList( this.foreignKey.getReferencesColumns( getTransaction() ) ), hasItems( columnA, columnB ) );
    }

    @Test
    public void shouldBeChildRestricted() {
        assertThat( this.foreignKey.isChildRestricted(), is( true ) );
    }

    @Test( expected = IllegalArgumentException.class )
    public void shouldFailAddingNullColumn() throws Exception {
        this.foreignKey.addColumn( getTransaction(), null );
    }

    @Test( expected = IllegalArgumentException.class )
    public void shouldFailAddingNullReferencesColumn() throws Exception {
        this.foreignKey.addReferencesColumn( getTransaction(), null );
    }

    @Test
    public void shouldFailConstructionIfNotForeignKey() {
        if ( RelationalObjectImpl.VALIDATE_INITIAL_STATE ) {
            try {
                new ForeignKeyImpl( getTransaction(), _repo, this.parentTable.getAbsolutePath() );
                fail();
            } catch ( final KException e ) {
                // expected
            }
        }
    }

    @Test
    public void shouldHaveCorrectConstraintType() throws Exception {
        assertThat( this.foreignKey.getConstraintType(), is( TableConstraint.ConstraintType.FOREIGN_KEY ) );
        assertThat( this.foreignKey.getRawProperty( getTransaction(), TeiidDdlLexicon.Constraint.TYPE ).getStringValue( getTransaction() ),
                    is( TableConstraint.ConstraintType.FOREIGN_KEY.toValue() ) );
    }

    @Test
    public void shouldHaveCorrectDescriptor() throws Exception {
        assertThat( this.foreignKey.hasDescriptor( getTransaction(), Constraint.FOREIGN_KEY_CONSTRAINT ), is( true ) );
    }

    @Test
    public void shouldHaveCorrectTypeIdentifier() throws Exception {
        assertThat(this.foreignKey.getTypeIdentifier( getTransaction() ), is(KomodoType.FOREIGN_KEY));
    }

    @Test
    public void shouldHaveMoreRawProperties() throws Exception {
        final String[] filteredProps = this.foreignKey.getPropertyNames( getTransaction() );
        final String[] rawProps = this.foreignKey.getRawPropertyNames( getTransaction() );
        assertThat( ( rawProps.length > filteredProps.length ), is( true ) );
    }

    @Test
    public void shouldHaveParentTableAfterConstruction() throws Exception {
        assertThat( this.foreignKey.getParent( getTransaction() ), is( instanceOf( Table.class ) ) );
        assertThat( this.foreignKey.getTable( getTransaction() ), is( this.parentTable ) );
    }

    @Test
    public void shouldHaveReferencesTableAfterConstruction() throws Exception {
        assertThat( this.foreignKey.getReferencesTable( getTransaction() ), is( this.refTable ) );
    }

    @Test( expected = UnsupportedOperationException.class )
    public void shouldNotAllowChildren() throws Exception {
        this.foreignKey.addChild( getTransaction(), "blah", null );
    }

    @Test( expected = IllegalArgumentException.class )
    public void shouldNotAllowNullTableReference() throws Exception {
        this.foreignKey.setReferencesTable( getTransaction(), null );
    }

    @Test
    public void shouldNotContainFilteredProperties() throws Exception {
        final String[] filteredProps = this.foreignKey.getPropertyNames( getTransaction() );
        final Filter[] filters = this.foreignKey.getFilters();

        for ( final String name : filteredProps ) {
            for ( final Filter filter : filters ) {
                assertThat( filter.rejectProperty( name ), is( false ) );
            }
        }
    }

    @Test
    public void shouldNotHaveColumnReferencesAfterConstruction() throws Exception {
        assertThat( this.foreignKey.getReferencesColumns( getTransaction() ), is( notNullValue() ) );
        assertThat( this.foreignKey.getReferencesColumns( getTransaction() ).length, is( 0 ) );
    }

    @Test
    public void shouldNotHaveColumnsAfterConstruction() throws Exception {
        assertThat( this.foreignKey.getColumns( getTransaction() ), is( notNullValue() ) );
        assertThat( this.foreignKey.getColumns( getTransaction() ).length, is( 0 ) );
    }

    @Test
    public void shouldRemoveReferencesColumn() throws Exception {
        final Column columnA = RelationalModelFactory.createColumn( getTransaction(), _repo, this.refTable, "removeRefColumnA" );
        this.foreignKey.addReferencesColumn( getTransaction(), columnA );
        commit(); // must commit so that query used in next method will work

        this.foreignKey.removeReferencesColumn( getTransaction(), columnA );
        assertThat( this.foreignKey.getReferencesColumns( getTransaction() ).length, is( 0 ) );
    }

    @Test
    public void shouldRename() throws Exception {
        final String newName = "blah";
        this.foreignKey.rename( getTransaction(), newName );
        assertThat( this.foreignKey.getName( getTransaction() ), is( newName ) );
    }

    @Test
    public void shouldSetTableReference() throws Exception {
        final Table newTable = RelationalModelFactory.createTable( getTransaction(), _repo, mock( Model.class ), "newTable" );
        this.foreignKey.setReferencesTable( getTransaction(), newTable );
        commit(); // must commit so that query used in next method will work

        assertThat( this.foreignKey.getReferencesTable( getTransaction() ), is( newTable ) );
    }

}
