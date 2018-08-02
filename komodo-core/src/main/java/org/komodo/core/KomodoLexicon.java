/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */
package org.komodo.core;

import org.komodo.spi.constants.StringConstants;
import org.komodo.spi.lexicon.LexiconConstants.NTLexicon;
import org.komodo.spi.lexicon.ddl.teiid.TeiidDdlLexicon;

/**
 * Constants for the JCR names of node types and properties related to the Komodo engine.
 */
public interface KomodoLexicon extends StringConstants {

    /**
     * JCR names that relate to the Teiid DDL namespace.
     */
    interface TeiidDdl extends TeiidDdlLexicon.CreateProcedure {

        /**
         * The node type name for a result set.
         */
        String RESULT_SET_NODE_TYPE = TeiidDdlLexicon.Namespace.PREFIX + ":resultSet"; //$NON-NLS-1$

    }

    /**
     * The JCR names associated with a data service node type.
     */
    interface DataService {

        /**
         * The name and node type name of the data services grouping node. Value is {@value} .
         */
        String GROUP_NODE = Namespace.PREFIX + COLON + "dataServices"; //$NON-NLS-1$

    }

    /**
     * The JCR names associated with a data source node type.
     */
    interface DataSource {

        /**
         * The name and node type name of the data sources grouping node. Value is {@value} .
         */
        String GROUP_NODE = Namespace.PREFIX + COLON + "dataSources"; //$NON-NLS-1$

    }

    /**
     * The JCR names associated with the Komodo environment node type.
     */
    interface Environment {

        /**
         * The name and node type name of the Komodo environment node. Value is {@value} .
         */
        String NODE_TYPE = Komodo.ENVIRONMENT;

        /**
         * The unqualified name of the environment area. Value is {@value}.
         */
        String UNQUALIFIED_NAME = NODE_TYPE.substring( NODE_TYPE.indexOf( ':' ) + 1 );

        /**
         * The name of the Komodo environment validation child node. Value is {@value} .
         */
        String VALIDATION = Namespace.PREFIX + COLON + "validation"; //$NON-NLS-1$

        /**
         * The name of the Komodo environment profiles child node. Value is {@value} .
         */
        String PROFILES = Namespace.PREFIX + COLON + "profiles"; //$NON-NLS-1$
    }

    /**
     * The JCR names associated with a folder node type.
     */
    interface Folder extends LibraryComponent, WorkspaceItem {

        /**
         * The node type name of a folder. Value is {@value} .
         */
        String NODE_TYPE = Namespace.PREFIX + COLON + "folder"; //$NON-NLS-1$

    }

    /**
     * The JCR names associated with the Komodo node type.
     */
    interface Komodo {

        /**
         * The name of the Komodo environment child node. Value is {@value} .
         */
        String ENVIRONMENT = Namespace.PREFIX + COLON + "environment"; //$NON-NLS-1$

        /**
         * The name and node type name of the Komodo library node. Value is {@value} .
         */
        String LIBRARY = Namespace.PREFIX + COLON + "library"; //$NON-NLS-1$

        /**
         * The name and node type name of the Komodo node. Value is {@value} .
         */
        String NODE_TYPE = Namespace.PREFIX + COLON + "komodo"; //$NON-NLS-1$

        /**
         * The name and node type name of the Komodo workspace node. Value is {@value} .
         */
        String WORKSPACE = Namespace.PREFIX + COLON + "workspace"; //$NON-NLS-1$

        /**
         * The name and node type name of Komodo home nodes. Value is {@value} .
         */
        String HOME = Namespace.PREFIX + COLON + "home"; //$NON-NLS-1$
    }

    /**
     * The JCR names associated with the Komodo library node type.
     */
    interface Library {

        /**
         * The name and node type name of the Komodo library's data source grouping node. Value is {@value} .
         */
        String DATA_SOURCES = DataSource.GROUP_NODE;

        /**
         * The name and node type name of the Komodo library node. Value is {@value} .
         */
        String NODE_TYPE = Komodo.LIBRARY;

        /**
         * The name and node type name of the Komodo library's schema grouping node. Value is {@value} .
         */
        String SCHEMAS = Schema.GROUP_NODE;

        /**
         * The unqualified name of the library area. Value is {@value}.
         */
        String UNQUALIFIED_NAME = NODE_TYPE.substring( NODE_TYPE.indexOf( ':' ) + 1 );

        /**
         * The name and node type name of the Komodo library's file entry grouping node. Value is {@value} .
         */
        String VDB_ENTRIES = VdbEntry.GROUP_NODE;

