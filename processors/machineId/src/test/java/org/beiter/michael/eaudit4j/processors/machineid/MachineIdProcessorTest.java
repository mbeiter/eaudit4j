/*
 * #%L
 * This file is part of eAudit4j, a library for creating pluggable
 * auditing solutions, providing an audit processor that creates
 * a unique machine ID for the machine executing the library and
 * appends it as a field to audit events.
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
package org.beiter.michael.eaudit4j.processors.machineid;

import org.beiter.michael.eaudit4j.common.*;
import org.beiter.michael.eaudit4j.common.impl.AuditEvent;
import org.beiter.michael.eaudit4j.common.propsbuilder.MapBasedCommonPropsBuilder;
import org.beiter.michael.eaudit4j.processors.machineid.propsbuilder.MapBasedMachineIdPropsBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class MachineIdProcessorTest {

    private java.lang.reflect.Field field_commonProperties;
    private java.lang.reflect.Field field_properties;

    private final String encoding = "UTF-8";
    private static final String UUID_REGEX = "[0-9a-fA-F]{8}(?:-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}";

    /**
     * Make some of the private fields in the MachineIdProcessor class accessible.
     * <p>
     * This is executed before every test to ensure consistency even if one of the tests mock with field accessibility.
     */
    @Before
    public void makePrivateFieldsAccessible() {

        // make private fields accessible as needed
        try {
            field_commonProperties = MachineIdProcessor.class.getDeclaredField("commonProperties");
            field_properties = MachineIdProcessor.class.getDeclaredField("properties");
        } catch (NoSuchFieldException e) {
            AssertionError ae = new AssertionError("An expected private field does not exist");
            ae.initCause(e);
            throw ae;
        }
        field_commonProperties.setAccessible(true);
        field_properties.setAccessible(true);
    }

    /**
     * Initialize the processor with 'null' properties
     */
    @Test(expected = NullPointerException.class)
    public void initWithNullPropertiesTest() {

        Processor processor = new MachineIdProcessor();
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

        Processor processor = new MachineIdProcessor();
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

        Processor processor = new MachineIdProcessor();
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
     * Test that the init() method correctly initializes the MachineId Properties values
     */
    @Test
    public void initPropertiesCorrectTest() {

        String key = MapBasedMachineIdPropsBuilder.KEY_MACHINE_ID;
        String value = "some value";
        Map<String, String> propsMap = new HashMap<>();
        propsMap.put(key, value);

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();
        commonProps.setAdditionalProperties(propsMap);

        Processor processor = new MachineIdProcessor();
        processor.init(commonProps);

        String error = "The method does not retain object keys or values";
        try {

            MachineIdProperties propsInObject = (MachineIdProperties) field_properties.get(processor);

            assertThat(error, propsInObject.getMachineId(), is(equalTo(value)));
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

        String key = MapBasedMachineIdPropsBuilder.KEY_MACHINE_ID;
        String value = "some value";
        Map<String, String> propsMap = new HashMap<>();
        propsMap.put(key, value);

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();
        commonProps.setAdditionalProperties(propsMap);

        Processor processor = new MachineIdProcessor();
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
        Processor processor = new MachineIdProcessor();
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
        Processor processor = new MachineIdProcessor();
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
        Processor processor = new MachineIdProcessor();
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
        Processor processor = new MachineIdProcessor();
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
        Processor processor = new MachineIdProcessor();
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
        Processor processor = new MachineIdProcessor();
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
        Processor processor = new MachineIdProcessor();
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
        Processor processor = new MachineIdProcessor();
        processor.init(commonProps);

        Event event = new AuditEvent();
        String auditStream = "some audit stream";
        ProcessingObjects processingObjects = null;

        processor.process(event, auditStream, processingObjects);
    }

    /**
     * Test that the processor refuses to overwrite a machine ID if such a field would already been present in an
     * event (i.e. this processor makes sure it is only executed once per audit chain)
     */
    @Test(expected = AuditException.class)
    public void doubleMachineIdTest()
            throws AuditException {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();
        Processor processor = new MachineIdProcessor();
        processor.init(commonProps);

        Event event = new AuditEvent();

        // create a first machine ID, and validate that it is there
        processor.process(event);

        String error = "The method does not add a machine ID";
        assertThat(error, event.containsField(MapBasedMachineIdPropsBuilder.DEFAULT_EVENT_FIELD_NAME), is(equalTo(true)));

        // create a second machine ID, this should throw an exception
        processor.process(event);
    }

    /**
     * Test that the processor uses the machine ID provided in the configuration, if that config parameter is neither
     * null nor empty
     */
    @Test
    public void useConfiguredMachineIdTest()
            throws AuditException {

        String key = MapBasedMachineIdPropsBuilder.KEY_MACHINE_ID;
        String value = "some machine ID";
        Map<String, String> props = new HashMap<>();
        props.put(key, value);

        CommonProperties commonProps = MapBasedCommonPropsBuilder.build(props);
        Processor processor = new MachineIdProcessor();
        processor.init(commonProps);

        Event event = new AuditEvent();

        processor.process(event);

        String error = "The field has not been set";
        assertThat(error, event.containsField(MapBasedMachineIdPropsBuilder.DEFAULT_EVENT_FIELD_NAME), is(equalTo(true)));
        error = "The field has the wrong value";
        assertThat(
                error,
                event.getField(MapBasedMachineIdPropsBuilder.DEFAULT_EVENT_FIELD_NAME).getCharValue(encoding),
                is(equalTo(value.toCharArray())));
    }


    /**
     * Test that the processor populates the machine ID field from an env variable, if so configured
     * <p>
     * This test uses the "PATH" environment variable, because it is usually set in Windows, Linux, Macs, etc.
     * <p>
     * This test may fail if the PATH variable is not set. If so, either set it, or disable the test temporarily.
     */
    //@Ignore
    @Test
    public void getMachineIdFromEnvTest()
            throws AuditException {

        Map<String, String> props = new HashMap<>();
        props.put(MapBasedMachineIdPropsBuilder.KEY_MACHINE_ID_FROM_ENV, "true");
        props.put(MapBasedMachineIdPropsBuilder.KEY_MACHINE_ID_ENV_NAME, "PATH");

        CommonProperties commonProps = MapBasedCommonPropsBuilder.build(props);
        Processor processor = new MachineIdProcessor();
        processor.init(commonProps);

        Event event = new AuditEvent();

        processor.process(event);

        String error = "The field has not been set";
        assertThat(error, event.containsField(MapBasedMachineIdPropsBuilder.DEFAULT_EVENT_FIELD_NAME), is(equalTo(true)));
        error = "The machineid field seems to be a UUID, which is the fallback. This means retrieval from the environment most likely failed";
        assertThat(
                error,
                String.valueOf(event.getField(MapBasedMachineIdPropsBuilder.DEFAULT_EVENT_FIELD_NAME).getCharValue(encoding)).matches(UUID_REGEX),
                is(false));
    }

    /**
     * Test that the processor populates the machine ID field from the hostname, if so configured
     */
    @Test
    public void getMachineIdFromHostnameTest()
            throws AuditException {

        Map<String, String> props = new HashMap<>();
        props.put(MapBasedMachineIdPropsBuilder.KEY_MACHINE_ID_FROM_HOSTNAME, "true");

        CommonProperties commonProps = MapBasedCommonPropsBuilder.build(props);
        Processor processor = new MachineIdProcessor();
        processor.init(commonProps);

        Event event = new AuditEvent();

        processor.process(event);

        String error = "The field has not been set";
        assertThat(error, event.containsField(MapBasedMachineIdPropsBuilder.DEFAULT_EVENT_FIELD_NAME), is(equalTo(true)));
        error = "The machineid field does not contain a ':', which means it is not the correct format for a hostname name machine ID";
        assertThat(
                error,
                String.valueOf(event.getField(MapBasedMachineIdPropsBuilder.DEFAULT_EVENT_FIELD_NAME).getCharValue(encoding)).contains(":"),
                is(true));
    }

    /**
     * Test that the processor populates the machine ID field from a random source, if so configured
     */
    @Test
    public void getRandomMachineIdTest()
            throws AuditException {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();
        Processor processor = new MachineIdProcessor();
        processor.init(commonProps);

        Event event = new AuditEvent();

        processor.process(event);

        String error = "The machineid is not a UUID";
        assertThat(
                error,
                String.valueOf(event.getField(MapBasedMachineIdPropsBuilder.DEFAULT_EVENT_FIELD_NAME).getCharValue(encoding)).matches(UUID_REGEX),
                is(true));
    }

    /**
     * Test that two processor instances return different machine IDs (using a random source for the machine ID,
     * assuming that this also works when using timestamp based hostname extensions)
     */
    @Test
    public void twoProcessorsReturnsDifferentMachineIdsTest()
            throws AuditException {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();
        Processor processor1 = new MachineIdProcessor();
        Processor processor2 = new MachineIdProcessor();
        processor1.init(commonProps);
        processor2.init(commonProps);

        Event event1 = new AuditEvent();
        Event event2 = new AuditEvent();

        processor1.process(event1);
        processor2.process(event2);

        String error = "The machine IDs generated by two different processor instances are identical";
        String id1 = String.valueOf(event1.getField(MapBasedMachineIdPropsBuilder.DEFAULT_EVENT_FIELD_NAME).getCharValue(encoding));
        String id2 = String.valueOf(event2.getField(MapBasedMachineIdPropsBuilder.DEFAULT_EVENT_FIELD_NAME).getCharValue(encoding));

        assertThat(error, id1, is(not(equalTo(id2))));
    }

    /**
     * Test that the the same processor instance always returns the same value for the machine ID (using a random
     * source for the machine ID, assuming that this also works when using timestamp based hostname extensions)
     */
    @Test
    public void singletonProcessorReturnsSameMachineIdsTest()
            throws AuditException {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();
        Processor processor = new MachineIdProcessor();
        processor.init(commonProps);

        Event event1 = new AuditEvent();
        Event event2 = new AuditEvent();

        processor.process(event1);
        processor.process(event2);

        String error = "The machine IDs generated by a singleton processor instances are different";
        String id1 = String.valueOf(event1.getField(MapBasedMachineIdPropsBuilder.DEFAULT_EVENT_FIELD_NAME).getCharValue(encoding));
        String id2 = String.valueOf(event2.getField(MapBasedMachineIdPropsBuilder.DEFAULT_EVENT_FIELD_NAME).getCharValue(encoding));

        assertThat(error, id1, is(equalTo(id2)));
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
        Processor processor = new MachineIdProcessor();
        processor.init(commonProps);

        Event event = null;

        // revert the operation (remove the machine ID) form a null Event
        ((Reversible) processor).revert(event);
    }

    /**
     * Test that the revert() method removes the machine ID field from the event
     */
    @Test
    public void removeMachineIdTest()
            throws AuditException {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();
        Processor processor = new MachineIdProcessor();
        processor.init(commonProps);

        Event event = new AuditEvent();

        // create a machine ID, and validate that it is there
        processor.process(event);
        String error = "The method does not add a machine ID";
        assertThat(error, event.containsField(MapBasedMachineIdPropsBuilder.DEFAULT_EVENT_FIELD_NAME), is(equalTo(true)));

        // revert the operation (remove the machine ID), and valdiate that it is gone
        Event modifiedEvent = ((Reversible) processor).revert(event);
        error = "The method does not remove the machine ID";
        assertThat(error, modifiedEvent.containsField(MapBasedMachineIdPropsBuilder.DEFAULT_EVENT_FIELD_NAME), is(equalTo(false)));
    }

    /**
     * Clean up test
     *
     * This test does not really do anything, but the cleanUp() method does not does
     * not to anything either, so I guess that is okay to get test coverage up :)
     */
    @Test
    public void cleanUpTest () {

        Processor processor = new MachineIdProcessor();
        processor.cleanUp();
    }
}
