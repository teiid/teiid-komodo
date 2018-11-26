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
package org.komodo.relational.vdb.internal;

import org.komodo.relational.internal.RelationalChildRestrictedObject;
import org.komodo.relational.vdb.Entry;
import org.komodo.relational.vdb.Vdb;
import org.komodo.spi.KException;
import org.komodo.spi.repository.KomodoObject;
import org.komodo.spi.repository.KomodoType;
import org.komodo.spi.repository.PropertyValueType;
import org.komodo.spi.repository.Repository;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.komodo.spi.repository.Repository.UnitOfWork.State;
import org.komodo.utils.ArgCheck;
import org.komodo.spi.lexicon.vdb.VdbLexicon;

/**
 * An implementation of a VDB entry.
 */
public final class EntryImpl extends RelationalChildRestrictedObject implements Entry {

    /**
     * @param uow
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param repository
     *        the repository where the relational object exists (cannot be <code>null</code>)
     * @param workspacePath
     *        the workspace path (cannot be empty)
     * @throws KException
     *         if an error occurs
     */
    public EntryImpl( final UnitOfWork uow,
                      final Repository repository,
                      final String workspacePath ) throws KException {
        super(uow, repository, workspacePath);
    }

    @Override
    public KomodoType getTypeIdentifier(UnitOfWork uow) {
        return Entry.IDENTIFIER;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.vdb.Entry#getDescription(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public String getDescription( final UnitOfWork uow ) throws KException {
        return getObjectProperty(uow, PropertyValueType.STRING, "getDescription", VdbLexicon.Entry.DESCRIPTION); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.internal.RelationalObjectImpl#getParent(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public Vdb getParent( final UnitOfWork transaction ) throws KException {
        ArgCheck.isNotNull( transaction, "transaction" ); //$NON-NLS-1$
        ArgCheck.isTrue( ( transaction.getState() == State.NOT_STARTED ), "transaction state must be NOT_STARTED" ); //$NON-NLS-1$

        final KomodoObject grouping = super.getParent( transaction );
        final Vdb result = Vdb.RESOLVER.resolve( transaction, grouping.getParent( transaction ) );
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.vdb.Entry#getPath(org.komodo.spi.repository.Repository.UnitOfWork)
     */
    @Override
    public String getPath( final UnitOfWork uow ) throws KException {
        return getObjectProperty(uow, PropertyValueType.STRING, "getPath", VdbLexicon.Entry.PATH); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.spi.repository.KomodoObject#getTypeId()
     */
    @Override
    public int getTypeId() {
        return TYPE_ID;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.vdb.Entry#setDescription(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String)
     */
    @Override
    public void setDescription( final UnitOfWork uow,
                                final String newDescription ) throws KException {
        setObjectProperty(uow, "setDescription", VdbLexicon.Entry.DESCRIPTION, newDescription); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     *
     * @see org.komodo.relational.vdb.Entry#setPath(org.komodo.spi.repository.Repository.UnitOfWork, java.lang.String)
     */
    @Override
    public void setPath( final UnitOfWork uow,
                         final String newPath ) throws KException {
        ArgCheck.isNotEmpty(newPath, "newPath"); //$NON-NLS-1$
        setObjectProperty(uow, "setPath", VdbLexicon.Entry.PATH, newPath); //$NON-NLS-1$
    }

}
