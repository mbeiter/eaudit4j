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
import org.beiter.michael.eaudit4j.common.impl.EventBuilder;
import org.beiter.michael.eaudit4j.common.impl.EventField;
import org.beiter.michael.eaudit4j.common.propsbuilder.MapBasedCommonPropsBuilder;
import org.beiter.michael.eaudit4j.processors.cassandra.propsbuilder.MapBasedCassandraPropsBuilder;

import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class CassandraProcessorTest {
    public static final String EVENT_ID_FIELD_NAME = "eventId";
    public static final String EVENT_ID = "1234567890ABCDEF";
    public static final String T_AUDIT_STREAM_NAME = "1234567890";
    public static final String T_SUBJECT = "SubjectId-1234";
    public static final String T_OBJECT = "ObjectId-3456";
    public static final String T_ACTOR = "ActorId-5678";
    public static final String T_RESULT = "Some result";
    public static final String T_EVENT_JSON = "{\"version\":\"1.0\","
            + "\"fields\":{\"actor\":\"" + T_ACTOR + "\",\"result\":\"" + T_RESULT + "\",\"" + EVENT_ID_FIELD_NAME
            + "\":\"" + EVENT_ID + "\",\"subject\":\"" + T_SUBJECT + "\",\"byteField\":\"31323334\",\"object\":\""
            + T_OBJECT + "\"}}";

    @Test
    public void shouldInsertAnEvent()
            throws FactoryException, AuditException, IOException, InterruptedException, TTransportException {

        CassandraServer.startCassandra();

        Map<String, String> propsMap = new HashMap<>();
        propsMap.put(MapBasedCassandraPropsBuilder.KEY_EVENT_ID_FIELD_NAME, EVENT_ID_FIELD_NAME);
        propsMap.put(MapBasedCassandraPropsBuilder.KEY_INSERT_EVENT_SQL_STMT,
                "INSERT INTO audit_test.events (eventId, auditStream, eventJson) VALUES (?, ?, ?)");

        ProcessingObjects processingObjects = new ProcessingObjects();
        processingObjects.add(MapBasedCassandraPropsBuilder.KEY_CASSANDRA_CONNECTION_SESSION,
                CassandraServer.getCluster().newSession());

        CassandraProperties properties = new CassandraProperties();
        properties.setAdditionalProperties(propsMap);

        CommonProperties commonProperties = MapBasedCommonPropsBuilder.build(propsMap);
        commonProperties.setDefaultAuditStream(T_AUDIT_STREAM_NAME);
        commonProperties.setEncoding("UTF-8");
        commonProperties.setFailOnMissingProcessors(true);
        commonProperties.setProcessors(CassandraProcessor.class.getCanonicalName());

        Audit audit = AuditFactory.getInstance(commonProperties.getAuditClassName(), commonProperties);
        Event event = getTestEvent(commonProperties);
        audit.audit(event, T_AUDIT_STREAM_NAME, processingObjects);

        Session session = CassandraServer.getCluster().newSession();
        ResultSet eventRs = session.execute("SELECT * FROM audit_test.events");

        int resultSize = eventRs.all().size();

        for(Row row : eventRs.all()){
            String auditStreamName = row.getString("auditStream");
            String eventJson = row.getString("eventJson");

            String error = "The auditStreamName does not have the correct value";
            assertThat(error, auditStreamName, is(equalTo(T_AUDIT_STREAM_NAME)));
            error = "The eventJson does not have the correct value";
            assertThat(error, eventJson, is(equalTo(T_EVENT_JSON)));
        }

        String error = "The event result set size does not have the correct size";
        assertThat(error, resultSize, is(equalTo(1)));

        session.close();

        CassandraServer.cleanUp();
    }

    @Test(expected = AuditException.class)
    public void shouldTryToInsertAnEventWithInvalidSql()
            throws FactoryException, AuditException, IOException, SQLException, InterruptedException,
            TTransportException {

        CassandraServer.startCassandra();

        Map<String, String> propsMap = new HashMap<>();
        propsMap.put(MapBasedCassandraPropsBuilder.KEY_EVENT_ID_FIELD_NAME, EVENT_ID_FIELD_NAME);
        propsMap.put(MapBasedCassandraPropsBuilder.KEY_INSERT_EVENT_SQL_STMT,
                "INSERT INTO audit_test.events_invalid (eventId, auditStream, eventJson) VALUES (?, ?, ?)");
        CassandraProperties properties = new CassandraProperties();
        properties.setAdditionalProperties(propsMap);

        ProcessingObjects processingObjects = new ProcessingObjects();
        processingObjects.add(MapBasedCassandraPropsBuilder.KEY_CASSANDRA_CONNECTION_SESSION,
                CassandraServer.getCluster().newSession());

        CommonProperties commonProperties = MapBasedCommonPropsBuilder.build(propsMap);
        commonProperties.setDefaultAuditStream(T_AUDIT_STREAM_NAME);
        commonProperties.setEncoding("UTF-8");
        commonProperties.setFailOnMissingProcessors(true);
        commonProperties.setProcessors(CassandraProcessor.class.getCanonicalName());

        Audit audit = AuditFactory.getInstance(commonProperties.getAuditClassName(), commonProperties);

        Event event = getTestEvent(commonProperties);

        try {
            audit.audit(event, T_AUDIT_STREAM_NAME, processingObjects);
        } catch (AuditException e) {
            CassandraServer.cleanUp();
            AuditErrorConditions expected = AuditErrorConditions.PROCESSING;
            String error = "The type of exception thrown is not correct. Expected " + expected + ", got " + e.getErrorCondition();
            assertThat(error, e.getErrorCondition(), is(equalTo(expected)));
            throw e;
        }
    }

    @Test(expected = AuditException.class)
    public void shouldTryToInsertWithoutASession()
        throws FactoryException, AuditException, IOException, InterruptedException, TTransportException {

        Map<String, String> propsMap = new HashMap<>();
        propsMap.put(MapBasedCassandraPropsBuilder.KEY_EVENT_ID_FIELD_NAME, EVENT_ID_FIELD_NAME);
        propsMap.put(MapBasedCassandraPropsBuilder.KEY_INSERT_EVENT_SQL_STMT,
            "INSERT INTO audit_test.events (eventId, auditStream, eventJson) VALUES (?, ?, ?)");

        ProcessingObjects processingObjects = new ProcessingObjects();

        CassandraProperties properties = new CassandraProperties();
        properties.setAdditionalProperties(propsMap);

        CommonProperties commonProperties = MapBasedCommonPropsBuilder.build(propsMap);
        commonProperties.setDefaultAuditStream(T_AUDIT_STREAM_NAME);
        commonProperties.setEncoding("UTF-8");
        commonProperties.setFailOnMissingProcessors(true);
        commonProperties.setProcessors(CassandraProcessor.class.getCanonicalName());

        Audit audit = AuditFactory.getInstance(commonProperties.getAuditClassName(), commonProperties);
        Event event = getTestEvent(commonProperties);
        audit.audit(event, T_AUDIT_STREAM_NAME, processingObjects);

    }

    @Test(expected = AuditException.class)
    public void shouldTryToInsertANullEvent()
            throws FactoryException, AuditException, IOException, InterruptedException, TTransportException {

        Map<String, String> propsMap = new HashMap<>();
        propsMap.put(MapBasedCassandraPropsBuilder.KEY_EVENT_ID_FIELD_NAME, EVENT_ID_FIELD_NAME);
        propsMap.put(MapBasedCassandraPropsBuilder.KEY_INSERT_EVENT_SQL_STMT,
                "INSERT INTO audit_test.events_invalid (eventId, auditStream, eventJson) VALUES (?, ?, ?)");
        CassandraProperties properties = new CassandraProperties();
        properties.setAdditionalProperties(propsMap);

        CommonProperties commonProperties = MapBasedCommonPropsBuilder.build(propsMap);
        commonProperties.setDefaultAuditStream(T_AUDIT_STREAM_NAME);
        commonProperties.setEncoding("UTF-8");
        commonProperties.setFailOnMissingProcessors(true);
        commonProperties.setProcessors(CassandraProcessor.class.getCanonicalName());

        CassandraProcessor processor = new CassandraProcessor();
        processor.init(commonProperties);
        processor.process(null);
    }

    @Test(expected = AuditException.class)
    public void shouldTryToInsertAnEventWithNullProperties()
            throws FactoryException, AuditException, UnsupportedEncodingException {

        Map<String, String> propsMap = new HashMap<>();
        CommonProperties commonProperties = MapBasedCommonPropsBuilder.build(propsMap);
        commonProperties.setProcessors(CassandraProcessor.class.getCanonicalName());

        CassandraProcessor processor = new CassandraProcessor();
        processor.init(commonProperties);
        Event event = getTestEvent(commonProperties);
        processor.process(event);
    }

    @Test
    public void shouldRevert()
            throws FactoryException, AuditException, UnsupportedEncodingException {

        Map<String, String> propsMap = new HashMap<>();
        CommonProperties commonProperties = MapBasedCommonPropsBuilder.build(propsMap);
        commonProperties.setProcessors(CassandraProcessor.class.getCanonicalName());

        CassandraProcessor processor = new CassandraProcessor();
        Event event = getTestEvent(commonProperties);
        assertEquals(event, processor.revert(event));
    }

    @Test(expected = AuditException.class)
    public void shouldTryToInsertAnEventWithNullEventId()
            throws FactoryException, AuditException, UnsupportedEncodingException {

        Map<String, String> propsMap = new HashMap<>();
        CommonProperties commonProperties = MapBasedCommonPropsBuilder.build(propsMap);
        commonProperties.setDefaultAuditStream(T_AUDIT_STREAM_NAME);
        commonProperties.setEncoding("UTF-8");
        commonProperties.setFailOnMissingProcessors(true);
        commonProperties.setProcessors(CassandraProcessor.class.getCanonicalName());

        Audit audit = AuditFactory.getInstance(commonProperties.getAuditClassName(), commonProperties);

        Event event = getTestEvent(commonProperties);
        audit.audit(event);
    }

    private static Event getTestEvent(CommonProperties properties) throws UnsupportedEncodingException {
        Field eventIdField = new EventField(EVENT_ID_FIELD_NAME, EVENT_ID.getBytes("UTF-8"));
        Field field = new EventField("byteField", new Hex().encode("1234".getBytes("UTF-8")), Encodings.HEX);

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
