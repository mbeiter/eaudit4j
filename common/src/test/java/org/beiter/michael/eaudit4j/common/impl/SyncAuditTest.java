/*
 * #%L
 * This file is part of eAudit4j, a library for creating pluggable auditing solutions.
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
package org.beiter.michael.eaudit4j.common.impl;

import org.beiter.michael.eaudit4j.common.*;
import org.beiter.michael.eaudit4j.common.propsbuilder.MapBasedCommonPropsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class SyncAuditTest {

    /**
     * The logger object for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(SyncAuditTest.class);

    /**
     * The test class to instantiate
     */
    private static final String CLASS_NAME = ProcessorDummy.class.getCanonicalName();


    private java.lang.reflect.Field field_defaultAuditStream;
    private java.lang.reflect.Field field_properties;
    private java.lang.reflect.Field field_processors;

    /**
     * Make some of the private fields in the EventBuilder class accessible.
     * <p>
     * This is executed before every test to ensure consistency even if one of the tests mock with field accessibility.
     */
    @Before
    public void makeAdditionalPropertiesPrivateFieldsAccessible() {

        // make private fields accessible as needed
        try {
            field_defaultAuditStream = CommonProperties.class.getDeclaredField("defaultAuditStream");
            field_properties = SyncAudit.class.getDeclaredField("commonProps");
            field_processors = SyncAudit.class.getDeclaredField("processors");
        } catch (NoSuchFieldException e) {
            AssertionError ae = new AssertionError("An expected private field does not exist");
            ae.initCause(e);
            throw ae;
        }
        field_defaultAuditStream.setAccessible(true);
        field_properties.setAccessible(true);
        field_processors.setAccessible(true);
    }


    ///////////////////////////////////////////////////////////////////////////
    // Initialization Tests
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Create a SyncAuditor, init with an empty processors list, and make sure that:
     * <ul>
     * <li>the constructor makes a defensive copy of the properties</li>
     * </ul>
     * <p>
     *
     * @throws IllegalAccessException when reflection does not work
     */
    @Test
    public void createSyncAuditWithEmptyProcessorListTest()
            throws IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        SyncAudit syncAudit = new SyncAudit();
        try {
            syncAudit.init(properties);
        } catch (AuditException e) {
            AssertionError ae = new AssertionError("Unexpected exception");
            ae.initCause(e);
            throw ae;
        }

        // Use reflection to get access to the internal fields
        CommonProperties propertiesInObject = (CommonProperties) field_properties.get(syncAudit);

        String error = "The method does not create an inbound defensive copy";
        assertThat(error, propertiesInObject, is(not(sameInstance(properties))));
    }

    /**
     * Create a SyncAuditor and init with null properties
     */
    @Test(expected = NullPointerException.class)
    public void createSyncAuditWithNullPropertiesTest() {

        CommonProperties properties = null;
        SyncAudit syncAudit = new SyncAudit();
        try {
            syncAudit.init(null);
        } catch (AuditException e) {
            AssertionError ae = new AssertionError("Unexpected exception");
            ae.initCause(e);
            throw ae;
        }
    }

    /**
     * A non-existing processor class name (i.e. a class not in the class path) should throw an exception
     *
     * @throws AuditException When things go well
     */
    @Test(expected = AuditException.class)
    public void getNonExistingImplementationTest()
            throws AuditException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        properties.setProcessors("someGarbageName");
        SyncAudit syncAudit = new SyncAudit();

        try {
            syncAudit.init(properties);
        } catch (AuditException e) {
            AuditErrorConditions expected = AuditErrorConditions.INITIALIZATION;
            String error = "The type of exception thrown is not correct. Expected " + expected + ", got " + e.getErrorCondition();
            assertThat(error, e.getErrorCondition(), is(equalTo(expected)));
            throw e;
        }

        throw new AssertionError("Expected an exception, but that exception was not thrown");
    }

    /**
     * An invalid processor class name (i.e. a class of the wrong type) should throw an exception
     *
     * @throws AuditException When things go well
     */
    @Test(expected = AuditException.class)
    public void getInvalidImplementationTest()
            throws AuditException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        properties.setProcessors(String.class.getCanonicalName());
        SyncAudit syncAudit = new SyncAudit();

        try {
            syncAudit.init(properties);
        } catch (AuditException e) {
            AuditErrorConditions expected = AuditErrorConditions.INITIALIZATION;
            String error = "The type of exception thrown is not correct. Expected " + expected + ", got " + e.getErrorCondition();
            assertThat(error, e.getErrorCondition(), is(equalTo(expected)));
            throw e;
        }

        throw new AssertionError("Expected an exception, but that exception was not thrown");
    }

    /**
     * Configure a specific implementation of the Processor interface in the properties, and assert that the returned
     * implementation equals the requested implementation.
     *
     * @throws IllegalAccessException when reflection does not work
     */
    @Test
    public void getSpecificImplementationTest()
            throws IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        properties.setProcessors(CLASS_NAME);
        SyncAudit syncAudit = new SyncAudit();

        try {
            syncAudit.init(properties);
        } catch (AuditException e) {
            AssertionError ae = new AssertionError("Instantiation error");
            ae.initCause(e);
            throw ae;
        }

        // Use reflection to get access to the internal fields
        List<Processor> processorsInObject = (List<Processor>) field_processors.get(syncAudit);

        String error = "The number of processors in the auditor is not correct";
        assertThat(error, processorsInObject.size(), is(equalTo(1)));

        error = "The class instantiated by the factory does not match the expected class";
        assertThat(error, ProcessorDummy.class.getCanonicalName(), is(equalTo(CLASS_NAME)));
        assertThat(error, processorsInObject.get(0).getClass().getCanonicalName(), is(equalTo(CLASS_NAME)));
    }

    /**
     * Configure two instances of a specific implementation of the Processor interface, and asserts that the returned
     * objects are two separate instances.
     *
     * @throws IllegalAccessException when reflection does not work
     */
    @Test
    public void twoInstancesAreDifferentTest()
            throws IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        properties.setProcessors(CLASS_NAME + "," + CLASS_NAME);
        SyncAudit syncAudit = new SyncAudit();

        try {
            syncAudit.init(properties);
        } catch (AuditException e) {
            AssertionError ae = new AssertionError("Instantiation error");
            ae.initCause(e);
            throw ae;
        }

        // Use reflection to get access to the internal fields
        List<Processor> processorsInObject = (List<Processor>) field_processors.get(syncAudit);

        String error = "The number of processors in the auditor is not correct";
        assertThat(error, processorsInObject.size(), is(equalTo(2)));

        error = "The class instantiated by the factory does not match the expected class";
        assertThat(error, ProcessorDummy.class.getCanonicalName(), is(equalTo(CLASS_NAME)));
        assertThat(error, processorsInObject.get(0).getClass().getCanonicalName(), is(equalTo(CLASS_NAME)));
        assertThat(error, processorsInObject.get(1).getClass().getCanonicalName(), is(equalTo(CLASS_NAME)));

        error = "The instantiated classes are singletons instead of a distinct objects";
        assertThat(error, processorsInObject.get(0), is(not(sameInstance(processorsInObject.get(1)))));
    }

    /**
     * Create two instances with identical configuration, and and assert that the processors used under the hood are
     * not identical.
     *
     * @throws IllegalAccessException when reflection does not work
     */
    @Test
    public void twoAuditorsDoNotShareSingletonProcessorsTest()
            throws IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        properties.setProcessors(CLASS_NAME);
        SyncAudit syncAudit1 = new SyncAudit();
        SyncAudit syncAudit2 = new SyncAudit();

        try {
            syncAudit1.init(properties);
            syncAudit2.init(properties);
        } catch (AuditException e) {
            AssertionError ae = new AssertionError("Instantiation error");
            ae.initCause(e);
            throw ae;
        }

        // Use reflection to get access to the internal fields
        List<Processor> processorsInObject1 = (List<Processor>) field_processors.get(syncAudit1);
        List<Processor> processorsInObject2 = (List<Processor>) field_processors.get(syncAudit2);

        String error = "The instantiated classes are singletons instead of a distinct objects";
        assertThat(error, processorsInObject1.get(0), is(not(sameInstance(processorsInObject2.get(0)))));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Auditor Tests - default audit stream
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Audit a null event for the default audit stream
     *
     * @throws AuditException when everything goes well
     */
    @Test(expected = AuditException.class)
    public void auditNullEventInDefaultAuditStreamTest()
            throws AuditException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        properties.setDefaultAuditStream("A default audit stream for testing");
        SyncAudit syncAudit = new SyncAudit();

        try {
            syncAudit.init(properties);
        } catch (AuditException e) {
            AssertionError ae = new AssertionError("Instantiation error");
            ae.initCause(e);
            throw ae;
        }

        try {
            syncAudit.audit(null);
        } catch (AuditException e) {
            AuditErrorConditions expected = AuditErrorConditions.INVALID_EVENT;
            String error = "The type of exception thrown is not correct. Expected " + expected + ", got " + e.getErrorCondition();
            assertThat(error, e.getErrorCondition(), is(equalTo(expected)));
            throw e;
        }

        throw new AssertionError("Expected an exception, but that exception was not thrown");
    }


    /**
     * Audit an event with the default audit stream being null
     *
     * @throws AuditException
     * @throws IllegalAccessException when reflection does not work
     */
    @Test(expected = AuditException.class)
    public void auditEventInNullDefaultAuditStreamTest()
            throws AuditException, IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        SyncAudit syncAudit = new SyncAudit();

        // Use reflection to get access to the internal fields
        String defaultAuditStreamInObject = (String) field_defaultAuditStream.get(properties);
        defaultAuditStreamInObject = null;

        try {
            syncAudit.init(properties);
        } catch (AuditException e) {
            AssertionError ae = new AssertionError("Instantiation error");
            ae.initCause(e);
            throw ae;
        }

        Event event = new AuditEvent();

        try {
            syncAudit.audit(event);
        } catch (AuditException e) {
            AuditErrorConditions expected = AuditErrorConditions.CONFIGURATION;
            String error = "The type of exception thrown is not correct. Expected " + expected + ", got " + e.getErrorCondition();
            assertThat(error, e.getErrorCondition(), is(equalTo(expected)));
            throw e;
        }

        throw new AssertionError("Expected an exception, but that exception was not thrown");
    }

    /**
     * Audit an event with the default audit stream being blank
     *
     * @throws AuditException
     */
    @Test(expected = AuditException.class)
    public void auditEventInBlankDefaultAuditStreamTest()
            throws AuditException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        properties.setDefaultAuditStream("");
        SyncAudit syncAudit = new SyncAudit();

        try {
            syncAudit.init(properties);
        } catch (AuditException e) {
            AssertionError ae = new AssertionError("Instantiation error");
            ae.initCause(e);
            throw ae;
        }

        Event event = new AuditEvent();

        try {
            syncAudit.audit(event);
        } catch (AuditException e) {
            AuditErrorConditions expected = AuditErrorConditions.CONFIGURATION;
            String error = "The type of exception thrown is not correct. Expected " + expected + ", got " + e.getErrorCondition();
            assertThat(error, e.getErrorCondition(), is(equalTo(expected)));
            throw e;
        }

        throw new AssertionError("Expected an exception, but that exception was not thrown");
    }

    ///////////////////////////////////////////////////////////////////////////
    // Auditor Tests - provided audit stream, default processorObjects
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Audit a null event for a non-default audit stream
     *
     * @throws AuditException when everything goes well
     */
    @Test(expected = AuditException.class)
    public void auditNullEventInNonDefaultAuditStreamTest()
            throws AuditException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        SyncAudit syncAudit = new SyncAudit();

        try {
            syncAudit.init(properties);
        } catch (AuditException e) {
            AssertionError ae = new AssertionError("Instantiation error");
            ae.initCause(e);
            throw ae;
        }

        String auditStream = "A custom audit stream for testing";

        try {
            syncAudit.audit(null, auditStream);
        } catch (AuditException e) {
            AuditErrorConditions expected = AuditErrorConditions.INVALID_EVENT;
            String error = "The type of exception thrown is not correct. Expected " + expected + ", got " + e.getErrorCondition();
            assertThat(error, e.getErrorCondition(), is(equalTo(expected)));
            throw e;
        }

        throw new AssertionError("Expected an exception, but that exception was not thrown");
    }

    /**
     * Audit an event with a non-default audit stream being null
     *
     * @throws NullPointerException when everything goes well
     */
    @Test(expected = NullPointerException.class)
    public void auditEventInNullNonDefaultAuditStreamTest()
            throws AuditException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        SyncAudit syncAudit = new SyncAudit();

        try {
            syncAudit.init(properties);
        } catch (AuditException e) {
            AssertionError ae = new AssertionError("Instantiation error");
            ae.initCause(e);
            throw ae;
        }

        Event event = new AuditEvent();

        syncAudit.audit(event, null);
    }

    /**
     * Audit an event with a non-default audit stream being blank
     *
     * @throws IllegalArgumentException when everything goes well
     */
    @Test(expected = IllegalArgumentException.class)
    public void auditEventInBlankNonDefaultAuditStreamTest()
            throws AuditException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        SyncAudit syncAudit = new SyncAudit();

        try {
            syncAudit.init(properties);
        } catch (AuditException e) {
            AssertionError ae = new AssertionError("Instantiation error");
            ae.initCause(e);
            throw ae;
        }

        Event event = new AuditEvent();
        String auditStream = "";

        syncAudit.audit(event, auditStream);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Auditor Tests - provided audit stream, provided processorObjects
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Audit a null event for a non-default audit stream
     *
     * @throws AuditException when everything goes well
     */
    @Test(expected = AuditException.class)
    public void auditNullEventInNonDefaultAuditStreamProvidedProcessingObjectsTest()
            throws AuditException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        SyncAudit syncAudit = new SyncAudit();

        try {
            syncAudit.init(properties);
        } catch (AuditException e) {
            AssertionError ae = new AssertionError("Instantiation error");
            ae.initCause(e);
            throw ae;
        }

        String auditStream = "A custom audit stream for testing";
        ProcessingObjects processingObjects = new ProcessingObjects();

        try {
            syncAudit.audit(null, auditStream, processingObjects );
        } catch (AuditException e) {
            AuditErrorConditions expected = AuditErrorConditions.INVALID_EVENT;
            String error = "The type of exception thrown is not correct. Expected " + expected + ", got " + e.getErrorCondition();
            assertThat(error, e.getErrorCondition(), is(equalTo(expected)));
            throw e;
        }

        throw new AssertionError("Expected an exception, but that exception was not thrown");
    }

    /**
     * Audit an event with a non-default audit stream being null
     *
     * @throws NullPointerException when everything goes well
     */
    @Test(expected = NullPointerException.class)
    public void auditEventInNullNonDefaultAuditStreamProvidedProcessingObjectsTest()
            throws AuditException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        SyncAudit syncAudit = new SyncAudit();

        try {
            syncAudit.init(properties);
        } catch (AuditException e) {
            AssertionError ae = new AssertionError("Instantiation error");
            ae.initCause(e);
            throw ae;
        }

        Event event = new AuditEvent();
        ProcessingObjects processingObjects = new ProcessingObjects();

        syncAudit.audit(event, null, processingObjects);
    }

    /**
     * Audit an event with a non-default audit stream being blank
     *
     * @throws IllegalArgumentException when everything goes well
     */
    @Test(expected = IllegalArgumentException.class)
    public void auditEventInBlankNonDefaultAuditStreamProvidedProcessingObjectsTest()
            throws AuditException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        SyncAudit syncAudit = new SyncAudit();

        try {
            syncAudit.init(properties);
        } catch (AuditException e) {
            AssertionError ae = new AssertionError("Instantiation error");
            ae.initCause(e);
            throw ae;
        }

        Event event = new AuditEvent();
        String auditStream = "";
        ProcessingObjects processingObjects = new ProcessingObjects();

        syncAudit.audit(event, auditStream, processingObjects);
    }

    /**
     * Audit an event with the ProcessingObjects being null
     *
     * @throws NullPointerException when everything goes well
     */
    @Test(expected = NullPointerException.class)
    public void auditEventWithNullProcessingObjectsTest()
            throws AuditException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        SyncAudit syncAudit = new SyncAudit();

        try {
            syncAudit.init(properties);
        } catch (AuditException e) {
            AssertionError ae = new AssertionError("Instantiation error");
            ae.initCause(e);
            throw ae;
        }

        Event event = new AuditEvent();
        String auditStream = "A custom audit stream for testing";

        syncAudit.audit(event, auditStream, null);
    }


    /**
     * Audit an event with the processors list null, and the config set to throw an exception in this case
     *
     * @throws AuditException when everything goes well
     */
    @Test(expected = AuditException.class)
    public void auditEventWithNullProcessorsToFailTest()
            throws AuditException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        properties.setProcessors(null);
        properties.setFailOnMissingProcessors(true);

        SyncAudit syncAudit = new SyncAudit();

        try {
            syncAudit.init(properties);
        } catch (AuditException e) {
            AssertionError ae = new AssertionError("Instantiation error");
            ae.initCause(e);
            throw ae;
        }

        Event event = new AuditEvent();
        String auditStream = "A custom audit stream for testing";

        try {
            syncAudit.audit(event, auditStream);
        } catch (AuditException e) {
            AuditErrorConditions expected = AuditErrorConditions.CONFIGURATION;
            String error = "The type of exception thrown is not correct. Expected " + expected + ", got " + e.getErrorCondition();
            assertThat(error, e.getErrorCondition(), is(equalTo(expected)));
            throw e;
        }

        throw new AssertionError("Expected an exception, but that exception was not thrown");
    }

    /**
     * Audit an event with the processors list null, and the config set to NOT
     * throw an exception in this case (must return the same event object)
     *
     * @throws AuditException in case of an error (test failure)
     */
    @Test
    public void auditEventWithNullProcessorsToPassTest()
            throws AuditException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        properties.setProcessors(null);
        properties.setFailOnMissingProcessors(false);

        SyncAudit syncAudit = new SyncAudit();

        try {
            syncAudit.init(properties);
        } catch (AuditException e) {
            AssertionError ae = new AssertionError("Instantiation error");
            ae.initCause(e);
            throw ae;
        }

        Event event = new AuditEvent();
        String auditStream = "A custom audit stream for testing";

        Event returnedEvent = syncAudit.audit(event, auditStream);

        String error = "The event returned by the auditor is not identical to the event that was passed in";
        assertThat(error, event, is(sameInstance(returnedEvent)));
    }

    /**
     * Audit an event with the processors list empty, and the config set to throw an exception in this case
     *
     * @throws AuditException when everything goes well
     */
    @Test(expected = AuditException.class)
    public void auditEventWithBlankProcessorsToFailTest()
            throws AuditException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        properties.setProcessors("");
        properties.setFailOnMissingProcessors(true);

        SyncAudit syncAudit = new SyncAudit();

        try {
            syncAudit.init(properties);
        } catch (AuditException e) {
            AssertionError ae = new AssertionError("Instantiation error");
            ae.initCause(e);
            throw ae;
        }

        Event event = new AuditEvent();
        String auditStream = "A custom audit stream for testing";

        try {
            syncAudit.audit(event, auditStream);
        } catch (AuditException e) {
            AuditErrorConditions expected = AuditErrorConditions.CONFIGURATION;
            String error = "The type of exception thrown is not correct. Expected " + expected + ", got " + e.getErrorCondition();
            assertThat(error, e.getErrorCondition(), is(equalTo(expected)));
            throw e;
        }

        throw new AssertionError("Expected an exception, but that exception was not thrown");
    }

    /**
     * Audit an event with the processors list empty, and the config set to NOT
     * throw an exception in this case (must return the same event object)
     *
     * @throws AuditException in case of an error (test failure)
     */
    @Test
    public void auditEventWithBlankProcessorsToPassTest()
            throws AuditException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        properties.setProcessors("");
        properties.setFailOnMissingProcessors(false);

        SyncAudit syncAudit = new SyncAudit();

        try {
            syncAudit.init(properties);
        } catch (AuditException e) {
            AssertionError ae = new AssertionError("Instantiation error");
            ae.initCause(e);
            throw ae;
        }

        Event event = new AuditEvent();
        String auditStream = "A custom audit stream for testing";

        Event returnedEvent = syncAudit.audit(event, auditStream);

        String error = "The event returned by the auditor is not identical to the event that was passed in";
        assertThat(error, event, is(sameInstance(returnedEvent)));
    }

    /**
     * Audit an event with the dummy (test) processor
     *
     * @throws AuditException in case of an error (test failure)
     */
    @Test
    public void auditEventWithDummyProcessorTest()
            throws AuditException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        properties.setProcessors(CLASS_NAME);
        properties.setFailOnMissingProcessors(true);

        SyncAudit syncAudit = new SyncAudit();

        try {
            syncAudit.init(properties);
        } catch (AuditException e) {
            AssertionError ae = new AssertionError("Instantiation error");
            ae.initCause(e);
            throw ae;
        }

        Event event = new AuditEvent();
        String auditStream = "A custom audit stream for testing";

        Event returnedEvent = syncAudit.audit(event, auditStream);

        String error = "The event returned by the auditor is not identical to the event that was passed in";
        assertThat(error, event, is(sameInstance(returnedEvent)));
    }
}
