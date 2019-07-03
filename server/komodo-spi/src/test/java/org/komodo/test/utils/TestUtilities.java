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
package org.komodo.test.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.komodo.spi.KException;
import org.komodo.spi.constants.StringConstants;
import org.komodo.spi.lexicon.LexiconConstants.CoreLexicon;
import org.teiid.modeshape.sequencer.dataservice.lexicon.DataVirtLexicon;
import org.teiid.modeshape.sequencer.vdb.lexicon.VdbLexicon;
import org.komodo.spi.repository.KomodoObject;
import org.komodo.spi.repository.Repository.UnitOfWork;
import org.komodo.utils.ArgCheck;
import org.komodo.utils.FileUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

/**
 *
 */
@SuppressWarnings( "nls" )
public class TestUtilities implements StringConstants {

    /**
     * Relative path to the resources directory
     */
    public static final String RESOURCES_DIRECTORY = convertPackageToDirPath(TestUtilities.class.getPackage());

    /**
     * Teiid Vdb Schema xsd
     */
    public static final String TEIID_VDB_XSD = "teiid-vdb.xsd";

    /**
     * Tweet example file name
     */
    public static final String TWEET_EXAMPLE_NAME = "tweet-example-vdb";

    /**
     * Tweet example vdb name
     */
    public static final String TWEET_EXAMPLE_VDB_NAME = "twitter";

    /**
     * Tweet example file, translator has no description defined
     */
    public static final String NO_TRANSLATOR_DESCRIP_NAME = "notranslator-descrip-vdb";

    /**
     * Tweet example file suffix
     */
    public static final String TWEET_EXAMPLE_SUFFIX = DOT + XML;

    /**
     * All elements example file name
     */
    public static final String ALL_ELEMENTS_EXAMPLE_NAME = "teiid-vdb-all-elements";

    /**
     * All elements example vdb name
     */
    public static final String ALL_ELEMENTS_EXAMPLE_VDB_NAME = "myVDB";

    /**
     * All elements example file suffix
     */
    public static final String ALL_ELEMENTS_EXAMPLE_SUFFIX = DOT + XML;

    public static final String S1_CONNECTION = "S1Connection";

    public static final String S2_CONNECTION = "S2Connection";

    public static final String REST_TRANSLATOR = "rest";

    public static final String TWITTER_MODEL = "twitter";

    private static final String TWITTER_CONNECTION = "twitterConnection1";

    public static final String TWITTER_VIEW_MODEL = "twitterview";

    /**
     * DDL for the twitter view model
     */
    public static final String TWITTER_VIEW_MODEL_DDL = EMPTY_STRING +
                            "CREATE VIRTUAL PROCEDURE getTweets(IN query varchar) " +
                            "RETURNS TABLE (created_on varchar(25), from_user varchar(25), " +
                            "to_user varchar(25), profile_image_url varchar(25), source " +
                            "varchar(25), text varchar(140)) AS select tweet.* from " +
                            "(EXEC twitter.invokeHTTP(" +
                            "action => 'GET', endpoint => querystring(\'', query as q))) AS w, " +
                            "XMLTABLE('results' passing JSONTOXML('myxml', w.result) columns " +
                            "created_on string PATH 'created_at', from_user string PATH 'from_user', " +
                            "to_user string PATH 'to_user', profile_image_url string PATH 'profile_image_url', " +
                            "source string PATH 'source', text string PATH 'text') AS tweet; " +
                            "CREATE VIEW Tweet AS select * FROM twitterview.getTweets;";

    /**
     * Dataservice vdb
     */
    public static final String DATASERVICE_VDB_FILE = "myService-vdb.xml";

    /**
     * Sample Dataservice Example Zip
     */
    public static final String SAMPLE_DATASERVICE_FILE = "sample-ds.zip";

    /**
     * Sample Dataservice name
     */
    public static final String SAMPLE_DATA_SERVICE_NAME = "MyDataService";

    /**
     * DSB Single Source Parts Dataservice Zip
     */
    public static final String PARTS_SINGLE_SOURCE_FILE = "PartsSingleSource.zip";

    /**
     * DSB Join With different table names Dataservice Zip
     */
    public static final String JOIN_DIFFERENT_TABLE_NAMES_FILE = "JoinServiceDifferentTableNames.zip";

    /**
     * DSB Join With same table names Dataservice Zip
     */
    public static final String JOIN_SAME_TABLE_NAMES_FILE = "JoinServiceSameTableNames.zip";

    /**
     * US States Dataservice Example Zip
     */
    public static final String US_STATES_SQL_FILE = "usstates.sql";

    /**
     * US States Dataservice Example Zip
     */
    public static final String US_STATES_DATASERVICE_TEIID_FILE = "usstates-dataservice.zip";

    /**
     * US States Dataservice name
     */
    public static final String US_STATES_DATA_SERVICE_NAME = "UsStatesService";

    /**
     * US States Dataservice Example vdb name
     */
    public static final String US_STATES_VDB_NAME = "usstates";

    /**
     * US States Dataservice Example data source name
     */
    public static final String US_STATES_DATA_SOURCE_NAME = "MySqlDS";

    /**
     * US States Dataservice Example data source name
     */
    public static final String US_STATES_DRIVER_NAME = "mysql-connector-java-5.1.39-bin.jar";

    /**
     * Parts Single source Dataservice name
     */
    public static final String PARTS_SINGLE_SOURCE_SERVICE_NAME = "PartsSingleSource";

    /**
     * Join different table names Dataservice name
     */
    public static final String JOIN_DIFFERENT_TABLE_NAMES_SERVICE_NAME = "JoinServiceDifferentTableNames";

    /**
     * Join same table names Dataservice name
     */
    public static final String JOIN_SAME_TABLE_NAMES_SERVICE_NAME = "JoinServiceSameTableNames";

    /**
     * Patients example file name
     */
    public static final String PATIENTS_VDB_FILE = "patients-vdb.xml";

    /**
     * Patients model name
     */
    public static final String PATIENTS_MODEL = "Patients";

    /**
     * Patients source name
     */
    public static final String PATIENTS_SOURCE = "PatientSource";

    /**
     * Patients connection name
     */
    public static final String PATIENTS_CONNECTION = "PatientSourceConnection1";

    /**
     * Patients DDL
     */
    public static final String PATIENTS_DDL_FILE = "patientsDDL.ddl";

    /**
     * Portfolio vdb
     */
    public static final String PORTFOLIO_VDB_FILE = "portfolio-vdb.xml";

    /**
     * Portfolio vdb name
     */
    public static final String PORTFOLIO_VDB_NAME = "Portfolio";

    /**
     * Portfolio model source connection
     */
    public static final String PORTFOLIO_CONNECTION_NAME = "ExcelConnection1";

    /**
     * Parts vdb
     */
    public static final String PARTS_VDB_FILE = "parts_dynamic-vdb.xml";

    /**
     * Parts vdb
     */
    public static final String PARTS_WITHKEYS_VDB_FILE = "parts_dynamic_withkeys-vdb.xml";

    /**
     * Service Source vdb for usstates
     */
    public static final String USSTATES_SOURCE_VDB_FILE = "USStates-source-vdb.xml";

    /**
     * Service Source vdb name for usstates
     */
    public static final String USSTATES_SOURCE_VDB_NAME = "ServiceSource";

    /**
     * Parts vdb name
     */
    public static final String PARTS_VDB_NAME = "MyPartsVDB_Dynamic";

    /**
     * Sample vdb file name
     */
    public static final String SAMPLE_VDB_FILE = "sample-vdb.xml";

    /**
     * Sample vdb
     */
    public static final String SAMPLE_VDB_NAME = "sample";

    /**
     * Roles vdb file name
     */
    public static final String ROLES_VDB_FILE = "roles-vdb.xml";

    /**
     * Roles vdb
     */
    public static final String ROLES_VDB_NAME = "z";

    /**
     * MySql Driver Jar
     */
    public static final String MYSQL_DRIVER_FILENAME = "mysql-connector-java-5.1.39-bin.jar";