        /**
         * The name and node type name of the Komodo library's Import VDB grouping node. Value is {@value} .
         */
        String VDB_IMPORTS = VdbImport.GROUP_NODE;

        /**
         * The name and node type name of the Komodo library's VDB manifest model source grouping node. Value is {@value} .
         */
        String VDB_MODEL_SOURCES = VdbModelSource.GROUP_NODE;

        /**
         * The name and node type name of the Komodo library's VDB manifest model grouping node. Value is {@value} .
         */
        String VDB_MODELS = VdbModel.GROUP_NODE;

        /**
         * The name and node type name of the Komodo library's translator grouping node. Value is {@value} .
         */
        String VDB_TRANSLATORS = VdbTranslator.GROUP_NODE;

        /**
         * The name and node type name of the Komodo library's VDB grouping node. Value is {@value} .
         */
        String VDBS = Vdb.GROUP_NODE;

    }

    /**
     * The JCR names associated with the library component mixin. Library nodes are versionable and referenceable.
     */
    interface LibraryComponent {

        /**
         * The name of the description property of a library component. Value is {@value} .
         */
        String DESCRIPTION = Namespace.PREFIX + COLON + "description"; //$NON-NLS-1$

        /**
         * The name of the library component mixin. Value is {@value} .
         */
        String MIXIN_TYPE = Namespace.PREFIX + COLON + "libraryComponent"; //$NON-NLS-1$

    }

    /**
     * The JCR names associated with the Komodo namespace.
     */
    interface Namespace {

        /**
         * The Komodo namespace prefix. Value is {@value} .
         */
        String PREFIX = "tko"; //$NON-NLS-1$

        /**
         * The Komodo namespace URI. Value is {@value} .
         */
        String URI = "http://www.teiid.org/komodo/1.0"; //$NON-NLS-1$

    }

    /**
     * The JCR names associated with repositories.
     */
    interface Repository extends WorkspaceItem {

        /**
         * The name of the <code>nt:address</code> node type for the child node. Value is {@value} .
         */
        String ADDRESS = "nt:address"; //$NON-NLS-1$

        /**
         * The name and node type name of the repositories grouping node. Value is {@value} .
         */
        String GROUP_NODE = Namespace.PREFIX + COLON + "repositories"; //$NON-NLS-1$

        /**
         * The name of the repository node type. Value is {@value} .
         */
        String NODE_TYPE = Namespace.PREFIX + COLON + "repository"; //$NON-NLS-1$

        /**
         * The name of the type property. Value is {@value} .
         */
        String TYPE = Namespace.PREFIX + COLON + "type"; //$NON-NLS-1$

    }

    /**
     * The JCR names associated with validation rules.
     */
    interface Rule {

        /**
         * The name of the property used to indicate if the rule is built-in. Value is {@value} .
         */
        String BUILT_IN = Namespace.PREFIX + COLON + "builtIn"; //$NON-NLS-1$

        /**
         * The name of the multi-valued property for node type names that children must not have. Value is {@value} .
         */
        String CHILD_ABSENT = Namespace.PREFIX + COLON + "childAbsent"; //$NON-NLS-1$

        /**
         * The name of the multi-valued property for node type names that at least one child must have. Value is {@value} .
         */
        String CHILD_EXISTS = Namespace.PREFIX + COLON + "childExists"; //$NON-NLS-1$

        /**
         * The name of the property used to indicate if the rule is enabled. Value is {@value} .
         */
        String ENABLED = Namespace.PREFIX + COLON + "enabled"; //$NON-NLS-1$

        /**
         * The name of the property used to store either the property name or the child node type name. Value is {@value} .
         */
        String JCR_NAME = Namespace.PREFIX + COLON + "jcrName"; //$NON-NLS-1$

        /**
         * The name of the property for the localized text. Value is {@value} .
         */
        String LOCALIZED_TEXT = Namespace.PREFIX + COLON + "text"; //$NON-NLS-1$

        /**
         * The name of the node type for a localized message. Value is {@value} .
         */
        String LOCALIZED_MESSAGE = Namespace.PREFIX + COLON + "localizedMessage"; //$NON-NLS-1$

        /**
         * The name of the node type for a localized message grouping node. Value is {@value} .
         */
        String LOCALIZED_MESSAGE_GROUPING = Namespace.PREFIX + COLON + "localizedMessageGroup"; //$NON-NLS-1$

        /**
         * The name of the node type for a localized text. Value is {@value} .
         */
        String LOCALIZED_TEXT_TYPE = Namespace.PREFIX + COLON + "localizedText"; //$NON-NLS-1$

        /**
         * The name of the property whose value indicates if SNS should only apply if the node types are the same. Value is *
         * {@value} .
         */
        String MATCH_TYPE = Namespace.PREFIX + COLON + "pattern"; //$NON-NLS-1$

