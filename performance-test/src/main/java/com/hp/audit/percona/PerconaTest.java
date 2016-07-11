/*
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
package com.hp.audit.percona;

import com.hp.audit.Performance;
import org.beiter.michael.eaudit4j.common.Audit;
import org.beiter.michael.eaudit4j.common.AuditFactory;
import org.beiter.michael.eaudit4j.common.CommonProperties;
import org.beiter.michael.eaudit4j.common.Event;
import org.beiter.michael.eaudit4j.common.Field;
import org.beiter.michael.eaudit4j.common.ProcessingObjects;
import org.beiter.michael.eaudit4j.common.impl.EventBuilder;
import org.beiter.michael.eaudit4j.common.impl.EventField;
import org.beiter.michael.eaudit4j.common.propsbuilder.MapBasedCommonPropsBuilder;
import org.beiter.michael.eaudit4j.processors.jdbc.JdbcDsProcessor;
import org.beiter.michael.eaudit4j.processors.jdbc.propsbuilder.MapBasedJdbcPropsBuilder;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.hp.audit.Performance.DEFAULT_FILE;
import static com.hp.audit.Performance.JTL_FILE;
import static com.hp.audit.Performance.MAX_TEST_TIME;
import static com.hp.audit.Performance.MAX_TEST_TIME_DEFAULT_UNIT;
import static com.hp.audit.Performance.MAX_TEST_TIME_UNIT;
import static com.hp.audit.Performance.MAX_WARM_UP_TIME;
import static com.hp.audit.Performance.MAX_WARM_UP_TIME_UNIT;
import static com.hp.audit.Performance.NUM_LOOPS;
import static com.hp.audit.Performance.NUM_THREADS;
import static com.hp.audit.Performance.RAMP_UP_TIME;
import static com.hp.audit.Performance.USER_HOME;
import static com.hp.audit.Performance.WARM_UP_TIME_DEFAULT_UNIT;
import static com.hp.audit.PropertiesUtil.getIntProperty;

public class PerconaTest {

    public static final String DATA_SOURCE_NAME = "dataSource";

    // a default field name and value for the event ID that we will use in this demo
    public static final String EVENT_ID_FIELD_NAME = "eventId";

    private static int numThreads = getIntProperty(NUM_THREADS, 120);
    private static int numLoops = getIntProperty(NUM_LOOPS, Integer.MAX_VALUE);
    private static int rampUpInMilliseconds = getIntProperty(RAMP_UP_TIME, 1);
    private static String pathname = System.getProperty(JTL_FILE, System.getProperty(USER_HOME) + DEFAULT_FILE);
    private static int maxTestTime = getIntProperty(MAX_TEST_TIME, 2);
    private static String maxTestTimeUnit = System.getProperty(MAX_TEST_TIME_UNIT, MAX_TEST_TIME_DEFAULT_UNIT).toUpperCase();
    private static int maxWarmUpTime = getIntProperty(MAX_WARM_UP_TIME, 1);
    private static String maxWarmUpTimeUnit = System.getProperty(MAX_WARM_UP_TIME_UNIT, WARM_UP_TIME_DEFAULT_UNIT).toUpperCase();

    Map<String, String> props;
    CommonProperties properties;
    Audit audit;
    ProcessingObjects processingObjects;

    private boolean storeFields = Boolean.valueOf(System.getProperty("storeFields", "true"));

    public static void main(String... args) throws Exception {
        try {
            PerconaTest perconaTest = new PerconaTest();
            MySqlServerUtil.startConnectionPool();
            MySqlServerUtil.recreateTables();

            perconaTest.fillJdbcProp();
            perconaTest.fillAuditProp();

            perconaTest.audit = AuditFactory.getInstance(perconaTest.properties.getAuditClassName(),
                    perconaTest.properties);


            Performance warmUpTest = new Performance(numThreads, numLoops, rampUpInMilliseconds,
                    new File(pathname.replace(".jtl", "_warmUp.jtl")), maxWarmUpTime, TimeUnit.valueOf(maxWarmUpTimeUnit));
            warmUpTest.runTests("Percona warm up test", perconaTest.supplier(), perconaTest.consumer());


            Performance performance = new Performance(numThreads, numLoops, rampUpInMilliseconds,
                    new File(pathname), maxTestTime, TimeUnit.valueOf(maxTestTimeUnit));
            performance.runTests("Percona test", perconaTest.supplier(), perconaTest.consumer());
            System.out.println("Done!!!");
        } finally {
            MySqlServerUtil.closePool();
        }
    }

    private void fillAuditProp() {
        // the audit library common configuration
        properties = MapBasedCommonPropsBuilder.build(props);
        properties.setDefaultAuditStream("1234567890");
        properties.setEncoding("UTF-8");
        properties.setFailOnMissingProcessors(true);
        properties.setProcessors(JdbcDsProcessor.class.getCanonicalName());
    }

    private void fillJdbcProp() throws SQLException {
        props = new HashMap<>();

        // the JDBC Processor configuration
        props.put(MapBasedJdbcPropsBuilder.KEY_DATA_SOURCE_NAME, DATA_SOURCE_NAME);
        processingObjects = new ProcessingObjects();
        processingObjects.add(DATA_SOURCE_NAME, MySqlServerUtil.getDataSource());

        props.put(MapBasedJdbcPropsBuilder.KEY_EVENT_ID_FIELD_NAME, EVENT_ID_FIELD_NAME);
        props.put(MapBasedJdbcPropsBuilder.KEY_INSERT_EVENT_SQL_STMT,
                "INSERT INTO events (eventId, auditStreamName, eventJson) VALUES (?, ?, ?)");
        if (storeFields) {
            props.put(MapBasedJdbcPropsBuilder.KEY_INSERT_INDEXED_FIELD_SQL_STMT,
                    "INSERT INTO fields (eventId, auditStreamName, fieldName, fieldValue) VALUES (?, ?, ?, ?)");
            props.put(MapBasedJdbcPropsBuilder.KEY_INDEXED_FIELDS, "subject,actor:myActor,byteField,invalidField:neverExists");
        }
    }

    private Supplier<Event> supplier() {
        return () -> {
            // the event ID is required for this processor to work
            byte[] randomValue = getRandomValue();
            Field eventIdField = new EventField(EVENT_ID_FIELD_NAME, randomValue);
            Field field = new EventField("byteField", randomValue);

            // create the event, using (amongst others) the custom field above
            return new EventBuilder(properties)
                    .setField(eventIdField)
                    .setSubject("SubjectId-1234".toCharArray())
                    .setObject("ObjectId-3456".toCharArray())
                    .setActor("ActorId-5678".toCharArray())
                    .setResult("Some result".toCharArray())
                    .setField(field)
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

    private static byte[] getRandomValue() {
        try {
            return UUID.randomUUID().toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }


}