    public static String convertPackageToDirPath(Package pkg) {
        return pkg.getName().replaceAll(DOUBLE_BACK_SLASH + DOT, FORWARD_SLASH);
    }

    /**
     * @return input stream of teiid vdb xsd
     * @throws Exception if error occurs
     */
    public static InputStream teiidVdbXsd() throws Exception {
        return getResourceAsStream(TestUtilities.class,
                                                                  RESOURCES_DIRECTORY,
                                                                  TEIID_VDB_XSD);
    }

    /**
     * @return input stream of patients example xml
     * @throws Exception if error occurs
     */
    public static InputStream patientsExample() throws Exception {
        return getResourceAsStream(TestUtilities.class,
                                                                  RESOURCES_DIRECTORY,
                                                                  PATIENTS_VDB_FILE);
    }

    /**
     * @return input stream of tweet example xml
     * @throws Exception if error occurs
     */
    public static InputStream tweetExample() throws Exception {
        return getResourceAsStream(TestUtilities.class,
                                                                  RESOURCES_DIRECTORY,
                                                                  TWEET_EXAMPLE_NAME,
                                                                  TWEET_EXAMPLE_SUFFIX);
    }

    /**
     * @return input stream of vdb xml with undefined translator description attribute
     * @throws Exception if error occurs
     */
    public static InputStream undefinedAttrExample() throws Exception {
        return getResourceAsStream(TestUtilities.class,
                                                                  RESOURCES_DIRECTORY,
                                                                  NO_TRANSLATOR_DESCRIP_NAME,
                                                                  TWEET_EXAMPLE_SUFFIX);
    }

    

    

    

    /**
     * @return input stream of all elements example xml
     * @throws Exception if error occurs
     */
    public static InputStream allElementsExample() throws Exception {
        return getResourceAsStream(TestUtilities.class,
                                                                  RESOURCES_DIRECTORY,
                                                                  ALL_ELEMENTS_EXAMPLE_NAME,
                                                                  ALL_ELEMENTS_EXAMPLE_SUFFIX);
    }

    

