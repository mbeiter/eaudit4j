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

import org.beiter.michael.array.Converter;
import org.beiter.michael.eaudit4j.common.CommonProperties;
import org.beiter.michael.eaudit4j.common.Event;
import org.beiter.michael.eaudit4j.common.Field;
import org.beiter.michael.eaudit4j.common.propsbuilder.MapBasedCommonPropsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

public class EventBuilderTest {

    /**
     * The logger object for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(EventBuilderTest.class);
    private static final String CHAR_ENCODING = "UTF-8";

    private java.lang.reflect.Field field_properties;
    private java.lang.reflect.Field field_event;

    /**
     * Make some of the private fields in the EventBuilder class accessible.
     * <p>
     * This is executed before every test to ensure consistency even if one of the tests mock with field accessibility.
     */
    @Before
    public void makeAdditionalPropertiesPrivateFieldsAccessible() {

        // make private fields accessible as needed
        try {
            field_properties = EventBuilder.class.getDeclaredField("properties");
            field_event = EventBuilder.class.getDeclaredField("event");
        } catch (NoSuchFieldException e) {
            AssertionError ae = new AssertionError("An expected private field does not exist");
            ae.initCause(e);
            throw ae;
        }
        field_properties.setAccessible(true);
        field_event.setAccessible(true);
    }

    /**
     * Create an EventBuilder, and make sure that:
     * <ul>
     * <li>the builder contains the no fields</li>
     * <li>the constructor makes a defensive copy of the properties</li>
     * </ul>
     * <p>
     *
     * @throws IllegalAccessException when reflection does not work
     */
    @Test
    public void createEventBuilderTest()
            throws IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        EventBuilder eventBuilder = new EventBuilder(properties);

        // Use reflection to get access to the internal fields
        CommonProperties propertiesInObject = (CommonProperties) field_properties.get(eventBuilder);
        Event eventInObject = (Event) field_event.get(eventBuilder);

        String error = "The number of fields in the builder is not correct";
        assertThat(error, eventInObject.getFieldNames().size(), is(equalTo(0)));

