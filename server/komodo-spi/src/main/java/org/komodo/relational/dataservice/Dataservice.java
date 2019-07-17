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
package org.komodo.relational.dataservice;

import java.util.Calendar;

import org.komodo.relational.RelationalObject;
import org.komodo.relational.vdb.Vdb;
import org.komodo.spi.KException;
import org.komodo.spi.repository.KomodoType;
import org.komodo.spi.repository.UnitOfWork;

/**
 * A model of a dataservice instance
 */
public interface Dataservice extends RelationalObject {

    /**
     * The type identifier.
     */
    int TYPE_ID = Dataservice.class.hashCode();

    /**
     * Identifier of this object
     */
    KomodoType IDENTIFIER = KomodoType.DATASERVICE;

    /**
     * @param uow
     *        the transaction (cannot be <code>null</code> and must have a state of
     *        {@link org.komodo.spi.repository.UnitOfWork.State#NOT_STARTED})
     * @param serviceVdb
     *        the service VDB being set (can be <code>null</code> when deleting current value)
     * @return the service VDB being replaced or <code>null</code> if one is not being replaced
     * @throws KException
     *         if an error occurs
     */
    Vdb setServiceVdb( final UnitOfWork uow,
                       final Vdb serviceVdb ) throws KException;

    /**
     * @param uow
     *        the transaction (cannot be <code>null</code> and must have a state of
     *        {@link org.komodo.spi.repository.UnitOfWork.State#NOT_STARTED})
     * @return the service VDB (may be <code>null</code> if not defined)
     * @throws KException
     *         if an error occurs
     */
    Vdb getServiceVdb( final UnitOfWork uow ) throws KException;

    /**
     * @param uow
     *        the transaction (cannot be <code>null</code> and must have a state of
     *        {@link org.komodo.spi.repository.UnitOfWork.State#NOT_STARTED})
     * @return the service VDB entry (may be <code>null</code> if not defined)
     * @throws KException
     *         if an error occurs
     */
    ServiceVdbEntry getServiceVdbEntry( final UnitOfWork uow ) throws KException;

    /**
     * @param uow
     *        the transaction (cannot be <code>null</code> or have a state that is not
     *        {@link org.komodo.spi.repository.UnitOfWork.State#NOT_STARTED})
     * @return the names of the ViewDefinitions for the dataservice (may be empty if not found)
     * @throws KException
     *         if an error occurs
     */
    String[] getViewDefinitionNames( UnitOfWork uow ) throws KException;

    /**
     * @param uow
     *        the transaction (cannot be <code>null</code> or have a state that is not
     *        {@link org.komodo.spi.repository.UnitOfWork.State#NOT_STARTED})
     * @return the name of the dataservice view model (may be <code>null</code> if not found)
     * @throws KException
     *         if an error occurs
     */
    String getServiceViewModelName( UnitOfWork uow ) throws KException;

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not
     *        {@link org.komodo.spi.repository.UnitOfWork.State#NOT_STARTED})
     * @return the value of the <code>description</code> property (can be empty)
     * @throws KException
     *         if an error occurs
     */
    String getDescription( final UnitOfWork transaction ) throws KException;

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not
     *        {@link org.komodo.spi.repository.UnitOfWork.State#NOT_STARTED})
     * @return the last time the manifest was modified (can be <code>null</code>)
     * @throws KException
     *         if an error occurs
     */
    Calendar getLastModified( final UnitOfWork transaction ) throws KException;

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not
     *        {@link org.komodo.spi.repository.UnitOfWork.State#NOT_STARTED})
     * @return the name of the user who last modified the data service (can be <code>null</code> or empty)
     * @throws KException
     *         if an error occurs
     */
    String getModifiedBy( final UnitOfWork transaction ) throws KException;

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param newDescription
     *        the new value of the <code>description</code> property
     * @throws KException
     *         if an error occurs
     */
    void setDescription( final UnitOfWork transaction,
                         final String newDescription ) throws KException;

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param newLastModified
     *        the new value of the <code>last modified date</code> property
     * @throws KException
     *         if an error occurs
     */
    void setLastModified( final UnitOfWork transaction,
                          final Calendar newLastModified ) throws KException;

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param newModifiedBy
     *        the new value of the <code>modified by</code> property
     * @throws KException
     *         if an error occurs
     */
    void setModifiedBy( final UnitOfWork transaction,
                        final String newModifiedBy ) throws KException;
    
}