    /**
     * Creates the structure of the all elements example vdb
     *
     * @param uow the transaction
     * @param parentObject parent to append the new vdb
     *
     * @return the new vdb node
     * @throws Exception if error occurs
     */
    public static KomodoObject createAllElementsExampleNode(UnitOfWork uow, KomodoObject parentObject) throws Exception {
        /*
         * Create twitter connection to base the model source on
         */
        KomodoObject S1Connection = parentObject.addChild(uow,
                                                               S1_CONNECTION,
                                                                DataVirtLexicon.Connection.NODE_TYPE);

        KomodoObject S2Connection = parentObject.addChild(uow,
                                                              S2_CONNECTION,
                                                              DataVirtLexicon.Connection.NODE_TYPE);

        /*
         * teiid-vdb-all-elements.xml
         *      @jcr:primaryType=vdb:virtualDatabase
         *      @jcr:mixinTypes=[mode:derived,mix:referenceable]
         *      @jcr:uuid={uuid-to-be-created}
         *      @mode:sha1={sha1-to-be-created}
         *      @vdb:preview=false
         *      @vdb:version=1
         *      @vdb:originalFile=/vdbs/teiid-vdb-all-elements.xml
         *      @vdb:name=myVDB
         *      @vdb:description=vdb description
         *      @vdb:connectionType=NONE
         *      @vdb-property2=vdb-value2
         *      @vdb-property=vdb-value
         */
        KomodoObject myVdbExample = parentObject.addChild(uow,
                                                          ALL_ELEMENTS_EXAMPLE_NAME + ALL_ELEMENTS_EXAMPLE_SUFFIX,
                                                          VdbLexicon.Vdb.VIRTUAL_DATABASE);
        myVdbExample.addDescriptor(uow, "mode:derived", "mix:referenceable");
        myVdbExample.setProperty(uow, VdbLexicon.Vdb.NAME, "myVDB");
        myVdbExample.setProperty(uow, VdbLexicon.Vdb.DESCRIPTION, "vdb description");
        myVdbExample.setProperty(uow, VdbLexicon.Vdb.CONNECTION_TYPE, "NONE");
        myVdbExample.setProperty(uow, VdbLexicon.Vdb.ORIGINAL_FILE, "/vdbs/" + ALL_ELEMENTS_EXAMPLE_NAME + ALL_ELEMENTS_EXAMPLE_SUFFIX);
        myVdbExample.setProperty(uow, VdbLexicon.Vdb.PREVIEW, false);
        myVdbExample.setProperty(uow, VdbLexicon.Vdb.VERSION, 1);
        myVdbExample.setProperty(uow, "vdb-property2", "vdb-value2");
        myVdbExample.setProperty(uow, "vdb-property", "vdb-value");

        /*
         *      vdb:importVdbs
         *          @jcr:primaryType=vdb:importVdb
         */
        KomodoObject importVdbs = myVdbExample.addChild(uow, VdbLexicon.Vdb.IMPORT_VDBS, VdbLexicon.Vdb.IMPORT_VDBS);

        /*
         *          x
         *              @jcr:primaryType=vdb:importVdb
         *              @vdb:version=2
         *              @vdb:import-data-policies=false
         */
        KomodoObject importVdb = importVdbs.addChild(uow, "x", VdbLexicon.ImportVdb.IMPORT_VDB);
        importVdb.setProperty(uow, VdbLexicon.ImportVdb.VERSION, 2);
        importVdb.setProperty(uow, VdbLexicon.ImportVdb.IMPORT_DATA_POLICIES, false);

        /*
         *      model-one
         *          @jcr:primaryType=vdb:declarativeModel
         *          @jcr:uuid={uuid-to-be-created}
         *          @mmcore:modelType=PHYSICAL
         *          @description=model description
         *          @vdb:visible=false
         *          @model-prop=model-value-override
         */
        KomodoObject modelOne = myVdbExample.addChild(uow, "model-one", VdbLexicon.Vdb.DECLARATIVE_MODEL);
        modelOne.setProperty(uow, CoreLexicon.MODEL_TYPE, CoreLexicon.ModelType.PHYSICAL);
        modelOne.setProperty(uow, VdbLexicon.Vdb.DESCRIPTION, "model description");
        modelOne.setProperty(uow, VdbLexicon.Model.VISIBLE, false);
        modelOne.setProperty(uow, "model-prop", "model-value-override");

        /*
         *          vdb:sources
         *              @jcr:primaryType=vdb:sources
         */
        KomodoObject model1Sources = modelOne.addChild(uow, VdbLexicon.Vdb.SOURCES, VdbLexicon.Vdb.SOURCES);

        /*
         *              s1
         *                  @jcr:primaryType=vdb:source
         *                  @vdb:sourceTranslator=translator
         *                  @vdb:sourceJndiName=java:mybinding
         */
        KomodoObject model1Src1 = model1Sources.addChild(uow, "s1", VdbLexicon.Source.SOURCE);
        model1Src1.setProperty(uow, VdbLexicon.Source.TRANSLATOR, "translator");
        model1Src1.setProperty(uow, VdbLexicon.Source.JNDI_NAME, "java:mybinding");
        setModelSourceOriginConnection(uow, model1Src1, S1Connection);

        /*
         *      model-two
         *          @jcr:primaryType=vdb:declarativeModel
         *          @jcr:uuid={uuid-to-be-created}
         *          @mmcore:modelType=VIRTUAL
         *          @vdb:visible=true
         *          @model-prop=model-value
         */
        KomodoObject modelTwo = myVdbExample.addChild(uow, "model-two", VdbLexicon.Vdb.DECLARATIVE_MODEL);
        modelTwo.setProperty(uow, CoreLexicon.MODEL_TYPE, CoreLexicon.ModelType.VIRTUAL);
        modelTwo.setProperty(uow, VdbLexicon.Model.VISIBLE, true);
        modelTwo.setProperty(uow, "model-prop", "model-value");
        modelTwo.setProperty(uow, VdbLexicon.Model.METADATA_TYPE, "DDL");

        String modelDefinition = "CREATE VIEW Test AS select * FROM Test.getTest;";
        modelTwo.setProperty(uow, VdbLexicon.Model.MODEL_DEFINITION, modelDefinition);

        /*
         *          vdb:sources
         *              @jcr:primaryType=vdb:sources
         */
        KomodoObject model2Sources = modelTwo.addChild(uow, VdbLexicon.Vdb.SOURCES, VdbLexicon.Vdb.SOURCES);

        /*
         *              s1
         *                  @jcr:primaryType=vdb:source
         *                  @vdb:sourceTranslator=translator
         *                  @vdb:sourceJndiName=java:binding-one
         */
        KomodoObject model2Src1 = model2Sources.addChild(uow, "s1", VdbLexicon.Source.SOURCE);
        model2Src1.setProperty(uow, VdbLexicon.Source.TRANSLATOR, "translator");
        model2Src1.setProperty(uow, VdbLexicon.Source.JNDI_NAME, "java:binding-one");
        setModelSourceOriginConnection(uow, model2Src1, S1Connection);

        /*
         *              s2
         *                  @jcr:primaryType=vdb:source
         *                  @vdb:sourceTranslator=translator
         *                  @vdb:sourceJndiName=java:binding-two
         */
        KomodoObject model2Src2 = model2Sources.addChild(uow, "s2", VdbLexicon.Source.SOURCE);
        model2Src2.setProperty(uow, VdbLexicon.Source.TRANSLATOR, "translator");
        model2Src2.setProperty(uow, VdbLexicon.Source.JNDI_NAME, "java:binding-two");
        setModelSourceOriginConnection(uow, model2Src2, S2Connection);

        /*
         *      vdb:translators
         *          @jcr:primaryType=vdb:translators
         */
        KomodoObject translators = myVdbExample.addChild(uow, VdbLexicon.Vdb.TRANSLATORS, VdbLexicon.Vdb.TRANSLATORS);

        /*
         *          oracleOverride
         *              @jcr:primaryType=vdb:translator
         *              @vdb:description=hello world
         *              @vdb:type=oracle
         *              my-property=my-value
         */
        KomodoObject oraTranslator = translators.addChild(uow, "oracleOverride", VdbLexicon.Translator.TRANSLATOR);
        oraTranslator.setProperty(uow, VdbLexicon.Translator.DESCRIPTION, "hello world");
        oraTranslator.setProperty(uow, VdbLexicon.Translator.TYPE, "oracle");
        oraTranslator.setProperty(uow, "my-property", "my-value");

        /*
         *      vdb:dataRoles
         *          @jcr:primaryType=vdb:dataRoles
         */
        KomodoObject dataRoles = myVdbExample.addChild(uow, VdbLexicon.Vdb.DATA_ROLES, VdbLexicon.Vdb.DATA_ROLES);

        /*
         *          roleOne
         *              @jcr:primaryType=vdb:dataRole
         *              @vdb:anyAuthenticated=false
         *              @vdb:grantAll=true
         *              @vdb:allowCreateTemporaryTables=true
         *              @vdb:description=roleOne described
         *              @vdb:mappedRoleNames=ROLE1, ROLE2
         */
        KomodoObject dataRole1 = dataRoles.addChild(uow, "roleOne", VdbLexicon.DataRole.DATA_ROLE);
        dataRole1.setProperty(uow, VdbLexicon.Translator.DESCRIPTION, "roleOne described");
        dataRole1.setProperty(uow, VdbLexicon.DataRole.ANY_AUTHENTICATED, false);
        dataRole1.setProperty(uow, VdbLexicon.DataRole.GRANT_ALL, true);
        dataRole1.setProperty(uow, VdbLexicon.DataRole.ALLOW_CREATE_TEMP_TABLES, true);
        dataRole1.setProperty(uow, VdbLexicon.DataRole.MAPPED_ROLE_NAMES, "ROLE1", "ROLE2");

        /*
         *              vdb:permissions
         *                  @jcr:primaryType=vdb:permissions
         */
        KomodoObject permissions = dataRole1.addChild(uow, VdbLexicon.DataRole.PERMISSIONS, VdbLexicon.DataRole.PERMISSIONS);

        /*
         *                  myTable.T1
         *                      @jcr.primaryType=vdb:permission
         *                      @allowRead=true
         */
        KomodoObject permission1 = permissions.addChild(uow, "myTable.T1", VdbLexicon.DataRole.Permission.PERMISSION);
        permission1.setProperty(uow, VdbLexicon.DataRole.Permission.ALLOW_READ, true);

        /*
         *                  myTable.T2
         *                      @jcr.primaryType=vdb:permission
         *                      @allowCreate=true
         *                      @allowRead=false
         *                      @allowUpdate=true
         *                      @allowDelete=true
         *                      @allowExecute=true
         *                      @allowAlter=true
         */
        KomodoObject permission2 = permissions.addChild(uow, "myTable.T2", VdbLexicon.DataRole.Permission.PERMISSION);
        permission2.setProperty(uow, VdbLexicon.DataRole.Permission.ALLOW_CREATE, true);
        permission2.setProperty(uow, VdbLexicon.DataRole.Permission.ALLOW_READ, false);
        permission2.setProperty(uow, VdbLexicon.DataRole.Permission.ALLOW_UPDATE, true);
        permission2.setProperty(uow, VdbLexicon.DataRole.Permission.ALLOW_DELETE, true);
        permission2.setProperty(uow, VdbLexicon.DataRole.Permission.ALLOW_EXECUTE, true);
        permission2.setProperty(uow, VdbLexicon.DataRole.Permission.ALLOW_ALTER, true);

        /*
         *                      vdb:conditions
         *                          @jcr:primaryType=vdb:conditions
         */
        KomodoObject conditions = permission2.addChild(uow, VdbLexicon.DataRole.Permission.CONDITIONS, VdbLexicon.DataRole.Permission.CONDITIONS);

        /*
         *                          col1 = user()
         *                              @jcr:primaryType=vdb:condition
         *                              @vdb:constraint=false
         */
        KomodoObject condition = conditions.addChild(uow, "col1 = user()", VdbLexicon.DataRole.Permission.Condition.CONDITION);
        condition.setProperty(uow, VdbLexicon.DataRole.Permission.Condition.CONSTRAINT, false);

        /*
         *                  myTable.T2.col1
         *                      @jcr.primaryType=vdb:permission
         */
        KomodoObject permission3 = permissions.addChild(uow, "myTable.T2.col1", VdbLexicon.DataRole.Permission.PERMISSION);

        /*
         *                      vdb:masks
         *                          @jcr:primaryType=vdb:masks
         */
        KomodoObject masks = permission3.addChild(uow, VdbLexicon.DataRole.Permission.MASKS, VdbLexicon.DataRole.Permission.MASKS);

        /*
         *                          col2
         *                              @jcr:primaryType=vdb:mask
         *                              @vdb:order=1
         */
        KomodoObject mask = masks.addChild(uow, "col2", VdbLexicon.DataRole.Permission.Mask.MASK);
        mask.setProperty(uow, VdbLexicon.DataRole.Permission.Mask.ORDER, 1);

        /*
         *                  javascript
         *                      @jcr.primaryType=vdb:permission
         *                      @allowLanguage=true
         */
        KomodoObject permission4 = permissions.addChild(uow, "javascript", VdbLexicon.DataRole.Permission.PERMISSION);
        permission4.setProperty(uow, VdbLexicon.DataRole.Permission.ALLOW_LANGUAGE, true);

        return myVdbExample;
    }

