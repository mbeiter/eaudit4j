/*
 * #%L
 * This file is part of eAudit4j, a library for creating pluggable
 * auditing solutions, providing an audit processor that retrieves
 * the system time and appends a timestamp as a field to audit events.
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
package org.beiter.michael.eaudit4j.processors.timestamp;

import org.beiter.michael.eaudit4j.common.*;
import org.beiter.michael.eaudit4j.common.impl.AuditEvent;
import org.beiter.michael.eaudit4j.common.propsbuilder.MapBasedCommonPropsBuilder;
import org.beiter.michael.eaudit4j.processors.timestamp.propsbuilder.MapBasedTimestampPropsBuilder;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class TimestampProcessorTest {

    private java.lang.reflect.Field field_commonProperties;
    private java.lang.reflect.Field field_properties;
    private java.lang.reflect.Method method_getTimestamp;

    private final String encoding = "UTF-8";
    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static final String TIMESTAMP_REGEX = "[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3}-[0-9]{4}";

    /**
     * Make some of the private fields in the TimestampProcessor class accessible.
     * <p>
     * This is executed before every test to ensure consistency even if one of the tests mock with field accessibility.
     */
    @Before
    public void makePrivateFieldsAccessible() {

        // make private fields accessible as needed
        try {
            field_commonProperties = TimestampProcessor.class.getDeclaredField("commonProperties");
            field_properties = TimestampProcessor.class.getDeclaredField("properties");
            method_getTimestamp = TimestampProcessor.class.getDeclaredMethod("getTimestamp", Date.class);
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            AssertionError ae = new AssertionError("An expected private field or method does not exist");
            ae.initCause(e);
            throw ae;
        }
        field_commonProperties.setAccessible(true);
        field_properties.setAccessible(true);
        method_getTimestamp.setAccessible(true);
    }

    /**
     * Initialize the processor with 'null' properties
     */
    @Test(expected = NullPointerException.class)
    public void initWithNullPropertiesTest() {

        Processor processor = new TimestampProcessor();
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

        Processor processor = new TimestampProcessor();
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

        Processor processor = new TimestampProcessor();
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
     * Test that the init() method correctly initializes the Timestamp Properties values
     */
    @Test
    public void initPropertiesCorrectTest() {

        String key = MapBasedTimestampPropsBuilder.KEY_TIMEZONE;
        String value = "some value";
        Map<String, String> propsMap = new HashMap<>();
        propsMap.put(key, value);

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();
        commonProps.setAdditionalProperties(propsMap);

        Processor processor = new TimestampProcessor();
        processor.init(commonProps);

        String error = "The method does not retain object keys or values";
        try {

            TimestampProperties propsInObject = (TimestampProperties) field_properties.get(processor);

            assertThat(error, propsInObject.getTimezone(), is(equalTo(value)));
        } catch (IllegalAccessException e) {
            AssertionError ae = new AssertionError("Cannot access private field");
            ae.initCause(e);
            throw ae;
        }
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
        Processor processor = new TimestampProcessor();
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
        Processor processor = new TimestampProcessor();
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
        Processor processor = new TimestampProcessor();
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
        Processor processor = new TimestampProcessor();
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
        Processor processor = new TimestampProcessor();
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
        Processor processor = new TimestampProcessor();
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
        Processor processor = new TimestampProcessor();
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
        Processor processor = new TimestampProcessor();
        processor.init(commonProps);

        Event event = new AuditEvent();
        String auditStream = "some audit stream";
        ProcessingObjects processingObjects = null;

        processor.process(event, auditStream, processingObjects);
    }

    /**
     * Test that the processor adds a timestamp to an event.
     */
    @Test
    public void addTimestampTest()
            throws AuditException {

        Map<String, String> props = new HashMap<>();
        props.put(MapBasedTimestampPropsBuilder.KEY_FORMAT, TIMESTAMP_FORMAT);

        CommonProperties commonProps = MapBasedCommonPropsBuilder.build(props);
        Processor processor = new TimestampProcessor();
        processor.init(commonProps);

        Event event = new AuditEvent();

        processor.process(event);

        String error = "The method does not add a timestamp";
        assertThat(error, event.containsField(MapBasedTimestampPropsBuilder.DEFAULT_EVENT_FIELD_NAME), is(equalTo(true)));
        error = "The timestamp field has the wrong format";
        assertThat(
                error,
                String.valueOf(event.getField(MapBasedTimestampPropsBuilder.DEFAULT_EVENT_FIELD_NAME).getCharValue(encoding)).matches(TIMESTAMP_REGEX),
                is(true));
    }

    /**
     * Test that the processor refuses to overwrite a timestamp if such a field would already been present in an
     * event (i.e. this processor makes sure it is only executed once per audit chain)
     */
    @Test(expected = AuditException.class)
    public void doubleTimestampTest()
            throws AuditException {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();
        Processor processor = new TimestampProcessor();
        processor.init(commonProps);

        Event event = new AuditEvent();

        // create a first timestamp, and validate that it is there
        processor.process(event);

        String error = "The method does not add a timestamp";
        assertThat(error, event.containsField(MapBasedTimestampPropsBuilder.DEFAULT_EVENT_FIELD_NAME), is(equalTo(true)));

        // create a second timestamp, this should throw an exception
        processor.process(event);
    }

    /**
     * Test that the method computing the timestamp creates the timestamp properly.
     * <p>
     * This test uses a static input timestamp (ie. a static return of Date() at a specific point in time), and then
     * ensures that the String representation of that timestamp is returned in the correct format and timezone.
     */
    @Test
    public void getTimestampTest()
            throws InvocationTargetException, IllegalAccessException {

        Date date = new Date(303866400000l);

        getTimestamp(date, "UTC", "1979-08-18T23:20:00.000+0000");
        getTimestamp(date, "America/Denver", "1979-08-18T17:20:00.000-0600");
        getTimestamp(date, "Europe/Berlin", "1979-08-19T00:20:00.000+0100");
    }

    // The actual test implementation of getTimestampTest()
    private void getTimestamp(Date date, String timezone, String expected)
            throws IllegalAccessException, InvocationTargetException {

        Map<String, String> props = new HashMap<>();
        props.put(MapBasedTimestampPropsBuilder.KEY_TIMEZONE, timezone);
        props.put(MapBasedTimestampPropsBuilder.KEY_FORMAT, TIMESTAMP_FORMAT);

        CommonProperties commonProps = MapBasedCommonPropsBuilder.build(props);
        Processor processor = new TimestampProcessor();
        processor.init(commonProps);

        String actualTimestamp = (String) method_getTimestamp.invoke(processor, date);

        String error = "The timestamp is null or empty";
        assertThat(error, actualTimestamp, is(not(nullValue())));
        assertThat(error, actualTimestamp.length(), is(greaterThan(0)));

        error = "The timestamp does not match the expected value";
        assertThat(error, actualTimestamp, is(equalTo(expected)));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Revert & clean up tests
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Test that the revert() method can handle null events
     */
    @Test(expected = NullPointerException.class)
    public void removeMachineIdFromNulEventTest()
            throws AuditException {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();
        Processor processor = new TimestampProcessor();
        processor.init(commonProps);

        Event event = null;

        // revert the operation (remove the timestamp) form a null Event
        ((Reversible) processor).revert(event);
    }

    /**
     * Test that the revert() method removes the timestamp field from the event
     */
    @Test
    public void removeMachineIdTest()
            throws AuditException {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();
        Processor processor = new TimestampProcessor();
        processor.init(commonProps);

        Event event = new AuditEvent();

        // create a timestamp, and validate that it is there
        processor.process(event);
        String error = "The method does not add a timestamp";
        assertThat(error, event.containsField(MapBasedTimestampPropsBuilder.DEFAULT_EVENT_FIELD_NAME), is(equalTo(true)));

        // revert the operation (remove the timestamp), and validate that it is gone
        Event modifiedEvent = ((Reversible) processor).revert(event);
        error = "The method does not remove the timestamp";
        assertThat(error, modifiedEvent.containsField(MapBasedTimestampPropsBuilder.DEFAULT_EVENT_FIELD_NAME), is(equalTo(false)));
    }

    /**
     * Clean up test
     * <p>
     * This test does not really do anything, but the cleanUp() method does not does
     * not to anything either, so I guess that is okay to get test coverage up :)
     */
    @Test
    public void cleanUpTest() {

        Processor processor = new TimestampProcessor();
        processor.cleanUp();
    }
}
