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
package org.komodo.relational.model;

import org.komodo.spi.KException;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.komodo.spi.repository.Repository.UnitOfWork.State;

/**
 * Represents an element that has a schema element type.
 */
public interface SchemaElement {

    /**
     * The schema element type.
     */
    public enum SchemaElementType {

        /**
         * A foreign or physical schema element.
         */
        FOREIGN,

        /**
         * A virtual schema element.
         */
        VIRTUAL;

        /**
         * The default type. Value is {@value} .
         */
        public static final SchemaElementType DEFAULT_VALUE = FOREIGN;

        /**
         * @param value
         *        the value whose <code>SchemaElementType</code> is being requested (can be empty)
         * @return the corresponding <code>SchemaElementType</code> or the default value if not found
         * @see #DEFAULT_VALUE
         */
        public static SchemaElementType fromValue( final String value ) {
            if (FOREIGN.name().equals(value)) {
                return FOREIGN;
            }

            if (VIRTUAL.name().equals(value)) {
                return VIRTUAL;
            }

            return DEFAULT_VALUE;
        }

    }

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @return the schema element type (never <code>null</code>)
     * @throws KException
     *         if an error occurs
     * @see SchemaElementType#DEFAULT_VALUE
     */
    SchemaElementType getSchemaElementType( final UnitOfWork transaction ) throws KException;

    /**
     * @param transaction
     *        the transaction (cannot be <code>null</code> or have a state that is not {@link State#NOT_STARTED})
     * @param newSchemaElementType
     *        the new value for the <code>schema element type</code> property (can be <code>null</code>)
     * @throws KException
     *         if an error occurs
     * @see SchemaElementType#DEFAULT_VALUE
     */
    void setSchemaElementType( final UnitOfWork transaction,
                               final SchemaElementType newSchemaElementType ) throws KException;

}