    /**
     * Creates the structure of the tweet example vdb
     *
     * @param parentKomodoObject parent to append the new vdb
     * @return the new vdb node
     * @throws Exception if error occurs
     */
    public static KomodoObject createTweetExampleNoTransDescripNode(UnitOfWork uow, KomodoObject parentObject) throws Exception {
        /*
         * Create twitter connection to base the model source on
         */
        KomodoObject twitterConnection = parentObject.addChild(uow,
                                                               TWITTER_CONNECTION,
                                                                DataVirtLexicon.Connection.NODE_TYPE);

        /*
         * tweet-example-vdb.xml
         *      @jcr:primaryType=vdb:virtualDatabase
         *      @jcr:mixinTypes=[mode:derived,mix:referenceable]
         *      @jcr:uuid={uuid-to-be-created}
         *      @mode:sha1={sha1-to-be-created}
         *      @vdb:preview=false
         *      @vdb:version=1
         *      @vdb:originalFile=/vdbs/declarativeModels-vdb.xml
         *      @vdb:name=twitter
         *      @vdb:description=Shows how to call Web Services
         *      @UseConnectorMetadata=cached
         */
        KomodoObject tweetExample = parentObject.addChild(uow,
                                                                    TWEET_EXAMPLE_NAME + TWEET_EXAMPLE_SUFFIX,
                                                                    VdbLexicon.Vdb.VIRTUAL_DATABASE);
        tweetExample.addDescriptor(uow, "mode:derived", "mix:referenceable");
        tweetExample.setProperty(uow, VdbLexicon.Vdb.NAME, "twitter");
        tweetExample.setProperty(uow, VdbLexicon.Vdb.DESCRIPTION, "Shows how to call Web Services");
    
        // Miscellaneous property
        tweetExample.setProperty(uow, "UseConnectorMetadata", "cached");
    
        tweetExample.setProperty(uow, VdbLexicon.Vdb.ORIGINAL_FILE, "/vdbs/" + TWEET_EXAMPLE_NAME + TWEET_EXAMPLE_SUFFIX);
        tweetExample.setProperty(uow, VdbLexicon.Vdb.PREVIEW, false);
        tweetExample.setProperty(uow, VdbLexicon.Vdb.VERSION, 1);
    
        /*
         *      vdb:translators
         *          @jcr:primaryType=vdb:translators
         */
        KomodoObject translators = tweetExample.addChild(uow, VdbLexicon.Vdb.TRANSLATORS, VdbLexicon.Vdb.TRANSLATORS);
    
        /*
         *          rest
         *              @jcr:primaryType=vdb:translator
         *              @DefaultServiceMode=MESSAGE
         *              @DefaultBinding=HTTP
         *              @vdb:type=ws
         *              @vdb:description=Rest Web Service translator
         */
        KomodoObject rest = translators.addChild(uow, REST_TRANSLATOR, VdbLexicon.Translator.TRANSLATOR);
        rest.setProperty(uow, "DefaultServiceMode", "MESSAGE");
        rest.setProperty(uow, "DefaultBinding", "HTTP");
        rest.setProperty(uow, VdbLexicon.Translator.TYPE, "ws");
    
        /*
         *      twitter
         *          @jcr:primaryType=vdb:declarativeModel
         *          @jcr:uuid={uuid-to-be-created}
         *          @mmcore:modelType=PHYSICAL
         *          @vdb:sourceTranslator=rest
         *          @vdb:sourceName=twitter
         *          @vdb:metadataType=DDL
         *          @vdb:visible=true
         *          @vdb:sourceJndiName=java:/twitterDS
         */
        KomodoObject twitter = tweetExample.addChild(uow, TWITTER_MODEL, VdbLexicon.Vdb.DECLARATIVE_MODEL);
        twitter.setProperty(uow, CoreLexicon.MODEL_TYPE, CoreLexicon.ModelType.PHYSICAL);
        twitter.setProperty(uow, VdbLexicon.Model.VISIBLE, true);
        twitter.setProperty(uow, VdbLexicon.Model.METADATA_TYPE, "DDL");
    
        /*
         *          vdb:sources
         *              @jcr:primaryType=vdb:sources
         */
        KomodoObject twitterSources = twitter.addChild(uow, VdbLexicon.Vdb.SOURCES, VdbLexicon.Vdb.SOURCES);
    
        /*
         *              twitter
         *                  @jcr:primaryType=vdb:source
         *                  @vdb:sourceTranslator=rest
         *                  @vdb:sourceJndiName=java:/twitterDS
         */
        KomodoObject twitterSource = twitterSources.addChild(uow, TWITTER_MODEL, VdbLexicon.Source.SOURCE);
        twitterSource.setProperty(uow, VdbLexicon.Source.TRANSLATOR, REST_TRANSLATOR);
        twitterSource.setProperty(uow, VdbLexicon.Source.JNDI_NAME, "java:/twitterDS");
        setModelSourceOriginConnection(uow, twitterSource, twitterConnection);
    
        /*
         *      twitterview
         *          @jcr:primaryType=vdb:declarativeModel
         *          @jcr:uuid={uuid-to-be-created}
         *          @mmcore:modelType=VIRTUAL
         *          @vdb:visible=true
         *          @vdb:metadataType=DDL
         *          @vdb:modelDefinition=CREATE VIRTUAL PROCEDURE getTweets(query varchar) RETURNS (created_on varchar(25), from_user varchar(25), to_user varchar(25), profile_image_url varchar(25), source varchar(25), text varchar(140)) AS select tweet.* from (call twitter.invokeHTTP(action => 'GET', endpoint =>querystring('',query as "q"))) w, XMLTABLE('results' passing JSONTOXML('myxml', w.result) columns created_on string PATH 'created_at', from_user string PATH 'from_user', to_user string PATH 'to_user', profile_image_url string PATH 'profile_image_url', source string PATH 'source', text string PATH 'text') tweet; CREATE VIEW Tweet AS select * FROM twitterview.getTweets;
         */
        KomodoObject twitterView = tweetExample.addChild(uow, TWITTER_VIEW_MODEL, VdbLexicon.Vdb.DECLARATIVE_MODEL);
        twitterView.setProperty(uow, CoreLexicon.MODEL_TYPE, CoreLexicon.ModelType.VIRTUAL);
        twitterView.setProperty(uow, VdbLexicon.Model.METADATA_TYPE, "DDL");
        twitterView.setProperty(uow, VdbLexicon.Model.VISIBLE, true);
        twitterView.setProperty(uow, VdbLexicon.Model.MODEL_DEFINITION, TWITTER_VIEW_MODEL_DDL);
    
        return tweetExample;
    }

