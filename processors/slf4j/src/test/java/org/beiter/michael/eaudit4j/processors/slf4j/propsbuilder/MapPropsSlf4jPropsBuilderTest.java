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
package org.beiter.michael.eaudit4j.processors.slf4j.propsbuilder;

import org.beiter.michael.eaudit4j.processors.slf4j.Slf4jProperties;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

public class MapPropsSlf4jPropsBuilderTest {

    ///////////////////////////////////////////////////////////////////////////
    // Additional Properties Tests
    //   (test the additional properties that are not explicitly named)
    ///////////////////////////////////////////////////////////////////////////

    /**
     * additionalProperties test: make sure that the additional properties are being set to a new object (i.e. a
     * defensive copy is being made)
     */
    @Test
    public void additionalPropertiesNoSingletonTest() {

        String key = "some property";
        String value = "some value";

        Map<String, String> map = new HashMap<>();

        map.put(key, value);
        Slf4jProperties properties = MapBasedSlf4jPropsBuilder.build(map);

        String error = "The properties builder returns a singleton";
        assertThat(error, map, is(not(sameInstance(properties.getAdditionalProperties()))));
    }


    ///////////////////////////////////////////////////////////////////////////
    // Named Properties Tests
    //   (test the explicitly named properties)
    ///////////////////////////////////////////////////////////////////////////

    /**
     * default Marker test
     */
    @Test
    public void defaultMarkerTest() {

        Slf4jProperties properties = MapBasedSlf4jPropsBuilder.buildDefault();

        String error = "Marker does not match expected default value";
        assertThat(error, properties.getMarker(),
                is(equalTo(MapBasedSlf4jPropsBuilder.DEFAULT_MARKER)));
        error = "Marker does not match expected value";
        properties.setMarker("42");
        assertThat(error, properties.getMarker(), is(equalTo("42")));
    }

