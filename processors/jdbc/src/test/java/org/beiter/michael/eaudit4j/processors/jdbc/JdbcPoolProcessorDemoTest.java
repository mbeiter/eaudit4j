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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class JdbcPoolProcessorDemoTest {

    /**
     * The logger object for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(JdbcPoolProcessorDemoTest.class);

    // a default field name and value for the event ID that we will use in this demo
    public static final String EVENT_ID_FIELD_NAME = "eventId";
    public static final String EVENT_ID = "1234567890ABCDEF";

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

    @Test
    public void jdbcPoolProcessorDemoTest()
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
        properties.setDefaultAuditStream("1234567890");
        properties.setEncoding("UTF-8");
        properties.setFailOnMissingProcessors(true);
        properties.setProcessors(JdbcPoolProcessor.class.getCanonicalName());

        Audit audit = AuditFactory.getInstance(properties.getAuditClassName(), properties);

        // the event ID is required for this processor to work
        Field eventIdField = new EventField(EVENT_ID_FIELD_NAME, EVENT_ID.getBytes("UTF-8"));

        Field field = new EventField("byteField", new Hex().encode("1234".getBytes("UTF-8")), Encodings.HEX);

        // create the event, using (amongst others) the custom field above
        Event event = new EventBuilder(properties)
                .setField(eventIdField)
                .setSubject("SubjectId-1234".toCharArray())
                .setObject("ObjectId-3456".toCharArray())
                .setActor("ActorId-5678".toCharArray())
                .setResult("Some result".toCharArray())
                .setField(field)
                .build();

        audit.audit(event);

        // demo that the audit operation was successful
        JdbcConnectionPool cp = JdbcConnectionPool.create(H2Server.URL, H2Server.USER, H2Server.PASSWORD);
        Connection con = cp.getConnection();
        String eventStmt = "SELECT * FROM events";
        ResultSet eventRs = con.prepareStatement(eventStmt).executeQuery();

        while (eventRs.next()) {
            String result = "event: " +
                    "\n\t id: " + eventRs.getString("id") +
                    "\n\t eventId: " + eventRs.getString("eventId") +
                    "\n\t auditStreamName: " + eventRs.getString("auditStreamName") +
                    "\n\t eventJson: " + eventRs.getString("eventJson");
            String eventId = eventRs.getString("eventId");

            String fieldStmt = "SELECT * FROM fields WHERE eventId = '" + eventId + "'";
            ResultSet fieldRs = con.prepareStatement(fieldStmt).executeQuery();
            int count = 1;
            while (fieldRs.next()) {
                result += "\nfield " + count++ + " in event " + eventId + ":" +
                        "\n\t id: " + fieldRs.getString("id") +
                        "\n\t eventId: " + fieldRs.getString("eventId") +
                        "\n\t auditStreamName: " + fieldRs.getString("auditStreamName") +
                        "\n\t fieldName: " + fieldRs.getString("fieldName") +
                        "\n\t fieldValue: " + fieldRs.getString("fieldValue");
            }

            LOG.info(result);
        }

        con.close();
    }
}