    /**
     * Creates the structure of the patient example vdb
     *
     * @param uow the transaction
     * @param parentObject parent to append the new vdb
     * @return the new vdb node
     * @throws KException if error occurs
     */
    public static KomodoObject createPatientsExampleNode(UnitOfWork uow, KomodoObject parentObject) throws KException {

        /*
         * patients-vdb.xml
         *      @jcr:primaryType=vdb:virtualDatabase
         *      @jcr:mixinTypes=[mode:derived,mix:referenceable]
         *      @jcr:uuid={uuid-to-be-created}
         *      @mode:sha1={sha1-to-be-created}
         *      @vdb:preview=false
         *      @vdb:version=1
         *      @vdb:originalFile=/vdbs/patients-vdb.xml
         *      @vdb:name=patients
         *      @vdb:description=Sample Dataservice for patient records
         *      @vdb:connectionType=BY_VERSION
         */
        KomodoObject patientsVdb = parentObject.addChild(uow,
                                                  PATIENTS_VDB_FILE,
                                                  VdbLexicon.Vdb.VIRTUAL_DATABASE);
        patientsVdb.addDescriptor(uow, "mode:derived", "mix:referenceable");
        patientsVdb.setProperty(uow, VdbLexicon.Vdb.NAME, "patients");
        patientsVdb.setProperty(uow, VdbLexicon.Vdb.DESCRIPTION, "Sample Dataservice for patient records");
        patientsVdb.setProperty(uow, VdbLexicon.Vdb.CONNECTION_TYPE, "BY_VERSION");

        patientsVdb.setProperty(uow, VdbLexicon.Vdb.ORIGINAL_FILE, "/vdbs/" + PATIENTS_VDB_FILE);
        patientsVdb.setProperty(uow, VdbLexicon.Vdb.PREVIEW, false);
        patientsVdb.setProperty(uow, VdbLexicon.Vdb.VERSION, 1);

        /*
         *      Patients
         *          @jcr:primaryType=vdb:declarativeModel
         *          @jcr:uuid={uuid-to-be-created}
         *          @mmcore:modelType=VIRTUAL
         *          @description=Example Patient Service
         *          @vdb:metadataType=DDL
         *          @vdb:visible=true
         */
        KomodoObject patientsModel = patientsVdb.addChild(uow,
                                                                                    PATIENTS_MODEL,
                                                                                    VdbLexicon.Vdb.DECLARATIVE_MODEL);
        patientsModel.setProperty(uow, CoreLexicon.MODEL_TYPE, CoreLexicon.ModelType.VIRTUAL);
        patientsModel.setProperty(uow,  VdbLexicon.Model.DESCRIPTION, "Example Patient Service");
        patientsModel.setProperty(uow, VdbLexicon.Model.VISIBLE, true);
        patientsModel.setProperty(uow, VdbLexicon.Model.METADATA_TYPE, "DDL");
        StringBuffer patientsModelDefn = new StringBuffer();
        patientsModelDefn.append("CREATE VIEW TheServiceView (")
                            .append("id long, ")
                            .append("firstName clob, ")
                            .append("lastName clob, ")
                            .append("gender clob, ")
                            .append("age long, ")
                            .append("currentSmoker boolean, ")
                            .append("lastPrimaryCareVisit timestamp, ")
                            .append("PRIMARY KEY(id) ")
                            .append(") ")
                            .append("AS ")
                            .append("SELECT id, firstName, lastName, gender, age, currentSmoker, lastPrimaryCareVisit FROM vdbwebtest.PATIENT;");
        patientsModel.setProperty(uow, VdbLexicon.Model.MODEL_DEFINITION, patientsModelDefn.toString());

        /*
         * Create patients connection to base the model source on
         */
        KomodoObject patientsConnection = parentObject.addChild(uow,
                                                               PATIENTS_CONNECTION,
                                                                DataVirtLexicon.Connection.NODE_TYPE);

        /*
         *      PatientSource
         *          @jcr:primaryType=vdb:declarativeModel
         *          @jcr:uuid={uuid-to-be-created}
         *          @mmcore:modelType=PHYSICAL
         *          @vdb:sourceTranslator=mysql5
         *          @vdb:sourceName=PatientSource
         *          @vdb:metadataType=DDL
         *          @vdb:visible=true
         *          @vdb:sourceJndiName=java:/MySqlPatients
         */
        KomodoObject patientSourceModel = patientsVdb.addChild(uow,
                                                                                    PATIENTS_SOURCE,
                                                                                    VdbLexicon.Vdb.DECLARATIVE_MODEL);
        patientSourceModel.setProperty(uow, CoreLexicon.MODEL_TYPE, CoreLexicon.ModelType.PHYSICAL);
        patientSourceModel.setProperty(uow, VdbLexicon.Model.VISIBLE, true);
        patientSourceModel.setProperty(uow, VdbLexicon.Model.METADATA_TYPE, "DDL");

        /*
         *          vdb:sources
         *              @jcr:primaryType=vdb:sources
         */
        KomodoObject patientSources = patientSourceModel.addChild(uow,
                                                                                    VdbLexicon.Vdb.SOURCES,
                                                                                    VdbLexicon.Vdb.SOURCES);

        /*
         *              PatientSource
         *                  @jcr:primaryType=vdb:source
         *                  @vdb:sourceTranslator=mysql5
         *                  @vdb:sourceJndiName=java:/MySqlPatients
         *                  @vdb:associatedConnection={uuid}
         */
        KomodoObject patientSource = patientSources.addChild(uow,
                                                                                               PATIENTS_SOURCE,
                                                                                               VdbLexicon.Source.SOURCE);
        patientSource.setProperty(uow, VdbLexicon.Source.TRANSLATOR, "mysql5");
        patientSource.setProperty(uow, VdbLexicon.Source.JNDI_NAME, "java:/MySqlPatients");
        setModelSourceOriginConnection(uow, patientSource, patientsConnection);

        return patientsVdb;
    }