        /**
         * The name of the property used to hold a maximum value of a number range. Value is {@value} .
         */
        String MAX_VALUE = Namespace.PREFIX + COLON + "maxValue"; //$NON-NLS-1$

        /**
         * The name of the property used to indicate if the max value is inclusive. Value is {@value} .
         */
        String MAX_VALUE_INCLUSIVE = Namespace.PREFIX + COLON + "maxInclusive"; //$NON-NLS-1$

        /**
         * The name of the child grouping node where the localized messages are kept. Value is {@value} .
         */
        String MESSAGES = Namespace.PREFIX + COLON + "messages"; //$NON-NLS-1$

        /**
         * The name of the property to hold the minimum value of a number range. Value is {@value} .
         */
        String MIN_VALUE = Namespace.PREFIX + COLON + "minValue"; //$NON-NLS-1$

        /**
         * The name of the property used to indicate if the min value is inclusive. Value is {@value} .
         */
        String MIN_VALUE_INCLUSIVE = Namespace.PREFIX + COLON + "minInclusive"; //$NON-NLS-1$

        /**
         * The name of the property used for the node type the rule pertains to. Value is {@value} .
         */
        String NODE_TYPE = Namespace.PREFIX + COLON + "nodeType"; //$NON-NLS-1$

        /**
         * The name of the property restrictions grouping node.  Value is {@value} .
         */
        String PROP_RESTRICTIONS_GROUPING = Namespace.PREFIX + COLON + "propRestrictions"; //$NON-NLS-1$

        /**
         * The name of the property restriction node. Value is {@value} .
         */
        String PROP_RESTRICTION = Namespace.PREFIX + COLON + "propRestriction"; //$NON-NLS-1$

        /**
         * The name of the property used for the node restricted to property value. Value is {@value} .
         */
        String PROP_VALUE = Namespace.PREFIX + COLON + "propValue"; //$NON-NLS-1$

        /**
         * The type of validation for this restriction. Value is {@value} .
         */
        String RESTRICTION_TYPE = Namespace.PREFIX + COLON + "type"; //$NON-NLS-1$

        /**
         * The name of the node type for a number rule.
         */
        String NUMBER_RULE = Namespace.PREFIX + COLON + "numberRule"; //$NON-NLS-1$

        /**
         * The name of the property used for a regular expression pattern. Value is {@value} .
         */
        String PATTERN = Namespace.PREFIX + COLON + "pattern"; //$NON-NLS-1$

        /**
         * The name of the node type for a pattern matching rule.
         */
        String PATTERN_RULE = Namespace.PREFIX + COLON + "patternRule"; //$NON-NLS-1$

        /**
         * The name of the multi-valued property used for properties that must not exist. Value is {@value} .
         */
        String PROP_ABSENT = Namespace.PREFIX + COLON + "propAbsent"; //$NON-NLS-1$

        /**
         * The name of the multi-valued property used for properties that must exist. Value is {@value} .
         */
        String PROP_EXISTS = Namespace.PREFIX + COLON + "propExists"; //$NON-NLS-1$

        /**
         * The name of the node type for a relationship rule.
         */
        String RELATIONSHIP_RULE = Namespace.PREFIX + COLON + "relationshipRule"; //$NON-NLS-1$

        /**
         * The name of the property used to indicate if the rule property or child is required. Value is {@value} .
         */
        String REQUIRED = Namespace.PREFIX + COLON + "required"; //$NON-NLS-1$

        /**
         * The name of the abstract node type for a rule.
         */
        String RULE_NODE_TYPE = Namespace.PREFIX + COLON + "rule"; //$NON-NLS-1$

        /**
         * The name of the property used for the rule type (REQUIRED, PATTERN, RELATIONSHIP, NUMBER). Value is {@value} .
         */
        String RULE_TYPE = Namespace.PREFIX + COLON + "ruleType"; //$NON-NLS-1$

        /**
         * The name of the node type for a same name sibling rule.
         */
        String SNS_RULE = Namespace.PREFIX + COLON + "snsRule"; //$NON-NLS-1$

        /**
         * The name of the property for the rule evaluation severity (ERROR, WARNING, INFO, OK). Value is {@value} .
         */
        String SEVERITY = Namespace.PREFIX + COLON + "severity"; //$NON-NLS-1$

        /**
         * The name of the property used for the validation type (PROPERTY, CHILD). Value is {@value} .
         */
        String VALIDATION_TYPE = Namespace.PREFIX + COLON + "validationType"; //$NON-NLS-1$

    }

