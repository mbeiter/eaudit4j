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

import org.beiter.michael.eaudit4j.common.Encodings;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class EventFieldTest {

    /**
     * The logger object for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(EventFieldTest.class);

    private Field field_value;

    /**
     * Make some of the private fields in the EventField class accessible.
     * <p>
     * This is executed before every test to ensure consistency even if one of the tests mock with field accessibility.
     */
    @Before
    public void makeAdditionalPropertiesPrivateFieldsAccessible() {

        // make private fields accessible as needed
        try {
            field_value = EventField.class.getDeclaredField("value");
        } catch (NoSuchFieldException e) {
            AssertionError ae = new AssertionError("An expected private field does not exist");
            ae.initCause(e);
            throw ae;
        }
        field_value.setAccessible(true);
    }

    /**
     * Create an EventField with standard encoding, and make sure that:
     * <ul>
     * <li>the name is correct</li>
     * <li>the encoding is PLAIN</li>
     * <li>the constructor makes a defensive copy of the value</li>
     * <li>the value is correct</li>
     * </ul>
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     * @throws IllegalAccessException       when reflection does not work
     */
    @Test
    public void createWithStandardEncodingTest()
            throws UnsupportedEncodingException, IllegalAccessException {

        String name = "name";
        byte[] value = "\u00C4-\u00D6-\u00DC".getBytes("UTF-8");
        EventField eventField = new EventField(name, value);

        String error = "The name is not correct";
        assertThat(error, eventField.getName(), is(equalTo(name)));

        error = "The encoding is not correct";
        assertThat(error, eventField.getEncoding(), is(equalTo(Encodings.PLAIN)));

        // Use reflection to check if a defensive copy has been made
        byte[] valueInObject = (byte[]) field_value.get(eventField);
        error = "The method does not create an inbound defensive copy";
        assertThat(error, valueInObject, is(not(sameInstance(value))));

        error = "The value is not correct";
        assertThat(error, eventField.getValue(), is(equalTo(value)));
    }

    /**
     * Create an EventField with a non-standard encoding (BASE64)
     * <ul>
     * <li>the name is correct</li>
     * <li>the encoding is BASE64</li>
     * <li>the constructor makes a defensive copy of the value</li>
     * <li>the value is correct</li>
     * </ul>
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     * @throws IllegalAccessException       when reflection does not work
     */
    @Test
    public void createWithNonStandardEncodingTest()
            throws UnsupportedEncodingException, IllegalAccessException {

        String name = "name";
        byte[] value = "\u00C4-\u00D6-\u00DC".getBytes("UTF-8");
        EventField eventField = new EventField(name, value, Encodings.BASE64);

        String error = "The name is not correct";
        assertThat(error, eventField.getName(), is(equalTo(name)));

        error = "The encoding is not correct";
        assertThat(error, eventField.getEncoding(), is(equalTo(Encodings.BASE64)));

        // Use reflection to check if a defensive copy has been made
        byte[] valueInObject = (byte[]) field_value.get(eventField);
        error = "The method does not create an inbound defensive copy";
        assertThat(error, valueInObject, is(not(sameInstance(value))));

        error = "The value is not correct";
        assertThat(error, eventField.getValue(), is(equalTo(value)));
    }

    /**
     * Create an EventField with a null name
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     */
    @Test(expected = NullPointerException.class)
    public void createWithNullNameTest()
            throws UnsupportedEncodingException {

        byte[] value = "\u00C4-\u00D6-\u00DC".getBytes("UTF-8");
        EventField eventField = new EventField(null, value);
    }

    /**
     * Create an EventField with a null value
     */
    @Test(expected = NullPointerException.class)
    public void createWithNullValueTest() {

        String name = "name";
        EventField eventField = new EventField(name, null);
    }

    /**
     * Create an EventField with an empty name
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     */
    @Test(expected = IllegalArgumentException.class)
    public void createWithEmptyNameValueTest()
            throws UnsupportedEncodingException {

        String name = "";
        byte[] value = "\u00C4-\u00D6-\u00DC".getBytes("UTF-8");
        EventField eventField = new EventField(name, value);
    }

    /**
     * Create an EventField using the copy constructor, and make sure that:
     * <ul>
     * <li>the created event is different from the provided event</li>
     * <li>the name is correct</li>
     * <li>the encoding is correct</li>
     * <li>the constructor makes a defensive copy of the value contained in the first event field</li>
     * <li>the value in both event fields are equal</li>
     * </ul>
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     * @throws IllegalAccessException       when reflection does not work
     */
    @Test
    public void createCopyWithCopyConstructorTest()
            throws UnsupportedEncodingException, IllegalAccessException {

        // create a first event field
        String name = "name";
        byte[] value = "\u00C4-\u00D6-\u00DC".getBytes("UTF-8");
        EventField eventField1 = new EventField(name, value);

        // create a clone with the copy constructor
        EventField eventField2 = new EventField(eventField1);

        String error = "The name is not correct";
        assertThat(error, eventField2.getName(), is(equalTo(name)));

        error = "The encoding is not correct";
        assertThat(error, eventField2.getEncoding(), is(equalTo(Encodings.PLAIN)));

        // Use reflection to check if a defensive copy has been made
        byte[] valueInObject1 = (byte[]) field_value.get(eventField1);
        byte[] valueInObject2 = (byte[]) field_value.get(eventField2);
        error = "The method does not create an inbound defensive copy";
        assertThat(error, valueInObject1, is(not(sameInstance(valueInObject2))));

        error = "The value is not correct";
        assertThat(error, eventField1.getValue(), is(equalTo(eventField2.getValue())));
    }

    /**
     * Create an EventField using the copy constructor with a null field
     */
    @Test(expected = NullPointerException.class)
    public void createCopyWithCopyConstructorAndNullFieldTest()
            throws UnsupportedEncodingException {

        EventField eventField1 = null;
        EventField eventField2 = new EventField(eventField1);
    }


    /**
     * Create an EventField using the copy method, and make sure that:
     * <ul>
     * <li>the created event is different from the provided event</li>
     * <li>the name is correct</li>
     * <li>the encoding is correct</li>
     * <li>the constructor makes a defensive copy of the value contained in the first event field</li>
     * <li>the value in both event fields are equal</li>
     * </ul>
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     * @throws IllegalAccessException       when reflection does not work
     */
    @Test
    public void createCopyWithCopyMethodTest()
            throws UnsupportedEncodingException, IllegalAccessException {

        // create a first event field
        String name = "name";
        byte[] value = "\u00C4-\u00D6-\u00DC".getBytes("UTF-8");
        EventField eventField1 = new EventField(name, value);

        // create a clone with the copy method
        EventField eventField2 = (EventField) eventField1.getCopy();

        String error = "The name is not correct";
        assertThat(error, eventField2.getName(), is(equalTo(name)));

        error = "The encoding is not correct";
        assertThat(error, eventField2.getEncoding(), is(equalTo(Encodings.PLAIN)));

        // Use reflection to check if a defensive copy has been made
        byte[] valueInObject1 = (byte[]) field_value.get(eventField1);
        byte[] valueInObject2 = (byte[]) field_value.get(eventField2);
        error = "The method does not create an inbound defensive copy";
        assertThat(error, valueInObject1, is(not(sameInstance(valueInObject2))));

        error = "The value is not correct";
        assertThat(error, eventField1.getValue(), is(equalTo(eventField2.getValue())));
    }

    /**
     * Set an EventField value with standard encoding, and make sure that:
     * <ul>
     * <li>the encoding is PLAIN</li>
     * <li>the method makes a defensive copy of the value</li>
     * <li>the value is correct</li>
     * </ul>
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     * @throws IllegalAccessException       when reflection does not work
     */
    @Test
    public void updateWithStandardEncodingTest()
            throws UnsupportedEncodingException, IllegalAccessException {

        // create a field
        String name = "name";
        byte[] value = "\u00C4-\u00D6-\u00DC".getBytes("UTF-8");
        EventField eventField = new EventField(name, value);

        // check the value and encoding
        String error = "The value is not correct";
        assertThat(error, eventField.getValue(), is(equalTo(value)));
        error = "The encoding is not correct";
        assertThat(error, eventField.getEncoding(), is(equalTo(Encodings.PLAIN)));

        // update the value
        byte[] newValue = "123-\u00C4-\u00D6-\u00DC".getBytes("UTF-8");
        eventField.setValue(newValue);

        // check the value and encoding of the updated object
        error = "The encoding is not correct";
        assertThat(error, eventField.getEncoding(), is(equalTo(Encodings.PLAIN)));

        // Use reflection to check if a defensive copy has been made
        byte[] valueInObject = (byte[]) field_value.get(eventField);
        error = "The method does not create an inbound defensive copy";
        assertThat(error, valueInObject, is(not(sameInstance(newValue))));

        error = "The value is not correct";
        assertThat(error, eventField.getValue(), is(equalTo(newValue)));
    }

    /**
     * Set an EventField value with a non-standard encoding (BASE64), and make sure that
     * <ul>
     * <li>the encoding is BASE64</li>
     * <li>the method makes a defensive copy of the value</li>
     * <li>the value is correct</li>
     * </ul>
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     * @throws IllegalAccessException       when reflection does not work
     */
    @Test
    public void updateWithNonStandardEncodingTest()
            throws UnsupportedEncodingException, IllegalAccessException {

        // create a field
        String name = "name";
        byte[] value = "\u00C4-\u00D6-\u00DC".getBytes("UTF-8");
        EventField eventField = new EventField(name, value);

        // check the value and encoding
        String error = "The value is not correct";
        assertThat(error, eventField.getValue(), is(equalTo(value)));
        error = "The encoding is not correct";
        assertThat(error, eventField.getEncoding(), is(equalTo(Encodings.PLAIN)));

        // update the value
        byte[] newValue = "123-\u00C4-\u00D6-\u00DC".getBytes("UTF-8");
        eventField.setValue(newValue, Encodings.BASE64);

        // check the value and encoding of the updated object
        error = "The encoding is not correct";
        assertThat(error, eventField.getEncoding(), is(equalTo(Encodings.BASE64)));

        // Use reflection to check if a defensive copy has been made
        byte[] valueInObject = (byte[]) field_value.get(eventField);
        error = "The method does not create an inbound defensive copy";
        assertThat(error, valueInObject, is(not(sameInstance(newValue))));

        error = "The value is not correct";
        assertThat(error, eventField.getValue(), is(equalTo(newValue)));
    }

    /**
     * Get the value of an EventField, and make sure that
     * <ul>
     * <li>the method makes a defensive copy of the value</li>
     * <li>the value is correct</li>
     * </ul>
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     * @throws IllegalAccessException       when reflection does not work
     */
    @Test
    public void getByteValueTest()
            throws UnsupportedEncodingException, IllegalAccessException {

        String name = "name";
        byte[] value = "\u00C4-\u00D6-\u00DC".getBytes("UTF-8");
        EventField eventField = new EventField(name, value);

        // get the value
        byte[] returnedValue = eventField.getValue();

        // Use reflection to check if a defensive copy has been made
        byte[] valueInObject = (byte[]) field_value.get(eventField);
        String error = "The method does not create an inbound defensive copy";
        assertThat(error, valueInObject, is(not(sameInstance(returnedValue))));

        error = "The value is not correct";
        assertThat(error, returnedValue, is(equalTo(value)));
    }

    /**
     * Get the char value of an EventField, and make sure that the value is correct for single-byte String encodings
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     */
    @Test
    public void getSingleByteCharValueTest()
            throws UnsupportedEncodingException {

        String name = "name";
        String value = "\u00C4-\u00D6-\u00DC";
        byte[] byteValue = value.getBytes("ISO8859-1");
        EventField eventField = new EventField(name, byteValue);

        // get the value
        char[] returnedValue = eventField.getCharValue("ISO8859-1");

        String error = "The value is not correct";
        assertThat(error, returnedValue, is(equalTo(value.toCharArray())));
    }

    /**
     * Get the char value of an EventField, and make sure that the value is correct for multi-byte String encodings
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     */
    @Test
    public void getMultiByteCharValueTest()
            throws UnsupportedEncodingException {

        String name = "name";
        String value = "\u00C4-\u00D6-\u00DC";
        byte[] byteValue = value.getBytes("UTF-8");
        EventField eventField = new EventField(name, byteValue);

        // get the value
        char[] returnedValue = eventField.getCharValue("UTF-8");

        String error = "The value is not correct";
        assertThat(error, returnedValue, is(equalTo(value.toCharArray())));
    }

    /**
     * Make sure that retrieving a single-byte encoded char of a multi-byte encoded value fails
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     */
    @Test
    public void getInvalidCharValueTest()
            throws UnsupportedEncodingException {

        String name = "name";
        String value = "\u00C4-\u00D6-\u00DC";
        byte[] byteValue = value.getBytes("UTF-8");
        EventField eventField = new EventField(name, byteValue);

        // get the value
        char[] returnedValue = eventField.getCharValue("ISO8859-1");

        String error = "The value is not correct";
        assertThat(error, returnedValue, is(not(equalTo(value.toCharArray()))));
    }

    /**
     * Clear the value of an EventField, and make sure the value is an array of '0' bytes
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     * @throws IllegalAccessException       when reflection does not work
     */
    @Test
    public void clearValueTest()
            throws UnsupportedEncodingException, IllegalAccessException {

        String name = "name";
        byte[] value = "\u00C4-\u00D6-\u00DC".getBytes("UTF-8");
        EventField eventField = new EventField(name, value);

        // clear the confidential data
        eventField.clear();

        // Use reflection to check if thew array has been reset
        byte[] valueInObject = (byte[]) field_value.get(eventField);
        byte[] expectedValue = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        String error = "The method does not zero the array";
        assertThat(error, valueInObject, is(equalTo(expectedValue)));
    }
}