    /**
     * Creates the structure of the tweet example vdb
     *
     * @param uow the transaction
     * @param parentObject parent to append the new vdb
     * @return the new vdb node
     * @throws KException if error occurs
     */
    public static KomodoObject createTweetExampleNode(UnitOfWork uow, KomodoObject parentObject) throws KException {

        /*
         * Create twitter connection to base the model source on
         */
        KomodoObject twitterConnection = parentObject.addChild(uow,
                                                               TWITTER_CONNECTION,
                                                                DataVirtLexicon.Connection.NODE_TYPE);
        
        /*
         * tweet-example-vdb.xml
         *      @jcr:primaryType=vdb:virtualDatabase
         *      @jcr:mixinTypes=[mode:derived,mix:referenceable]
         *      @jcr:uuid={uuid-to-be-created}
         *      @mode:sha1={sha1-to-be-created}
         *      @vdb:preview=false
         *      @vdb:version=1
         *      @vdb:originalFile=/vdbs/declarativeModels-vdb.xml
         *      @vdb:name=twitter
         *      @vdb:description=Shows how to call Web Services
         *      @UseConnectorMetadata=cached
         */
        KomodoObject tweetExample = parentObject.addChild(uow,
                                                  TWEET_EXAMPLE_NAME + TWEET_EXAMPLE_SUFFIX,
                                                  VdbLexicon.Vdb.VIRTUAL_DATABASE);
        tweetExample.addDescriptor(uow, "mode:derived", "mix:referenceable");
        tweetExample.setProperty(uow, VdbLexicon.Vdb.NAME, "twitter");
        tweetExample.setProperty(uow, VdbLexicon.Vdb.DESCRIPTION, "Shows how to call Web Services");
    
        // Miscellaneous property
        tweetExample.setProperty(uow, "UseConnectorMetadata", "cached");
    
        tweetExample.setProperty(uow, VdbLexicon.Vdb.ORIGINAL_FILE, "/vdbs/" + TWEET_EXAMPLE_NAME + TWEET_EXAMPLE_SUFFIX);
        tweetExample.setProperty(uow, VdbLexicon.Vdb.PREVIEW, false);
        tweetExample.setProperty(uow, VdbLexicon.Vdb.VERSION, 1);
    
        /*
         *      vdb:translators
         *          @jcr:primaryType=vdb:translators
         */
        KomodoObject translators = tweetExample.addChild(uow,
                                                                            VdbLexicon.Vdb.TRANSLATORS,
                                                                            VdbLexicon.Vdb.TRANSLATORS);
    
        /*
         *          rest
         *              @jcr:primaryType=vdb:translator
         *              @DefaultServiceMode=MESSAGE
         *              @DefaultBinding=HTTP
         *              @vdb:type=ws
         *              @vdb:description=Rest Web Service translator
         */
        KomodoObject rest = translators.addChild(uow,
                                                                          REST_TRANSLATOR,
                                                                          VdbLexicon.Translator.TRANSLATOR);
        rest.setProperty(uow, VdbLexicon.Translator.DESCRIPTION, "Rest Web Service translator");
        rest.setProperty(uow, "DefaultServiceMode", "MESSAGE");
        rest.setProperty(uow, "DefaultBinding", "HTTP");
        rest.setProperty(uow, VdbLexicon.Translator.TYPE, "ws");
    
        /*
         *      twitter
         *          @jcr:primaryType=vdb:declarativeModel
         *          @jcr:uuid={uuid-to-be-created}
         *          @mmcore:modelType=PHYSICAL
         *          @vdb:sourceTranslator=rest
         *          @vdb:sourceName=twitter
         *          @vdb:metadataType=DDL
         *          @vdb:visible=true
         *          @vdb:sourceJndiName=java:/twitterDS
         */
        KomodoObject twitter = tweetExample.addChild(uow,
                                                                                    TWITTER_MODEL,
                                                                                    VdbLexicon.Vdb.DECLARATIVE_MODEL);
        twitter.setProperty(uow, CoreLexicon.MODEL_TYPE, CoreLexicon.ModelType.PHYSICAL);
        twitter.setProperty(uow, VdbLexicon.Model.VISIBLE, true);
        twitter.setProperty(uow, VdbLexicon.Model.METADATA_TYPE, "DDL");
    
        /*
         *          vdb:sources
         *              @jcr:primaryType=vdb:sources
         */
        KomodoObject twitterSources = twitter.addChild(uow,
                                                                                    VdbLexicon.Vdb.SOURCES,
                                                                                    VdbLexicon.Vdb.SOURCES);

        /*
         *              twitter
         *                  @jcr:primaryType=vdb:source
         *                  @vdb:sourceTranslator=rest
         *                  @vdb:sourceJndiName=java:/twitterDS
         *                  @vdb:associatedConnection={uuid}
         */
        KomodoObject twitterSource = twitterSources.addChild(uow,
                                                                                               TWITTER_MODEL,
                                                                                               VdbLexicon.Source.SOURCE);
        twitterSource.setProperty(uow, VdbLexicon.Source.TRANSLATOR, REST_TRANSLATOR);
        twitterSource.setProperty(uow, VdbLexicon.Source.JNDI_NAME, "java:/twitterDS");
        setModelSourceOriginConnection(uow, twitterSource, twitterConnection);

        /*
         *      twitterview
         *          @jcr:primaryType=vdb:declarativeModel
         *          @jcr:uuid={uuid-to-be-created}
         *          @mmcore:modelType=VIRTUAL
         *          @vdb:visible=true
         *          @vdb:metadataType=DDL
         *          @vdb:modelDefinition=CREATE VIRTUAL PROCEDURE getTweets(query varchar) RETURNS (created_on varchar(25), from_user varchar(25), to_user varchar(25), profile_image_url varchar(25), source varchar(25), text varchar(140)) AS select tweet.* from (call twitter.invokeHTTP(action => 'GET', endpoint =>querystring('',query as "q"))) w, XMLTABLE('results' passing JSONTOXML('myxml', w.result) columns created_on string PATH 'created_at', from_user string PATH 'from_user', to_user string PATH 'to_user', profile_image_url string PATH 'profile_image_url', source string PATH 'source', text string PATH 'text') tweet; CREATE VIEW Tweet AS select * FROM twitterview.getTweets;
         */
        KomodoObject twitterView = tweetExample.addChild(uow,
                                                                                           TWITTER_VIEW_MODEL,
                                                                                           VdbLexicon.Vdb.DECLARATIVE_MODEL);
        twitterView.setProperty(uow, CoreLexicon.MODEL_TYPE, CoreLexicon.ModelType.VIRTUAL);
        twitterView.setProperty(uow, VdbLexicon.Model.METADATA_TYPE, "DDL");
        twitterView.setProperty(uow, VdbLexicon.Model.VISIBLE, true);
    
        StringBuffer modelDefinition = new StringBuffer();
        modelDefinition.append("CREATE VIRTUAL PROCEDURE getTweets(IN query varchar) ")
                                .append("RETURNS TABLE (created_on varchar(25), from_user varchar(25), ")
                                .append("to_user varchar(25), profile_image_url varchar(25), source ")
                                .append("varchar(25), text varchar(140)) AS select tweet.* from ")
                                .append("(EXEC twitter.invokeHTTP(")
                                .append("action => 'GET', endpoint => querystring(\'', query as q))) AS w, ")
                                .append("XMLTABLE('results' passing JSONTOXML('myxml', w.result) columns ")
                                .append("created_on string PATH 'created_at', from_user string PATH 'from_user', ")
                                .append("to_user string PATH 'to_user', profile_image_url string PATH 'profile_image_url', ")
                                .append("source string PATH 'source', text string PATH 'text') AS tweet; ")
                                .append("CREATE VIEW Tweet AS select * FROM twitterview.getTweets;");
        twitterView.setProperty(uow, VdbLexicon.Model.MODEL_DEFINITION, modelDefinition.toString());
    
        return tweetExample;
    }

    private static void setModelSourceOriginConnection(UnitOfWork uow, KomodoObject source, KomodoObject connection) throws KException {
        source.setProperty(uow, VdbLexicon.Source.ORIGIN_CONNECTION, connection.getName(uow));
    }

    /**
     * @return input stream of portfolio example xml
     * @throws Exception if error occurs
     */
    public static InputStream dataserviceVdbExample() throws Exception {
        return getResourceAsStream(TestUtilities.class,
                                   RESOURCES_DIRECTORY,
                                   DATASERVICE_VDB_FILE);
    }

    /**
     * @return input stream of sample data service example
     * @throws Exception if error occurs
     */
    public static InputStream sampleDataserviceExample() throws Exception {
        return getResourceAsStream(TestUtilities.class,
                                   RESOURCES_DIRECTORY,
                                   SAMPLE_DATASERVICE_FILE);
    }

    /**
     * @return input stream of us-states sql schema
     * @throws Exception if error occurs
     */
    public static InputStream usStatesSql() throws Exception {
            return getResourceAsStream(TestUtilities.class,
                                       RESOURCES_DIRECTORY,
                                       US_STATES_SQL_FILE);
    }

    /**
     * @return input stream of us-states data service example
     * @throws Exception if error occurs
     */
    public static InputStream usStatesDataserviceExample() throws Exception {
            return getResourceAsStream(TestUtilities.class,
                                       RESOURCES_DIRECTORY,
                                       US_STATES_DATASERVICE_TEIID_FILE);
    }

    /**
     * @return input stream of DSB single source parts dataservice
     * @throws Exception if error occurs
     */
    public static InputStream dsbDataserviceSingleSourceParts() throws Exception {
        return getResourceAsStream(TestUtilities.class,
                                   RESOURCES_DIRECTORY,
                                   PARTS_SINGLE_SOURCE_FILE);
    }

    /**
     * @return input stream of DSB join dataservice with different table names
     * @throws Exception if error occurs
     */
    public static InputStream dsbDataserviceJoinDifferentTableNames() throws Exception {
        return getResourceAsStream(TestUtilities.class,
                                   RESOURCES_DIRECTORY,
                                   JOIN_DIFFERENT_TABLE_NAMES_FILE);
    }

    /**
     * @return input stream of DSB join dataservice with same table names
     * @throws Exception if error occurs
     */
    public static InputStream dsbDataserviceJoinSameTableNames() throws Exception {
        return getResourceAsStream(TestUtilities.class,
                                   RESOURCES_DIRECTORY,
                                   JOIN_SAME_TABLE_NAMES_FILE);
    }

    /**
     * @return input stream of portfolio example xml
     * @throws Exception if error occurs
     */
    public static InputStream portfolioExample() throws Exception {
        return getResourceAsStream(TestUtilities.class,
                                   RESOURCES_DIRECTORY,
                                   PORTFOLIO_VDB_FILE);
    }

    /**
     * @return input stream of patients ddl
     * @throws Exception if error occurs
     */
    public static InputStream patientsDdl() throws Exception {
        return getResourceAsStream(TestUtilities.class,
                                   RESOURCES_DIRECTORY,
                                   PATIENTS_DDL_FILE);
    }

    /**
     * @return input stream of parts example xml
     * @throws Exception if error occurs
     */
    public static InputStream partsExample() throws Exception {
        return getResourceAsStream(TestUtilities.class,
                                   RESOURCES_DIRECTORY,
                                   PARTS_VDB_FILE);
    }

    /**
     * @return input stream of parts example xml
     * @throws Exception if error occurs
     */
    public static InputStream partsWithKeysExample() throws Exception {
        return getResourceAsStream(TestUtilities.class,
                                   RESOURCES_DIRECTORY,
                                   PARTS_WITHKEYS_VDB_FILE);
    }