    /**
     * Marker test
     */
    @Test
    public void markerTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedSlf4jPropsBuilder.KEY_MARKER, null);
        Slf4jProperties properties = MapBasedSlf4jPropsBuilder.build(map);
        String error = "Marker does not match expected default value";
        assertThat(error, properties.getMarker(),
                is(equalTo(MapBasedSlf4jPropsBuilder.DEFAULT_MARKER)));

        map.put(MapBasedSlf4jPropsBuilder.KEY_MARKER, "42");
        properties = MapBasedSlf4jPropsBuilder.build(map);
        error = "Marker does not match expected value";
        assertThat(error, properties.getMarker(), is(equalTo("42")));

        // copy constructor test
        Slf4jProperties properties2 = new Slf4jProperties(properties);
        error = "copy constructor does not copy field";
        assertThat(error, properties2.getMarker(), is(equalTo("42")));
    }

    /**
     * default string encoding test
     */
    @Test
    public void defaultStringEncodingTest() {

        Slf4jProperties properties = MapBasedSlf4jPropsBuilder.buildDefault();

        String error = "String encoding does not match expected default value";
        assertThat(error, properties.getStringEncoding(),
                is(equalTo(MapBasedSlf4jPropsBuilder.DEFAULT_STRING_ENCODING)));
        error = "String encoding does not match expected value";
        properties.setStringEncoding("42");
        assertThat(error, properties.getStringEncoding(), is(equalTo("42")));
    }

    /**
     * string encoding test
     */
    @Test
    public void stringEncodingTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedSlf4jPropsBuilder.KEY_STRING_ENCODING, null);
        Slf4jProperties properties = MapBasedSlf4jPropsBuilder.build(map);
        String error = "String encoding does not match expected default value";
        assertThat(error, properties.getStringEncoding(),
                is(equalTo(MapBasedSlf4jPropsBuilder.DEFAULT_STRING_ENCODING)));

        map.put(MapBasedSlf4jPropsBuilder.KEY_STRING_ENCODING, "42");
        properties = MapBasedSlf4jPropsBuilder.build(map);
        error = "Strign encoding does not match expected value";
        assertThat(error, properties.getStringEncoding(), is(equalTo("42")));

        // copy constructor test
        Slf4jProperties properties2 = new Slf4jProperties(properties);
        error = "copy constructor does not copy field";
        assertThat(error, properties2.getStringEncoding(), is(equalTo("42")));
    }

    /**
     * default audit stream field name test
     */
    @Test
    public void defaultAuditStreamFieldNameTest() {

        Slf4jProperties properties = MapBasedSlf4jPropsBuilder.buildDefault();

        String error = "Audit stream field name does not match expected default value";
        assertThat(error, properties.getAuditStreamFieldName(),
                is(equalTo(MapBasedSlf4jPropsBuilder.DEFAULT_AUDIT_STREAM_FIELD_NAME)));
        error = "Audit stream field name does not match expected value";
        properties.setAuditStreamFieldName("42");
        assertThat(error, properties.getAuditStreamFieldName(), is(equalTo("42")));
    }

    /**
     * Audit stream field name test
     */
    @Test
    public void auditStreamFieldNameTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedSlf4jPropsBuilder.KEY_AUDIT_STREAM_FIELD_NAME, null);
        Slf4jProperties properties = MapBasedSlf4jPropsBuilder.build(map);
        String error = "Audit stream field name does not match expected default value";
        assertThat(error, properties.getAuditStreamFieldName(),
                is(equalTo(MapBasedSlf4jPropsBuilder.DEFAULT_AUDIT_STREAM_FIELD_NAME)));

        map.put(MapBasedSlf4jPropsBuilder.KEY_AUDIT_STREAM_FIELD_NAME, "42");
        properties = MapBasedSlf4jPropsBuilder.build(map);
        error = "Audit stream field name does not match expected value";
        assertThat(error, properties.getAuditStreamFieldName(), is(equalTo("42")));

        // copy constructor test
        Slf4jProperties properties2 = new Slf4jProperties(properties);
        error = "copy constructor does not copy field";
        assertThat(error, properties2.getAuditStreamFieldName(), is(equalTo("42")));
    }

    /**
     * default serialized event field name test
     */
    @Test
    public void defaultSerializedEventFieldNameTest() {

        Slf4jProperties properties = MapBasedSlf4jPropsBuilder.buildDefault();

        String error = "Serialized event field name does not match expected default value";
        assertThat(error, properties.getSerializedEventFieldName(),
                is(equalTo(MapBasedSlf4jPropsBuilder.DEFAULT_SERIALIZED_EVENT_FIELD_NAME)));
        error = "Serialized event field name does not match expected value";
        properties.setSerializedEventFieldName("42");
        assertThat(error, properties.getSerializedEventFieldName(), is(equalTo("42")));
    }

    /**
     * Serialized event field name test
     */
    @Test
    public void serializedEventFieldNameTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedSlf4jPropsBuilder.KEY_SERIALIZED_EVENT_FIELD_NAME, null);
        Slf4jProperties properties = MapBasedSlf4jPropsBuilder.build(map);
        String error = "Serialized Event field name does not match expected default value";
        assertThat(error, properties.getSerializedEventFieldName(),
                is(equalTo(MapBasedSlf4jPropsBuilder.DEFAULT_SERIALIZED_EVENT_FIELD_NAME)));

        map.put(MapBasedSlf4jPropsBuilder.KEY_SERIALIZED_EVENT_FIELD_NAME, "42");
        properties = MapBasedSlf4jPropsBuilder.build(map);
        error = "Serialized Event field name does not match expected value";
        assertThat(error, properties.getSerializedEventFieldName(), is(equalTo("42")));

        // copy constructor test
        Slf4jProperties properties2 = new Slf4jProperties(properties);
        error = "copy constructor does not copy field";
        assertThat(error, properties2.getSerializedEventFieldName(), is(equalTo("42")));
    }

    /**
     * default MDC fields test
     */
    @Test
    public void defaultMdcFieldsTest() {

        Slf4jProperties properties = MapBasedSlf4jPropsBuilder.buildDefault();

        String error = "MDC fields does not match expected default value";
        assertThat(error, properties.getMdcFields(),
                is(equalTo(MapBasedSlf4jPropsBuilder.DEFAULT_MDC_FIELDS)));
        error = "MDC fields does not match expected value";
        properties.setMdcFields("42");
        assertThat(error, properties.getMdcFields(), is(equalTo("42")));
    }

    /**
     * MDC fields test
     */
    @Test
    public void mdcFieldsTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedSlf4jPropsBuilder.KEY_MDC_FIELDS, null);
        Slf4jProperties properties = MapBasedSlf4jPropsBuilder.build(map);
        String error = "MDC fields does not match expected default value";
        assertThat(error, properties.getMdcFields(),
                is(equalTo(MapBasedSlf4jPropsBuilder.DEFAULT_MDC_FIELDS)));

        map.put(MapBasedSlf4jPropsBuilder.KEY_MDC_FIELDS, "42");
        properties = MapBasedSlf4jPropsBuilder.build(map);
        error = "MDC fields does not match expected value";
        assertThat(error, properties.getMdcFields(), is(equalTo("42")));

        // copy constructor test
        Slf4jProperties properties2 = new Slf4jProperties(properties);
        error = "copy constructor does not copy field";
        assertThat(error, properties2.getMdcFields(), is(equalTo("42")));
    }

    /**
     * default MDC field separator test
     */
    @Test
    public void defaultMdcFieldSeparatorTest() {

        Slf4jProperties properties = MapBasedSlf4jPropsBuilder.buildDefault();

        String error = "MDC field separator does not match expected default value";
        assertThat(error, properties.getMdcFieldSeparator(),
                is(equalTo(MapBasedSlf4jPropsBuilder.DEFAULT_MDC_FIELD_SEPARATOR)));
        error = "MDC fiedl separator does not match expected value";
        properties.setMdcFieldSeparator("!");
        assertThat(error, properties.getMdcFieldSeparator(), is(equalTo("!")));
    }

    /**
     * MDC field separator test
     */
    @Test
    public void mdcFieldSeparatorTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedSlf4jPropsBuilder.KEY_MDC_FIELD_SEPARATOR, null);
        Slf4jProperties properties = MapBasedSlf4jPropsBuilder.build(map);
        String error = "MDC field separator does not match expected default value";
        assertThat(error, properties.getMdcFieldSeparator(),
                is(equalTo(MapBasedSlf4jPropsBuilder.DEFAULT_MDC_FIELD_SEPARATOR)));

        map.put(MapBasedSlf4jPropsBuilder.KEY_MDC_FIELD_SEPARATOR, "!");
        properties = MapBasedSlf4jPropsBuilder.build(map);
        error = "MDC field separator does not match expected value";
        assertThat(error, properties.getMdcFieldSeparator(), is(equalTo("!")));

        // copy constructor test
        Slf4jProperties properties2 = new Slf4jProperties(properties);
        error = "copy constructor does not copy field";
        assertThat(error, properties2.getMdcFieldSeparator(), is(equalTo("!")));
    }

    /**
     * MDC field separator length
     */
    @Test(expected = IllegalArgumentException.class)
    public void mdcFieldSeparatorLengthTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedSlf4jPropsBuilder.KEY_MDC_FIELD_SEPARATOR, "42");

        // this should throw an Exception (length "42" is too big, only allow single chars here)
        Slf4jProperties properties = MapBasedSlf4jPropsBuilder.build(map);
    }

    /**
     * default MDC field name separator
     */
    @Test
    public void defaultMdcFieldNameSeparatorTest() {

        Slf4jProperties properties = MapBasedSlf4jPropsBuilder.buildDefault();

        String error = "MDC field name separator does not match expected default value";
        assertThat(error, properties.getMdcFieldNameSeparator(),
                is(equalTo(MapBasedSlf4jPropsBuilder.DEFAULT_MDC_FIELD_NAME_SEPARATOR)));
        error = "MDC field name separator does not match expected value";
        properties.setMdcFieldNameSeparator("!");
        assertThat(error, properties.getMdcFieldNameSeparator(), is(equalTo("!")));
    }

    /**
     * MDC field name separator
     */
    @Test
    public void mdcFieldNameSeparatorTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedSlf4jPropsBuilder.KEY_MDC_FIELD_NAME_SEPARATOR, null);
        Slf4jProperties properties = MapBasedSlf4jPropsBuilder.build(map);
        String error = "MDC field name separator does not match expected default value";
        assertThat(error, properties.getMdcFieldNameSeparator(),
                is(equalTo(MapBasedSlf4jPropsBuilder.DEFAULT_MDC_FIELD_NAME_SEPARATOR)));

        map.put(MapBasedSlf4jPropsBuilder.KEY_MDC_FIELD_NAME_SEPARATOR, "!");
        properties = MapBasedSlf4jPropsBuilder.build(map);
        error = "MDC field name separator does not match expected value";
        assertThat(error, properties.getMdcFieldNameSeparator(), is(equalTo("!")));

        // copy constructor test
        Slf4jProperties properties2 = new Slf4jProperties(properties);
        error = "copy constructor does not copy field";
        assertThat(error, properties2.getMdcFieldNameSeparator(), is(equalTo("!")));
    }

    /**
     * MDC field name separator length
     */
    @Test(expected = IllegalArgumentException.class)
    public void mdcFieldNameSeparatorLengthTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedSlf4jPropsBuilder.KEY_MDC_FIELD_NAME_SEPARATOR, "42");

        // this should throw an Exception (length "42" is too big, only allow single chars here)
        Slf4jProperties properties = MapBasedSlf4jPropsBuilder.build(map);
    }
}
