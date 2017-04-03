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

import org.beiter.michael.eaudit4j.common.CommonProperties;
import org.beiter.michael.eaudit4j.common.ExtendedEvent;
import org.beiter.michael.eaudit4j.common.Field;
import org.beiter.michael.eaudit4j.common.propsbuilder.MapBasedCommonPropsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

import static org.apache.commons.lang3.StringEscapeUtils.escapeJson;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

public class ExtendedAuditEventTest {

    /**
     * The logger object for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(ExtendedAuditEventTest.class);

    private java.lang.reflect.Field field_properties;

    /**
     * Make some of the private fields in the ExtendedAuditEvent class accessible.
     * <p>
     * This is executed before every test to ensure consistency even if one of the tests mock with field accessibility.
     */
    @Before
    public void makeAdditionalPropertiesPrivateFieldsAccessible() {

        // make private fields accessible as needed
        try {
            field_properties = ExtendedAuditEvent.class.getDeclaredField("properties");
        } catch (NoSuchFieldException e) {
            AssertionError ae = new AssertionError("An expected private field does not exist");
            ae.initCause(e);
            throw ae;
        }
        field_properties.setAccessible(true);
    }

    /**
     * Create an ExtendedAuditEvent without fields, and make sure that:
     * <ul>
     * <li>the audit event contains the no fields</li>
     * <li>the constructor makes a defensive copy of the properties</li>
     * </ul>
     * <p>
     *
     * @throws IllegalAccessException when reflection does not work
     */
    @Test
    public void createExtendedAuditEventTest()
            throws IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        ExtendedEvent extendedEvent = new ExtendedAuditEvent(properties);

        String error = "The number of fields in the event is not correct";
        assertThat(error, extendedEvent.getFieldNames().size(), is(equalTo(0)));

        // Use reflection to get access to the internal fields
        CommonProperties propertiesInObject = (CommonProperties) field_properties.get(extendedEvent);