    /**
     * @return input stream of usstates service source xml
     * @throws Exception if error occurs
     */
    public static InputStream usStatesSourceExample() throws Exception {
        return getResourceAsStream(TestUtilities.class,
                                   RESOURCES_DIRECTORY,
                                   USSTATES_SOURCE_VDB_FILE);
    }

    /**
     * @return input stream of sample xml
     * @throws Exception if error occurs
     */
    public static InputStream sampleExample() throws Exception {
        return getResourceAsStream(TestUtilities.class,
                                   RESOURCES_DIRECTORY,
                                   SAMPLE_VDB_FILE);
    }

    /**
     * @return input stream of roles xml
     * @throws Exception if error occurs
     */
    public static InputStream rolesExample() throws Exception {
        return getResourceAsStream(TestUtilities.class,
                                   RESOURCES_DIRECTORY,
                                   ROLES_VDB_FILE);
    }

    /**
     * @return input stream of mysql driver
     * @throws Exception
     */
    public static InputStream mySqlDriver() throws Exception {
        return getResourceAsStream(TestUtilities.class,
                                  RESOURCES_DIRECTORY,
                                  MYSQL_DRIVER_FILENAME);
    }

    /**
     * @param contentFile file to extract contents
     * @return the contents of the given file as a string
     *                Note: an extra \n character will proceed the string
     * @throws Exception if error occurs
     */
    public static String fileToString(File contentFile) throws Exception {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(contentFile));

