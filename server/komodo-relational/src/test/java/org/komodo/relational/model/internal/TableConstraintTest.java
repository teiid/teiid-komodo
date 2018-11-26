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
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import org.junit.Before;
import org.junit.Test;
import org.komodo.relational.RelationalModelFactory;
import org.komodo.relational.RelationalModelTest;
import org.komodo.relational.RelationalObject.Filter;
import org.komodo.relational.model.Column;
import org.komodo.relational.model.Table;
import org.komodo.relational.model.TableConstraint;
import org.komodo.spi.KException;
import org.komodo.spi.lexicon.ddl.teiid.TeiidDdlLexicon;

@SuppressWarnings( { "javadoc", "nls" } )
public final class TableConstraintTest extends RelationalModelTest {

    private static final String NAME = "tableConstraint";

    private TableConstraint constraint;
    private Table table;

    @Before
    public void init() throws Exception {
        this.table = createTable();
        this.constraint = this.table.addAccessPattern( getTransaction(), NAME );
        commit();
    }

    @Test
    public void shouldAddColumn() throws Exception {
        final Column column = RelationalModelFactory.createColumn( getTransaction(), _repo, mock( Table.class ), "column" );
        this.constraint.addColumn( getTransaction(), column );

        assertThat( this.constraint.hasProperty( getTransaction(), TeiidDdlLexicon.Constraint.REFERENCES ), is( true ) );
        assertThat( this.constraint.getProperty( getTransaction(), TeiidDdlLexicon.Constraint.REFERENCES ).getValues( getTransaction() ).length,
                    is( 1 ) );

        commit(); // must commit so that query used in next method will work
        assertThat( this.constraint.getColumns( getTransaction() ).length, is( 1 ) );
    }

    @Test
    public void shouldBeChildRestricted() {
        assertThat( this.constraint.isChildRestricted(), is( true ) );
    }

    @Test( expected = KException.class )
    public void shouldFailWhenRemovingColumnThatWasNeverAdded() throws Exception {
        final Column column = RelationalModelFactory.createColumn( getTransaction(), _repo, mock( Table.class ), "column" );
        this.constraint.removeColumn( getTransaction(), column );
    }

    @Test
    public void shouldHaveMoreRawProperties() throws Exception {
        final String[] filteredProps = this.constraint.getPropertyNames( getTransaction() );
        final String[] rawProps = this.constraint.getRawPropertyNames( getTransaction() );
        assertThat( ( rawProps.length > filteredProps.length ), is( true ) );
    }

    @Test
    public void shouldHaveTableAfterConstruction() throws Exception {
        assertThat( this.constraint.getTable( getTransaction() ), is( this.table ) );
    }

    @Test( expected = UnsupportedOperationException.class )
    public void shouldNotAllowChildren() throws Exception {
        this.constraint.addChild( getTransaction(), "blah", null );
    }

    @Test( expected = IllegalArgumentException.class )
    public void shouldNotBeAbleToAddNullColumn() throws Exception {
        this.constraint.addColumn( getTransaction(), null );
    }

    @Test( expected = UnsupportedOperationException.class )
    public void shouldNotBeAbleToSetConstraintType() throws Exception {
        this.constraint.setProperty( getTransaction(), TeiidDdlLexicon.Constraint.TYPE, "INDEX" );
    }

    @Test
    public void shouldNotContainFilteredProperties() throws Exception {
        final String[] filteredProps = this.constraint.getPropertyNames( getTransaction() );
        final Filter[] filters = this.constraint.getFilters();

        for ( final String name : filteredProps ) {
            for ( final Filter filter : filters ) {
                assertThat( filter.rejectProperty( name ), is( false ) );
            }
        }
    }

    @Test
    public void shouldNotHaveColumnsAfterConstruction() throws Exception {
        assertThat( this.constraint.getColumns( getTransaction() ).length, is( 0 ) );
    }

    @Test
    public void shouldRemoveColumn() throws Exception {
        final Column column = RelationalModelFactory.createColumn( getTransaction(), _repo, mock( Table.class ), "column" );
        this.constraint.addColumn( getTransaction(), column );
        commit(); // must commit so that query used in next method will work

        this.constraint.removeColumn( getTransaction(), column );
        assertThat( this.constraint.hasProperty( getTransaction(), TeiidDdlLexicon.Constraint.REFERENCES ), is( false ) );
    }

}
