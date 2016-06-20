/*
 * #%L
 * This file is part of eAudit4j, a library for creating pluggable
 * auditing solutions, providing an audit processor that logs audit
 * events using slf4j.
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
package org.beiter.michael.eaudit4j.processors.slf4j;

import org.beiter.michael.eaudit4j.common.AuditErrorConditions;
import org.beiter.michael.eaudit4j.common.AuditException;
import org.beiter.michael.eaudit4j.common.CommonProperties;
import org.beiter.michael.eaudit4j.common.Event;
import org.beiter.michael.eaudit4j.common.ProcessingObjects;
import org.beiter.michael.eaudit4j.common.Processor;
import org.beiter.michael.eaudit4j.common.Reversible;
import org.beiter.michael.eaudit4j.common.impl.AuditEvent;
import org.beiter.michael.eaudit4j.common.impl.EventBuilder;
import org.beiter.michael.eaudit4j.common.propsbuilder.MapBasedCommonPropsBuilder;
import org.beiter.michael.eaudit4j.processors.slf4j.propsbuilder.MapBasedSlf4jPropsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.spi.MDCAdapter;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;


public class Slf4jProcessorTest {

    private java.lang.reflect.Field field_commonProperties;
    private java.lang.reflect.Field field_properties;
    private java.lang.reflect.Method method_addAuditStreamNameToMdc;
    private java.lang.reflect.Method method_addSerializedEventToMdc;
    private java.lang.reflect.Method method_addEventFieldsToMdc;

    /**
     * Make some of the private fields and methods in the Slf4jProcessor class accessible.
     * <p>
     * This is executed before every test to ensure consistency even if one of the tests mock with field accessibility.
     */
    @Before
    public void makePrivateFieldsAccessible() {

        // make private fields accessible as needed
        try {
            field_commonProperties = Slf4jProcessor.class.getDeclaredField("commonProperties");
            field_properties = Slf4jProcessor.class.getDeclaredField("properties");
            method_addAuditStreamNameToMdc = Slf4jProcessor.class.getDeclaredMethod("addAuditStreamNameToMdc", String.class, Slf4jProperties.class);
            method_addSerializedEventToMdc = Slf4jProcessor.class.getDeclaredMethod("addSerializedEventToMdc", String.class, Slf4jProperties.class);
            method_addEventFieldsToMdc = Slf4jProcessor.class.getDeclaredMethod("addEventFieldsToMdc", Event.class, Slf4jProperties.class);
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            AssertionError ae = new AssertionError("An expected private field or method does not exist");
            ae.initCause(e);
            throw ae;
        }
        field_commonProperties.setAccessible(true);
        field_properties.setAccessible(true);
        method_addAuditStreamNameToMdc.setAccessible(true);
        method_addSerializedEventToMdc.setAccessible(true);
        method_addEventFieldsToMdc.setAccessible(true);
    }

    /**
     * Initialize the processor with 'null' properties
     */
    @Test(expected = NullPointerException.class)
    public void initWithNullPropertiesTest() {

        Processor processor = new Slf4jProcessor();
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

        Processor processor = new Slf4jProcessor();
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

        Processor processor = new Slf4jProcessor();
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
     * Test that the init() method correctly initializes the Slf4j Properties values
     */
    @Test
    public void initPropertiesCorrectTest() {

        String key = MapBasedSlf4jPropsBuilder.KEY_MARKER;
        String value = "some value";
        Map<String, String> propsMap = new HashMap<>();
        propsMap.put(key, value);

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();
        commonProps.setAdditionalProperties(propsMap);

        Processor processor = new Slf4jProcessor();
        processor.init(commonProps);

        String error = "The method does not retain object keys or values";
        try {

            Slf4jProperties propsInObject = (Slf4jProperties) field_properties.get(processor);

            assertThat(error, propsInObject.getMarker(), is(equalTo(value)));
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

        String key = MapBasedSlf4jPropsBuilder.KEY_MARKER;
        String value = "some value";
        Map<String, String> propsMap = new HashMap<>();
        propsMap.put(key, value);

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();
        commonProps.setAdditionalProperties(propsMap);

        Processor processor = new Slf4jProcessor();
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
        Processor processor = new Slf4jProcessor();
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
        Processor processor = new Slf4jProcessor();
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
        Processor processor = new Slf4jProcessor();
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
        Processor processor = new Slf4jProcessor();
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
        Processor processor = new Slf4jProcessor();
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
        Processor processor = new Slf4jProcessor();
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
        Processor processor = new Slf4jProcessor();
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
        Processor processor = new Slf4jProcessor();
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
     * Test that the MDC contains the audit stream field, and the correct audit stream and value
     */
    @Test
    public void testDefaultAuditStreamInMdc()
            throws InvocationTargetException, IllegalAccessException {

        Slf4jProperties slf4jProps = MapBasedSlf4jPropsBuilder.buildDefault();
        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();
        Processor processor = new Slf4jProcessor();
        processor.init(commonProps);

        String auditStreamName = "Test audit stream name";

        MDCAdapter adapter = (MDCAdapter) method_addAuditStreamNameToMdc.invoke(processor, auditStreamName, slf4jProps);

        String fieldName = slf4jProps.getAuditStreamFieldName();

        String error = "The MDC is missing the audit stream name field key";
        assertThat(error, adapter.get(fieldName), is(not(nullValue())));
        error = "The MDC accepts any field name";
        assertThat(error, adapter.get(fieldName + "_invalid"), is(nullValue()));
        error = "The MDC does not return the correct value";
        assertThat(error, adapter.get(fieldName), is(equalTo(auditStreamName)));
    }

    /**
     * Test that the MDC contains the serialized event field, and the correct serialized event and value
     */
    @Test
    public void testSerializedEventInMdc()
            throws InvocationTargetException, IllegalAccessException {

        Slf4jProperties slf4jProps = MapBasedSlf4jPropsBuilder.buildDefault();
        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();
        Processor processor = new Slf4jProcessor();
        processor.init(commonProps);

        // we could really use an arbitrary string instead of going through the pain of creating an event
        // and serializing it here. However, maybe someone will find this example useful at some point...
        Event event = new EventBuilder(commonProps)
                .setSubject("SubjectId-1234".toCharArray())
                .build();
        String eventJson = String.valueOf(event.toJson(commonProps.getEncoding()));

        MDCAdapter adapter = (MDCAdapter) method_addSerializedEventToMdc.invoke(processor, eventJson, slf4jProps);

        String fieldName = slf4jProps.getSerializedEventFieldName();

        String error = "The MDC is missing the serialized event field key";
        assertThat(error, adapter.get(fieldName), is(not(nullValue())));
        error = "The MDC accepts any field name";
        assertThat(error, adapter.get(fieldName + "_invalid"), is(nullValue()));
        error = "The MDC does not return the correct value";
        assertThat(error, adapter.get(fieldName), is(equalTo(eventJson)));
    }

    /**
     * Test that the MDC contains the correct fields from the event, as configured, with the correct names and content
     * <p>
     * - number of fields
     * - field name
     * - field value
     */
    @Test
    public void testEventFieldsInMdc()
            throws InvocationTargetException, IllegalAccessException {

        Map<String, String> props = new HashMap<>();
        props.put(MapBasedSlf4jPropsBuilder.KEY_MDC_FIELDS,"subject,actor:myActor,invalidField:neverExists");

        Slf4jProperties slf4jProps = MapBasedSlf4jPropsBuilder.build(props);
        CommonProperties commonProps = MapBasedCommonPropsBuilder.build(props);
        Processor processor = new Slf4jProcessor();
        processor.init(commonProps);

        Event event = new EventBuilder(commonProps)
                .setSubject("SubjectId-1234".toCharArray())
                .setObject("ObjectId-3456".toCharArray())
                .setActor("ActorId-5678".toCharArray())
                .setResult("Some result".toCharArray())
                .build();

        MDCAdapter adapter = (MDCAdapter) method_addEventFieldsToMdc.invoke(processor, event, slf4jProps);

        String error = "The MDC does not have the correct number of fields";
        assertThat(error, adapter.getCopyOfContextMap().size(), is(equalTo(2)));

        // Test the subject - we use the default name ("subject") - see configuration above for the MdcFields
        error = "The MDC is missing the correct subject key";
        assertThat(error, adapter.get("subject"), is(not(nullValue())));
        error = "The MDC does not return the correct value for the subject";
        assertThat(error, adapter.get("subject"), is(equalTo("SubjectId-1234")));

        // Test the subject - we use the default name ("myActor") - see configuration above for the MdcFields
        error = "The MDC is missing the correct actor key";
        assertThat(error, adapter.get("myActor"), is(not(nullValue())));
        error = "The MDC does not return the correct value for the actor";
        assertThat(error, adapter.get("myActor"), is(equalTo("ActorId-5678")));
    }


    ///////////////////////////////////////////////////////////////////////////
    // Revert & clean up tests
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Test that the revert() method can handle null events
     */
    @Test(expected = NullPointerException.class)
    public void revertFromNulEventTest()
            throws AuditException {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();
        Processor processor = new Slf4jProcessor();
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

        Processor processor = new Slf4jProcessor();
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

        Processor processor = new Slf4jProcessor();
        processor.cleanUp();
    }
}