    /**
     * The JCR names associated with schemas.
     */
    interface Schema extends LibraryComponent, WorkspaceItem {

        /**
         * The name of the property used for the external file location. Value is {@value} .
         */
        String EXTERNAL_LOCATION = Namespace.PREFIX + COLON + "externalLocation"; //$NON-NLS-1$

        /**
         * The name and node type name of the Komodo library and workspace's Teiid server grouping node. Value is {@value} .
         */
        String GROUP_NODE = Namespace.PREFIX + COLON + "schemas"; //$NON-NLS-1$

        /**
         * The name of the rendition property. Value is {@value} .
         */
        String RENDITION = Namespace.PREFIX + COLON + "rendition"; //$NON-NLS-1$

        /**
         * The name of the language object node type for child node(s). Value is {@value} .
         */
        String LANGUAGE_OBJECT = "tsql:languageObject"; //$NON-NLS-1$

        /**
         * The name of the schema node type. Value is {@value} .
         */
        String NODE_TYPE = Namespace.PREFIX + COLON + "schema"; //$NON-NLS-1$

    }

    /**
     * The JCR names associated with searches.
     */
    interface Search {
        /**
         * The name and node type name of the Komodo Search grouping node. Value is {@value}.
         */
        String GROUP_NODE = Namespace.PREFIX + COLON + "searches"; //$NON-NLS-1$

        /**
         * The name of the Search node type. Value is {@value} .
         */
        String NODE_TYPE = Namespace.PREFIX + COLON + "search"; //$NON-NLS-1$

        /**
         * The date of when the search was saved. Value is {@value}
         */
        String SEARCH_DATE = Namespace.PREFIX + COLON + "searchDate"; //$NON-NLS-1$

        /**
         * The custom where clause of the search. Value is {@value}
         */
        String CUSTOM_WHERE = Namespace.PREFIX + COLON + "customWhereClause"; //$NON-NLS-1$

        /**
         * The from type of the search. Value is {@value}
         */
        String FROM_TYPE = Namespace.PREFIX + COLON + "fromType"; //$NON-NLS-1$

        /**
         * The where clause of the search. Value is {@value}
         */
        String WHERE_CLAUSE = Namespace.PREFIX + COLON + "whereClause"; //$NON-NLS-1$

        /**
         * The JCR names for search from type
         */
        interface FromType {
            /**
             * The name of the fromType node type. Value is {@value} .
             */
            String NODE_TYPE = Namespace.PREFIX + COLON + "fromType"; //$NON-NLS-1$

            /**
             * The node type of the fromType. Value is {@value}
             */
            String TYPE = Namespace.PREFIX + COLON + "type"; //$NON-NLS-1$

            /**
             * The alias of the fromType. Value is {@value}
             */
            String ALIAS = Namespace.PREFIX + COLON + "alias"; //$NON-NLS-1$
        }

        /**
         * The JCR names for search where clause
         */
        interface WhereClause {
            /**
             * The name of the whereClause node type. Value is {@value} .
             */
            String NODE_TYPE = Namespace.PREFIX + COLON + "whereClause"; //$NON-NLS-1$

            /**
             * The pre clause operator of the where clause (AND, OR). Value is {@value}
             */
            String PRE_CLAUSE_OPERATOR = Namespace.PREFIX + COLON + "preClauseOperator"; //$NON-NLS-1$

            /**
             * The alias of the where clause. Value is {@value}
             */
            String ALIAS = Namespace.PREFIX + COLON + "alias"; //$NON-NLS-1$
        }

        /**
         * The JCR names for search where compare clause
         */
        interface WhereCompareClause extends WhereClause {
            /**
             * The name of the whereCompareClause node type. Value is {@value} .
             */
            String NODE_TYPE = Namespace.PREFIX + COLON + "whereCompareClause"; //$NON-NLS-1$

            /**
             * The property of the where compare clause. Value is {@value}
             */
            String PROPERTY = Namespace.PREFIX + COLON + "property"; //$NON-NLS-1$

            /**
             * The compare operator of the where compare clause.
             * (EQUALS, NOT_EQUALS, LESS_THAN, LESS_THAN_EQUAL_TO, GREATER_THAN,
             *  GREATER_THAN_EQUAL_TO, LIKE, NOT_LIKE)
             * Value is {@value}
             */
            String COMPARE_OPERATOR = Namespace.PREFIX + COLON + "compareOperator"; //$NON-NLS-1$

            /**
             * The value of the where compare clause. Value is {@value}
             */
            String VALUE = Namespace.PREFIX + COLON + "value"; //$NON-NLS-1$