        error = "The method does not create an inbound defensive copy";
        assertThat(error, propertiesInObject, is(not(sameInstance(properties))));
    }

    /**
     * Create an EventBuilder with null properties
     */
    @Test(expected = NullPointerException.class)
    public void createEventBuilderWithNullPropertiesTest() {

        CommonProperties properties = null;
        EventBuilder eventBuilder = new EventBuilder(properties);
    }

    /**
     * Set a first field in an EventBuilder (byte[] value), and make sure that:
     * <ul>
     * <li>the builder contains the provided field name, and the provided field name only</li>
     * <li>the value of the field is correct</li>
     * </ul>
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     * @throws IllegalAccessException       when reflection does not work
     */
    @Test
    public void createOneByteValueFieldTest()
            throws UnsupportedEncodingException, IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        EventBuilder eventBuilder = new EventBuilder(properties);

        String field_1_name = "field_1";
        byte[] field_1_value = "\u00C4-\u00D6-\u00DC field 1".getBytes(CHAR_ENCODING);

        // Use reflection to get access to the internal fields
        Event eventInObject = (Event) field_event.get(eventBuilder);

        eventBuilder.setField(field_1_name, field_1_value);
        String error = "The number of fields in the event is not correct";
        assertThat(error, eventInObject.getFieldNames().size(), is(equalTo(1)));
        error = "An expected field is missing in the event";
        assertThat(error, eventInObject.getFieldNames(), containsInAnyOrder(field_1_name));

        error = "The value is not correct";
        assertThat(error, eventInObject.getField(field_1_name).getValue(), is(equalTo(field_1_value)));
    }

    /**
     * Set an additional field in an EventBuilder (byte[] value), and make sure that:
     * <ul>
     * <li>the builder contains the provided field names, and the provided field names only, including the additional field</li>
     * <li>the value of each field (including the new field) is correct</li>
     * </ul>
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     * @throws IllegalAccessException       when reflection does not work
     */
    @Test
    public void createMultipleByteValueFieldTest()
            throws UnsupportedEncodingException, IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        EventBuilder eventBuilder = new EventBuilder(properties);

        String field_1_name = "field_1";
        byte[] field_1_value = "\u00C4-\u00D6-\u00DC field 1".getBytes(CHAR_ENCODING);
        String field_2_name = "field_2";
        byte[] field_2_value = "\u00C4-\u00D6-\u00DC field 2".getBytes(CHAR_ENCODING);
        String field_3_name = "field_3";
        byte[] field_3_value = "\u00C4-\u00D6-\u00DC field 3".getBytes(CHAR_ENCODING);

        // Use reflection to get access to the internal fields
        Event eventInObject = (Event) field_event.get(eventBuilder);

        eventBuilder.setField(field_1_name, field_1_value);
        String error = "The number of fields in the event is not correct";
        assertThat(error, eventInObject.getFieldNames().size(), is(equalTo(1)));
        error = "An expected field is missing in the event";
        assertThat(error, eventInObject.getFieldNames(), containsInAnyOrder(field_1_name));

        eventBuilder.setField(field_2_name, field_2_value);
        error = "The number of fields in the event is not correct";
        assertThat(error, eventInObject.getFieldNames().size(), is(equalTo(2)));
        error = "An expected field is missing in the event";
        assertThat(error, eventInObject.getFieldNames(), containsInAnyOrder(field_1_name, field_2_name));

        eventBuilder.setField(field_3_name, field_3_value);
        error = "The number of fields in the event is not correct";
        assertThat(error, eventInObject.getFieldNames().size(), is(equalTo(3)));
        error = "An expected field is missing in the event";
        assertThat(error, eventInObject.getFieldNames(), containsInAnyOrder(field_1_name, field_2_name, field_3_name));

        error = "The value is not correct";
        assertThat(error, eventInObject.getField(field_1_name).getValue(), is(equalTo(field_1_value)));
        assertThat(error, eventInObject.getField(field_2_name).getValue(), is(equalTo(field_2_value)));
        assertThat(error, eventInObject.getField(field_3_name).getValue(), is(equalTo(field_3_value)));
    }

    /**
     * Set (update) an existing field in an EventBuilder (byte[] value), and make sure that:
     * <ul>
     * <li>the builder contains the provided field names, and the provided field names only, including the updated field</li>
     * <li>the method makes a defensive copy of the fields</li>
     * <li>the value of each field (including the updated field) is correct</li>
     * </ul>
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     * @throws IllegalAccessException       when reflection does not work
     */
    @Test
    public void updateMultipleByteValueFieldTest()
            throws UnsupportedEncodingException, IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        EventBuilder eventBuilder = new EventBuilder(properties);

        String field_1_name = "field_1";
        byte[] field_1_value = "\u00C4-\u00D6-\u00DC field 1".getBytes(CHAR_ENCODING);

        // Use reflection to get access to the internal fields
        Event eventInObject = (Event) field_event.get(eventBuilder);

        eventBuilder.setField(field_1_name, field_1_value);
        String error = "The number of fields in the event is not correct";
        assertThat(error, eventInObject.getFieldNames().size(), is(equalTo(1)));
        error = "An expected field is missing in the event";
        assertThat(error, eventInObject.getFieldNames(), containsInAnyOrder(field_1_name));

        error = "The value is not correct";
        assertThat(error, eventInObject.getField(field_1_name).getValue(), is(equalTo(field_1_value)));

        field_1_value = "\u00C4-\u00D6-\u00DC field 1 updated".getBytes(CHAR_ENCODING);
        eventBuilder.setField(field_1_name, field_1_value);
        error = "The number of fields in the event is not correct";
        assertThat(error, eventInObject.getFieldNames().size(), is(equalTo(1)));
        error = "An expected field is missing in the event";
        assertThat(error, eventInObject.getFieldNames(), containsInAnyOrder(field_1_name));

        error = "The value is not correct";
        assertThat(error, eventInObject.getField(field_1_name).getValue(), is(equalTo(field_1_value)));
    }


    /**
     * Set a null value in an EventBuilder (byte[] value)
     */
    @Test(expected = NullPointerException.class)
    public void setNullByteValueFieldTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        EventBuilder eventBuilder = new EventBuilder(properties);

        String field_1_name = "field_1";
        byte[] field_1_value = null;

        eventBuilder.setField(field_1_name, field_1_value);
    }


    /**
     * Set a null field name in an EventBuilder (byte[] value)
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     */
    @Test(expected = NullPointerException.class)
    public void setNullNameByteValueFieldTest()
            throws UnsupportedEncodingException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        EventBuilder eventBuilder = new EventBuilder(properties);

        String field_1_name = null;
        byte[] field_1_value = "\u00C4-\u00D6-\u00DC field 1 updated".getBytes(CHAR_ENCODING);

        eventBuilder.setField(field_1_name, field_1_value);
    }


    /**
     * Set an empty field name in an EventBuilder (byte[] value)
     */
    @Test(expected = IllegalArgumentException.class)
    public void setBlankNameByteValueFieldTest()
            throws UnsupportedEncodingException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        EventBuilder eventBuilder = new EventBuilder(properties);

        String field_1_name = "";
        byte[] field_1_value = "\u00C4-\u00D6-\u00DC field 1 updated".getBytes(CHAR_ENCODING);

        eventBuilder.setField(field_1_name, field_1_value);
    }

    /**
     * Set a first field in an EventBuilder (char[] value), and make sure that:
     * <ul>
     * <li>the builder contains the provided field name, and the provided field name only</li>
     * <li>the method makes a defensive copy of the fields</li>
     * <li>the value of the field is correct</li>
     * </ul>
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     * @throws IllegalAccessException       when reflection does not work
     */
    @Test
    public void createOneCharValueFieldTest()
            throws UnsupportedEncodingException, IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        properties.setEncoding(CHAR_ENCODING);
        EventBuilder eventBuilder = new EventBuilder(properties);

        String field_1_name = "field_1";
        char[] field_1_value = "\u00C4-\u00D6-\u00DC field 1".toCharArray();

        // Use reflection to get access to the internal fields
        Event eventInObject = (Event) field_event.get(eventBuilder);

        eventBuilder.setField(field_1_name, field_1_value);
        String error = "The number of fields in the event is not correct";
        assertThat(error, eventInObject.getFieldNames().size(), is(equalTo(1)));
        error = "An expected field is missing in the event";
        assertThat(error, eventInObject.getFieldNames(), containsInAnyOrder(field_1_name));

        error = "The value is not correct";

        assertThat(error, eventInObject.getField(field_1_name).getValue(), is(equalTo(Converter.toBytes(field_1_value, CHAR_ENCODING))));
    }

    /**
     * Set an additional field in an EventBuilder (char[] value), and make sure that:
     * <ul>
     * <li>the builder contains the provided field names, and the provided field names only, including the additional field</li>
     * <li>the method makes a defensive copy of the fields</li>
     * <li>the value of each field (including the new field) is correct</li>
     * </ul>
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     * @throws IllegalAccessException       when reflection does not work
     */
    @Test
    public void createMultipleCharValueFieldTest()
            throws UnsupportedEncodingException, IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        properties.setEncoding(CHAR_ENCODING);
        EventBuilder eventBuilder = new EventBuilder(properties);

        String field_1_name = "field_1";
        char[] field_1_value = "\u00C4-\u00D6-\u00DC field 1".toCharArray();
        String field_2_name = "field_2";
        char[] field_2_value = "\u00C4-\u00D6-\u00DC field 2".toCharArray();
        String field_3_name = "field_3";
        char[] field_3_value = "\u00C4-\u00D6-\u00DC field 3".toCharArray();

        // Use reflection to get access to the internal fields
        Event eventInObject = (Event) field_event.get(eventBuilder);

        eventBuilder.setField(field_1_name, field_1_value);
        String error = "The number of fields in the event is not correct";
        assertThat(error, eventInObject.getFieldNames().size(), is(equalTo(1)));
        error = "An expected field is missing in the event";
        assertThat(error, eventInObject.getFieldNames(), containsInAnyOrder(field_1_name));

        eventBuilder.setField(field_2_name, field_2_value);
        error = "The number of fields in the event is not correct";
        assertThat(error, eventInObject.getFieldNames().size(), is(equalTo(2)));
        error = "An expected field is missing in the event";
        assertThat(error, eventInObject.getFieldNames(), containsInAnyOrder(field_1_name, field_2_name));

        eventBuilder.setField(field_3_name, field_3_value);
        error = "The number of fields in the event is not correct";
        assertThat(error, eventInObject.getFieldNames().size(), is(equalTo(3)));
        error = "An expected field is missing in the event";
        assertThat(error, eventInObject.getFieldNames(), containsInAnyOrder(field_1_name, field_2_name, field_3_name));

        error = "The value is not correct";
        assertThat(error, eventInObject.getField(field_1_name).getValue(), is(equalTo(Converter.toBytes(field_1_value, CHAR_ENCODING))));
        assertThat(error, eventInObject.getField(field_2_name).getValue(), is(equalTo(Converter.toBytes(field_2_value, CHAR_ENCODING))));
        assertThat(error, eventInObject.getField(field_3_name).getValue(), is(equalTo(Converter.toBytes(field_3_value, CHAR_ENCODING))));
    }

    /**
     * Set (update) an existing field in an EventBuilder (char[] value), and make sure that:
     * <ul>
     * <li>the builder contains the provided field names, and the provided field names only, including the updated field</li>
     * <li>the method makes a defensive copy of the fields</li>
     * <li>the value of each field (including the updated field) is correct</li>
     * </ul>
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     * @throws IllegalAccessException       when reflection does not work
     */
    @Test
    public void updateMultipleCharValueFieldTest()
            throws UnsupportedEncodingException, IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        properties.setEncoding(CHAR_ENCODING);
        EventBuilder eventBuilder = new EventBuilder(properties);

        String field_1_name = "field_1";
        char[] field_1_value = "\u00C4-\u00D6-\u00DC field 1".toCharArray();

        // Use reflection to get access to the internal fields
        Event eventInObject = (Event) field_event.get(eventBuilder);

        eventBuilder.setField(field_1_name, field_1_value);
        String error = "The number of fields in the event is not correct";
        assertThat(error, eventInObject.getFieldNames().size(), is(equalTo(1)));
        error = "An expected field is missing in the event";
        assertThat(error, eventInObject.getFieldNames(), containsInAnyOrder(field_1_name));

        error = "The value is not correct";
        assertThat(error, eventInObject.getField(field_1_name).getValue(), is(equalTo(Converter.toBytes(field_1_value, CHAR_ENCODING))));

        field_1_value = "\u00C4-\u00D6-\u00DC field 1 updated".toCharArray();
        eventBuilder.setField(field_1_name, field_1_value);
        error = "The number of fields in the event is not correct";
        assertThat(error, eventInObject.getFieldNames().size(), is(equalTo(1)));
        error = "An expected field is missing in the event";
        assertThat(error, eventInObject.getFieldNames(), containsInAnyOrder(field_1_name));

        error = "The value is not correct";
        assertThat(error, eventInObject.getField(field_1_name).getValue(), is(equalTo(Converter.toBytes(field_1_value, CHAR_ENCODING))));
    }


    /**
     * Set a null value in an EventBuilder (char[] value)
     */
    @Test(expected = NullPointerException.class)
    public void setNullCharValueFieldTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        EventBuilder eventBuilder = new EventBuilder(properties);

        String field_1_name = "field_1";
        char[] field_1_value = null;

        eventBuilder.setField(field_1_name, field_1_value);
    }


    /**
     * Set a null field name in an EventBuilder (char[] value)
     */
    @Test(expected = NullPointerException.class)
    public void setNullNameCharValueFieldTest()
            throws UnsupportedEncodingException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        EventBuilder eventBuilder = new EventBuilder(properties);

        String field_1_name = null;
        char[] field_1_value = "\u00C4-\u00D6-\u00DC field 1 updated".toCharArray();

        eventBuilder.setField(field_1_name, field_1_value);
    }

    /**
     * Set an empty field name in an EventBuilder (char[] value)
     */
    @Test(expected = IllegalArgumentException.class)
    public void setBlankNameCharValueFieldTest()
            throws UnsupportedEncodingException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        EventBuilder eventBuilder = new EventBuilder(properties);

        String field_1_name = "";
        char[] field_1_value = "\u00C4-\u00D6-\u00DC field 1 updated".toCharArray();

        eventBuilder.setField(field_1_name, field_1_value);
    }

    /**
     * Set a first named field in an EventBuilder (byte[] value), and make sure that:
     * <ul>
     * <li>the builder contains the provided field name, and the provided field name only</li>
     * <li>the method makes a defensive copy of the fields</li>
     * <li>the value of the field is correct</li>
     * </ul>
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     * @throws IllegalAccessException       when reflection does not work
     */
    @Test
    public void createOneByteValueNamedFieldTest()
            throws UnsupportedEncodingException, IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        EventBuilder eventBuilder = new EventBuilder(properties);

        String field_1_name = properties.getFieldNameActor();
        byte[] field_1_value = "\u00C4-\u00D6-\u00DC field 1".getBytes(CHAR_ENCODING);

        // Use reflection to get access to the internal fields
        Event eventInObject = (Event) field_event.get(eventBuilder);

        eventBuilder.setField(field_1_name, field_1_value);
        String error = "The number of fields in the event is not correct";
        assertThat(error, eventInObject.getFieldNames().size(), is(equalTo(1)));
        error = "An expected field is missing in the event";
        assertThat(error, eventInObject.getFieldNames(), containsInAnyOrder(field_1_name));

        error = "The value is not correct";
        assertThat(error, eventInObject.getField(field_1_name).getValue(), is(equalTo(field_1_value)));
    }

    /**
     * Set an additional named field in an EventBuilder (byte[] value), and make sure that:
     * <ul>
     * <li>the builder contains the provided field names, and the provided field names only, including the additional field</li>
     * <li>the method makes a defensive copy of the fields</li>
     * <li>the value of each field (including the new field) is correct</li>
     * </ul>
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     * @throws IllegalAccessException       when reflection does not work
     */
    @Test
    public void createMultipleByteValueNamedFieldTest()
            throws UnsupportedEncodingException, IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        EventBuilder eventBuilder = new EventBuilder(properties);

        String field_1_name = properties.getFieldNameActor();
        byte[] field_1_value = "\u00C4-\u00D6-\u00DC field 1".getBytes(CHAR_ENCODING);
        String field_2_name = properties.getFieldNameContentAfterOperation();
        byte[] field_2_value = "\u00C4-\u00D6-\u00DC field 2".getBytes(CHAR_ENCODING);
        String field_3_name = properties.getFieldNameContentBeforeOperation();
        byte[] field_3_value = "\u00C4-\u00D6-\u00DC field 3".getBytes(CHAR_ENCODING);

        // Use reflection to get access to the internal fields
        Event eventInObject = (Event) field_event.get(eventBuilder);

        eventBuilder.setField(field_1_name, field_1_value);
        String error = "The number of fields in the event is not correct";
        assertThat(error, eventInObject.getFieldNames().size(), is(equalTo(1)));
        error = "An expected field is missing in the event";
        assertThat(error, eventInObject.getFieldNames(), containsInAnyOrder(field_1_name));

        eventBuilder.setField(field_2_name, field_2_value);
        error = "The number of fields in the event is not correct";
        assertThat(error, eventInObject.getFieldNames().size(), is(equalTo(2)));
        error = "An expected field is missing in the event";
        assertThat(error, eventInObject.getFieldNames(), containsInAnyOrder(field_1_name, field_2_name));

        eventBuilder.setField(field_3_name, field_3_value);
        error = "The number of fields in the event is not correct";
        assertThat(error, eventInObject.getFieldNames().size(), is(equalTo(3)));
        error = "An expected field is missing in the event";
        assertThat(error, eventInObject.getFieldNames(), containsInAnyOrder(field_1_name, field_2_name, field_3_name));

        error = "The value is not correct";
        assertThat(error, eventInObject.getField(field_1_name).getValue(), is(equalTo(field_1_value)));
        assertThat(error, eventInObject.getField(field_2_name).getValue(), is(equalTo(field_2_value)));
        assertThat(error, eventInObject.getField(field_3_name).getValue(), is(equalTo(field_3_value)));
    }

    /**
     * Set (update) an existing named field in an EventBuilder (byte[] value), and make sure that:
     * <ul>
     * <li>the builder contains the provided field names, and the provided field names only, including the updated field</li>
     * <li>the method makes a defensive copy of the fields</li>
     * <li>the value of each field (including the updated field) is correct</li>
     * </ul>
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     * @throws IllegalAccessException       when reflection does not work
     */
    @Test
    public void updateMultipleByteValueNamedFieldTest()
            throws UnsupportedEncodingException, IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        EventBuilder eventBuilder = new EventBuilder(properties);

        String field_1_name = properties.getFieldNameActor();
        byte[] field_1_value = "\u00C4-\u00D6-\u00DC field 1".getBytes(CHAR_ENCODING);

        // Use reflection to get access to the internal fields
        Event eventInObject = (Event) field_event.get(eventBuilder);

        eventBuilder.setField(field_1_name, field_1_value);
        String error = "The number of fields in the event is not correct";
        assertThat(error, eventInObject.getFieldNames().size(), is(equalTo(1)));
        error = "An expected field is missing in the event";
        assertThat(error, eventInObject.getFieldNames(), containsInAnyOrder(field_1_name));

        error = "The value is not correct";
        assertThat(error, eventInObject.getField(field_1_name).getValue(), is(equalTo(field_1_value)));

        field_1_value = "\u00C4-\u00D6-\u00DC field 1 updated".getBytes(CHAR_ENCODING);
        eventBuilder.setField(field_1_name, field_1_value);
        error = "The number of fields in the event is not correct";
        assertThat(error, eventInObject.getFieldNames().size(), is(equalTo(1)));
        error = "An expected field is missing in the event";
        assertThat(error, eventInObject.getFieldNames(), containsInAnyOrder(field_1_name));

        error = "The value is not correct";
        assertThat(error, eventInObject.getField(field_1_name).getValue(), is(equalTo(field_1_value)));
    }

    /**
     * Set a null value for a named field in an EventBuilder (byte[] value)
     */
    @Test(expected = NullPointerException.class)
    public void setNullByteValueNamedFieldTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        EventBuilder eventBuilder = new EventBuilder(properties);

        String field_1_name = properties.getFieldNameActor();
        byte[] field_1_value = null;

        eventBuilder.setField(field_1_name, field_1_value);
    }

    /**
     * Set a null field name for a named field in an EventBuilder (byte[] value)
     */
    @Test(expected = NullPointerException.class)
    public void setNullNameByteValueNamedFieldTest()
            throws UnsupportedEncodingException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        EventBuilder eventBuilder = new EventBuilder(properties);

        String field_1_name = null;
        byte[] field_1_value = "\u00C4-\u00D6-\u00DC field 1 updated".getBytes(CHAR_ENCODING);

        eventBuilder.setField(field_1_name, field_1_value);
    }

    /**
     * Set a first named field in an EventBuilder (char[] value), and make sure that:
     * <ul>
     * <li>the builder contains the provided field name, and the provided field name only</li>
     * <li>the method makes a defensive copy of the fields</li>
     * <li>the value of the field is correct</li>
     * </ul>
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     * @throws IllegalAccessException       when reflection does not work
     */
    @Test
    public void createOneCharValueNamedFieldTest()
            throws UnsupportedEncodingException, IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        properties.setEncoding(CHAR_ENCODING);
        EventBuilder eventBuilder = new EventBuilder(properties);

        String field_1_name = properties.getFieldNameActor();
        char[] field_1_value = "\u00C4-\u00D6-\u00DC field 1".toCharArray();

        // Use reflection to get access to the internal fields
        Event eventInObject = (Event) field_event.get(eventBuilder);

        eventBuilder.setField(field_1_name, field_1_value);
        String error = "The number of fields in the event is not correct";
        assertThat(error, eventInObject.getFieldNames().size(), is(equalTo(1)));
        error = "An expected field is missing in the event";
        assertThat(error, eventInObject.getFieldNames(), containsInAnyOrder(field_1_name));

        error = "The value is not correct";

        assertThat(error, eventInObject.getField(field_1_name).getValue(), is(equalTo(Converter.toBytes(field_1_value, CHAR_ENCODING))));
    }

    /**
     * Set an additional named field in an EventBuilder (char[] value), and make sure that:
     * <ul>
     * <li>the builder contains the provided field names, and the provided field names only, including the additional field</li>
     * <li>the method makes a defensive copy of the fields</li>
     * <li>the value of each field (including the new field) is correct</li>
     * </ul>
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     * @throws IllegalAccessException       when reflection does not work
     */
    @Test
    public void createMultipleCharValueNamedFieldTest()
            throws UnsupportedEncodingException, IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        properties.setEncoding(CHAR_ENCODING);
        EventBuilder eventBuilder = new EventBuilder(properties);

        String field_1_name = properties.getFieldNameActor();
        char[] field_1_value = "\u00C4-\u00D6-\u00DC field 1".toCharArray();
        String field_2_name = properties.getFieldNameContentAfterOperation();
        char[] field_2_value = "\u00C4-\u00D6-\u00DC field 2".toCharArray();
        String field_3_name = properties.getFieldNameContentBeforeOperation();
        char[] field_3_value = "\u00C4-\u00D6-\u00DC field 3".toCharArray();

        // Use reflection to get access to the internal fields
        Event eventInObject = (Event) field_event.get(eventBuilder);

        eventBuilder.setField(field_1_name, field_1_value);
        String error = "The number of fields in the event is not correct";
        assertThat(error, eventInObject.getFieldNames().size(), is(equalTo(1)));
        error = "An expected field is missing in the event";
        assertThat(error, eventInObject.getFieldNames(), containsInAnyOrder(field_1_name));

        eventBuilder.setField(field_2_name, field_2_value);
        error = "The number of fields in the event is not correct";
        assertThat(error, eventInObject.getFieldNames().size(), is(equalTo(2)));
        error = "An expected field is missing in the event";
        assertThat(error, eventInObject.getFieldNames(), containsInAnyOrder(field_1_name, field_2_name));

        eventBuilder.setField(field_3_name, field_3_value);
        error = "The number of fields in the event is not correct";
        assertThat(error, eventInObject.getFieldNames().size(), is(equalTo(3)));
        error = "An expected field is missing in the event";
        assertThat(error, eventInObject.getFieldNames(), containsInAnyOrder(field_1_name, field_2_name, field_3_name));

        error = "The value is not correct";
        assertThat(error, eventInObject.getField(field_1_name).getValue(), is(equalTo(Converter.toBytes(field_1_value, CHAR_ENCODING))));
        assertThat(error, eventInObject.getField(field_2_name).getValue(), is(equalTo(Converter.toBytes(field_2_value, CHAR_ENCODING))));
        assertThat(error, eventInObject.getField(field_3_name).getValue(), is(equalTo(Converter.toBytes(field_3_value, CHAR_ENCODING))));
    }

    /**
     * Set (update) an existing named field in an EventBuilder (char[] value), and make sure that:
     * <ul>
     * <li>the builder contains the provided field names, and the provided field names only, including the updated field</li>
     * <li>the method makes a defensive copy of the fields</li>
     * <li>the value of each field (including the updated field) is correct</li>
     * </ul>
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     * @throws IllegalAccessException       when reflection does not work
     */
    @Test
    public void updateMultipleCharValueNamedFieldTest()
            throws UnsupportedEncodingException, IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        properties.setEncoding(CHAR_ENCODING);
        EventBuilder eventBuilder = new EventBuilder(properties);

        String field_1_name = properties.getFieldNameActor();
        char[] field_1_value = "\u00C4-\u00D6-\u00DC field 1".toCharArray();

        // Use reflection to get access to the internal fields
        Event eventInObject = (Event) field_event.get(eventBuilder);

        eventBuilder.setField(field_1_name, field_1_value);
        String error = "The number of fields in the event is not correct";
        assertThat(error, eventInObject.getFieldNames().size(), is(equalTo(1)));
        error = "An expected field is missing in the event";
        assertThat(error, eventInObject.getFieldNames(), containsInAnyOrder(field_1_name));

        error = "The value is not correct";
        assertThat(error, eventInObject.getField(field_1_name).getValue(), is(equalTo(Converter.toBytes(field_1_value, CHAR_ENCODING))));

        field_1_value = "\u00C4-\u00D6-\u00DC field 1 updated".toCharArray();
        eventBuilder.setField(field_1_name, field_1_value);
        error = "The number of fields in the event is not correct";
        assertThat(error, eventInObject.getFieldNames().size(), is(equalTo(1)));
        error = "An expected field is missing in the event";
        assertThat(error, eventInObject.getFieldNames(), containsInAnyOrder(field_1_name));

        error = "The value is not correct";
        assertThat(error, eventInObject.getField(field_1_name).getValue(), is(equalTo(Converter.toBytes(field_1_value, CHAR_ENCODING))));
    }

    /**
     * Set a null value for a named field in an EventBuilder (char[] value)
     */
    @Test(expected = NullPointerException.class)
    public void setNullCharValueNamedFieldTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        EventBuilder eventBuilder = new EventBuilder(properties);

        String field_1_name = properties.getFieldNameActor();
        char[] field_1_value = null;

        eventBuilder.setField(field_1_name, field_1_value);
    }

    /**
     * Set a null field name for a named field in an EventBuilder (char[] value)
     */
    @Test(expected = NullPointerException.class)
    public void setNullNameCharValueNamedFieldTest()
            throws UnsupportedEncodingException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        EventBuilder eventBuilder = new EventBuilder(properties);

        String field_1_name = null;
        char[] field_1_value = "\u00C4-\u00D6-\u00DC field 1 updated".toCharArray();

        eventBuilder.setField(field_1_name, field_1_value);
    }

    /**
     * Set a first Field (type) in an EventBuilder, and make sure that:
     * <ul>
     * <li>the builder contains the provided field name, and the provided field name only</li>
     * <li>the method makes a defensive copy of the fields</li>
     * <li>the value of the field is correct</li>
     * </ul>
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     * @throws IllegalAccessException       when reflection does not work
     */
    @Test
    public void createOneCharValueFieldTypeTest()
            throws UnsupportedEncodingException, IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        EventBuilder eventBuilder = new EventBuilder(properties);

        Field field_1 = new EventField("field_1", "field_1_value".getBytes(CHAR_ENCODING));

        // Use reflection to get access to the internal fields
        Event eventInObject = (Event) field_event.get(eventBuilder);

        eventBuilder.setField(field_1);
        String error = "The number of fields in the event is not correct";
        assertThat(error, eventInObject.getFieldNames().size(), is(equalTo(1)));
        error = "An expected field is missing in the event";
        assertThat(error, eventInObject.getFieldNames(), containsInAnyOrder(field_1.getName()));

        error = "The value is not correct";

        assertThat(error, eventInObject.getField(field_1.getName()).getValue(), is(equalTo(field_1.getValue())));
    }

    /**
     * Set an additional Field (type) in an EventBuilder, and make sure that:
     * <ul>
     * <li>the builder contains the provided field names, and the provided field names only, including the additional field</li>
     * <li>the method makes a defensive copy of the fields</li>
     * <li>the value of each field (including the new field) is correct</li>
     * </ul>
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     * @throws IllegalAccessException       when reflection does not work
     */
    @Test
    public void createMultipleCharValueFieldTypeTest()
            throws UnsupportedEncodingException, IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        EventBuilder eventBuilder = new EventBuilder(properties);

        Field field_1 = new EventField("field_1", "field_1_value".getBytes(CHAR_ENCODING));
        Field field_2 = new EventField("field_2", "field_2_value".getBytes(CHAR_ENCODING));
        Field field_3 = new EventField("field_3", "field_3_value".getBytes(CHAR_ENCODING));

        // Use reflection to get access to the internal fields
        Event eventInObject = (Event) field_event.get(eventBuilder);

        eventBuilder.setField(field_1);
        String error = "The number of fields in the event is not correct";
        assertThat(error, eventInObject.getFieldNames().size(), is(equalTo(1)));
        error = "An expected field is missing in the event";
        assertThat(error, eventInObject.getFieldNames(), containsInAnyOrder(field_1.getName()));

        eventBuilder.setField(field_2);
        error = "The number of fields in the event is not correct";
        assertThat(error, eventInObject.getFieldNames().size(), is(equalTo(2)));
        error = "An expected field is missing in the event";
        assertThat(error, eventInObject.getFieldNames(), containsInAnyOrder(field_1.getName(), field_2.getName()));

        eventBuilder.setField(field_3);
        error = "The number of fields in the event is not correct";
        assertThat(error, eventInObject.getFieldNames().size(), is(equalTo(3)));
        error = "An expected field is missing in the event";
        assertThat(error, eventInObject.getFieldNames(), containsInAnyOrder(field_1.getName(), field_2.getName(), field_3.getName()));

        error = "The value is not correct";
        assertThat(error, eventInObject.getField(field_1.getName()).getValue(), is(equalTo(field_1.getValue())));
        assertThat(error, eventInObject.getField(field_2.getName()).getValue(), is(equalTo(field_2.getValue())));
        assertThat(error, eventInObject.getField(field_3.getName()).getValue(), is(equalTo(field_3.getValue())));
    }

    /**
     * Set (update) an existing Field (type) in an EventBuilder, and make sure that:
     * <ul>
     * <li>the builder contains the provided field names, and the provided field names only, including the updated field</li>
     * <li>the method makes a defensive copy of the fields</li>
     * <li>the value of each field (including the updated field) is correct</li>
     * </ul>
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     * @throws IllegalAccessException       when reflection does not work
     */
    @Test
    public void updateMultipleCharValueFieldTypeTest()
            throws UnsupportedEncodingException, IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        EventBuilder eventBuilder = new EventBuilder(properties);

        Field field_1 = new EventField("field_1", "field_1_value".getBytes(CHAR_ENCODING));

        // Use reflection to get access to the internal fields
        Event eventInObject = (Event) field_event.get(eventBuilder);

        eventBuilder.setField(field_1);
        String error = "The number of fields in the event is not correct";
        assertThat(error, eventInObject.getFieldNames().size(), is(equalTo(1)));
        error = "An expected field is missing in the event";
        assertThat(error, eventInObject.getFieldNames(), containsInAnyOrder(field_1.getName()));

        error = "The value is not correct";
        assertThat(error, eventInObject.getField(field_1.getName()).getValue(), is(equalTo(field_1.getValue())));

        field_1 = new EventField("field_1", "field_1_value updated".getBytes(CHAR_ENCODING));
        eventBuilder.setField(field_1);
        error = "The number of fields in the event is not correct";
        assertThat(error, eventInObject.getFieldNames().size(), is(equalTo(1)));
        error = "An expected field is missing in the event";
        assertThat(error, eventInObject.getFieldNames(), containsInAnyOrder(field_1.getName()));

        error = "The value is not correct";
        assertThat(error, eventInObject.getField(field_1.getName()).getValue(), is(equalTo(field_1.getValue())));
    }

    /**
     * Set a null value for a Field (type) in an EventBuilder
     */
    @Test(expected = NullPointerException.class)
    public void setNullCharValueFieldTypeTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        EventBuilder eventBuilder = new EventBuilder(properties);

        Field field_1 = null;

        eventBuilder.setField(field_1);
    }

    /**
     * Set the "Event Type" field in an ExtendedAuditEvent, and make sure that the field name is set correctly as
     * configured.
     *
     * @throws IllegalAccessException when reflection does not work
     */
    @Test
    public void setEventTypeFieldTest()
            throws IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();

        EventBuilder eventBuilder1 = new EventBuilder(properties);
        eventBuilder1.setEventType("value".toCharArray());

        // Use reflection to get access to the internal fields
        Event eventInObject = (Event) field_event.get(eventBuilder1);
        String error = "The name has not been set correctly";
        assertThat(error, eventInObject.containsField(properties.getFieldNameEventType()), is(true));
    }

    /**
     * Set the "Event Type" field in an ExtendedAuditEvent with a null value
     */
    @Test(expected = NullPointerException.class)
    public void setEventTypeFieldNullValueTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        EventBuilder eventBuilder = new EventBuilder(properties);

        eventBuilder.setEventType(null);
    }

    /**
     * Set the "Event Group Type" field in an ExtendedAuditEvent, and make sure that the field name is set correctly as
     * configured.
     *
     * @throws IllegalAccessException when reflection does not work
     */
    @Test
    public void setEventGroupTypeFieldTest()
            throws IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();

        EventBuilder eventBuilder1 = new EventBuilder(properties);
        eventBuilder1.setEventGroupType("value".toCharArray());

        // Use reflection to get access to the internal fields
        Event eventInObject = (Event) field_event.get(eventBuilder1);
        String error = "The name has not been set correctly";
        assertThat(error, eventInObject.containsField(properties.getFieldNameEventGroupType()), is(true));
    }

    /**
     * Set the "Event Group Type" field in an ExtendedAuditEvent with a null value
     */
    @Test(expected = NullPointerException.class)
    public void setEventGroupTypeFieldNullValueTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        EventBuilder eventBuilder = new EventBuilder(properties);

        eventBuilder.setEventGroupType(null);
    }

    /**
     * Set the "Subject" field in an ExtendedAuditEvent, and make sure that the field name is set correctly as
     * configured.
     *
     * @throws IllegalAccessException when reflection does not work
     */
    @Test
    public void seSubjectFieldTest()
            throws IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();

        EventBuilder eventBuilder1 = new EventBuilder(properties);
        eventBuilder1.setSubject("value".toCharArray());

        // Use reflection to get access to the internal fields
        Event eventInObject = (Event) field_event.get(eventBuilder1);
        String error = "The name has not been set correctly";
        assertThat(error, eventInObject.containsField(properties.getFieldNameSubject()), is(true));
    }


    /**
     * Set the "Subject" field in an ExtendedAuditEvent with a null value
     */
    @Test(expected = NullPointerException.class)
    public void setSubjectNullValueTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        EventBuilder eventBuilder = new EventBuilder(properties);

        eventBuilder.setSubject(null);
    }

    /**
     * Set the "Subject Location" field in an ExtendedAuditEvent, and make sure that the field name is set correctly as
     * configured.
     *
     * @throws IllegalAccessException when reflection does not work
     */
    @Test
    public void seSubjectLocationFieldTest()
            throws IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();

        EventBuilder eventBuilder1 = new EventBuilder(properties);
        eventBuilder1.setSubjectLocation("value".toCharArray());

        // Use reflection to get access to the internal fields
        Event eventInObject = (Event) field_event.get(eventBuilder1);
        String error = "The name has not been set correctly";
        assertThat(error, eventInObject.containsField(properties.getFieldNameSubjectLocation()), is(true));
    }

    /**
     * Set the "Subject Location" field in an ExtendedAuditEvent with a null value
     */
    @Test(expected = NullPointerException.class)
    public void setSubjectLocationNullValueTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        EventBuilder eventBuilder = new EventBuilder(properties);

        eventBuilder.setSubjectLocation(null);
    }

    /**
     * Set the "Actor" field in an ExtendedAuditEvent, and make sure that the field name is set correctly as
     * configured.
     *
     * @throws IllegalAccessException when reflection does not work
     */
    @Test
    public void seActorFieldTest()
            throws IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();

        EventBuilder eventBuilder1 = new EventBuilder(properties);
        eventBuilder1.setActor("value".toCharArray());

        // Use reflection to get access to the internal fields
        Event eventInObject = (Event) field_event.get(eventBuilder1);
        String error = "The name has not been set correctly";
        assertThat(error, eventInObject.containsField(properties.getFieldNameActor()), is(true));
    }

    /**
     * Set the "Actor" field in an ExtendedAuditEvent with a null value
     */
    @Test(expected = NullPointerException.class)
    public void setActorNullValueTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        EventBuilder eventBuilder = new EventBuilder(properties);

        eventBuilder.setActor(null);
    }

    /**
     * Set the "Object" field in an ExtendedAuditEvent, and make sure that the field name is set correctly as
     * configured.
     *
     * @throws IllegalAccessException when reflection does not work
     */
    @Test
    public void setObjectFieldTest()
            throws IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();

        EventBuilder eventBuilder1 = new EventBuilder(properties);
        eventBuilder1.setObject("value".toCharArray());

        // Use reflection to get access to the internal fields
        Event eventInObject = (Event) field_event.get(eventBuilder1);
        String error = "The name has not been set correctly";
        assertThat(error, eventInObject.containsField(properties.getFieldNameObject()), is(true));
    }

    /**
     * Set the "Object" field in an ExtendedAuditEvent with a null value
     */
    @Test(expected = NullPointerException.class)
    public void setObjectNullValueTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        EventBuilder eventBuilder = new EventBuilder(properties);

        eventBuilder.setObject(null);
    }

    /**
     * Set the "Object Location" field in an ExtendedAuditEvent, and make sure that the field name is set correctly as
     * configured.
     *
     * @throws IllegalAccessException when reflection does not work
     */
    @Test
    public void setObjectLocationFieldTest()
            throws IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();

        EventBuilder eventBuilder1 = new EventBuilder(properties);
        eventBuilder1.setObjectLocation("value".toCharArray());

        // Use reflection to get access to the internal fields
        Event eventInObject = (Event) field_event.get(eventBuilder1);
        String error = "The name has not been set correctly";
        assertThat(error, eventInObject.containsField(properties.getFieldNameObjectLocation()), is(true));
    }

    /**
     * Set the "Object Location" field in an ExtendedAuditEvent with a null value
     */
    @Test(expected = NullPointerException.class)
    public void setObjectLocationNullValueTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        EventBuilder eventBuilder = new EventBuilder(properties);

        eventBuilder.setObject(null);
    }

    /**
     * Set the "Content Before Operation" field in an ExtendedAuditEvent, and make sure that the field name is set
     * correctly as configured.
     *
     * @throws IllegalAccessException when reflection does not work
     */
    @Test
    public void setContentBeforeModificationFieldTest()
            throws IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();

        EventBuilder eventBuilder1 = new EventBuilder(properties);
        eventBuilder1.setContentBeforeOperation("value".toCharArray());

        // Use reflection to get access to the internal fields
        Event eventInObject = (Event) field_event.get(eventBuilder1);
        String error = "The name has not been set correctly";
        assertThat(error, eventInObject.containsField(properties.getFieldNameContentBeforeOperation()), is(true));
    }

    /**
     * Set the "Content Before Operation" field in an ExtendedAuditEvent with a null value
     */
    @Test(expected = NullPointerException.class)
    public void setContentBeforeModificationNullValueTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        EventBuilder eventBuilder = new EventBuilder(properties);

        eventBuilder.setContentBeforeOperation(null);
    }

    /**
     * Set the "Content After Operation" field in an ExtendedAuditEvent, and make sure that the field name is set
     * correctly as configured.
     *
     * @throws IllegalAccessException when reflection does not work
     */
    @Test
    public void setContentAfterModificationFieldTest()
            throws IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();

        EventBuilder eventBuilder1 = new EventBuilder(properties);
        eventBuilder1.setContentAfterOperation("value".toCharArray());

        // Use reflection to get access to the internal fields
        Event eventInObject = (Event) field_event.get(eventBuilder1);
        String error = "The name has not been set correctly";
        assertThat(error, eventInObject.containsField(properties.getFieldNameContentAfterOperation()), is(true));
    }

    /**
     * Set the "Content After Operation" field in an ExtendedAuditEvent with a null value
     */
    @Test(expected = NullPointerException.class)
    public void setContentAfterodificationNullValueTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        EventBuilder eventBuilder = new EventBuilder(properties);

        eventBuilder.setContentAfterOperation(null);
    }

    /**
     * Set the "Result" field in an ExtendedAuditEvent, and make sure that the field name is set correctly as
     * configured.
     *
     * @throws IllegalAccessException when reflection does not work
     */
    @Test
    public void setResultFieldTest()
            throws IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();

        EventBuilder eventBuilder1 = new EventBuilder(properties);
        eventBuilder1.setResult("value".toCharArray());

        // Use reflection to get access to the internal fields
        Event eventInObject = (Event) field_event.get(eventBuilder1);
        String error = "The name has not been set correctly";
        assertThat(error, eventInObject.containsField(properties.getFieldNameResult()), is(true));
    }


    /**
     * Set the "Result" field in an EventBuilder with a null value
     */
    @Test(expected = NullPointerException.class)
    public void setResultNullValueTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        EventBuilder eventBuilder = new EventBuilder(properties);

        eventBuilder.setResult(null);
    }

    /**
     * Set the "Result Summary" field in an EventBuilder, and make sure that the field name is set correctly as
     * configured.
     *
     * @throws IllegalAccessException when reflection does not work
     */
    @Test
    public void setResultSummaryFieldTest()
            throws IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();

        EventBuilder eventBuilder1 = new EventBuilder(properties);
        eventBuilder1.setResultSummary("value".toCharArray());

        // Use reflection to get access to the internal fields
        Event eventInObject = (Event) field_event.get(eventBuilder1);
        String error = "The name has not been set correctly";
        assertThat(error, eventInObject.containsField(properties.getFieldNameResultSummary()), is(true));
    }

    /**
     * Set the "Result Summary" field in an EventBuilder with a null value
     */
    @Test(expected = NullPointerException.class)
    public void setResultSummaryNullValueTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        EventBuilder eventBuilder = new EventBuilder(properties);

        eventBuilder.setResultSummary(null);
    }

    /**
     * Set the "Event Summary" field in an EventBuilder, and make sure that the field name is set correctly as
     * configured.
     *
     * @throws IllegalAccessException when reflection does not work
     */
    @Test
    public void setEventSummaryFieldTest()
            throws IllegalAccessException {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();

        EventBuilder eventBuilder1 = new EventBuilder(properties);
        eventBuilder1.setEventSummary("value".toCharArray());

        // Use reflection to get access to the internal fields
        Event eventInObject = (Event) field_event.get(eventBuilder1);
        String error = "The name has not been set correctly";
        assertThat(error, eventInObject.containsField(properties.getFieldNameEventSummary()), is(true));
    }

    /**
     * Set the "Event Summary" field in an EventBuilder with a null value
     */
    @Test(expected = NullPointerException.class)
    public void setEventSummaryNullValueTest() {

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        EventBuilder eventBuilder = new EventBuilder(properties);

        eventBuilder.setEventSummary(null);
    }

    /**
     * Get an Event from the builder, and make sure it contains the correct fields:
     * <ul>
     * <li>For an empty event</li>
     * <li>For an event that contains fields</li>
     * </ul>
     *
     * @throws UnsupportedEncodingException when the encoding used to create the value bytes is invalid
     */
    @Test
    public void eventFieldContentTest()
            throws UnsupportedEncodingException {
        // start with an empty event, keep adding to it, and check after each
        // step that the event contains the correct fields with the correct value

        CommonProperties properties = MapBasedCommonPropsBuilder.buildDefault();
        properties.setEncoding(CHAR_ENCODING);
        EventBuilder eventBuilder = new EventBuilder(properties);

        // test for a first field
        eventBuilder.setActor("actor".toCharArray());
        Event event = eventBuilder.build();
        String error = "The number of fields in the event is not correct";
        assertThat(error, event.getFieldNames().size(), is(equalTo(1)));
        error = "An expected field is missing in the event";
        assertThat(error, event.getFieldNames(), containsInAnyOrder(properties.getFieldNameActor()));
        error = "The value is not correct";
        assertThat(error, event.getField(properties.getFieldNameActor()).getValue(), is(equalTo("actor".getBytes(CHAR_ENCODING))));

        // test for a second field
        eventBuilder.setResult("result".toCharArray());
        event = eventBuilder.build();
        error = "The number of fields in the event is not correct";
        assertThat(error, event.getFieldNames().size(), is(equalTo(2)));
        error = "An expected field is missing in the event";
        assertThat(error, event.getFieldNames(), containsInAnyOrder(properties.getFieldNameActor(), properties.getFieldNameResult()));
        error = "The value is not correct";
        assertThat(error, event.getField(properties.getFieldNameActor()).getValue(), is(equalTo("actor".getBytes(CHAR_ENCODING))));
        assertThat(error, event.getField(properties.getFieldNameResult()).getValue(), is(equalTo("result".getBytes(CHAR_ENCODING))));

    }
}
