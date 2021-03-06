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

import org.beiter.michael.eaudit4j.processors.timestamp.propsbuilder.MapBasedTimestampPropsBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class TimestampPropertiesTest {

    private java.lang.reflect.Field field_additionalProperties;

    /**
     * Make some of the private fields in the TimestampProperties class accessible.
     * <p>
     * This is executed before every test to ensure consistency even if one of the tests mock with field accessibility.
     */
    @Before
    public void makeAdditionalPropertiesPrivateFieldsAccessible() {

        // make private fields accessible as needed
        try {
            field_additionalProperties = TimestampProperties.class.getDeclaredField("additionalProperties");
        } catch (NoSuchFieldException e) {
            AssertionError ae = new AssertionError("An expected private field does not exist");
            ae.initCause(e);
            throw ae;
        }
        field_additionalProperties.setAccessible(true);
    }

    /**
     * Test that the additional properties are never <code>null</code>
     */
    @Test
    public void additionalPropertiesAreNeverNullTest() {

        String key = "some property";
        String value = "some value";

        Map<String, String> originalMap = new HashMap<>();

        originalMap.put(key, value);
        TimestampProperties properties = new TimestampProperties();

        String error = "The additional properties are null after create";
        try {
            Map<String, String> mapInObject = (Map<String, String>) field_additionalProperties.get(properties);
            assertThat(error, mapInObject, is(not(nullValue())));
        } catch (IllegalAccessException e) {
            AssertionError ae = new AssertionError("Cannot access private field");
            ae.initCause(e);
            throw ae;
        }

        properties = new TimestampProperties();
        error = "The additional properties are null after null put";
        properties.setAdditionalProperties(null);
        try {
            Map<String, String> mapInObject = (Map<String, String>) field_additionalProperties.get(properties);
            assertThat(error, mapInObject, is(not(nullValue())));
        } catch (IllegalAccessException e) {
            AssertionError ae = new AssertionError("Cannot access private field");
            ae.initCause(e);
            throw ae;
        }

        properties = new TimestampProperties();
        error = "The additional properties are null at get";
        Map<String, String> mapInObject = properties.getAdditionalProperties();
        assertThat(error, mapInObject, is(not(nullValue())));
    }

    /**
     * Test that the additional properties are copied inbound
     */
    @Test
    public void additionalPropertiesInboundDefensiveCopyTest() {

        String key = "some property";
        String value = "some value";

        Map<String, String> originalMap = new HashMap<>();

        originalMap.put(key, value);
        TimestampProperties properties = new TimestampProperties();
        properties.setAdditionalProperties(originalMap);

        String error = "The properties POJO does not create an inbound defensive copy";
        try {
            Map<String, String> mapInObject = (Map<String, String>) field_additionalProperties.get(properties);
            assertThat(error, mapInObject, is(not(sameInstance(originalMap))));
        } catch (IllegalAccessException e) {
            AssertionError ae = new AssertionError("Cannot access private field");
            ae.initCause(e);
            throw ae;
        }
    }

    /**
     * Test that the additional properties are copied outbound
     */
    @Test
    public void additionalPropertiesOutboundDefensiveCopyTest() {

        TimestampProperties properties = MapBasedTimestampPropsBuilder.buildDefault();

        String error = "The properties POJO does not create an outbound defensive copy";
        try {
            Map<String, String> mapInObject = (Map<String, String>) field_additionalProperties.get(properties);
            assertThat(error, mapInObject, is(not(sameInstance(properties.getAdditionalProperties()))));
        } catch (IllegalAccessException e) {
            AssertionError ae = new AssertionError("Cannot access private field");
            ae.initCause(e);
            throw ae;
        }
    }

    /**
     * Test that the copy constructor creates a new object instance
     */
    @Test
    public void copyConstructorTest() {

        TimestampProperties properties1 = MapBasedTimestampPropsBuilder.buildDefault();
        TimestampProperties properties2 = new TimestampProperties(properties1);

        String error = "The copy constructor does not create a new object instance";
        assertThat(error, properties1, is(not(sameInstance(properties2))));
    }
}