            /**
             * The case insensitive flag of the where compare clause. Value is {@value}
             */
            String CASE_INSENSITIVE = Namespace.PREFIX + COLON + "caseInsensitive"; //$NON-NLS-1$
        }

        /**
         * The JCR names for search where contains clause
         */
        interface WhereContainsClause extends WhereClause {
            /**
             * The name of the whereContainsClause node type. Value is {@value} .
             */
            String NODE_TYPE = Namespace.PREFIX + COLON + "whereContainsClause"; //$NON-NLS-1$

            /**
             * The property of the where contains clause. Value is {@value}
             */
            String PROPERTY = Namespace.PREFIX + COLON + "property"; //$NON-NLS-1$

            /**
             * The keyword criteria of the where contains clause (ALL, ANY, NONE). Value is {@value}
             */
            String KEYWORD_CRITERIA = Namespace.PREFIX + COLON + "keywordCriteria"; //$NON-NLS-1$

            /**
             * The keyword of the where contains clause. Value is {@value}
             */
            String KEYWORDS = Namespace.PREFIX + COLON + "keywords"; //$NON-NLS-1$
        }

        /**
         * The JCR names for search where set clause
         */
        interface WhereSetClause extends WhereClause {
            /**
             * The name of the whereSetClause node type. Value is {@value} .
             */
            String NODE_TYPE = Namespace.PREFIX + COLON + "whereSetClause"; //$NON-NLS-1$

            /**
             * The property of the where set clause. Value is {@value}
             */
            String PROPERTY = Namespace.PREFIX + COLON + "property"; //$NON-NLS-1$

            /**
             * The values of the where set clause. Value is {@value}
             */
            String VALUES = Namespace.PREFIX + COLON + "values"; //$NON-NLS-1$
        }

        /**
         * The JCR names for search where path clause
         */
        interface WherePathClause extends WhereClause {
            /**
             * The name of the wherePathClause node type. Value is {@value} .
             */
            String NODE_TYPE = Namespace.PREFIX + COLON + "wherePathClause"; //$NON-NLS-1$

            /**
             * The path of the where path clause. Value is {@value}
             */
            String PATH = Namespace.PREFIX + COLON + "path"; //$NON-NLS-1$
        }

        /**
         * The JCR names for search where parent path clause
         */
        interface WhereParentPathClause extends WherePathClause {
            /**
             * The name of the whereParentPathClause node type. Value is {@value} .
             */
            String NODE_TYPE = Namespace.PREFIX + COLON + "whereParentPathClause"; //$NON-NLS-1$

            /**
             * The children-only property of the where parent path clause. Value is {@value}
             */
            String CHILDREN_ONLY = Namespace.PREFIX + COLON + "childrenOnly"; //$NON-NLS-1$
        }

        /**
         * The JCR names for search where paranthesis clause
         */
        interface WhereParanthesisClause extends WhereClause {
            /**
             * The name of the whereParanthesisClause node type. Value is {@value} .
             */
            String NODE_TYPE = Namespace.PREFIX + COLON + "whereParanthesisClause"; //$NON-NLS-1$
        }
    }

    /**
     * Abstract model of a Teiid connection properties
     */
    interface TeiidArchetype {

        /**
         * The version property. Value is {@value} .
         */
        String VERSION = Namespace.PREFIX + COLON + "version"; //$NON-NLS-1$

        /**
         * The name of the administrator port property. Value is {@value} .
         */
        String ADMIN_PORT = Namespace.PREFIX + COLON + "adminPort"; //$NON-NLS-1$

        /**
         * The name of the administrator password property. Value is {@value} .
         */
        String ADMIN_PSWD = Namespace.PREFIX + COLON + "adminPswd"; //$NON-NLS-1$

        /**
         * The name of the administrator user name property. Value is {@value} .
         */
        String ADMIN_USER = Namespace.PREFIX + COLON + "adminUser"; //$NON-NLS-1$

        /**
         * The admin connection is encrypted. Value is {@value} .
         */
        String ADMIN_SECURE = Namespace.PREFIX + COLON + "adminSecure"; //$NON-NLS-1$

        /**
         * The name of the connected property. Value is {@value} .
         */
        String CONNECTED = Namespace.PREFIX + COLON + "connected"; //$NON-NLS-1$

        /**
         * The name of the host URL property. Value is {@value} .
         */
        String HOST = Namespace.PREFIX + COLON + "host"; //$NON-NLS-1$

        /**
         * The name of the JDBC port property. Value is {@value} .
         */
        String JDBC_PORT = Namespace.PREFIX + COLON + "jdbcPort"; //$NON-NLS-1$