            String line;
            StringBuffer buf = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                buf.append(line).append(NEW_LINE);
            }

            return buf.toString();
        } finally {
            if (reader != null)
                reader.close();
        }
    }

    /**
     * @param klazz class related to the resource
     * @param parentDirectory parent directory of the resource location
     * @param fileName file name of the resource
     * @param suffix suffix of the resource
     *
     * @return input stream of the resource referred to by the parameters
     *
     * @throws Exception if error occurs
     */
    public static InputStream getResourceAsStream(Class<?> klazz, String parentDirectory, String fileName, String suffix) throws Exception {
        String filePath;
        if (parentDirectory == null || parentDirectory.isEmpty())
            filePath = fileName + suffix;
        else
            filePath = parentDirectory + FORWARD_SLASH + fileName + suffix;

        InputStream fileStream = klazz.getClassLoader().getResourceAsStream(filePath);
        assertNotNull("File " + filePath + " does not exist", fileStream);

        return fileStream;
    }

    /**
     * @param klazz class related to the resource
     * @param parentDirectory parent directory of the resource location
     * @param fileName full file name, including suffix
     *
     * @return input stream of the resource referred to by the parameters
     *
     * @throws Exception if error occurs
     */
    public static InputStream getResourceAsStream(Class<?> klazz, String parentDirectory, String fileName) throws Exception {
        return getResourceAsStream(klazz, parentDirectory, fileName, EMPTY_STRING);
    }

    /**
     * @param inStream input stream
     * @return String representation of stream
     * @throws IOException if error occurs
     */
    public static byte[] streamToBytes(InputStream inStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            int nRead;
            byte[] data = new byte[16384];

            while ((nRead = inStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();

            return buffer.toByteArray();
        } finally {
            buffer.close();
            inStream.close();
        }
    }

    /**
     * @param inStream input stream
     * @return String representation of stream
     * @throws IOException if error occurs
     */
    public static String streamToString(InputStream inStream) throws IOException {
        InputStreamReader in = new InputStreamReader(inStream);
        BufferedReader reader = new BufferedReader(in);

        try {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(NEW_LINE);
            }

            return builder.toString();
        } finally {
            reader.close();
            in.close();
            inStream.close();
        }
    }

    /**
     * Create a document from the given xml string
     *
     * @param xml the string of xml
     * @param errorHandler handler for validation errors
     * @return the new document
     * @throws Exception if error occurs
     */
    public static Document createDocument(String xml, ErrorHandler errorHandler) throws Exception {
        xml = xml.replaceAll(NEW_LINE, SPACE);
        xml = xml.replaceAll(">[\\s]+<", CLOSE_ANGLE_BRACKET + OPEN_ANGLE_BRACKET);
        xml = xml.replaceAll("[\\s]+", SPACE);
        xml = xml.replaceAll("CDATA\\[[\\s]+", "CDATA[");
        xml = xml.replaceAll("; \\]\\]", ";]]");

        InputStream is = null;
        Schema schema = null;
        try {
            String language = XMLConstants.W3C_XML_SCHEMA_NS_URI;
            SchemaFactory factory = SchemaFactory.newInstance(language);
            is = TestUtilities.teiidVdbXsd();
            Source source = new StreamSource(is);
            schema = factory.newSchema(source);           
        } finally {
            if (is != null)
                is.close();
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setIgnoringComments(true);
        dbf.setNamespaceAware(true);
        dbf.setValidating(true);
        dbf.setSchema(schema);
        DocumentBuilder db = dbf.newDocumentBuilder();
        if (errorHandler != null)
            db.setErrorHandler(errorHandler);

        Document doc = db.parse(new InputSource(new StringReader(xml)));
        doc .setXmlStandalone(true);
        doc.normalizeDocument();
        return doc;
    }

    /**
     * Create a document from the given xml stream
     *
     * @param xmlStream the stream of xml
     * @param errorHandler handler for validation errors
     * @return the new document
     * @throws Exception if error occurs
     */
    public static Document createDocument(InputStream xmlStream, ErrorHandler errorHandler) throws Exception {
        String xml = streamToString(xmlStream);
        return createDocument(xml, errorHandler);
    }

    private static boolean compareAttributes(Element expectedElement, Element actualElement, StringBuilder errorMessages) {
        NamedNodeMap expectedAttrs = expectedElement.getAttributes();
        NamedNodeMap actualAttrs = actualElement.getAttributes();
        if (expectedAttrs.getLength() != actualAttrs.getLength()) {
            errorMessages.append(OPEN_ANGLE_BRACKET + expectedElement.getNodeName() + CLOSE_ANGLE_BRACKET +
                                 " has different number of attributes (" + expectedAttrs.getLength() + ") to " +
                                 OPEN_ANGLE_BRACKET + actualElement.getNodeName() + CLOSE_ANGLE_BRACKET +
                                 " (" + actualAttrs.getLength() + ")" + NEW_LINE);
            errorMessages.append(TAB + "Expected Attributes:" + NEW_LINE);
            for (int i = 0; i < expectedAttrs.getLength(); ++i) {
                errorMessages.append(TAB + expectedAttrs.item(i).getNodeName() + NEW_LINE);
            }
            errorMessages.append(NEW_LINE + TAB + "Actual Attributes:" + NEW_LINE);
            for (int i = 0; i < actualAttrs.getLength(); ++i) {
                errorMessages.append(TAB + actualAttrs.item(i).getNodeName() + NEW_LINE);
            }
            return false;
        }

        for (int i = 0; i < expectedAttrs.getLength(); i++) {
            Attr expectedAttr = (Attr)expectedAttrs.item(i);

            Attr actualAttr = null;
            if (expectedAttr.getNamespaceURI() == null)
                actualAttr = (Attr)actualAttrs.getNamedItem(expectedAttr.getName());
            else
                actualAttr = (Attr)actualAttrs.getNamedItemNS(expectedAttr.getNamespaceURI(), expectedAttr.getLocalName());

            if (actualAttr == null) {
                errorMessages.append("Attribute " + expectedAttr.getName() + " is missing from " + actualElement.getNodeName() + NEW_LINE);
                return false;
            }

            if (! expectedAttr.getValue().equals(actualAttr.getValue())) {
                errorMessages.append("Attribute '" + expectedAttr.getName() +
                                     "' expected value of '" + expectedAttr.getValue() + "' but was '" + actualAttr.getValue() +
                                     "' in " + OPEN_ANGLE_BRACKET + expectedElement.getNodeName() +
                                     CLOSE_ANGLE_BRACKET + NEW_LINE);
                return false;
            }
        }

        return true;
    }

    private static boolean compareElements(org.w3c.dom.Node expected, org.w3c.dom.Node actual, StringBuilder errorMessages) {
        Element expectedElement = (Element) expected;
        Element actualElement = (Element) actual;
        String errorMsg = "Failed to match <" + expected.getNodeName() + "> with <" + actual.getNodeName() + ">: ";

        // compare element names
        if (expectedElement.getLocalName() != null) {
            if (! expectedElement.getLocalName().equals(actualElement.getLocalName())) {
                errorMessages.append(errorMsg + " Different name\n");
                return false;
            }
        }

        // compare element ns
        if (expectedElement.getNamespaceURI() != null) {
            if (! expectedElement.getNamespaceURI().equals(actualElement.getNamespaceURI())) {
                errorMessages.append(errorMsg + " Different namespace URI\n");
                return false;
            }
        }

        // compare attributes
        if (! compareAttributes(expectedElement, actualElement, errorMessages)) {
            return false;
        }

        // compare children
        NodeList expectedChildren = expectedElement.getChildNodes();
        NodeList actualChildren = actualElement.getChildNodes();
        if (expectedChildren.getLength() != actualChildren.getLength()) {
            errorMessages.append(errorMsg + " Different number of children: Expected = " + expectedChildren.getLength() + "\t Actual = " + actualChildren.getLength() + NEW_LINE);
            errorMessages.append(TAB + "Expected:" + NEW_LINE);
            for (int i = 0; i < expectedChildren.getLength(); ++i) {
                errorMessages.append(TAB + expectedChildren.item(i).getNodeName() + NEW_LINE);
            }
            errorMessages.append(NEW_LINE + TAB + "Actual:" + NEW_LINE);
            for (int i = 0; i < actualChildren.getLength(); ++i) {
                errorMessages.append(TAB + actualChildren.item(i).getNodeName() + NEW_LINE);
            }
            return false;
        }

        // Same number but could be in a different order
        for (int i = 0; i < expectedChildren.getLength(); ++i) {
            org.w3c.dom.Node expectedChild = expectedChildren.item(i);

            boolean matchMade = false;
            for (int j = 0; j < actualChildren.getLength(); ++j) {
                org.w3c.dom.Node actualChild = actualChildren.item(j);
                if (! expectedChild.getNodeName().equals(actualChild.getNodeName()))
                    continue;

                if (compareNodes(expectedChild, actualChild, errorMessages)) {
                    matchMade = true;
                    break;
                }
            }

            if (! matchMade) {
                errorMessages.append(errorMsg + " Failed to match child " + expectedChild.getNodeName() + " to any node\n");
                return false;
            }
        }

        return true;
    }

    private static boolean compareTextNode(org.w3c.dom.Text expected, org.w3c.dom.Text actual, StringBuilder errorMessages) {
        String expectedData = expected.getData().trim();
        String actualData = actual.getData().trim();
        if (expectedData.equalsIgnoreCase(actualData))
            return true;

        errorMessages.append(expected.getData() + " does not match " + actual.getData() + NEW_LINE);
        return false;
    }

    private static boolean compareNodes(org.w3c.dom.Node expected, org.w3c.dom.Node actual, StringBuilder errorMessages) {
        if (expected.getNodeType() != actual.getNodeType()) {
            return false;
        }

        if (expected instanceof Element)
            return compareElements(expected, actual, errorMessages);
        else if (expected instanceof Text)
            return compareTextNode((Text) expected, (Text) actual, errorMessages);

        return false;
    }

    /**
     * Compare two documents
     *
     * @param document1 first document
     * @param document2 second document
     */
    public static void compareDocuments(Document document1, Document document2) {
        assertNotNull(document1);
        assertNotNull(document2);
        assertEquals(document1.getNodeType(), document2.getNodeType());

        StringBuilder errorMessages = new StringBuilder();
        if (! compareNodes(document1.getDocumentElement(), document2.getDocumentElement(), errorMessages))
            fail(errorMessages.toString());
    }

    /**
     * Print an xml {@link Document} to the given {@link OutputStream}
     * @param doc the given document
     * @param out the output stream, eg. System.out
     * @throws Exception if error occurs
     */
    public static void printDocument(Document doc, OutputStream out) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        transformer.transform(new DOMSource(doc), new StreamResult(new OutputStreamWriter(out, "UTF-8")));
    }

    /**
     * @param inputStream the input stream
     * @return convert the given input stream to a string
     * @throws Exception if error occurs
     */
    public static String toString(InputStream inputStream) throws Exception {
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int result = bis.read();
        while (result != -1) {
            byte b = (byte)result;
            buf.write(b);
            result = bis.read();
        }

        return buf.toString();
    }

    /**
     * @param prefix
     * @param suffix
     * @return a temporary file that is deleted on exit
     * @throws IOException
     */
    public static File createTempFile(String prefix, String suffix) throws IOException {
        File tempFile = File.createTempFile(prefix, suffix);
        tempFile.deleteOnExit();
        return tempFile;
    }

    /**
     * Compare the contents of 2 files
     *
     * @param original
     * @param fileToCompare
     * @throws IOException
     */
    public static void compareFileContents(File original, File fileToCompare) throws IOException {
        assertTrue(org.apache.commons.io.FileUtils.contentEquals(original, fileToCompare));
    }

    /**
     * Tries to open a zip and if an error occurs fails the test
     *
     * @param zipFile
     */
    public static void testZipFile(File zipFile) {
        ZipFile zipfile = null;
        try {
            zipfile = new ZipFile(zipFile);
        } catch (IOException e) {
            fail("Zip file test failed: " + e.getLocalizedMessage());
        } finally {
            try {
                if (zipfile != null) {
                    zipfile.close();
                    zipfile = null;
                }
            } catch (IOException e) {}
        }
    }

    /**
     * Return a list of a zip file's contents
     *
     * @param zipName the name of the zip file. Cannot be <code>null</code>
     * @param fileStream a file stream to a zip file. Cannot be <code>null</code>
     *                  Will be closed on completion.
     *
     * @throws Exception if an error occurs
     *
     * Note: This function uses a {@link ZipFile} with a temp file to return the entries
     *            of the {@link InputStream}. This is necessary since {@link ZipInputStream}
     *            cannot be relied upon to return all entries.
     */
    public static List<String> zipEntries(String zipName, InputStream fileStream) throws Exception {
        ArgCheck.isNotNull(zipName, "zip name");
        ArgCheck.isNotNull(fileStream, "file stream");

        List<String> entries = new ArrayList<>();
        File tmpFile = null;
        ZipFile zipFile = null;
        try {
            tmpFile = File.createTempFile(zipName, ZIP_SUFFIX);
            FileUtils.write(fileStream, tmpFile);

            zipFile = new ZipFile(tmpFile);
            Enumeration<? extends ZipEntry> zEntries = zipFile.entries();
            while (zEntries.hasMoreElements()) {
                ZipEntry entry = zEntries.nextElement();
                String fileName = entry.getName();
                String entryName = zipName == null ? fileName : zipName + FORWARD_SLASH + fileName;

                // Remove trailing forward slashes
                if (entryName.endsWith(FORWARD_SLASH))
                    entryName = entryName.substring(0, entryName.length() - 1);

                entries.add(entryName);
            }
        } finally {
            try {
                if (zipFile != null) {
                    zipFile.close();
                    zipFile = null;
                }

                if (tmpFile != null)
                    tmpFile.delete();
            } catch (IOException e) {
            }
        }

        return entries;
    }

    /**
     * @param bytes
     * @return checksum value of the given bytes
     */
    public static long checksum(byte[] bytes) {
        Checksum contentCRC = new CRC32();
        contentCRC.update(bytes, 0, bytes.length);
        return contentCRC.getValue();
    }

    /**
     * @param num
     * @return string of tabs
     */
    public static String tab(int num) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < num; ++i)
            buf.append(TAB);

        return buf.toString();
    }

    /**
     * @param num
     * @return string of spaces
     */
    public static String space(int num) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < num; ++i)
            buf.append(SPACE);

        return buf.toString();
    }
}
