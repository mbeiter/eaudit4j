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

import org.beiter.michael.eaudit4j.common.Field;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class AuditEventTest {


    /**
     * The logger object for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(AuditEventTest.class);

    private java.lang.reflect.Field field_fields;

    /**
     * Make some of the private fields in the AuditEvent class accessible.
     * <p>
     * This is executed before every test to ensure consistency even if one of the tests mock with field accessibility.
     */
    @Before
    public void makeAdditionalPropertiesPrivateFieldsAccessible() {

        // make private fields accessible as needed
        try {
            field_fields = AuditEvent.class.getDeclaredField("fields");
        } catch (NoSuchFieldException e) {
            AssertionError ae = new AssertionError("An expected private field does not exist");
            ae.initCause(e);
            throw ae;
        }
        field_fields.setAccessible(true);
    }

    /**
     * Create an AuditEvent without fields
     */
    @Test
    public void createAuditEventTest() {

        AuditEvent auditEvent = new AuditEvent();

        String error = "The number of fields in the event is not correct";
        assertThat(error, auditEvent.getFieldNames().size(), is(equalTo(0)));
    }

    /**
     * Create an AuditEvent with a set of fields, and make sure that:
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
    public void createAuditEventWithFieldsTest()
            throws UnsupportedEncodingException, IllegalAccessException {

        Field field1 = new EventField("field_1", "field_1_value".getBytes("UTF-8"));
        Field field2 = new EventField("field_2", "field_2_value".getBytes("UTF-8"));
        Field field3 = new EventField("field_3", "field_3_value".getBytes("UTF-8"));

        AuditEvent auditEvent = new AuditEvent(field1, field2, field3);

        // Use reflection to get access to the internal fields
        ConcurrentHashMap<String, Field> fieldsInObject = (ConcurrentHashMap<String, Field>) field_fields.get(auditEvent);


        String error = "The number of fields in the event is not correct";
        assertThat(error, fieldsInObject.size(), is(equalTo(3)));
        error = "An expected field is missing in the event";
        assertThat(error, fieldsInObject.keySet(), containsInAnyOrder("field_1", "field_2", "field_3"));

        error = "The method does not create an inbound defensive copy";
        assertThat(error, fieldsInObject.get(field1.getName()), is(not(sameInstance(field1))));
        assertThat(error, fieldsInObject.get(field2.getName()), is(not(sameInstance(field2))));
        assertThat(error, fieldsInObject.get(field3.getName()), is(not(sameInstance(field3))));

        error = "The value is not correct";
        assertThat(error, fieldsInObject.get(field1.getName()).getValue(), is(equalTo(field1.getValue())));
        assertThat(error, fieldsInObject.get(field2.getName()).getValue(), is(equalTo(field2.getValue())));
        assertThat(error, fieldsInObject.get(field3.getName()).getValue(), is(equalTo(field3.getValue())));
    }

    /**
     * Create an AuditEvent with an empty fields list
     */
    @Test
    public void createAuditEventWithEmptyFieldListTest() {

        Field[] fields = new Field[]{};
        AuditEvent auditEvent = new AuditEvent(fields);

        String error = "The number of fields in the event is not correct";
        assertThat(error, auditEvent.getFieldNames().size(), is(equalTo(0)));
    }

    /**
     * Create an AuditEvent with a null fields list
     */
    @Test
    public void createAuditEventWithNullFieldListTest() {

        Field[] fields = null;
        AuditEvent auditEvent = new AuditEvent(fields);

        String error = "The number of fields in the event is not correct";
        assertThat(error, auditEvent.getFieldNames().size(), is(equalTo(0)));
    }

    /**
     * Create an AuditEvent with one null reference in the fields list
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     */
    @Test(expected = NullPointerException.class)
    public void createAuditEventWithOneNullFieldTest()
            throws UnsupportedEncodingException {

        Field field1 = new EventField("field_1", "field_1_value".getBytes("UTF-8"));
        Field field2 = null;
        Field field3 = new EventField("field_3", "field_3_value".getBytes("UTF-8"));

        AuditEvent auditEvent = new AuditEvent(field1, field2, field3);
    }

    /**
     * Test that the record format version returned is not null and matches the implementation's published record
     * format version
     */
    @Test
    public void getRecordFormatVersionTest()
            throws UnsupportedEncodingException, IllegalAccessException {

        String recordFormatVersion = new AuditEvent().getRecordFormatVersion();

        String error = "The record format version is null";
        assertThat(error, recordFormatVersion, is(not(nullValue())));
        error = "The record format version is not correct";
        assertThat(error, recordFormatVersion, is(equalTo(AuditEvent.FORMAT_VERSION)));
    }

    /**
     * Set an additional field in an AuditEvent, and make sure that:
     * <ul>
     * <li>the audit event contains the provided field names, and the provided field names only, including the additional field</li>
     * <li>the method makes a defensive copy of the fields</li>
     * <li>the value of each field (including the new field) is correct</li>
     * </ul>
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     * @throws IllegalAccessException       when reflection does not work
     */
    @Test
    public void updateAuditEventWithAdditionalFieldTest()
            throws UnsupportedEncodingException, IllegalAccessException {

        Field field1 = new EventField("field_1", "field_1_value".getBytes("UTF-8"));
        Field field2 = new EventField("field_2", "field_2_value".getBytes("UTF-8"));

        AuditEvent auditEvent = new AuditEvent(field1, field2);

        // Use reflection to get access to the internal fields
        ConcurrentHashMap<String, Field> fieldsInObject = (ConcurrentHashMap<String, Field>) field_fields.get(auditEvent);

        String error = "The number of fields in the event is not correct";
        assertThat(error, fieldsInObject.size(), is(equalTo(2)));
        error = "An expected field is missing in the event";
        assertThat(error, fieldsInObject.keySet(), containsInAnyOrder("field_1", "field_2"));

        // set an additional field
        Field field3 = new EventField("field_3", "field_3_value".getBytes("UTF-8"));
        auditEvent.setField(field3);

        error = "The number of fields in the event is not correct";
        assertThat(error, fieldsInObject.size(), is(equalTo(3)));
        error = "An expected field is missing in the event";
        assertThat(error, fieldsInObject.keySet(), containsInAnyOrder("field_1", "field_2", "field_3"));

        error = "The method does not create an inbound defensive copy";
        assertThat(error, fieldsInObject.get(field3.getName()), is(not(sameInstance(field3))));

        error = "The value is not correct";
        assertThat(error, fieldsInObject.get(field3.getName()).getValue(), is(equalTo(field3.getValue())));
    }

    /**
     * Set (update) an existing field in an AuditEvent, and make sure that:
     * <ul>
     * <li>the audit event contains the provided field names, and the provided field names only, including the updated field</li>
     * <li>the method makes a defensive copy of the fields</li>
     * <li>the value of each field (including the updated field) is correct</li>
     * </ul>
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     * @throws IllegalAccessException       when reflection does not work
     */
    @Test
    public void updateAuditEventWithExistingFieldTest()
            throws UnsupportedEncodingException, IllegalAccessException {

        Field field1 = new EventField("field_1", "field_1_value".getBytes("UTF-8"));
        Field field2 = new EventField("field_2", "field_2_value".getBytes("UTF-8"));

        AuditEvent auditEvent = new AuditEvent(field1, field2);

        // Use reflection to get access to the internal fields
        ConcurrentHashMap<String, Field> fieldsInObject = (ConcurrentHashMap<String, Field>) field_fields.get(auditEvent);

        String error = "The number of fields in the event is not correct";
        assertThat(error, fieldsInObject.size(), is(equalTo(2)));
        error = "An expected field is missing in the event";
        assertThat(error, fieldsInObject.keySet(), containsInAnyOrder("field_1", "field_2"));

        // set an additional field (two variants: reusing an existing Field object and creating a new one)
        field1.setValue("field_1_new_value".getBytes("UTF-8"));
        Field field2_newValue = new EventField("field_2", "field_2_new_value".getBytes("UTF-8"));
        auditEvent.setField(field1);
        auditEvent.setField(field2_newValue);

        error = "The number of fields in the event is not correct";
        assertThat(error, fieldsInObject.size(), is(equalTo(2)));
        error = "An expected field is missing in the event";
        assertThat(error, fieldsInObject.keySet(), containsInAnyOrder("field_1", "field_2"));

        error = "The method does not create an inbound defensive copy";
        assertThat(error, fieldsInObject.get(field1.getName()), is(not(sameInstance(field1))));
        assertThat(error, fieldsInObject.get(field2.getName()), is(not(sameInstance(field2))));

        error = "The value is not correct";
        assertThat(error, fieldsInObject.get(field1.getName()).getValue(), is(equalTo(field1.getValue())));
        assertThat(error, fieldsInObject.get(field2.getName()).getValue(), is(equalTo(field2_newValue.getValue())));
    }

    /**
     * Set a null field in an AuditEvent
     */
    @Test(expected = NullPointerException.class)
    public void updateAuditEventWithNullFieldTest() {

        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setField(null);
    }

    /**
     * Unset (remove) an existing field from an AuditEvent, and make sure that:
     * <ul>
     * <li>the audit event no longer contains the provided field name after the remove operation</li>
     * </ul>
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     * @throws IllegalAccessException       when reflection does not work
     */
    @Test
    public void removeFieldFromAuditEventTest()
            throws UnsupportedEncodingException, IllegalAccessException {

        Field field1 = new EventField("field_1", "field_1_value".getBytes("UTF-8"));
        Field field2 = new EventField("field_2", "field_2_value".getBytes("UTF-8"));

        AuditEvent auditEvent = new AuditEvent(field1, field2);

        // Use reflection to get access to the internal fields
        ConcurrentHashMap<String, Field> fieldsInObject = (ConcurrentHashMap<String, Field>) field_fields.get(auditEvent);

        String error = "The number of fields in the event is not correct";
        assertThat(error, fieldsInObject.size(), is(equalTo(2)));
        error = "An expected field is missing in the event";
        assertThat(error, fieldsInObject.keySet(), containsInAnyOrder("field_1", "field_2"));

        // remove a field
        boolean result = auditEvent.unsetField(field1.getName());

        error = "The number of fields in the event is not correct";
        assertThat(error, fieldsInObject.size(), is(equalTo(1)));
        error = "An expected field is missing in the event";
        assertThat(error, fieldsInObject.keySet(), containsInAnyOrder("field_2"));
        error = "The returned result of the remove operation is not correct";
        assertThat(error, result, is(true));
    }

    /**
     * Unset (remove) a null field from an AuditEvent
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     */
    @Test(expected = NullPointerException.class)
    public void removeNullFieldFromAuditEventTest()
            throws UnsupportedEncodingException {

        Field field1 = new EventField("field_1", "field_1_value".getBytes("UTF-8"));
        Field field2 = new EventField("field_2", "field_2_value".getBytes("UTF-8"));

        AuditEvent auditEvent = new AuditEvent(field1, field2);

        // remove a null field
        auditEvent.unsetField(null);
    }

    /**
     * Unset (remove) a blank field from an AuditEvent
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     */
    @Test(expected = IllegalArgumentException.class)
    public void removeBlankFieldFromAuditEventTest()
            throws UnsupportedEncodingException {

        Field field1 = new EventField("field_1", "field_1_value".getBytes("UTF-8"));
        Field field2 = new EventField("field_2", "field_2_value".getBytes("UTF-8"));

        AuditEvent auditEvent = new AuditEvent(field1, field2);

        // remove a null field
        auditEvent.unsetField("");
    }

    /**
     * Unset (remove) a non-existing field from an AuditEvent
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     */
    @Test
    public void removeNonExistingFieldFromAuditEventTest()
            throws UnsupportedEncodingException {

        Field field1 = new EventField("field_1", "field_1_value".getBytes("UTF-8"));
        Field field2 = new EventField("field_2", "field_2_value".getBytes("UTF-8"));

        AuditEvent auditEvent = new AuditEvent(field1, field2);

        // remove a null field
        boolean result = auditEvent.unsetField("invalid_field");

        String error = "The returned result of the remove operation is not correct";
        assertThat(error, result, is(false));
    }

    /**
     * Check if an existing field is correctly reported as existing in an AuditEvent
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     */
    @Test
    public void checkExistingFieldExistsInAuditEventTest()
            throws UnsupportedEncodingException {

        Field field1 = new EventField("field_1", "field_1_value".getBytes("UTF-8"));
        Field field2 = new EventField("field_2", "field_2_value".getBytes("UTF-8"));

        AuditEvent auditEvent = new AuditEvent(field1, field2);

        boolean result = auditEvent.containsField(field1.getName());

        String error = "The returned result of the 'contains' operation is not correct";
        assertThat(error, result, is(true));
    }

    /**
     * Check if a non-existing field is correctly reported as non-existing in an AuditEvent
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     */
    @Test
    public void checkNonExistingFieldDoesNotExistInAuditEventTest()
            throws UnsupportedEncodingException {

        Field field1 = new EventField("field_1", "field_1_value".getBytes("UTF-8"));
        Field field2 = new EventField("field_2", "field_2_value".getBytes("UTF-8"));

        AuditEvent auditEvent = new AuditEvent(field1, field2);

        boolean result = auditEvent.containsField("invalid_field");

        String error = "The returned result of the 'contains' operation is not correct";
        assertThat(error, result, is(false));
    }


    /**
     * Get (retrieve) an existing field from an AuditEvent, and make sure that:
     * <ul>
     * <li>the method makes a defensive copy of the fields</li>
     * <li>the value is correct</li>
     * </ul>
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     * @throws IllegalAccessException       when reflection does not work
     */
    @Test
    public void getExistingFieldFromAuditEventTest()
            throws UnsupportedEncodingException, IllegalAccessException {

        Field field1 = new EventField("field_1", "field_1_value".getBytes("UTF-8"));
        Field field2 = new EventField("field_2", "field_2_value".getBytes("UTF-8"));

        AuditEvent auditEvent = new AuditEvent(field1, field2);

        // Use reflection to get access to the internal fields
        ConcurrentHashMap<String, Field> fieldsInObject = (ConcurrentHashMap<String, Field>) field_fields.get(auditEvent);


        String error = "The method does not create an inbound defensive copy";
        assertThat(error, fieldsInObject.get(field1.getName()), is(not(sameInstance(auditEvent.getField(field1.getName())))));
        assertThat(error, fieldsInObject.get(field2.getName()), is(not(sameInstance(auditEvent.getField(field2.getName())))));

        error = "The value is not correct";
        assertThat(error, fieldsInObject.get(field1.getName()).getValue(), is(equalTo(auditEvent.getField(field1.getName()).getValue())));
        assertThat(error, fieldsInObject.get(field2.getName()).getValue(), is(equalTo(auditEvent.getField(field2.getName()).getValue())));
    }

    /**
     * Get (retrieve) a non-existing field from an AuditEvent
     */
    @Test(expected = NoSuchElementException.class)
    public void getNonExistingFieldFromAuditEventTest() {

        AuditEvent auditEvent = new AuditEvent();

        Field field = auditEvent.getField("invalid_field");
    }

    /**
     * Get (retrieve) a list of fields from an AuditEvent, and make sure the list contains the provided field names,
     * and the provided field names only.
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     */
    @Test
    public void getFieldListFromAuditEventTest()
            throws UnsupportedEncodingException {

        Field field1 = new EventField("field_1", "field_1_value".getBytes("UTF-8"));
        Field field2 = new EventField("field_2", "field_2_value".getBytes("UTF-8"));

        AuditEvent auditEvent = new AuditEvent(field1, field2);

        String error = "The number of fields in the event is not correct";
        assertThat(error, auditEvent.getFieldNames().size(), is(equalTo(2)));
        error = "An expected field is missing in the event";
        assertThat(error, auditEvent.getFieldNames(), containsInAnyOrder("field_1", "field_2"));
    }

    /**
     * Clear an AuditEvent, and make sure that:
     * <p>
     * <ul>
     * <li>all individual fields are cleared</li>
     * <li>the internal list is cleared (size 0)</li>
     * </ul>
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     * @throws IllegalAccessException       when reflection does not work
     */
    @Test
    public void clearFieldsInAuditEventTest()
            throws UnsupportedEncodingException, IllegalAccessException {

        Field field1 = new EventField("field_1", "field_1_value".getBytes("UTF-8"));
        Field field2 = new EventField("field_2", "field_2_value".getBytes("UTF-8"));

        AuditEvent auditEvent = new AuditEvent(field1, field2);

        // Use reflection to get access to the internal fields
        ConcurrentHashMap<String, Field> fieldsInObject = (ConcurrentHashMap<String, Field>) field_fields.get(auditEvent);

        // keep references to the internal fields so that we can check them later
        Field internalField1 = fieldsInObject.get(field1.getName());
        Field internalField2 = fieldsInObject.get(field2.getName());

        // clear the audit event's data
        auditEvent.clear();

        // check that the data in our saved references really has been cleared (zero arrays)
        byte[] expectedValue = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] value1 = internalField1.getValue();
        byte[] value2 = internalField2.getValue();
        String error = "The method does not zero the array";
        assertThat(error, value1, is(equalTo(expectedValue)));
        assertThat(error, value2, is(equalTo(expectedValue)));

        // check that the size of the internal map is zero
        error = "The method does not empty the map";
        assertThat(error, fieldsInObject.size(), is(equalTo(0)));
    }

    /**
     * Get a serialized representation of an AuditEvent, and make sure the JSON is correct
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     */
    @Test
    public void auditEventToJsonTest()
            throws UnsupportedEncodingException {

        Field field1 = new EventField("field_1", "field_1_value".getBytes("UTF-8"));
        Field field2 = new EventField("field_2", "field_2_value".getBytes("UTF-8"));

        String expectedJson = "{\"version\":\"1.0\",\"fields\":{\"field_1\":\"field_1_value\",\"field_2\":\"field_2_value\"}}";

        AuditEvent auditEvent = new AuditEvent(field1, field2);

        String json = String.valueOf(auditEvent.toJson("UTF-8"));

        String error = "JSON serialization returns an incorrect JSON representation";
        assertThat(error, json, is(equalTo(expectedJson)));
    }

    /**
     * Get a serialized representation of an AuditEvent with a null encoding
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     */
    @Test(expected = NullPointerException.class)
    public void auditEventToJsonNullEncodingTest()
            throws UnsupportedEncodingException {

        Field field1 = new EventField("field_1", "field_1_value".getBytes("UTF-8"));
        Field field2 = new EventField("field_2", "field_2_value".getBytes("UTF-8"));

        AuditEvent auditEvent = new AuditEvent(field1, field2);

        String json = String.valueOf(auditEvent.toJson(null));
    }

    /**
     * Get a serialized representation of an AuditEvent with a blank encoding
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     */
    @Test(expected = IllegalArgumentException.class)
    public void auditEventToJsonEmptyEncodingTest()
            throws UnsupportedEncodingException {

        Field field1 = new EventField("field_1", "field_1_value".getBytes("UTF-8"));
        Field field2 = new EventField("field_2", "field_2_value".getBytes("UTF-8"));

        AuditEvent auditEvent = new AuditEvent(field1, field2);

        String json = String.valueOf(auditEvent.toJson(""));
    }
}