        /**
         * The name of the JDBC password property. Value is {@value} .
         */
        String JDBC_PSWD = Namespace.PREFIX + COLON + "jdbcPswd"; //$NON-NLS-1$

        /**
         * The name of the JDBC user name property. Value is {@value} .
         */
        String JDBC_USER = Namespace.PREFIX + COLON + "jdbcUser"; //$NON-NLS-1$

        /**
         * The jdbc connection is encrypted. Value is {@value} .
         */
        String JDBC_SECURE = Namespace.PREFIX + COLON + "jdbcSecure"; //$NON-NLS-1$
    }

    /**
     * The JCR names associated with VDBs.
     */
    interface Vdb extends LibraryComponent, WorkspaceItem {

        /**
         * The name and node type name of the Komodo workspace and library's VDB grouping node. Value is {@value} .
         */
        String GROUP_NODE = Namespace.PREFIX + COLON + "vdbs"; //$NON-NLS-1$

        /**
         * The name of the VDB node type. Value is {@value} .
         */
        String NODE_TYPE = Namespace.PREFIX + COLON + "vdb"; //$NON-NLS-1$

    }

    /**
     * The JCR names associated with VDB manifest entries.
     */
    interface VdbEntry extends LibraryComponent {

        /**
         * The name and node type name of the Komodo library's VDB manifest entry grouping node. Value is {@value} .
         */
        String GROUP_NODE = Namespace.PREFIX + COLON + "vdbEntries"; //$NON-NLS-1$

        /**
         * The name of the VDB manifest entry node type. Value is {@value} .
         */
        String NODE_TYPE = Namespace.PREFIX + COLON + "vdbEntry"; //$NON-NLS-1$

    }

    /**
     * The JCR names associated with VDB manifest Import VDBs.
     */
    interface VdbImport extends LibraryComponent {

        /**
         * The name and node type name of the Komodo library's VDB manifest Import VDB grouping node. Value is {@value} .
         */
        String GROUP_NODE = Namespace.PREFIX + COLON + "vdbImports"; //$NON-NLS-1$

        /**
         * The name of the VDB manifest entry node type. Value is {@value} .
         */
        String NODE_TYPE = Namespace.PREFIX + COLON + "vdbImport"; //$NON-NLS-1$

    }

    /**
     * The JCR names associated with VDB manifest models.
     */
    interface VdbModel extends LibraryComponent {

        /**
         * The node type name of the model's file child node. Value is {@value} .
         */
        String FILE = NTLexicon.NT_FILE;

        /**
         * The name of the metadata property (e.g., DDL). Value is {@value} .
         */

        /**
         * The name and node type name of the Komodo library's VDB manifest model grouping node. Value is {@value} .
         */
        String GROUP_NODE = Namespace.PREFIX + COLON + "vdbModels"; //$NON-NLS-1$

        /**
         * The name of the metadata property (e.g., DDL). Value is {@value} .
         */
        String METADATA_TYPE = "vdb:metadataType"; //$NON-NLS-1$

        /**
         * The name of the model definition property. This is the code, like DDL, that defines the schema/model. Value is {@value}
         * .
         */
        String MODEL_DEFINITION = "vdb:modelDefinition"; //$NON-NLS-1$

        /**
         * The node type name of the model's source child node(s). Value is {@value} .
         */
        String MODEL_SOURCE = VdbModelSource.NODE_TYPE;

        /**
         * The name of the VDB manifest model node type. Value is {@value} .
         */
        String NODE_TYPE = Namespace.PREFIX + COLON + "vdbModel"; //$NON-NLS-1$

    }

    /**
     * The JCR names associated with VDB manifest model sources.
     */
    interface VdbModelSource extends LibraryComponent {

        /**
         * The name and node type name of the Komodo library's VDB manifest model source grouping node. Value is {@value} .
         */
        String GROUP_NODE = Namespace.PREFIX + COLON + "vdbModelSources"; //$NON-NLS-1$

        /**
         * The name of the JNDI property. Value is {@value} .
         */
        String JNDI_NAME = "vdb:sourceJndiName"; //$NON-NLS-1$

        /**
         * The name of the VDB manifest model source node type. Value is {@value} .
         */
        String NODE_TYPE = Namespace.PREFIX + COLON + "vdbModelSource"; //$NON-NLS-1$

        /**
         * The name of the translator property. Value is {@value} .
         */
        String TRANSLATOR = "vdb:sourceTranslator"; //$NON-NLS-1$
    }

    /**
     * The JCR names associated with VDB manifest translators.
     */
    interface VdbTranslator extends LibraryComponent {

        /**
         * The name and node type name of the Komodo library's VDB manifest translator grouping chld node. Value is {@value} .
         */
        String GROUP_NODE = Namespace.PREFIX + COLON + "vdbTranslators"; //$NON-NLS-1$

