/*
<<<<<<< HEAD
 * #%L
 * This file is part of eAudit4j, a library for creating pluggable
 * auditing solutions, providing an application to execute performance tests agains supported databases.
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
package com.hp.audit.cassandra;
import com.hp.audit.Performance;
import org.beiter.michael.eaudit4j.common.Event;
import org.beiter.michael.eaudit4j.common.Audit;
import org.beiter.michael.eaudit4j.common.AuditFactory;
import org.beiter.michael.eaudit4j.common.CommonProperties;
import org.beiter.michael.eaudit4j.common.ProcessingObjects;
import org.beiter.michael.eaudit4j.common.Field;
import org.beiter.michael.eaudit4j.common.impl.EventBuilder;
import org.beiter.michael.eaudit4j.common.impl.EventField;
import org.beiter.michael.eaudit4j.common.propsbuilder.MapBasedCommonPropsBuilder;
import org.beiter.michael.eaudit4j.processors.cassandra.CassandraProcessor;
import org.beiter.michael.eaudit4j.processors.cassandra.propsbuilder.MapBasedCassandraPropsBuilder;
import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.hp.audit.Performance.*;
import static com.hp.audit.PropertiesUtil.getIntProperty;

public class CassandraTest {
    public static final String EVENT_ID_FIELD_NAME = "eventId";
    private static final String EVENT_ID_CQL_FIELD = "eventId";
    private static final String AUDIT_STREAM_NAME_CQL_FIELD = "auditStreamName";
    private static final String EVENT_JSON_CQL_FIELD = "eventJson";
    private static final String CASSANDRA_KEYSPACE = "audit";
    private static final String INSERT_EVENT_CQL_STMT =
            "INSERT INTO " + CASSANDRA_KEYSPACE + ".events (eventId, auditStream, eventJson) VALUES (:"
                    + EVENT_ID_CQL_FIELD + ", :" + AUDIT_STREAM_NAME_CQL_FIELD + ", :" + EVENT_JSON_CQL_FIELD + ")";

    private static int numThreads = getIntProperty(NUM_THREADS, 10);
    private static int numLoops = getIntProperty(NUM_LOOPS, Integer.MAX_VALUE);
    private static int rampUpInMilliseconds = getIntProperty(RAMP_UP_TIME, 1);
    private static String pathname = System.getProperty(JTL_FILE, System.getProperty(USER_HOME) + DEFAULT_FILE);
    private static int maxTestTime = getIntProperty(MAX_TEST_TIME, 5);
    private static String maxTestTimeUnit = System.getProperty(MAX_TEST_TIME_UNIT, MAX_TEST_TIME_DEFAULT_UNIT)
            .toUpperCase();
    private static int maxWarmUpTime = getIntProperty(MAX_WARM_UP_TIME, 10);
    private static String maxWarmUpTimeUnit = System.getProperty(MAX_WARM_UP_TIME_UNIT, WARM_UP_TIME_DEFAULT_UNIT)
            .toUpperCase();

    ProcessingObjects processingObjects;
    Map<String, String> props;
    CommonProperties properties;
    Audit audit;

    public static void main(String... args) throws Exception {
        try {
            CassandraTest cassandraTest = new CassandraTest();
            CassandraServerUtil.startConnection();
            CassandraServerUtil.recreateTables();

            cassandraTest.fillProperties();
            cassandraTest.fillAuditProp();

            cassandraTest.audit = AuditFactory.getInstance(cassandraTest.properties.getAuditClassName(),
                    cassandraTest.properties);

            Performance warmUpTest = new Performance(numThreads, numLoops, rampUpInMilliseconds,
                    new File(pathname.replace(".jtl", "_warmUp.jtl")), maxWarmUpTime, TimeUnit.valueOf(maxWarmUpTimeUnit));
            warmUpTest.runTests("Cassandra warm up test", cassandraTest.supplier(), cassandraTest.consumer());

            Performance performance = new Performance(numThreads, numLoops, rampUpInMilliseconds,
                    new File(pathname), maxTestTime, TimeUnit.valueOf(maxTestTimeUnit));

            performance.runTests("Cassandra test", cassandraTest.supplier(), cassandraTest.consumer());
            System.out.println("Done!!!");

        } finally {
            CassandraServerUtil.close();
        }
    }

    private void fillAuditProp() {
        // the audit library common configuration
        properties = MapBasedCommonPropsBuilder.build(props);
        properties.setDefaultAuditStream("1234567890");
        properties.setEncoding("UTF-8");
        properties.setFailOnMissingProcessors(true);
        properties.setProcessors(CassandraProcessor.class.getCanonicalName());
    }

    private void fillProperties() throws SQLException {
        // the Processor configuration
        props = new HashMap<>();
        processingObjects = new ProcessingObjects();
        processingObjects.add("mySession", CassandraServerUtil.getCluster().newSession());

        props.put(MapBasedCassandraPropsBuilder.KEY_SESSION_NAME, "mySession");
        props.put(MapBasedCassandraPropsBuilder.KEY_EVENT_ID_FIELD_NAME, EVENT_ID_FIELD_NAME);

        props.put(MapBasedCassandraPropsBuilder.KEY_EVENT_ID_CQL_PARAM, EVENT_ID_CQL_FIELD);
        props.put(MapBasedCassandraPropsBuilder.KEY_AUDIT_STREAM_NAME_CQL_PARAM, AUDIT_STREAM_NAME_CQL_FIELD);
        props.put(MapBasedCassandraPropsBuilder.KEY_EVENT_JSON_CQL_PARAM, EVENT_JSON_CQL_FIELD);
        props.put(MapBasedCassandraPropsBuilder.KEY_INSERT_EVENT_CQL_STMT, INSERT_EVENT_CQL_STMT);
    }

    private Supplier<Event> supplier() {
        return () -> {
            // the event ID is required for this processor to work
            Field eventIdField = new EventField(EVENT_ID_FIELD_NAME,UUID.randomUUID().toString().getBytes());

            // create the event, using (amongst others) the custom field above
            return new EventBuilder(properties)
                    .setField(eventIdField)
                    .setSubject("SubjectId-1234".toCharArray())
                    .setObject("ObjectId-3456".toCharArray())
                    .setActor("ActorId-5678".toCharArray())
                    .setResult("Some result".toCharArray())
                    .build();
        };
    }

    private Consumer<Event> consumer() {
        return (event) -> {
            try {
                audit.audit(event, "1234567890", processingObjects);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            } finally {
                System.out.print(".");
            }
        };
    }
}
