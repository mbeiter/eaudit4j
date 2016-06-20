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
package org.beiter.michael.eaudit4j.processors.timestamp.propsbuilder;

import org.beiter.michael.eaudit4j.processors.timestamp.TimestampProperties;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class MapPropsTimestampPropsBuilderTest {

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
        TimestampProperties properties = MapBasedTimestampPropsBuilder.build(map);

        String error = "The properties builder returns a singleton";
        assertThat(error, map, is(not(sameInstance(properties.getAdditionalProperties()))));
    }


    ///////////////////////////////////////////////////////////////////////////
    // Named Properties Tests
    //   (test the explicitly named properties)
    ///////////////////////////////////////////////////////////////////////////

    /**
     * default timezone test
     */
    @Test
    public void defaultTimezoneTest() {

        TimestampProperties properties = MapBasedTimestampPropsBuilder.buildDefault();

        String error = "timezone does not match expected default value";
        assertThat(error, properties.getTimezone(),
                is(equalTo(MapBasedTimestampPropsBuilder.DEFAULT_TIMEZONE)));
        error = "timezone does not match expected value";
        properties.setTimezone("42");
        assertThat(error, properties.getTimezone(), is(equalTo("42")));
    }

    /**
     * timezone test
     */
    @Test
    public void timezoneTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedTimestampPropsBuilder.KEY_TIMEZONE, null);
        TimestampProperties properties = MapBasedTimestampPropsBuilder.build(map);
        String error = "timezone does not match expected default value";
        assertThat(error, properties.getTimezone(),
                is(equalTo(MapBasedTimestampPropsBuilder.DEFAULT_TIMEZONE)));

        map.put(MapBasedTimestampPropsBuilder.KEY_TIMEZONE, "42");
        properties = MapBasedTimestampPropsBuilder.build(map);
        error = "timezone does not match expected value";
        assertThat(error, properties.getTimezone(), is(equalTo("42")));

        // copy constructor test
        TimestampProperties properties2 = new TimestampProperties(properties);
        error = "copy constructor does not copy field";
        assertThat(error, properties2.getTimezone(), is(equalTo("42")));
    }

    /**
     * default format test
     */
    @Test
    public void defaultFormatTest() {

        TimestampProperties properties = MapBasedTimestampPropsBuilder.buildDefault();

        String error = "format does not match expected default value";
        assertThat(error, properties.getFormat(),
                is(equalTo(MapBasedTimestampPropsBuilder.DEFAULT_FORMAT)));
        error = "format does not match expected value";
        properties.setFormat("42");
        assertThat(error, properties.getFormat(), is(equalTo("42")));
    }

    /**
     * format test
     */
    @Test
    public void formatTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedTimestampPropsBuilder.KEY_FORMAT, null);
        TimestampProperties properties = MapBasedTimestampPropsBuilder.build(map);
        String error = "format does not match expected default value";
        assertThat(error, properties.getFormat(),
                is(equalTo(MapBasedTimestampPropsBuilder.DEFAULT_FORMAT)));

        map.put(MapBasedTimestampPropsBuilder.KEY_FORMAT, "42");
        properties = MapBasedTimestampPropsBuilder.build(map);
        error = "format does not match expected value";
        assertThat(error, properties.getFormat(), is(equalTo("42")));

        // copy constructor test
        TimestampProperties properties2 = new TimestampProperties(properties);
        error = "copy constructor does not copy field";
        assertThat(error, properties2.getFormat(), is(equalTo("42")));
    }


    /**
     * default event field name test
     */
    @Test
    public void defaultEventFieldNameTest() {

        TimestampProperties properties = MapBasedTimestampPropsBuilder.buildDefault();

        String error = "event field name does not match expected default value";
        assertThat(error, properties.getEventFieldName(),
                is(equalTo(MapBasedTimestampPropsBuilder.DEFAULT_EVENT_FIELD_NAME)));
        error = "event field name does not match expected value";
        properties.setEventFieldName("42");
        assertThat(error, properties.getEventFieldName(), is(equalTo("42")));
    }

    /**
     * event field name test
     */
    @Test
    public void eventFieldNameTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedTimestampPropsBuilder.KEY_EVENT_FIELD_NAME, null);
        TimestampProperties properties = MapBasedTimestampPropsBuilder.build(map);
        String error = "event field name does not match expected default value";
        assertThat(error, properties.getEventFieldName(),
                is(equalTo(MapBasedTimestampPropsBuilder.DEFAULT_EVENT_FIELD_NAME)));

        map.put(MapBasedTimestampPropsBuilder.KEY_EVENT_FIELD_NAME, "42");
        properties = MapBasedTimestampPropsBuilder.build(map);
        error = "event field name does not match expected value";
        assertThat(error, properties.getEventFieldName(), is(equalTo("42")));

        // copy constructor test
        TimestampProperties properties2 = new TimestampProperties(properties);
        error = "copy constructor does not copy field";
        assertThat(error, properties2.getEventFieldName(), is(equalTo("42")));
    }
}