        /**
         * The name of the VDB manifest translator node type. Value is {@value} .
         */
        String NODE_TYPE = Namespace.PREFIX + COLON + "vdbTranslator"; //$NON-NLS-1$

    }

    /**
     * The JCR names associated with the Komodo workspace node type.
     */
    interface Workspace {

        /**
         * The name and node type name of the Komodo workspace node. Value is {@value} .
         */
        String NODE_TYPE = Komodo.WORKSPACE;

        /**
         * The unqualified name of the workspace area. Value is {@value}.
         */
        String UNQUALIFIED_NAME = NODE_TYPE.substring( NODE_TYPE.indexOf( ':' ) + 1 );

    }

    /**
     * The JCR names associated with the Komodo workspace node type.
     */
    interface Home {

        /**
         * The name and node type name of the Komodo workspace node. Value is {@value} .
         */
        String NODE_TYPE = Komodo.HOME;

        /**
         * The unqualified name of the workspace area. Value is {@value}.
         */
        String UNQUALIFIED_NAME = NODE_TYPE.substring( NODE_TYPE.indexOf( ':' ) + 1 );
    }

    /**
     * The JCR names associated with the workspace item mixin. Workspace item nodes are referenceable.
     */
    interface WorkspaceItem {

        /**
         * The name of the workspace item mixin. Value is {@value} .
         */
        String MIXIN_TYPE = Namespace.PREFIX + COLON + "workspaceItem"; //$NON-NLS-1$

        /**
         * The name of the external file location property. Value is {@value} .
         */
        String EXT_LOC = Namespace.PREFIX + COLON + "externalLocation"; //$NON-NLS-1$

        /**
         * The node type name of the file child node. Value is {@value} .
         */
        String FILE_NODE_TYPE = NTLexicon.NT_FILE;

        /**
         * The node name of the child node that contains the original imported resource. Value is {@value} .
         */
        String ORIGINAL_FILE = Namespace.PREFIX + COLON + "originalFile"; //$NON-NLS-1$

    }

    /**
     * The JCR names associated with the user profile node type.
     */
    interface Profile {

        /**
         * The name and node type name of the user profile grouping child node. Value is {@value} .
         */
        String GROUP_NODE = Namespace.PREFIX + COLON + "profiles"; //$NON-NLS-1$

        /**
         * The name of the user profile node type. Value is {@value} .
         */
        String NODE_TYPE = Namespace.PREFIX + COLON + "profile"; //$NON-NLS-1$

        /**
         * The name of the git repositories child node. Value is {@value} .
         */
        String GIT_REPOSITORIES = Namespace.PREFIX + COLON + "gitRepositories"; //$NON-NLS-1$

        /**
         * The name of the view editor states child node. Value is {@value} .
         */
        String VIEW_EDITOR_STATES = Namespace.PREFIX + COLON + "viewEditorStates"; //$NON-NLS-1$
    }

    /**
     * The JCR names associated with the git repository node type.
     */
    interface GitRepository {

        /**
         * The name of the git repository node type. Value is {@value} .
         */
        String NODE_TYPE = Namespace.PREFIX + COLON + "gitRepository"; //$NON-NLS-1$

        /**
         * The name of the repository url property. Value is {@value} .
         */
        String URL = Namespace.PREFIX + COLON + "url"; //$NON-NLS-1$

        /**
         * The name of the repository branch property. Value is {@value} .
         */
        String BRANCH = Namespace.PREFIX + COLON + "branch"; //$NON-NLS-1$

        /**
         * The name of the repository user property. Value is {@value} .
         */
        String USER = Namespace.PREFIX + COLON + "user"; //$NON-NLS-1$

        /**
         * The name of the repository password property. Value is {@value} .
         */
        String PASSWORD = Namespace.PREFIX + COLON + "pswd"; //$NON-NLS-1$

        /**
         * The name of commit author property. Value is {@value} .
         */
        String COMMIT_AUTHOR = Namespace.PREFIX + COLON + "commitAuthor"; //$NON-NLS-1$

        /**
         * The name of the commit email property. Value is {@value} .
         */
        String COMMIT_EMAIL = Namespace.PREFIX + COLON + "commitEmail"; //$NON-NLS-1$

        /**
         * The name of the target directory property. Value is {@value} .
         */
        String TARGET_DIRECTORY = Namespace.PREFIX + COLON + "targetDirectory"; //$NON-NLS-1$
    }

    /**
     * The JCR names associated with the view editor state node type.
     */
    interface ViewEditorState {

