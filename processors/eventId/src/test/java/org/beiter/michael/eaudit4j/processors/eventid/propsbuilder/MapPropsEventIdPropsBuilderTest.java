/*
 * #%L
 * This file is part of eAudit4j, a library for creating pluggable
 * auditing solutions, providing an audit processor that creates
 * a random event ID that is appended as a field to audit events.
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
package org.beiter.michael.eaudit4j.processors.eventid.propsbuilder;

import org.beiter.michael.eaudit4j.processors.eventid.EventIdProperties;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class MapPropsEventIdPropsBuilderTest {

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
        EventIdProperties properties = MapBasedEventIdPropsBuilder.build(map);

        String error = "The properties builder returns a singleton";
        assertThat(error, map, is(not(sameInstance(properties.getAdditionalProperties()))));
    }


    ///////////////////////////////////////////////////////////////////////////
    // Named Properties Tests
    //   (test the explicitly named properties)
    ///////////////////////////////////////////////////////////////////////////

    /**
     * default length test
     */
    @Test
    public void defaultEventIdLengthTest() {

        EventIdProperties properties = MapBasedEventIdPropsBuilder.buildDefault();

        String error = "event ID length does not match expected default value";
        assertThat(error, properties.getLength(), is(equalTo(MapBasedEventIdPropsBuilder.DEFAULT_LENGTH)));
        error = "event ID length does not match expected value";
        properties.setLength(42);
        assertThat(error, properties.getLength(), is(equalTo(42)));
    }

    /**
     * event ID length test
     */
    @Test
    public void eventIdLengthTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedEventIdPropsBuilder.KEY_LENGTH, null);
        EventIdProperties properties = MapBasedEventIdPropsBuilder.build(map);
        String error = "event ID length does not match expected default value";
        assertThat(error, properties.getLength(), is(equalTo(MapBasedEventIdPropsBuilder.DEFAULT_LENGTH)));

        map.put(MapBasedEventIdPropsBuilder.KEY_LENGTH, "asdf");
        properties = MapBasedEventIdPropsBuilder.build(map);
        error = "event ID length does not match expected value";
        assertThat(error, properties.getLength(), is(equalTo(MapBasedEventIdPropsBuilder.DEFAULT_LENGTH)));

        map.put(MapBasedEventIdPropsBuilder.KEY_LENGTH, "42");
        properties = MapBasedEventIdPropsBuilder.build(map);
        error = "event ID length does not match expected value";
        assertThat(error, properties.getLength(), is(equalTo(42)));

        // copy constructor test
        EventIdProperties properties2 = new EventIdProperties(properties);
        error = "copy constructor does not copy field";
        assertThat(error, properties2.getLength(), is(equalTo(42)));
    }

    /**
     * default event field name test
     */
    @Test
    public void defaultEventFieldNameTest() {

        EventIdProperties properties = MapBasedEventIdPropsBuilder.buildDefault();

        String error = "event field name does not match expected default value";
        assertThat(error, properties.getEventFieldName(),
                is(equalTo(MapBasedEventIdPropsBuilder.DEFAULT_EVENT_FIELD_NAME)));
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

        map.put(MapBasedEventIdPropsBuilder.KEY_EVENT_FIELD_NAME, null);
        EventIdProperties properties = MapBasedEventIdPropsBuilder.build(map);
        String error = "event field name does not match expected default value";
        assertThat(error, properties.getEventFieldName(),
                is(equalTo(MapBasedEventIdPropsBuilder.DEFAULT_EVENT_FIELD_NAME)));

        map.put(MapBasedEventIdPropsBuilder.KEY_EVENT_FIELD_NAME, "42");
        properties = MapBasedEventIdPropsBuilder.build(map);
        error = "event field name does not match expected value";
        assertThat(error, properties.getEventFieldName(), is(equalTo("42")));

        // copy constructor test
        EventIdProperties properties2 = new EventIdProperties(properties);
        error = "copy constructor does not copy field";
        assertThat(error, properties2.getEventFieldName(), is(equalTo("42")));
    }
}
