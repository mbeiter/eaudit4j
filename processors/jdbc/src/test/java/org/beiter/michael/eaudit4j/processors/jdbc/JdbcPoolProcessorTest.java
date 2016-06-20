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
import org.beiter.michael.eaudit4j.common.AuditErrorConditions;
import org.beiter.michael.eaudit4j.common.AuditException;
import org.beiter.michael.eaudit4j.common.AuditFactory;
import org.beiter.michael.eaudit4j.common.CommonProperties;
import org.beiter.michael.eaudit4j.common.Encodings;
import org.beiter.michael.eaudit4j.common.Event;
import org.beiter.michael.eaudit4j.common.FactoryException;
import org.beiter.michael.eaudit4j.common.Field;
import org.beiter.michael.eaudit4j.common.Processor;
import org.beiter.michael.eaudit4j.common.impl.EventBuilder;
import org.beiter.michael.eaudit4j.common.impl.EventField;
import org.beiter.michael.eaudit4j.common.propsbuilder.MapBasedCommonPropsBuilder;
import org.beiter.michael.eaudit4j.processors.jdbc.propsbuilder.MapBasedJdbcPropsBuilder;
import org.beiter.michael.db.propsbuilder.MapBasedConnPropsBuilder;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class JdbcPoolProcessorTest {

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
    // Event insert tests
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Test that, when a connection and SQL statement are provided, we can correctly insert events into a database.
     * <p>
     * Also test that a null field list does not throw an exception
     */
    @Test
    public void eventInsertTest()
            throws FactoryException, AuditException, UnsupportedEncodingException, SQLException {

        Map<String, String> props = new HashMap<>();

        // the DB configuration
        props.put(MapBasedConnPropsBuilder.KEY_DRIVER, H2Server.DRIVER);
        props.put(MapBasedConnPropsBuilder.KEY_URL, H2Server.URL);
        props.put(MapBasedConnPropsBuilder.KEY_USERNAME, H2Server.USER);
        props.put(MapBasedConnPropsBuilder.KEY_PASSWORD, H2Server.PASSWORD);

        // the JDBC Processor configuration
        props.put(MapBasedJdbcPropsBuilder.KEY_EVENT_ID_FIELD_NAME, EVENT_ID_FIELD_NAME);
        props.put(MapBasedJdbcPropsBuilder.KEY_INSERT_EVENT_SQL_STMT,
                "INSERT INTO events (eventId, auditStreamName, eventJson) VALUES (?, ?, ?)");
        props.put(MapBasedJdbcPropsBuilder.KEY_INDEXED_FIELDS, null);

        // the audit library common configuration
        CommonProperties properties = MapBasedCommonPropsBuilder.build(props);
        properties.setDefaultAuditStream(T_AUDIT_STREAM_NAME);
        properties.setEncoding("UTF-8");
        properties.setFailOnMissingProcessors(true);
        properties.setProcessors(JdbcPoolProcessor.class.getCanonicalName());

        Audit audit = AuditFactory.getInstance(properties.getAuditClassName(), properties);

        Event event = getTestEvent(properties);

        audit.audit(event);

        // assert that the audit operation was successful - get the strings back from the DB!
        JdbcConnectionPool cp = JdbcConnectionPool.create(H2Server.URL, H2Server.USER, H2Server.PASSWORD);
        Connection con = cp.getConnection();
        String eventStmt = "SELECT * FROM events";
        ResultSet eventRs = con.prepareStatement(eventStmt).executeQuery();

        int rsEventSize = 0;
        while (eventRs.next()) {

            // count the size of the rs
            ++rsEventSize;

            String eventId = eventRs.getString("eventId");
            String auditStreamName = eventRs.getString("auditStreamName");
            String eventJson = eventRs.getString("eventJson");

            String error = "The auditStreamName does not have the correct value";
            assertThat(error, auditStreamName, is(equalTo(T_AUDIT_STREAM_NAME)));
            error = "The eventJson does not have the correct value";
            assertThat(error, eventJson, is(equalTo(T_EVENT_JSON)));


            String fieldStmt = "SELECT * FROM fields WHERE eventId = '" + eventId + "'";
            ResultSet fieldRs = con.prepareStatement(fieldStmt).executeQuery();
            int rsFieldSize = 0;
            while (fieldRs.next()) {

                // count the size of the rs
                ++rsFieldSize;
            }
            error = "The indexed fields result set size does not have the correct size";
            assertThat(error, rsFieldSize, is(equalTo(0)));

        }

        String error = "The event result set size does not have the correct size";
        assertThat(error, rsEventSize, is(equalTo(1)));

        con.close();
    }

    /**
     * Test that an empty field list does not throw an exception
     */
    @Test
    public void eventInsertEmptyFieldListTest()
            throws FactoryException, AuditException, UnsupportedEncodingException, SQLException {

        Map<String, String> props = new HashMap<>();

        // the DB configuration
        props.put(MapBasedConnPropsBuilder.KEY_DRIVER, H2Server.DRIVER);
        props.put(MapBasedConnPropsBuilder.KEY_URL, H2Server.URL);
        props.put(MapBasedConnPropsBuilder.KEY_USERNAME, H2Server.USER);
        props.put(MapBasedConnPropsBuilder.KEY_PASSWORD, H2Server.PASSWORD);

        // the JDBC Processor configuration
        props.put(MapBasedJdbcPropsBuilder.KEY_EVENT_ID_FIELD_NAME, EVENT_ID_FIELD_NAME);
        props.put(MapBasedJdbcPropsBuilder.KEY_INSERT_EVENT_SQL_STMT,
                "INSERT INTO events (eventId, auditStreamName, eventJson) VALUES (?, ?, ?)");
        props.put(MapBasedJdbcPropsBuilder.KEY_INDEXED_FIELDS, "");

        // the audit library common configuration
        CommonProperties properties = MapBasedCommonPropsBuilder.build(props);
        properties.setDefaultAuditStream(T_AUDIT_STREAM_NAME);
        properties.setEncoding("UTF-8");
        properties.setFailOnMissingProcessors(true);
        properties.setProcessors(JdbcPoolProcessor.class.getCanonicalName());

        Audit audit = AuditFactory.getInstance(properties.getAuditClassName(), properties);

        Event event = getTestEvent(properties);

        audit.audit(event);
    }

    /**
     * Test that an invalid SQL statement throws an exception
     */
    @Test(expected = AuditException.class)
    public void eventInsertInvalidSqlTest()
            throws FactoryException, AuditException, UnsupportedEncodingException, SQLException {

        Map<String, String> props = new HashMap<>();

        // the DB configuration
        props.put(MapBasedConnPropsBuilder.KEY_DRIVER, H2Server.DRIVER);
        props.put(MapBasedConnPropsBuilder.KEY_URL, H2Server.URL);
        props.put(MapBasedConnPropsBuilder.KEY_USERNAME, H2Server.USER);
        props.put(MapBasedConnPropsBuilder.KEY_PASSWORD, H2Server.PASSWORD);

        // the JDBC Processor configuration
        props.put(MapBasedJdbcPropsBuilder.KEY_EVENT_ID_FIELD_NAME, EVENT_ID_FIELD_NAME);
        props.put(MapBasedJdbcPropsBuilder.KEY_INSERT_EVENT_SQL_STMT,
                "INSERT INTO events_invalid (eventId, auditStreamName, eventJson) VALUES (?, ?, ?) INVALID");

        // the audit library common configuration
        CommonProperties properties = MapBasedCommonPropsBuilder.build(props);
        properties.setDefaultAuditStream(T_AUDIT_STREAM_NAME);
        properties.setEncoding("UTF-8");
        properties.setFailOnMissingProcessors(true);
        properties.setProcessors(JdbcPoolProcessor.class.getCanonicalName());

        Audit audit = AuditFactory.getInstance(properties.getAuditClassName(), properties);

        Event event = getTestEvent(properties);

        try {
            audit.audit(event);
        } catch (AuditException e) {
            AuditErrorConditions expected = AuditErrorConditions.PROCESSING;
            String error = "The type of exception thrown is not correct. Expected " + expected + ", got " + e.getErrorCondition();
            assertThat(error, e.getErrorCondition(), is(equalTo(expected)));
            throw e;
        }

        throw new AssertionError("Expected an exception, but that exception was not thrown");
    }


    ///////////////////////////////////////////////////////////////////////////
    // Field insert tests
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Test that, when a connection and SQL statements for event and field insertion are provided, and a list of fields
     * to be indexed is configured, we can correctly insert events and fields into a database.
     */
    @Test
    public void indexedFieldsInsertTest()
            throws FactoryException, AuditException, UnsupportedEncodingException, SQLException {

        Map<String, String> props = new HashMap<>();

        // the DB configuration
        props.put(MapBasedConnPropsBuilder.KEY_DRIVER, H2Server.DRIVER);
        props.put(MapBasedConnPropsBuilder.KEY_URL, H2Server.URL);
        props.put(MapBasedConnPropsBuilder.KEY_USERNAME, H2Server.USER);
        props.put(MapBasedConnPropsBuilder.KEY_PASSWORD, H2Server.PASSWORD);

        // the JDBC Processor configuration
        props.put(MapBasedJdbcPropsBuilder.KEY_EVENT_ID_FIELD_NAME, EVENT_ID_FIELD_NAME);
        props.put(MapBasedJdbcPropsBuilder.KEY_INSERT_EVENT_SQL_STMT,
                "INSERT INTO events (eventId, auditStreamName, eventJson) VALUES (?, ?, ?)");
        props.put(MapBasedJdbcPropsBuilder.KEY_INSERT_INDEXED_FIELD_SQL_STMT,
                "INSERT INTO fields (eventId, auditStreamName, fieldName, fieldValue) VALUES (?, ?, ?, ?)");
        props.put(MapBasedJdbcPropsBuilder.KEY_INDEXED_FIELDS, "subject,actor:myActor,byteField,invalidField:neverExists");

        // the audit library common configuration
        CommonProperties properties = MapBasedCommonPropsBuilder.build(props);
        properties.setDefaultAuditStream(T_AUDIT_STREAM_NAME);
        properties.setEncoding("UTF-8");
        properties.setFailOnMissingProcessors(true);
        properties.setProcessors(JdbcPoolProcessor.class.getCanonicalName());

        Audit audit = AuditFactory.getInstance(properties.getAuditClassName(), properties);

        Event event = getTestEvent(properties);

        audit.audit(event);

        // assert that the audit operation was successful - get the strings back from the DB!
        JdbcConnectionPool cp = JdbcConnectionPool.create(H2Server.URL, H2Server.USER, H2Server.PASSWORD);
        Connection con = cp.getConnection();
        String eventStmt = "SELECT * FROM events";
        ResultSet eventRs = con.prepareStatement(eventStmt).executeQuery();

        int rsEventSize = 0;
        while (eventRs.next()) {

            // count the size of the rs
            ++rsEventSize;

            String eventId = eventRs.getString("eventId");
            String auditStreamName = eventRs.getString("auditStreamName");
            String eventJson = eventRs.getString("eventJson");

            String error = "The auditStreamName does not have the correct value";
            assertThat(error, auditStreamName, is(equalTo(T_AUDIT_STREAM_NAME)));
            error = "The eventJson does not have the correct value";
            assertThat(error, eventJson, is(equalTo(T_EVENT_JSON)));


            String fieldStmt = "SELECT * FROM fields WHERE eventId = '" + eventId + "'";
            ResultSet fieldRs = con.prepareStatement(fieldStmt).executeQuery();
            int rsFieldSize = 0;
            while (fieldRs.next()) {

                // count the size of the rs
                ++rsFieldSize;

                auditStreamName = fieldRs.getString("auditStreamName");
                String fieldName = fieldRs.getString("fieldName");
                String fieldValue = fieldRs.getString("fieldValue");

                error = "The auditStreamName does not have the correct value";
                assertThat(error, auditStreamName, is(equalTo(T_AUDIT_STREAM_NAME)));
                error = "The fieldName is not in the list of expected values";
                assertThat(error, Arrays.asList(T_FIELD_NAMES), hasItem(fieldName));
                error = "The fieldValue is not in the list of expected values";
                assertThat(error, Arrays.asList(T_FIELD_VALUES), hasItem(fieldValue));
            }

            error = "The indexed fields result set size does not have the correct size";
            assertThat(error, rsFieldSize, is(equalTo(3)));
        }

        String error = "The event result set size does not have the correct size";
        assertThat(error, rsEventSize, is(equalTo(1)));

        con.close();
    }

    /**
     * Test that, when trying to insert an oversized field in the index table, that field is truncated
     * to the correct maximum length.
     */
    @Test
    public void indexedFieldsOversizedFieldInsertTest()
            throws FactoryException, AuditException, UnsupportedEncodingException, SQLException {

        Map<String, String> props = new HashMap<>();

        // the DB configuration
        props.put(MapBasedConnPropsBuilder.KEY_DRIVER, H2Server.DRIVER);
        props.put(MapBasedConnPropsBuilder.KEY_URL, H2Server.URL);
        props.put(MapBasedConnPropsBuilder.KEY_USERNAME, H2Server.USER);
        props.put(MapBasedConnPropsBuilder.KEY_PASSWORD, H2Server.PASSWORD);

        // the JDBC Processor configuration
        props.put(MapBasedJdbcPropsBuilder.KEY_EVENT_ID_FIELD_NAME, EVENT_ID_FIELD_NAME);
        props.put(MapBasedJdbcPropsBuilder.KEY_INSERT_EVENT_SQL_STMT,
                "INSERT INTO events (eventId, auditStreamName, eventJson) VALUES (?, ?, ?)");
        props.put(MapBasedJdbcPropsBuilder.KEY_INSERT_INDEXED_FIELD_SQL_STMT,
                "INSERT INTO fields (eventId, auditStreamName, fieldName, fieldValue) VALUES (?, ?, ?, ?)");
        props.put(MapBasedJdbcPropsBuilder.KEY_INDEXED_FIELDS, "subject");
        props.put(MapBasedJdbcPropsBuilder.KEY_INDEXED_FIELDS_MAX_LENGTH, "3");

        // the audit library common configuration
        CommonProperties properties = MapBasedCommonPropsBuilder.build(props);
        properties.setDefaultAuditStream(T_AUDIT_STREAM_NAME);
        properties.setEncoding("UTF-8");
        properties.setFailOnMissingProcessors(true);
        properties.setProcessors(JdbcPoolProcessor.class.getCanonicalName());

        Audit audit = AuditFactory.getInstance(properties.getAuditClassName(), properties);

        Event event = getTestEvent(properties);

        audit.audit(event);

        // assert that the audit operation was successful - get the strings back from the DB!
        JdbcConnectionPool cp = JdbcConnectionPool.create(H2Server.URL, H2Server.USER, H2Server.PASSWORD);
        Connection con = cp.getConnection();
        String eventStmt = "SELECT * FROM events";
        ResultSet eventRs = con.prepareStatement(eventStmt).executeQuery();

        while (eventRs.next()) {

            String eventId = eventRs.getString("eventId");

            String fieldStmt = "SELECT * FROM fields WHERE eventId = '" + eventId + "'";
            ResultSet fieldRs = con.prepareStatement(fieldStmt).executeQuery();
            while (fieldRs.next()) {

                String fieldName = fieldRs.getString("fieldName");
                String fieldValue = fieldRs.getString("fieldValue");

                // there should only be one field (and one iteration) of this loop - the `subject` field:
                String error = "The fieldName does not match the expected value";
                assertThat(error, fieldName, is(equalTo("subject")));
                // the subject should have been truncated to the first three chars:
                error = "The fieldValue does not match the expected value";
                assertThat(error, fieldValue, is(equalTo("Sub")));
            }

        }

        con.close();
    }

    /**
     * Test that a field is converted to lowercase if so configured.
     */
    @Test
    public void indexedFieldsToLowerFieldInsertTest()
            throws FactoryException, AuditException, UnsupportedEncodingException, SQLException {

        Map<String, String> props = new HashMap<>();

        // the DB configuration
        props.put(MapBasedConnPropsBuilder.KEY_DRIVER, H2Server.DRIVER);
        props.put(MapBasedConnPropsBuilder.KEY_URL, H2Server.URL);
        props.put(MapBasedConnPropsBuilder.KEY_USERNAME, H2Server.USER);
        props.put(MapBasedConnPropsBuilder.KEY_PASSWORD, H2Server.PASSWORD);

        // the JDBC Processor configuration
        props.put(MapBasedJdbcPropsBuilder.KEY_EVENT_ID_FIELD_NAME, EVENT_ID_FIELD_NAME);
        props.put(MapBasedJdbcPropsBuilder.KEY_INSERT_EVENT_SQL_STMT,
                "INSERT INTO events (eventId, auditStreamName, eventJson) VALUES (?, ?, ?)");
        props.put(MapBasedJdbcPropsBuilder.KEY_INSERT_INDEXED_FIELD_SQL_STMT,
                "INSERT INTO fields (eventId, auditStreamName, fieldName, fieldValue) VALUES (?, ?, ?, ?)");
        props.put(MapBasedJdbcPropsBuilder.KEY_INDEXED_FIELDS, "someCaseSensitiveField");
        props.put(MapBasedJdbcPropsBuilder.KEY_INDEXED_FIELDS_TO_LOWER, "true");

        // the audit library common configuration
        CommonProperties properties = MapBasedCommonPropsBuilder.build(props);
        properties.setDefaultAuditStream(T_AUDIT_STREAM_NAME);
        properties.setEncoding("UTF-8");
        properties.setFailOnMissingProcessors(true);
        properties.setProcessors(JdbcPoolProcessor.class.getCanonicalName());

        Audit audit = AuditFactory.getInstance(properties.getAuditClassName(), properties);

        Event event = getTestEvent(properties);
        Field field = new EventField("someCaseSensitiveField", "CaseSensitiveText".getBytes(StandardCharsets.UTF_8));
        event.setField(field);

        audit.audit(event);

        // assert that the audit operation was successful - get the strings back from the DB!
        JdbcConnectionPool cp = JdbcConnectionPool.create(H2Server.URL, H2Server.USER, H2Server.PASSWORD);
        Connection con = cp.getConnection();
        String eventStmt = "SELECT * FROM events";
        ResultSet eventRs = con.prepareStatement(eventStmt).executeQuery();

        while (eventRs.next()) {

            String eventId = eventRs.getString("eventId");

            String fieldStmt = "SELECT * FROM fields WHERE eventId = '" + eventId + "'";
            ResultSet fieldRs = con.prepareStatement(fieldStmt).executeQuery();
            while (fieldRs.next()) {

                String fieldName = fieldRs.getString("fieldName");
                String fieldValue = fieldRs.getString("fieldValue");

                // there should only be one field (and one iteration) of this loop - the `someCaseSensitiveField` field:
                String error = "The fieldName does not match the expected value";
                assertThat(error, fieldName, is(equalTo("someCaseSensitiveField")));
                // the field value should be all lowercase
                error = "The fieldValue does not match the expected value";
                assertThat(error, fieldValue, is(equalTo("CaseSensitiveText".toLowerCase())));
            }

        }

        con.close();
    }

    /**
     * Test that a field value is properly normalized to NFC.
     */
    @Test
    public void indexedFieldsNormalizedFieldInsertTest()
            throws FactoryException, AuditException, UnsupportedEncodingException, SQLException {

        Map<String, String> props = new HashMap<>();

        // the DB configuration
        props.put(MapBasedConnPropsBuilder.KEY_DRIVER, H2Server.DRIVER);
        props.put(MapBasedConnPropsBuilder.KEY_URL, H2Server.URL);
        props.put(MapBasedConnPropsBuilder.KEY_USERNAME, H2Server.USER);
        props.put(MapBasedConnPropsBuilder.KEY_PASSWORD, H2Server.PASSWORD);

        // the JDBC Processor configuration
        props.put(MapBasedJdbcPropsBuilder.KEY_EVENT_ID_FIELD_NAME, EVENT_ID_FIELD_NAME);
        props.put(MapBasedJdbcPropsBuilder.KEY_INSERT_EVENT_SQL_STMT,
                "INSERT INTO events (eventId, auditStreamName, eventJson) VALUES (?, ?, ?)");
        props.put(MapBasedJdbcPropsBuilder.KEY_INSERT_INDEXED_FIELD_SQL_STMT,
                "INSERT INTO fields (eventId, auditStreamName, fieldName, fieldValue) VALUES (?, ?, ?, ?)");
        props.put(MapBasedJdbcPropsBuilder.KEY_INDEXED_FIELDS, "umlautField,accentField");

        // the audit library common configuration
        CommonProperties properties = MapBasedCommonPropsBuilder.build(props);
        properties.setDefaultAuditStream(T_AUDIT_STREAM_NAME);
        properties.setEncoding("UTF-8");
        properties.setFailOnMissingProcessors(true);
        properties.setProcessors(JdbcPoolProcessor.class.getCanonicalName());

        Audit audit = AuditFactory.getInstance(properties.getAuditClassName(), properties);

        Event event = getTestEvent(properties);
        // LATIN CAPITAL LETTER A + COMBINING DIAERESES
        Field field = new EventField("umlautField", "\u0041\u0308".getBytes(StandardCharsets.UTF_8));
        event.setField(field);
        // LATIN SMALL LETTER E + COMBINING ACUTE ACCENT
        field = new EventField("accentField", "\u0065\u0301".getBytes(StandardCharsets.UTF_8));
        event.setField(field);

        audit.audit(event);

        // assert that the audit operation was successful - get the strings back from the DB!
        JdbcConnectionPool cp = JdbcConnectionPool.create(H2Server.URL, H2Server.USER, H2Server.PASSWORD);
        Connection con = cp.getConnection();
        String eventStmt = "SELECT * FROM events";
        ResultSet eventRs = con.prepareStatement(eventStmt).executeQuery();

        while (eventRs.next()) {

            String eventId = eventRs.getString("eventId");

            String fieldStmt = "SELECT * FROM fields WHERE eventId = '" + eventId + "'";
            ResultSet fieldRs = con.prepareStatement(fieldStmt).executeQuery();
            while (fieldRs.next()) {

                String fieldName = fieldRs.getString("fieldName");
                String fieldValue = fieldRs.getString("fieldValue");

                switch (fieldName) {
                    case "umlautField":
                        String error = "The fieldValue does not match the expected value";
                        // LATIN CAPITAL LETTER A WITH DIAERESES
                        assertThat(error, fieldValue, is(equalTo("\u00C4")));
                        break;
                    case "accentField":
                        error = "The fieldValue does not match the expected value";
                        // LATIN SMALL LETTER E WITH ACUTE
                        assertThat(error, fieldValue, is(equalTo("\u00E9")));
                        break;
                    default:
                        throw new AssertionError("Unexpected field name: " + fieldName);
                }
            }

        }

        con.close();
    }

    /**
     * Test that an invalid field SQL statement throws an exception
     */
    @Test(expected = AuditException.class)
    public void indexedFieldsInsertInvalidSqlTest()
            throws FactoryException, AuditException, UnsupportedEncodingException, SQLException {

        Map<String, String> props = new HashMap<>();

        // the DB configuration
        props.put(MapBasedConnPropsBuilder.KEY_DRIVER, H2Server.DRIVER);
        props.put(MapBasedConnPropsBuilder.KEY_URL, H2Server.URL);
        props.put(MapBasedConnPropsBuilder.KEY_USERNAME, H2Server.USER);
        props.put(MapBasedConnPropsBuilder.KEY_PASSWORD, H2Server.PASSWORD);

        // the JDBC Processor configuration
        props.put(MapBasedJdbcPropsBuilder.KEY_EVENT_ID_FIELD_NAME, EVENT_ID_FIELD_NAME);
        props.put(MapBasedJdbcPropsBuilder.KEY_INSERT_EVENT_SQL_STMT,
                "INSERT INTO events (eventId, auditStreamName, eventJson) VALUES (?, ?, ?)");
        props.put(MapBasedJdbcPropsBuilder.KEY_INSERT_INDEXED_FIELD_SQL_STMT,
                "INSERT INTO fields_invalid (eventId, auditStreamName, fieldName, fieldValue) VALUES (?, ?, ?, ?) INVALID");
        props.put(MapBasedJdbcPropsBuilder.KEY_INDEXED_FIELDS, "subject,actor:myActor,byteField,invalidField:neverExists");

        // the audit library common configuration
        CommonProperties properties = MapBasedCommonPropsBuilder.build(props);
        properties.setDefaultAuditStream(T_AUDIT_STREAM_NAME);
        properties.setEncoding("UTF-8");
        properties.setFailOnMissingProcessors(true);
        properties.setProcessors(JdbcPoolProcessor.class.getCanonicalName());

        Audit audit = AuditFactory.getInstance(properties.getAuditClassName(), properties);

        Event event = getTestEvent(properties);

        try {
            audit.audit(event);
        } catch (AuditException e) {
            AuditErrorConditions expected = AuditErrorConditions.PROCESSING;
            String error = "The type of exception thrown is not correct. Expected " + expected + ", got " + e.getErrorCondition();
            assertThat(error, e.getErrorCondition(), is(equalTo(expected)));
            throw e;
        }

        throw new AssertionError("Expected an exception, but that exception was not thrown");
    }

    ///////////////////////////////////////////////////////////////////////////
    // DB connection tests
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Test that an invalid Driver throws an exception
     */
    @Test(expected = AuditException.class)
    public void invalidDriverTest()
            throws FactoryException, AuditException, UnsupportedEncodingException, SQLException {

        Map<String, String> props = new HashMap<>();

        // the DB configuration
        props.put(MapBasedConnPropsBuilder.KEY_DRIVER, H2Server.DRIVER + "_INVALID");
        props.put(MapBasedConnPropsBuilder.KEY_URL, H2Server.URL);
        props.put(MapBasedConnPropsBuilder.KEY_USERNAME, H2Server.USER);
        props.put(MapBasedConnPropsBuilder.KEY_PASSWORD, H2Server.PASSWORD);

        // the JDBC Processor configuration
        props.put(MapBasedJdbcPropsBuilder.KEY_EVENT_ID_FIELD_NAME, EVENT_ID_FIELD_NAME);
        props.put(MapBasedJdbcPropsBuilder.KEY_INSERT_EVENT_SQL_STMT,
                "INSERT INTO events (eventId, auditStreamName, eventJson) VALUES (?, ?, ?)");
        props.put(MapBasedJdbcPropsBuilder.KEY_INDEXED_FIELDS, "");

        // the audit library common configuration
        CommonProperties properties = MapBasedCommonPropsBuilder.build(props);
        properties.setDefaultAuditStream(T_AUDIT_STREAM_NAME);
        properties.setEncoding("UTF-8");
        properties.setFailOnMissingProcessors(true);
        properties.setProcessors(JdbcPoolProcessor.class.getCanonicalName());

        Audit audit = AuditFactory.getInstance(properties.getAuditClassName(), properties);

        Event event = getTestEvent(properties);

        audit.audit(event);
    }

    /**
     * Test that an invalid URL throws an exception
     */
    @Test(expected = AuditException.class)
    public void invalidUrlTest()
            throws FactoryException, AuditException, UnsupportedEncodingException, SQLException {

        Map<String, String> props = new HashMap<>();

        // the DB configuration
        props.put(MapBasedConnPropsBuilder.KEY_DRIVER, H2Server.DRIVER);
        props.put(MapBasedConnPropsBuilder.KEY_URL, H2Server.URL + "_INVALID");
        props.put(MapBasedConnPropsBuilder.KEY_USERNAME, H2Server.USER);
        props.put(MapBasedConnPropsBuilder.KEY_PASSWORD, H2Server.PASSWORD);

        // the JDBC Processor configuration
        props.put(MapBasedJdbcPropsBuilder.KEY_EVENT_ID_FIELD_NAME, EVENT_ID_FIELD_NAME);
        props.put(MapBasedJdbcPropsBuilder.KEY_INSERT_EVENT_SQL_STMT,
                "INSERT INTO events (eventId, auditStreamName, eventJson) VALUES (?, ?, ?)");
        props.put(MapBasedJdbcPropsBuilder.KEY_INDEXED_FIELDS, "");

        // the audit library common configuration
        CommonProperties properties = MapBasedCommonPropsBuilder.build(props);
        properties.setDefaultAuditStream(T_AUDIT_STREAM_NAME);
        properties.setEncoding("UTF-8");
        properties.setFailOnMissingProcessors(true);
        properties.setProcessors(JdbcPoolProcessor.class.getCanonicalName());

        Audit audit = AuditFactory.getInstance(properties.getAuditClassName(), properties);

        Event event = getTestEvent(properties);

        audit.audit(event);
    }

    /**
     * Test that an invalid username throws an exception
     */
    @Test(expected = AuditException.class)
    public void invalidUserTest()
            throws FactoryException, AuditException, UnsupportedEncodingException, SQLException {

        Map<String, String> props = new HashMap<>();

        // the DB configuration
        props.put(MapBasedConnPropsBuilder.KEY_DRIVER, H2Server.DRIVER);
        props.put(MapBasedConnPropsBuilder.KEY_URL, H2Server.URL);
        props.put(MapBasedConnPropsBuilder.KEY_USERNAME, H2Server.USER + "_INVALID");
        props.put(MapBasedConnPropsBuilder.KEY_PASSWORD, H2Server.PASSWORD);

        // the JDBC Processor configuration
        props.put(MapBasedJdbcPropsBuilder.KEY_EVENT_ID_FIELD_NAME, EVENT_ID_FIELD_NAME);
        props.put(MapBasedJdbcPropsBuilder.KEY_INSERT_EVENT_SQL_STMT,
                "INSERT INTO events (eventId, auditStreamName, eventJson) VALUES (?, ?, ?)");
        props.put(MapBasedJdbcPropsBuilder.KEY_INDEXED_FIELDS, "");

        // the audit library common configuration
        CommonProperties properties = MapBasedCommonPropsBuilder.build(props);
        properties.setDefaultAuditStream(T_AUDIT_STREAM_NAME);
        properties.setEncoding("UTF-8");
        properties.setFailOnMissingProcessors(true);
        properties.setProcessors(JdbcPoolProcessor.class.getCanonicalName());

        Audit audit = AuditFactory.getInstance(properties.getAuditClassName(), properties);

        Event event = getTestEvent(properties);

        audit.audit(event);
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

        Processor processor = new JdbcPoolProcessor();
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