        /**
         * The name of the view editor state node type. Value is {@value} .
         */
        String NODE_TYPE = Namespace.PREFIX + COLON + "viewEditorState"; //$NON-NLS-1$
        
        /**
         * The name of the view definition child node. Value is {@value} .
         */
        String VIEW_DEFINITION = Namespace.PREFIX + COLON + "viewDefinition"; //$NON-NLS-1$
    }
    
    /**
     * The JCR names associated with the view definition node type.
     */
    interface ViewDefinition {

        /**
         * The name of the view definition node type. Value is {@value} .
         */
        String NODE_TYPE = Namespace.PREFIX + COLON + "viewDefinition"; //$NON-NLS-1$
        
        /**
         * The name of the view definition description property. Value is {@value} .
         */
        String VIEW_NAME = Namespace.PREFIX + COLON + "viewName"; //$NON-NLS-1$        

        /**
         * The name of the view definition description property. Value is {@value} .
         */
        String DESCRIPTION = Namespace.PREFIX + COLON + "description"; //$NON-NLS-1$
        
        /**
         * The name of the view definition isComplete property. Value is {@value} .
         */
        String IS_COMPLETE = Namespace.PREFIX + COLON + "isComplete"; //$NON-NLS-1$
        
        /**
         * The name of the view definition source paths container property. Value is {@value} .
         */
        String SOURCE_PATHS = Namespace.PREFIX + COLON + "sourcePaths"; //$NON-NLS-1$
        
        /**
         * The name of the sql compositions child node. Value is {@value} .
         */
        String SQL_COMPOSITIONS = Namespace.PREFIX + COLON + "sqlCompositions"; //$NON-NLS-1$
    }
    
    /**
     * The JCR names associated with the view definition node type.
     */
    interface SqlComposition {

        /**
         * The name of the view definition node type. Value is {@value} .
         */
        String NODE_TYPE = Namespace.PREFIX + COLON + "sqlComposition"; //$NON-NLS-1$
        
        /**
         * The name of the view definition description property. Value is {@value} .
         */
        String DESCRIPTION = Namespace.PREFIX + COLON + "description"; //$NON-NLS-1$

        /**
         * The name of the view definition leftSource property. Value is {@value} .
         */
        String LEFT_SOURCE_PATH = Namespace.PREFIX + COLON + "leftSourcePath"; //$NON-NLS-1$

        /**
         * The name of the view definition rightSource property. Value is {@value} .
         */
        String RIGHT_SOURCE_PATH = Namespace.PREFIX + COLON + "rightSourcePath"; //$NON-NLS-1$

        /**
         * The name of the view definition leftSource property. Value is {@value} .
         */
        String LEFT_CRITERIA_COLUMN = Namespace.PREFIX + COLON + "leftCriteriaColumn"; //$NON-NLS-1$

        /**
         * The name of the view definition rightSource property. Value is {@value} .
         */
        String RIGHT_CRITERIA_COLUMN = Namespace.PREFIX + COLON + "rightCriteriaColumn"; //$NON-NLS-1$
        
        /**
         * The name of the view definition type property. Value is {@value} .
         */
        String TYPE = Namespace.PREFIX + COLON + "type"; //$NON-NLS-1$

        /**
         * The name of the view definition operator property. Value is {@value} .
         */
        String OPERATOR = Namespace.PREFIX + COLON + "operator"; //$NON-NLS-1$
    }

    /**
     * The JCR names associated with the view editor state command aggregate node type.
     */
    interface StateCommandAggregate {
        /**
         * The name of the view editor state node type. Value is {@value} .
         */
        String NODE_TYPE = Namespace.PREFIX + COLON + "stateCommandAggregate"; //$NON-NLS-1$

        /**
         * The prefix used to prepend to the name index
         */
        String NAME_PREFIX = "INDEX_";

        /**
         * The name of the undo child. Value is {@value} .
         */
        String UNDO = Namespace.PREFIX + COLON + "undo";

        /**
         * The name of the redo child. Value is {@value} .
         */
        String REDO = Namespace.PREFIX + COLON + "redo";
    }

    /**
     * The JCR names associated with the view editor state command node type.
     */
    interface StateCommand {

        /**
         * The name of the view editor state node type. Value is {@value} .
         */
        String NODE_TYPE = Namespace.PREFIX + COLON + "stateCommand"; //$NON-NLS-1$

        /**
         * The name of the command id property. Value is {@value} .
         */
        String ID = Namespace.PREFIX + COLON + "id";

        /**
         * The prefix of the view editor state command argument properties. Value is {@value} .
         */
        String ARGS_PREFIX = Namespace.PREFIX + COLON + "ARGS_";
    }
}
