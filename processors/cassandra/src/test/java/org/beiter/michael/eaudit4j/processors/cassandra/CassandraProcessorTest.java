/*
 * #%L
 * This file is part of eAudit4j, a library for creating pluggable
 * auditing solutions, providing an audit processor that persists
 * audit events to a Cassandra database.
 * %%
 * Copyright (C) 2015 - 2016 Michael Beiter <michael@beiter.org>
 * %%
 * All rights reserved.
 * .
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the copyright holder nor the names of the
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 * .
 * .
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.beiter.michael.eaudit4j.processors.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.apache.commons.codec.binary.Hex;
import org.apache.thrift.transport.TTransportException;
import org.beiter.michael.eaudit4j.common.Audit;
import org.beiter.michael.eaudit4j.common.Event;
import org.beiter.michael.eaudit4j.common.CommonProperties;
import org.beiter.michael.eaudit4j.common.ProcessingObjects;
import org.beiter.michael.eaudit4j.common.AuditFactory;
import org.beiter.michael.eaudit4j.common.AuditException;
import org.beiter.michael.eaudit4j.common.AuditErrorConditions;
import org.beiter.michael.eaudit4j.common.FactoryException;
import org.beiter.michael.eaudit4j.common.Encodings;
import org.beiter.michael.eaudit4j.common.Field;
import org.beiter.michael.eaudit4j.common.Processor;
import org.beiter.michael.eaudit4j.common.Reversible;
import org.beiter.michael.eaudit4j.common.impl.AuditEvent;
import org.beiter.michael.eaudit4j.common.impl.EventBuilder;
import org.beiter.michael.eaudit4j.common.impl.EventField;
import org.beiter.michael.eaudit4j.common.propsbuilder.MapBasedCommonPropsBuilder;
import org.beiter.michael.eaudit4j.processors.cassandra.propsbuilder.MapBasedCassandraPropsBuilder;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

public class CassandraProcessorTest {

    // The Cassandra session configuration
    private static final String SESSION_NAME = "mySession";

    // The CQL settings
    private static final String EVENT_ID_CQL_FIELD = "eventId";
    private static final String AUDIT_STREAM_NAME_CQL_FIELD = "auditStreamName";
    private static final String EVENT_JSON_CQL_FIELD = "eventJson";
    // note: we could use INSET INTO keyspace.events ... here, but we rather use a dedicated session per keyspace
    private static final String INSERT_EVENT_CQL_STMT =
            "INSERT INTO events (eventId, auditStream, eventJson) VALUES (:"
                    + EVENT_ID_CQL_FIELD + ", :" + AUDIT_STREAM_NAME_CQL_FIELD + ", :" + EVENT_JSON_CQL_FIELD + ")";

    // a default field name and value for the event ID that we will use in this test
    public static final String EVENT_ID_FIELD_NAME = "eventId";
    public static final String EVENT_ID = "1234567890ABCDEF";

    // test strings
    public static final String T_AUDIT_STREAM_NAME = "1234567890";
    public static final String T_SUBJECT = "SubjectId-1234";
    public static final String T_OBJECT = "ObjectId-3456";
    public static final String T_ACTOR = "ActorId-5678";
    public static final String T_RESULT = "Some result";
    public static final String T_EVENT_JSON = "{\"version\":\"1.0\","
            + "\"fields\":{\"actor\":\"" + T_ACTOR + "\",\"result\":\"" + T_RESULT + "\",\"" + EVENT_ID_FIELD_NAME
            + "\":\"" + EVENT_ID + "\",\"subject\":\"" + T_SUBJECT + "\",\"byteField\":\"31323334\",\"object\":\""
            + T_OBJECT + "\"}}";
    public static final String[] T_FIELD_NAMES = {"subject", "myActor", "byteField"};
    public static final String[] T_FIELD_VALUES = {T_SUBJECT, T_ACTOR, "31323334"};

    // the private fields in the processor we want to inspect with reflection
    private java.lang.reflect.Field field_commonProperties;
    private java.lang.reflect.Field field_properties;
    private java.lang.reflect.Method method_getSession;

    /**
     * Make some of the private fields and methods in the AbstractJdbcProcessor class accessible.
     * <p>
     * This is executed before every test to ensure consistency even if one of the tests mock with field accessibility.
     */
    @Before
    public void makePrivateFieldsAccessible() {

        // make private fields accessible as needed
        try {
            field_commonProperties = CassandraProcessor.class.getDeclaredField("commonProperties");
            field_properties = CassandraProcessor.class.getDeclaredField("properties");
            method_getSession = CassandraProcessor.class.getDeclaredMethod("getSession", CassandraProperties.class, ProcessingObjects.class);
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            AssertionError ae = new AssertionError("An expected private field or method does not exist");
            ae.initCause(e);
            throw ae;
        }
        field_commonProperties.setAccessible(true);
        field_properties.setAccessible(true);
        method_getSession.setAccessible(true);
    }

    /**
     * Start the Cassandra server
     *
     * @throws Exception When the startup fails
     */
    @BeforeClass
    public static void startCassandraServer()
            throws Exception {

        CassandraServer.start();
    }

    /**
     * Stop the Cassandra server
     */
    @AfterClass
    public static void stopCassandraServer() {

        CassandraServer.stop();
    }

    /**
     * Initialize the database with a default database schema + values
     */
    @Before
    public void initDatabase() {

        CassandraServer.init();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Initialization tests
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Initialize the processor with 'null' properties
     */
    @Test(expected = NullPointerException.class)
    public void initWithNullPropertiesTest() {

        Processor processor = new CassandraProcessor();
        processor.init(null);
    }

    /**
     * Test that the init() method creates a defensive copy of the provided properties
     */
    @Test
    public void initPropertiesInboundDefensiveCopyTest() {

        String key = "some property";
        String value = "some value";

        Map<String, String> propsMap = new HashMap<>();
        propsMap.put(key, value);
        CommonProperties commonProps = MapBasedCommonPropsBuilder.build(propsMap);

        Processor processor = new CassandraProcessor();
        processor.init(commonProps);

        String error = "The method does not create an inbound defensive copy";
        try {
            CommonProperties commonPropsInObject = (CommonProperties) field_commonProperties.get(processor);
            assertThat(error, commonPropsInObject, is(not(sameInstance(commonProps))));
        } catch (IllegalAccessException e) {
            AssertionError ae = new AssertionError("Cannot access private field");
            ae.initCause(e);
            throw ae;
        }
    }

    /**
     * Test that the init() method correctly initializes a local copy of the Common Properties
     */
    @Test
    public void initCommonPropertiesCorrectTest() {

        String auditStreamName = "audit stream name";
        String key = "some property";
        String value = "some value";

        Map<String, String> propsMap = new HashMap<>();
        propsMap.put(key, value);


        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();
        commonProps.setDefaultAuditStream(auditStreamName);
        commonProps.setAdditionalProperties(propsMap);

        Processor processor = new CassandraProcessor();
        processor.init(commonProps);

        String error = "The method does not retain object keys or values";
        try {

            CommonProperties commonPropsInObject = (CommonProperties) field_commonProperties.get(processor);

            assertThat(error, commonPropsInObject.getDefaultAuditStream(), is(equalTo(auditStreamName)));
            assertThat(error, commonPropsInObject.getAdditionalProperties().containsKey(key), is(true));
            assertThat(error, commonPropsInObject.getAdditionalProperties().get(key), is(equalTo(value)));
        } catch (IllegalAccessException e) {
            AssertionError ae = new AssertionError("Cannot access private field");
            ae.initCause(e);
            throw ae;
        }
    }

    /**
     * Test that the init() method correctly initializes the CassandraProperties values
     */
    @Test
    public void initPropertiesCorrectTest() {

        String key = MapBasedCassandraPropsBuilder.KEY_INSERT_EVENT_CQL_STMT;
        String value = "some value";
        Map<String, String> propsMap = new HashMap<>();
        propsMap.put(key, value);

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();
        commonProps.setAdditionalProperties(propsMap);

        Processor processor = new CassandraProcessor();
        processor.init(commonProps);

        String error = "The method does not retain object keys or values";
        try {

            CassandraProperties propsInObject = (CassandraProperties) field_properties.get(processor);

            assertThat(error, propsInObject.getInsertEventCqlStmt(), is(equalTo(value)));
        } catch (IllegalAccessException e) {
            AssertionError ae = new AssertionError("Cannot access private field");
            ae.initCause(e);
            throw ae;
        }
    }

    /**
     * Test that null-processor properties throw an appropriate exception when called upon by the process() method
     */
    @Test(expected = AuditException.class)
    public void initPropertiesCorrect2Test()
            throws AuditException {

        String key = MapBasedCassandraPropsBuilder.KEY_INSERT_EVENT_CQL_STMT;
        String value = "some value";
        Map<String, String> propsMap = new HashMap<>();
        propsMap.put(key, value);

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();
        commonProps.setAdditionalProperties(propsMap);

        Processor processor = new CassandraProcessor();
        processor.init(commonProps);

        try {
            // setting these properties to null should trigger an exception when executing the process() method
            field_properties.set(processor, null);
        } catch (IllegalAccessException e) {
            AssertionError ae = new AssertionError("Cannot access private field");
            ae.initCause(e);
            throw ae;
        }

        Event event = new AuditEvent();
        String auditStream = "some audit stream";
        ProcessingObjects processingObjects = new ProcessingObjects();

        try {
            // this should throw an exception, because the internal configuration is invalid
            processor.process(event, auditStream, processingObjects);
        } catch (AuditException e) {
            AuditErrorConditions expected = AuditErrorConditions.INITIALIZATION;
            String error = "The type of exception thrown is not correct. Expected " + expected + ", got " + e.getErrorCondition();
            assertThat(error, e.getErrorCondition(), is(equalTo(expected)));
            throw e;
        }

        throw new AssertionError("Expected an exception, but that exception was not thrown");
    }


    ///////////////////////////////////////////////////////////////////////////
    // Process events in the default audit stream
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Test processing a null Event
     * <p>
     * (should throw a AuditException if things go well)
     */
    @Test(expected = AuditException.class)
    public void processNullEventInDefaultAuditStreamTest()
            throws AuditException {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();
        Processor processor = new CassandraProcessor();
        processor.init(commonProps);

        Event event = null;

        processor.process(event);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Process events in a provided audit stream, with default processing objects
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Test processing a null Event
     * <p>
     * (should throw a AuditException if things go well)
     */
    @Test(expected = AuditException.class)
    public void processNullEventInProvidedAuditStreamTest()
            throws AuditException {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();
        Processor processor = new CassandraProcessor();
        processor.init(commonProps);

        Event event = null;
        String auditStream = "some audit stream";

        processor.process(event, auditStream);
    }

    /**
     * Test processing in a null audit stream
     * <p>
     * (should throw a NullPointerException if things go well)
     */
    @Test(expected = NullPointerException.class)
    public void processEventInProvidedNullAuditStreamTest()
            throws AuditException {

        String auditStream = null;

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();
        Processor processor = new CassandraProcessor();
        processor.init(commonProps);

        Event event = new AuditEvent();

        processor.process(event, auditStream);
    }

    /**
     * Test processing in a blank audit stream
     * <p>
     * (should throw a IllegalArgumentException if things go well)
     */
    @Test(expected = IllegalArgumentException.class)
    public void processEventInProvidedBlankAuditStreamTest()
            throws AuditException {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();
        Processor processor = new CassandraProcessor();
        processor.init(commonProps);

        Event event = new AuditEvent();
        String auditStream = "";

        processor.process(event, auditStream);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Process events in a provided audit stream, with provided processing objects
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Test processing a null Event
     * <p>
     * (should throw a AuditException if things go well)
     */
    @Test(expected = AuditException.class)
    public void processNullEventWithProcessingObjectsTest()
            throws AuditException {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();
        Processor processor = new CassandraProcessor();
        processor.init(commonProps);

        Event event = null;
        String auditStream = "some audit stream";
        ProcessingObjects processingObjects = new ProcessingObjects();

        processor.process(event, auditStream, processingObjects);
    }

    /**
     * Test processing in a null audit stream
     * <p>
     * (should throw a NullPointerException if things go well)
     */
    @Test(expected = NullPointerException.class)
    public void processEventInNullAuditStreamWithProcessingObjectsTest()
            throws AuditException {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();
        Processor processor = new CassandraProcessor();
        processor.init(commonProps);

        Event event = new AuditEvent();
        String auditStream = null;
        ProcessingObjects processingObjects = new ProcessingObjects();

        processor.process(event, auditStream, processingObjects);
    }

    /**
     * Test processing in a blank audit stream
     * <p>
     * (should throw a IllegalArgumentException if things go well)
     */
    @Test(expected = IllegalArgumentException.class)
    public void processEventInBlankAuditStreamWithProcessingObjectsTest()
            throws AuditException {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();
        Processor processor = new CassandraProcessor();
        processor.init(commonProps);

        Event event = new AuditEvent();
        String auditStream = "";
        ProcessingObjects processingObjects = new ProcessingObjects();

        processor.process(event, auditStream, processingObjects);
    }

    /**
     * Test processing with a null set of processing objects
     * <p>
     * (should throw a NullPointerException if things go well)
     */
    @Test(expected = NullPointerException.class)
    public void processEventWithNullProcessingObjectsTest()
            throws AuditException {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();
        Processor processor = new CassandraProcessor();
        processor.init(commonProps);

        Event event = new AuditEvent();
        String auditStream = "some audit stream";
        ProcessingObjects processingObjects = null;

        processor.process(event, auditStream, processingObjects);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Business logic test
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Test that auditing fails if the event ID is not present
     */
    @Test(expected = AuditException.class)
    public void testProcessEventWithMissingEventId()
            throws AuditException {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();

        Processor processor = new CassandraProcessor();
        processor.init(commonProps);

        Event event = new AuditEvent();
        String auditStream = "some audit stream";
        ProcessingObjects processingObjects = new ProcessingObjects();

        try {
            // this should throw an exception, because the internal configuration is invalid
            processor.process(event, auditStream, processingObjects);
        } catch (AuditException e) {
            AuditErrorConditions expected = AuditErrorConditions.CONFIGURATION;
            String error = "The type of exception thrown is not correct. Expected " + expected + ", got " + e.getErrorCondition();
            assertThat(error, e.getErrorCondition(), is(equalTo(expected)));
            throw e;
        }

        throw new AssertionError("Expected an exception, but that exception was not thrown");
    }


    ///////////////////////////////////////////////////////////////////////////
    // Revert & clean up tests
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Test that the revert() method can handle null events
     */
    @Test(expected = NullPointerException.class)
    public void revertFromNullEventTest()
            throws AuditException {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();
        Processor processor = new CassandraProcessor();
        processor.init(commonProps);

        Event event = null;

        // revert the operation from null Event
        ((Reversible) processor).revert(event);
    }

    /**
     * Revert test
     * <p>
     * This test does not really do anything, but the revert() method does not does
     * not to anything either, so I guess that is okay to get test coverage up :)
     */
    @Test
    public void revertTest()
            throws AuditException {

        Processor processor = new CassandraProcessor();
        Event event = new AuditEvent();
        ((Reversible) processor).revert(event);
    }

    /**
     * Clean up test
     * <p>
     * This test does not really do anything, but the cleanUp() method does not does
     * not to anything either, so I guess that is okay to get test coverage up :)
     */
    @Test
    public void cleanUpTest() {

        Processor processor = new CassandraProcessor();
        processor.cleanUp();
    }

    ///////////////////////////////////////////////////////////////////////////
    // GetConnection() tests
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Test that we can retrieve a Cassandra session.
     */
    @Test
    public void getSessionTest()
            throws InvocationTargetException, IllegalAccessException {

        // get a data source and add it to the ProcessingObjects
        Session session1 = CassandraServer.getSessionWithKeyspace();
        ProcessingObjects processingObjects = new ProcessingObjects();
        processingObjects.add(SESSION_NAME, session1);

        Map<String, String> props = new HashMap<>();

        // the Cassandra Processor configuration
        props.put(MapBasedCassandraPropsBuilder.KEY_SESSION_NAME, SESSION_NAME);

        CassandraProperties cassandraProps = MapBasedCassandraPropsBuilder.build(props);
        CommonProperties commonProps = MapBasedCommonPropsBuilder.build(props);
        Processor processor = new CassandraProcessor();
        processor.init(commonProps);

        // retrieve the session back from the processor
        Session session2 = (Session) method_getSession.invoke(processor, cassandraProps, processingObjects);

        String error = "The connection is null";
        assertThat(error, session1, is(sameInstance(session2)));
    }

    ///////////////////////////////////////////////////////////////////////////
    // DB connection tests
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Test that an invalid session name (i.e. a session name pointing to a non-existing entry in the map)
     * throws an exception
     */
    @Test(expected = AuditException.class)
    public void invalidSessionNameTest()
            throws FactoryException, AuditException, UnsupportedEncodingException {

        Map<String, String> props = new HashMap<>();

        // the Cassandra Processor configuration
        props.put(MapBasedCassandraPropsBuilder.KEY_SESSION_NAME, SESSION_NAME + "_INVALID");
        props.put(MapBasedCassandraPropsBuilder.KEY_EVENT_ID_FIELD_NAME, EVENT_ID_FIELD_NAME);
        props.put(MapBasedCassandraPropsBuilder.KEY_EVENT_ID_CQL_PARAM, EVENT_ID_CQL_FIELD);
        props.put(MapBasedCassandraPropsBuilder.KEY_AUDIT_STREAM_NAME_CQL_PARAM, AUDIT_STREAM_NAME_CQL_FIELD);
        props.put(MapBasedCassandraPropsBuilder.KEY_EVENT_JSON_CQL_PARAM, EVENT_JSON_CQL_FIELD);
        props.put(MapBasedCassandraPropsBuilder.KEY_INSERT_EVENT_CQL_STMT, INSERT_EVENT_CQL_STMT);

        // the audit library common configuration
        CommonProperties properties = MapBasedCommonPropsBuilder.build(props);
        properties.setDefaultAuditStream(T_AUDIT_STREAM_NAME);
        properties.setEncoding("UTF-8");
        properties.setFailOnMissingProcessors(true);
        properties.setProcessors(CassandraProcessor.class.getCanonicalName());

        Audit audit = AuditFactory.getInstance(properties.getAuditClassName(), properties);

        Event event = getTestEvent(properties);

        try {
            // this should throw an exception, because the internal configuration is invalid
            audit.audit(event, T_AUDIT_STREAM_NAME, new ProcessingObjects());
        } catch (AuditException e) {
            AuditErrorConditions expected = AuditErrorConditions.PROCESSING;
            String error = "The type of exception thrown is not correct. Expected " + expected + ", got " + e.getErrorCondition();
            assertThat(error, e.getErrorCondition(), is(equalTo(expected)));
            throw e;
        }

        throw new AssertionError("Expected an exception, but that exception was not thrown");
    }

    /**
     * Test that a session name that references an object of wrong type throws an exception
     */
    @Test(expected = AuditException.class)
    public void invalidSessionObjectTest()
            throws FactoryException, AuditException, UnsupportedEncodingException {

        // add a non-DataSource object to the ProcessingObjects
        ProcessingObjects processingObjects = new ProcessingObjects();
        processingObjects.add(SESSION_NAME, "This is not a DataSource");

        Map<String, String> props = new HashMap<>();

        // the Cassandra Processor configuration
        props.put(MapBasedCassandraPropsBuilder.KEY_SESSION_NAME, SESSION_NAME);
        props.put(MapBasedCassandraPropsBuilder.KEY_EVENT_ID_FIELD_NAME, EVENT_ID_FIELD_NAME);
        props.put(MapBasedCassandraPropsBuilder.KEY_EVENT_ID_CQL_PARAM, EVENT_ID_CQL_FIELD);
        props.put(MapBasedCassandraPropsBuilder.KEY_AUDIT_STREAM_NAME_CQL_PARAM, AUDIT_STREAM_NAME_CQL_FIELD);
        props.put(MapBasedCassandraPropsBuilder.KEY_EVENT_JSON_CQL_PARAM, EVENT_JSON_CQL_FIELD);
        props.put(MapBasedCassandraPropsBuilder.KEY_INSERT_EVENT_CQL_STMT, INSERT_EVENT_CQL_STMT);

        // the audit library common configuration
        CommonProperties properties = MapBasedCommonPropsBuilder.build(props);
        properties.setDefaultAuditStream(T_AUDIT_STREAM_NAME);
        properties.setEncoding("UTF-8");
        properties.setFailOnMissingProcessors(true);
        properties.setProcessors(CassandraProcessor.class.getCanonicalName());

        Audit audit = AuditFactory.getInstance(properties.getAuditClassName(), properties);

        Event event = getTestEvent(properties);

        try {
            // this should throw an exception, because the internal configuration is invalid
            audit.audit(event, T_AUDIT_STREAM_NAME, processingObjects);
        } catch (AuditException e) {
            AuditErrorConditions expected = AuditErrorConditions.PROCESSING;
            String error = "The type of exception thrown is not correct. Expected " + expected + ", got " + e.getErrorCondition();
            assertThat(error, e.getErrorCondition(), is(equalTo(expected)));
            throw e;
        }

        throw new AssertionError("Expected an exception, but that exception was not thrown");
    }

    ///////////////////////////////////////////////////////////////////////////
    // Event insert tests
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Test that, when a session and CQL statement are provided, we can correctly insert events into a database.
     * <p>
     * Also test that a null field list does not throw an exception
     */
    @Test
    public void eventInsertTest()
            throws FactoryException, AuditException, IOException, InterruptedException, TTransportException {

        Map<String, String> props = new HashMap<>();

        // the Cassandra Processor configuration
        props.put(MapBasedCassandraPropsBuilder.KEY_SESSION_NAME, SESSION_NAME);
        props.put(MapBasedCassandraPropsBuilder.KEY_EVENT_ID_FIELD_NAME, EVENT_ID_FIELD_NAME);
        props.put(MapBasedCassandraPropsBuilder.KEY_EVENT_ID_CQL_PARAM, EVENT_ID_CQL_FIELD);
        props.put(MapBasedCassandraPropsBuilder.KEY_AUDIT_STREAM_NAME_CQL_PARAM, AUDIT_STREAM_NAME_CQL_FIELD);
        props.put(MapBasedCassandraPropsBuilder.KEY_EVENT_JSON_CQL_PARAM, EVENT_JSON_CQL_FIELD);
        props.put(MapBasedCassandraPropsBuilder.KEY_INSERT_EVENT_CQL_STMT, INSERT_EVENT_CQL_STMT);

        // the audit library common configuration
        CommonProperties commonProperties = MapBasedCommonPropsBuilder.build(props);
        commonProperties.setDefaultAuditStream(T_AUDIT_STREAM_NAME);
        commonProperties.setEncoding("UTF-8");
        commonProperties.setFailOnMissingProcessors(true);
        commonProperties.setProcessors(CassandraProcessor.class.getCanonicalName());

        // Create the session and add it to the processing objects
        ProcessingObjects processingObjects = new ProcessingObjects();
        processingObjects.add(SESSION_NAME, CassandraServer.getSessionWithKeyspace());

        // audit the event
        Audit audit = AuditFactory.getInstance(commonProperties.getAuditClassName(), commonProperties);
        Event event = getTestEvent(commonProperties);
        audit.audit(event, T_AUDIT_STREAM_NAME, processingObjects);


        // assert that the audit operation was successful - get the strings back from the DB!
        Session session = CassandraServer.getSessionWithKeyspace();
        ResultSet eventRs = session.execute("SELECT * FROM events");

        int rsEventSize = 0;
        Iterator<Row> rowIterator = eventRs.iterator();
        while (rowIterator.hasNext()) {

            Row row = rowIterator.next();
            // count the size of the rs
            ++rsEventSize;

            String eventId = row.getString("eventId");
            String auditStreamName = row.getString("auditStream");
            String eventJson = row.getString("eventJson");

            String error = "The auditStreamName does not have the correct value";
            assertThat(error, auditStreamName, is(equalTo(T_AUDIT_STREAM_NAME)));
            error = "The eventJson does not have the correct value";
            assertThat(error, eventJson, is(equalTo(T_EVENT_JSON)));
        }

        String error = "The event result set size does not have the correct size";
        assertThat(error, rsEventSize, is(equalTo(1)));
    }

    /**
     * Test that an invalid CQL statement throws an exception
     */
    @Test(expected = AuditException.class)
    public void eventInsertInvalidCqlTest()
            throws FactoryException, AuditException, UnsupportedEncodingException {

        Map<String, String> props = new HashMap<>();

        // the Cassandra Processor configuration
        props.put(MapBasedCassandraPropsBuilder.KEY_SESSION_NAME, SESSION_NAME);
        props.put(MapBasedCassandraPropsBuilder.KEY_EVENT_ID_FIELD_NAME, EVENT_ID_FIELD_NAME);
        props.put(MapBasedCassandraPropsBuilder.KEY_EVENT_ID_CQL_PARAM, EVENT_ID_CQL_FIELD);
        props.put(MapBasedCassandraPropsBuilder.KEY_AUDIT_STREAM_NAME_CQL_PARAM, AUDIT_STREAM_NAME_CQL_FIELD);
        props.put(MapBasedCassandraPropsBuilder.KEY_EVENT_JSON_CQL_PARAM, EVENT_JSON_CQL_FIELD);
        props.put(MapBasedCassandraPropsBuilder.KEY_INSERT_EVENT_CQL_STMT,
                "INSERT INTO events (eventId, auditStream, eventJson) VALUES (:"
                        + EVENT_ID_CQL_FIELD + ", :" + AUDIT_STREAM_NAME_CQL_FIELD + ", :" + EVENT_JSON_CQL_FIELD + ") INVALID");

        // the audit library common configuration
        CommonProperties commonProperties = MapBasedCommonPropsBuilder.build(props);
        commonProperties.setDefaultAuditStream(T_AUDIT_STREAM_NAME);
        commonProperties.setEncoding("UTF-8");
        commonProperties.setFailOnMissingProcessors(true);
        commonProperties.setProcessors(CassandraProcessor.class.getCanonicalName());

        // Create the session and add it to the processing objects
        ProcessingObjects processingObjects = new ProcessingObjects();
        processingObjects.add(SESSION_NAME, CassandraServer.getSessionWithKeyspace());

        Audit audit = AuditFactory.getInstance(commonProperties.getAuditClassName(), commonProperties);

        Event event = getTestEvent(commonProperties);

        try {
            audit.audit(event, T_AUDIT_STREAM_NAME, processingObjects);
        } catch (AuditException e) {
            AuditErrorConditions expected = AuditErrorConditions.PROCESSING;
            String error = "The type of exception thrown is not correct. Expected " + expected + ", got " + e.getErrorCondition();
            assertThat(error, e.getErrorCondition(), is(equalTo(expected)));
            throw e;
        }

        throw new AssertionError("Expected an exception, but that exception was not thrown");
    }

    /**
     * Test that an invalid CQL query parameter in the parameterized statement throws an exception
     */
    @Test(expected = AuditException.class)
    public void eventInsertInvalidCqlParameterTest()
            throws FactoryException, AuditException, UnsupportedEncodingException {

        Map<String, String> props = new HashMap<>();

        // the Cassandra Processor configuration
        props.put(MapBasedCassandraPropsBuilder.KEY_SESSION_NAME, SESSION_NAME);
        props.put(MapBasedCassandraPropsBuilder.KEY_EVENT_ID_FIELD_NAME, EVENT_ID_FIELD_NAME);
        props.put(MapBasedCassandraPropsBuilder.KEY_EVENT_ID_CQL_PARAM, EVENT_ID_CQL_FIELD);
        props.put(MapBasedCassandraPropsBuilder.KEY_AUDIT_STREAM_NAME_CQL_PARAM, "INVALID-PARAM-NAME");
        props.put(MapBasedCassandraPropsBuilder.KEY_EVENT_JSON_CQL_PARAM, EVENT_JSON_CQL_FIELD);
        props.put(MapBasedCassandraPropsBuilder.KEY_INSERT_EVENT_CQL_STMT, INSERT_EVENT_CQL_STMT);

        // the audit library common configuration
        CommonProperties commonProperties = MapBasedCommonPropsBuilder.build(props);
        commonProperties.setDefaultAuditStream(T_AUDIT_STREAM_NAME);
        commonProperties.setEncoding("UTF-8");
        commonProperties.setFailOnMissingProcessors(true);
        commonProperties.setProcessors(CassandraProcessor.class.getCanonicalName());

        // Create the session and add it to the processing objects
        ProcessingObjects processingObjects = new ProcessingObjects();
        processingObjects.add(SESSION_NAME, CassandraServer.getSessionWithKeyspace());

        Audit audit = AuditFactory.getInstance(commonProperties.getAuditClassName(), commonProperties);

        Event event = getTestEvent(commonProperties);

        try {
            audit.audit(event, T_AUDIT_STREAM_NAME, processingObjects);
        } catch (AuditException e) {
            AuditErrorConditions expected = AuditErrorConditions.PROCESSING;
            String error = "The type of exception thrown is not correct. Expected " + expected + ", got " + e.getErrorCondition();
            assertThat(error, e.getErrorCondition(), is(equalTo(expected)));
            throw e;
        }

        throw new AssertionError("Expected an exception, but that exception was not thrown");
    }

    ///////////////////////////////////////////////////////////////////////////
    // Utility methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Produce a test event
     */
    private Event getTestEvent(CommonProperties properties) throws UnsupportedEncodingException {

        // the event ID is required for this processor to work
        Field eventIdField = new EventField(EVENT_ID_FIELD_NAME, EVENT_ID.getBytes("UTF-8"));

        // test data
        Field field = new EventField("byteField", new Hex().encode("1234".getBytes("UTF-8")), Encodings.HEX);

        // create the event, using (amongst others) the custom field above
        return new EventBuilder(properties)
                .setField(eventIdField)
                .setSubject(T_SUBJECT.toCharArray())
                .setObject(T_OBJECT.toCharArray())
                .setActor(T_ACTOR.toCharArray())
                .setResult(T_RESULT.toCharArray())
                .setField(field)
                .build();
    }
}