        error = "The method does not create an inbound defensive copy";
        assertThat(error, propertiesInObject, is(not(sameInstance(properties))));
    }

    /**
     * Create an ExtendedAuditEvent without fields and null properties
     */
    @Test(expected = NullPointerException.class)
    public void createExtendedAuditEventWithNullPropertiesTest() {

        CommonProperties properties = null;
        ExtendedEvent extendedEvent = new ExtendedAuditEvent(properties);
    }

    /**
     * Create an ExtendedAuditEvent with a set of fields, and make sure that:
     * <ul>
     * <li>the constructor makes a defensive copy of the properties</li>
     * </ul>
     * No need to test the following for this implementation, because we use the super class' constructor which is
     * unit tested separately:
     * <ul>
     * <li>the audit event contains the provided field names, and the provided field names only</li>
     * <li>the constructor makes a defensive copy of the fields</li>
     * <li>the value of each field is correct</li>
     * </ul>
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     * @throws IllegalAccessException       when reflection does not work
     */
    @Test
    public void createExtendedAuditEventWithFieldsTest()
            throws UnsupportedEncodingException, IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();

        Field field1 = new EventField("field_1", "field_1_value".getBytes("UTF-8"));
        Field field2 = new EventField("field_2", "field_2_value".getBytes("UTF-8"));
        Field field3 = new EventField("field_3", "field_3_value".getBytes("UTF-8"));

        ExtendedEvent extendedEvent = new ExtendedAuditEvent(properties, field1, field2, field3);

        String error = "The number of fields in the event is not correct";
        assertThat(error, extendedEvent.getFieldNames().size(), is(equalTo(3)));

        // Use reflection to get access to the internal fields
        CommonProperties propertiesInObject = (CommonProperties) field_properties.get(extendedEvent);

        error = "The method does not create an inbound defensive copy";
        assertThat(error, propertiesInObject, is(not(sameInstance(properties))));
    }

    /**
     * Create an ExtendedAuditEvent without fields and null properties
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     */
    @Test(expected = NullPointerException.class)
    public void createExtendedAuditEventWithFieldsAndNullPropertiesTest()
            throws UnsupportedEncodingException {

        CommonProperties properties = null;

        Field field1 = new EventField("field_1", "field_1_value".getBytes("UTF-8"));
        Field field2 = new EventField("field_2", "field_2_value".getBytes("UTF-8"));
        Field field3 = new EventField("field_3", "field_3_value".getBytes("UTF-8"));

        ExtendedEvent extendedEvent = new ExtendedAuditEvent(properties, field1, field2, field3);
    }

    /**
     * Set the "Event Type" field in an ExtendedAuditEvent, and make sure that:
     * <ul>
     * <li>The short field name is set correctly (if short name is configured)</li>
     * <li>The fully qualified field name is set correctly (if so configured)</li>
     * </ul>
     */
    @Test
    public void setEventTypeFieldTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();

        ExtendedEvent extendedEvent1 = new ExtendedAuditEvent(properties);

        extendedEvent1.setEventType("value".toCharArray());
        String error = "The short name has not been set correctly";
        assertThat(error, extendedEvent1.containsField(properties.getFieldNameEventType()), is(true));
    }

    /**
     * Set the "Event Type" field in an ExtendedAuditEvent with a null value
     */
    @Test(expected = NullPointerException.class)
    public void setEventTypeFieldNullValueTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        ExtendedEvent extendedEvent = new ExtendedAuditEvent(properties);

        extendedEvent.setEventType(null);
    }

    /**
     * Set the "Event Group Type" field in an ExtendedAuditEvent, and make sure that:
     * <ul>
     * <li>The short field name is set correctly (if short name is configured)</li>
     * <li>The fully qualified field name is set correctly (if so configured)</li>
     * </ul>
     */
    @Test
    public void setEventGroupTypeFieldTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();

        ExtendedEvent extendedEvent1 = new ExtendedAuditEvent(properties);

        extendedEvent1.setEventGroupType("value".toCharArray());
        String error = "The short name has not been set correctly";
        assertThat(error, extendedEvent1.containsField(properties.getFieldNameEventGroupType()), is(true));
    }

    /**
     * Set the "Event Group Type" field in an ExtendedAuditEvent with a null value
     */
    @Test(expected = NullPointerException.class)
    public void setEventGroupTypeFieldNullValueTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        ExtendedEvent extendedEvent = new ExtendedAuditEvent(properties);

        extendedEvent.setEventGroupType(null);
    }

    /**
     * Set the "Subject" field in an ExtendedAuditEvent, and make sure that:
     * <ul>
     * <li>The short field name is set correctly (if short name is configured)</li>
     * <li>The fully qualified field name is set correctly (if so configured)</li>
     * </ul>
     */
    @Test
    public void setSubjectFieldTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();

        // test short name
        ExtendedEvent extendedEvent1 = new ExtendedAuditEvent(properties);

        extendedEvent1.setSubject("value".toCharArray());
        String error = "The short name has not been set correctly";
        assertThat(error, extendedEvent1.containsField(properties.getFieldNameSubject()), is(true));
    }

    /**
     * Set the "Subject" field in an ExtendedAuditEvent with a null value
     */
    @Test(expected = NullPointerException.class)
    public void setSubjectFieldNullValueTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        ExtendedEvent extendedEvent = new ExtendedAuditEvent(properties);

        extendedEvent.setSubject(null);
    }

    /**
     * Set the "Subject Location" field in an ExtendedAuditEvent, and make sure that:
     * <ul>
     * <li>The short field name is set correctly (if short name is configured)</li>
     * <li>The fully qualified field name is set correctly (if so configured)</li>
     * </ul>
     */
    @Test
    public void setSubjectLocationFieldTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();

        ExtendedEvent extendedEvent1 = new ExtendedAuditEvent(properties);

        extendedEvent1.setSubjectLocation("value".toCharArray());
        String error = "The short name has not been set correctly";
        assertThat(error, extendedEvent1.containsField(properties.getFieldNameSubjectLocation()), is(true));
    }

    /**
     * Set the "Subject Location" field in an ExtendedAuditEvent with a null value
     */
    @Test(expected = NullPointerException.class)
    public void setSubjectLocationFieldNullValueTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        ExtendedEvent extendedEvent = new ExtendedAuditEvent(properties);

        extendedEvent.setSubjectLocation(null);
    }

    /**
     * Set the "Actor" field in an ExtendedAuditEvent, and make sure that:
     * <ul>
     * <li>The short field name is set correctly (if short name is configured)</li>
     * <li>The fully qualified field name is set correctly (if so configured)</li>
     * </ul>
     */
    @Test
    public void setActorFieldTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();

        ExtendedEvent extendedEvent1 = new ExtendedAuditEvent(properties);

        extendedEvent1.setActor("value".toCharArray());
        String error = "The short name has not been set correctly";
        assertThat(error, extendedEvent1.containsField(properties.getFieldNameActor()), is(true));
    }

    /**
     * Set the "Actor" field in an ExtendedAuditEvent with a null value
     */
    @Test(expected = NullPointerException.class)
    public void setActorFieldNullValueTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        ExtendedEvent extendedEvent = new ExtendedAuditEvent(properties);

        extendedEvent.setActor(null);
    }

    /**
     * Set the "Object" field in an ExtendedAuditEvent, and make sure that:
     * <ul>
     * <li>The short field name is set correctly (if short name is configured)</li>
     * <li>The fully qualified field name is set correctly (if so configured)</li>
     * </ul>
     */
    @Test
    public void setObjectFieldTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();

        ExtendedEvent extendedEvent1 = new ExtendedAuditEvent(properties);

        extendedEvent1.setObject("value".toCharArray());
        String error = "The short name has not been set correctly";
        assertThat(error, extendedEvent1.containsField(properties.getFieldNameObject()), is(true));
    }

    /**
     * Set the "Object" field in an ExtendedAuditEvent with a null value
     */
    @Test(expected = NullPointerException.class)
    public void setObjectFieldNullValueTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        ExtendedEvent extendedEvent = new ExtendedAuditEvent(properties);

        extendedEvent.setObject(null);
    }

    /**
     * Set the "Object Location" field in an ExtendedAuditEvent, and make sure that:
     * <ul>
     * <li>The short field name is set correctly (if short name is configured)</li>
     * <li>The fully qualified field name is set correctly (if so configured)</li>
     * </ul>
     */
    @Test
    public void setObjectLocationFieldTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();

        ExtendedEvent extendedEvent1 = new ExtendedAuditEvent(properties);

        extendedEvent1.setObjectLocation("value".toCharArray());
        String error = "The short name has not been set correctly";
        assertThat(error, extendedEvent1.containsField(properties.getFieldNameObjectLocation()), is(true));
    }

    /**
     * Set the "Object Location" field in an ExtendedAuditEvent with a null value
     */
    @Test(expected = NullPointerException.class)
    public void setObjectLocationFieldNullValueTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        ExtendedEvent extendedEvent = new ExtendedAuditEvent(properties);

        extendedEvent.setObjectLocation(null);
    }

    /**
     * Set the "Content Before Operation" field in an ExtendedAuditEvent, and make sure that:
     * <ul>
     * <li>The short field name is set correctly (if short name is configured)</li>
     * <li>The fully qualified field name is set correctly (if so configured)</li>
     * </ul>
     */
    @Test
    public void setContentBeforeOperationFieldTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();

        ExtendedEvent extendedEvent1 = new ExtendedAuditEvent(properties);

        extendedEvent1.setContentBeforeOperation("value".toCharArray());
        String error = "The short name has not been set correctly";
        assertThat(error, extendedEvent1.containsField(properties.getFieldNameContentBeforeOperation()), is(true));
    }

    /**
     * Set the "Content Before Operation" field in an ExtendedAuditEvent with a null value
     */
    @Test(expected = NullPointerException.class)
    public void setContentBeforeOperationFieldNullValueTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        ExtendedEvent extendedEvent = new ExtendedAuditEvent(properties);

        extendedEvent.setContentBeforeOperation(null);
    }

    /**
     * Set the "Content After Operation" field in an ExtendedAuditEvent, and make sure that:
     * <ul>
     * <li>The short field name is set correctly (if short name is configured)</li>
     * <li>The fully qualified field name is set correctly (if so configured)</li>
     * </ul>
     */
    @Test
    public void setContentAfterOperationFieldTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();

        ExtendedEvent extendedEvent1 = new ExtendedAuditEvent(properties);

        extendedEvent1.setContentAfterOperation("value".toCharArray());
        String error = "The short name has not been set correctly";
        assertThat(error, extendedEvent1.containsField(properties.getFieldNameContentAfterOperation()), is(true));
    }

    /**
     * Set the "Content After Operation" field in an ExtendedAuditEvent with a null value
     */
    @Test(expected = NullPointerException.class)
    public void setContentAfterOperationFieldNullValueTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        ExtendedEvent extendedEvent = new ExtendedAuditEvent(properties);

        extendedEvent.setContentAfterOperation(null);
    }

    /**
     * Set the "Result" field in an ExtendedAuditEvent, and make sure that:
     * <ul>
     * <li>The short field name is set correctly (if short name is configured)</li>
     * <li>The fully qualified field name is set correctly (if so configured)</li>
     * </ul>
     */
    @Test
    public void setResultFieldTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();

        ExtendedEvent extendedEvent1 = new ExtendedAuditEvent(properties);

        extendedEvent1.setResult("value".toCharArray());
        String error = "The short name has not been set correctly";
        assertThat(error, extendedEvent1.containsField(properties.getFieldNameResult()), is(true));
    }

    /**
     * Set the "Result" field in an ExtendedAuditEvent with a null value
     */
    @Test(expected = NullPointerException.class)
    public void setResultFieldNullValueTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        ExtendedEvent extendedEvent = new ExtendedAuditEvent(properties);

        extendedEvent.setResult(null);
    }

    /**
     * Set the "Result Summary" field in an ExtendedAuditEvent, and make sure that:
     * <ul>
     * <li>The short field name is set correctly (if short name is configured)</li>
     * <li>The fully qualified field name is set correctly (if so configured)</li>
     * </ul>
     */
    @Test
    public void setResultSummaryFieldTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();

        ExtendedEvent extendedEvent1 = new ExtendedAuditEvent(properties);

        extendedEvent1.setResultSummary("value".toCharArray());
        String error = "The short name has not been set correctly";
        assertThat(error, extendedEvent1.containsField(properties.getFieldNameResultSummary()), is(true));
    }

    /**
     * Set the "Result Summary" field in an ExtendedAuditEvent with a null value
     */
    @Test(expected = NullPointerException.class)
    public void setResultSummaryFieldNullValueTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        ExtendedEvent extendedEvent = new ExtendedAuditEvent(properties);

        extendedEvent.setResultSummary(null);
    }

    /**
     * Set the "Event Summary" field in an ExtendedAuditEvent, and make sure that:
     * <ul>
     * <li>The short field name is set correctly (if short name is configured)</li>
     * <li>The fully qualified field name is set correctly (if so configured)</li>
     * </ul>
     */
    @Test
    public void setEventSummaryFieldTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();

        ExtendedEvent extendedEvent1 = new ExtendedAuditEvent(properties);

        extendedEvent1.setEventSummary("value".toCharArray());
        String error = "The short name has not been set correctly";
        assertThat(error, extendedEvent1.containsField(properties.getFieldNameEventSummary()), is(true));
    }

    /**
     * Set the "Event Summary" field in an ExtendedAuditEvent with a null value
     */
    @Test(expected = NullPointerException.class)
    public void setEventSummaryFieldNullValueTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        ExtendedEvent extendedEvent = new ExtendedAuditEvent(properties);

        extendedEvent.setEventSummary(null);
    }

    /**
     * Get a serialized representation of an ExtendedAuditEvent, and make sure the JSON is correct
     */
    @Test
    public void auditEventToJsonTest()
            throws UnsupportedEncodingException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        ExtendedEvent extendedEvent = new ExtendedAuditEvent(properties);

        extendedEvent.setActor("actor_value".toCharArray());
        extendedEvent.setResult("result_value".toCharArray());

        String expectedJson = "{\"version\":\"1.0\",\"fields\":{\"actor\":\"actor_value\",\"result\":\"result_value\"}}";

        String json = String.valueOf(extendedEvent.toJson());

        String error = "JSON serialization returns an incorrect JSON representation";
        assertThat(error, json, is(equalTo(expectedJson)));
    }

    /**
     * Get a serialized representation of an ExtendedAuditEvent when the field value is a Json String,
     * and make sure the JSON is correct
     */
    @Test
    public void auditEventToJsonFieldTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        ExtendedEvent extendedEvent = new ExtendedAuditEvent(properties);

        String actor = "{\"application\":\"test\"}";
        extendedEvent.setActor(actor.toCharArray());
        extendedEvent.setResult("result_value".toCharArray());

        String expectedJson = "{\"version\":\"1.0\",\"fields\":{\"actor\":\"" + escapeJson(actor)
            + "\",\"result\":\"result_value\"}}";

        String json = String.valueOf(extendedEvent.toJson());

        String error = "JSON serialization returns an incorrect JSON representation";
        assertThat(error, json, is(equalTo(expectedJson)));
    }
}
