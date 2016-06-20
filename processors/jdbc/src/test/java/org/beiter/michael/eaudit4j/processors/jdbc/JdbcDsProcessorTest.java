/*
 * #%L
 * This file is part of eAudit4j, a library for creating pluggable
 * auditing solutions, providing an audit processor that persists
 * audit events to a JDBC database.
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
package org.beiter.michael.eaudit4j.processors.jdbc;

import org.apache.commons.codec.binary.Hex;
import org.beiter.michael.eaudit4j.common.Audit;
import org.beiter.michael.eaudit4j.common.AuditException;
import org.beiter.michael.eaudit4j.common.AuditFactory;
import org.beiter.michael.eaudit4j.common.CommonProperties;
import org.beiter.michael.eaudit4j.common.Encodings;
import org.beiter.michael.eaudit4j.common.Event;
import org.beiter.michael.eaudit4j.common.FactoryException;
import org.beiter.michael.eaudit4j.common.Field;
import org.beiter.michael.eaudit4j.common.ProcessingObjects;
import org.beiter.michael.eaudit4j.common.Processor;
import org.beiter.michael.eaudit4j.common.impl.EventBuilder;
import org.beiter.michael.eaudit4j.common.impl.EventField;
import org.beiter.michael.eaudit4j.common.propsbuilder.MapBasedCommonPropsBuilder;
import org.beiter.michael.eaudit4j.processors.jdbc.propsbuilder.MapBasedJdbcPropsBuilder;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class JdbcDsProcessorTest {

    // The data source  configuration
    private static final String DS_NAME = "myDataSource";

    // a default field name and value for the event ID that we will use in this test
    public static final String EVENT_ID_FIELD_NAME = "eventId";
    public static final String EVENT_ID = "1234567890ABCDEF";

    // test strings
    public static final String T_AUDIT_STREAM_NAME = "1234567890";
    public static final String T_SUBJECT = "SubjectId-1234";
    public static final String T_OBJECT = "ObjectId-3456";
    public static final String T_ACTOR = "ActorId-5678";
    public static final String T_RESULT = "Some result";

    private java.lang.reflect.Method method_getConnection;

    /**
     * Make some of the private methods in the JdbcDsProcessor class accessible.
     * <p>
     * This is executed before every test to ensure consistency even if one of the tests mock with field accessibility.
     */
    @Before
    public void makePrivateFieldsAccessible() {

        // make private fields accessible as needed
        try {
            method_getConnection = JdbcDsProcessor.class.getDeclaredMethod("getConnection", JdbcProperties.class, ProcessingObjects.class);
        } catch (NoSuchMethodException e) {
            AssertionError ae = new AssertionError("An expected private field or method does not exist");
            ae.initCause(e);
            throw ae;
        }
        method_getConnection.setAccessible(true);
    }

    /**
     * Start the in-memory database server
     *
     * @throws SQLException When the startup fails
     */
    @BeforeClass
    public static void startDbServer()
            throws SQLException {

        H2Server.start();
    }

    /**
     * Stops the in-memory database server
     */
    @AfterClass
    public static void stopDbServer() {

        H2Server.stop();
    }

    /**
     * Initialize the database with a default database schema + values
     *
     * @throws SQLException When the initialization fails
     */
    @Before
    public void initDatabase()
            throws SQLException {

        H2Server.init();
    }

    ///////////////////////////////////////////////////////////////////////////
    // GetConnection() tests
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Test that we can retrieve a connection.
     */
    @Test
    public void getConnectionTest()
            throws InvocationTargetException, IllegalAccessException, SQLException {

        // get a data source and add it to the ProcessingObjects
        JdbcConnectionPool cp = JdbcConnectionPool.create(H2Server.URL, H2Server.USER, H2Server.PASSWORD);
        ProcessingObjects processingObjects = new ProcessingObjects();
        processingObjects.add(DS_NAME, cp);

        Map<String, String> props = new HashMap<>();

        // the JDBC Processor configuration
        props.put(MapBasedJdbcPropsBuilder.KEY_DATA_SOURCE_NAME, DS_NAME);

        JdbcProperties jdbcProps = MapBasedJdbcPropsBuilder.build(props);
        CommonProperties commonProps = MapBasedCommonPropsBuilder.build(props);
        Processor processor = new JdbcDsProcessor();
        processor.init(commonProps);

        Connection con = (Connection) method_getConnection.invoke(processor, jdbcProps, processingObjects);

        String error = "The connection is null";
        assertThat(error, con, is(notNullValue()));

        con.close();
    }

    ///////////////////////////////////////////////////////////////////////////
    // DB connection tests
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Test that an invalid data source name throws an exception
     */
    @Test(expected = AuditException.class)
    public void invalidDataSourceNameTest()
            throws FactoryException, AuditException, UnsupportedEncodingException, SQLException {

        Map<String, String> props = new HashMap<>();

        // the JDBC Processor configuration
        props.put(MapBasedJdbcPropsBuilder.KEY_DATA_SOURCE_NAME, DS_NAME + "_INVALID");
        props.put(MapBasedJdbcPropsBuilder.KEY_EVENT_ID_FIELD_NAME, EVENT_ID_FIELD_NAME);
        props.put(MapBasedJdbcPropsBuilder.KEY_INSERT_EVENT_SQL_STMT,
                "INSERT INTO events (eventId, auditStreamName, eventJson) VALUES (?, ?, ?)");
        props.put(MapBasedJdbcPropsBuilder.KEY_INDEXED_FIELDS, "");

        // the audit library common configuration
        CommonProperties properties = MapBasedCommonPropsBuilder.build(props);
        properties.setDefaultAuditStream(T_AUDIT_STREAM_NAME);
        properties.setEncoding("UTF-8");
        properties.setFailOnMissingProcessors(true);
        properties.setProcessors(JdbcDsProcessor.class.getCanonicalName());

        Audit audit = AuditFactory.getInstance(properties.getAuditClassName(), properties);

        Event event = getTestEvent(properties);

        audit.audit(event, T_AUDIT_STREAM_NAME, new ProcessingObjects());
    }

    /**
     * Test that a data source name that references an object of wrong type throws an exception
     */
    @Test(expected = AuditException.class)
    public void invalidDataSourceObjectTest()
            throws FactoryException, AuditException, UnsupportedEncodingException, SQLException {

        // add a non-DataSource object to the ProcessingObjects
        ProcessingObjects processingObjects = new ProcessingObjects();
        processingObjects.add(DS_NAME, "This is not a DataSource");

        Map<String, String> props = new HashMap<>();

        // the JDBC Processor configuration
        props.put(MapBasedJdbcPropsBuilder.KEY_DATA_SOURCE_NAME, DS_NAME);
        props.put(MapBasedJdbcPropsBuilder.KEY_EVENT_ID_FIELD_NAME, EVENT_ID_FIELD_NAME);
        props.put(MapBasedJdbcPropsBuilder.KEY_INSERT_EVENT_SQL_STMT,
                "INSERT INTO events (eventId, auditStreamName, eventJson) VALUES (?, ?, ?)");
        props.put(MapBasedJdbcPropsBuilder.KEY_INDEXED_FIELDS, "");

        // the audit library common configuration
        CommonProperties properties = MapBasedCommonPropsBuilder.build(props);
        properties.setDefaultAuditStream(T_AUDIT_STREAM_NAME);
        properties.setEncoding("UTF-8");
        properties.setFailOnMissingProcessors(true);
        properties.setProcessors(JdbcDsProcessor.class.getCanonicalName());

        Audit audit = AuditFactory.getInstance(properties.getAuditClassName(), properties);

        Event event = getTestEvent(properties);

        audit.audit(event, T_AUDIT_STREAM_NAME, processingObjects);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clean up tests
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Clean up test
     *
     * This test does not really do anything, but the cleanUp() method does not does
     * not to anything either, so I guess that is okay to get test coverage up :)
     */
    @Test
    public void cleanUpTest () {

        Processor processor = new JdbcDsProcessor();
        processor.cleanUp();
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
