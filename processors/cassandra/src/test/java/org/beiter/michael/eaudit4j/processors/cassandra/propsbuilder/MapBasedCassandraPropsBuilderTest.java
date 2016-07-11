/*
 * #%L
 * This file is part of eAudit4j, a library for creating pluggable
 * auditing solutions, providing an audit processor that persists
 * audit events to a Cassandra database.
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
package org.beiter.michael.eaudit4j.processors.cassandra.propsbuilder;

import org.beiter.michael.eaudit4j.processors.cassandra.CassandraProperties;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

public class MapBasedCassandraPropsBuilderTest {

    @Test
    public void shouldValidateAdditionalPropertiesNoSingleton() {

        String key = "some property";
        String value = "some value";

        Map<String, String> map = new HashMap<>();

        map.put(key, value);
        CassandraProperties properties = MapBasedCassandraPropsBuilder.build(map);

        String error = "The properties builder returns a singleton";
        assertThat(error, map, is(not(sameInstance(properties.getAdditionalProperties()))));
    }

    @Test
    public void shouldValidateDefaultInsertEventSqlStmt() {

        CassandraProperties properties = MapBasedCassandraPropsBuilder.buildDefault();

        String error = "INSERT Event SQL statement does not match expected default value";
        assertThat(error, properties.getInsertEventSqlStmt(),
                is(equalTo(MapBasedCassandraPropsBuilder.DEFAULT_INSERT_EVENT_SQL_STMT)));
        error = "INSERT Event SQL statement  does not match expected value";
        properties.setInsertEventSqlStmt("42");
        assertThat(error, properties.getInsertEventSqlStmt(), is(equalTo("42")));
    }

    @Test
    public void ShouldValidateInsertEventSqlStmt() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedCassandraPropsBuilder.KEY_INSERT_EVENT_SQL_STMT, null);
        CassandraProperties properties = MapBasedCassandraPropsBuilder.build(map);
        String error = "INSERT Event SQL statement does not match expected default value";
        assertThat(error, properties.getInsertEventSqlStmt(),
                is(equalTo(MapBasedCassandraPropsBuilder.DEFAULT_INSERT_EVENT_SQL_STMT)));

        map.put(MapBasedCassandraPropsBuilder.KEY_INSERT_EVENT_SQL_STMT, "42");
        properties = MapBasedCassandraPropsBuilder.build(map);
        error = "INSERT Event SQL statement does not match expected value";
        assertThat(error, properties.getInsertEventSqlStmt(), is(equalTo("42")));

        CassandraProperties properties2 = new CassandraProperties(properties);
        error = "copy constructor does not copy field";
        assertThat(error, properties2.getInsertEventSqlStmt(), is(equalTo("42")));
    }

    @Test
    public void shouldValidateDefaultStringEncodingTest() {

        CassandraProperties properties = MapBasedCassandraPropsBuilder.buildDefault();

        String error = "String encoding does not match expected default value";
        assertThat(error, properties.getStringEncoding(),
                is(equalTo(MapBasedCassandraPropsBuilder.DEFAULT_STRING_ENCODING)));
        error = "String encoding does not match expected value";
        properties.setStringEncoding("42");
        assertThat(error, properties.getStringEncoding(), is(equalTo("42")));
    }

    @Test
    public void shouldValidateStringEncodingTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedCassandraPropsBuilder.KEY_STRING_ENCODING, null);
        CassandraProperties properties = MapBasedCassandraPropsBuilder.build(map);
        String error = "String encoding does not match expected default value";
        assertThat(error, properties.getStringEncoding(),
                is(equalTo(MapBasedCassandraPropsBuilder.DEFAULT_STRING_ENCODING)));

        map.put(MapBasedCassandraPropsBuilder.KEY_STRING_ENCODING, "42");
        properties = MapBasedCassandraPropsBuilder.build(map);
        error = "String encoding does not match expected value";
        assertThat(error, properties.getStringEncoding(), is(equalTo("42")));

        CassandraProperties properties2 = new CassandraProperties(properties);
        error = "copy constructor does not copy field";
        assertThat(error, properties2.getStringEncoding(), is(equalTo("42")));
    }


    @Test
    public void shouldValidateDefaultEventIdFieldNameTest() {

        CassandraProperties properties = MapBasedCassandraPropsBuilder.buildDefault();

        String error = "Event ID field name does not match expected default value";
        assertThat(error, properties.getEventIdFieldName(),
                is(equalTo(MapBasedCassandraPropsBuilder.DEFAULT_EVENT_ID_FIELD_NAME)));
        error = "Event ID field name does not match expected value";
        properties.setEventIdFieldName("42");
        assertThat(error, properties.getEventIdFieldName(), is(equalTo("42")));
    }


    @Test
    public void shouldValidateEventIdFieldNameTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedCassandraPropsBuilder.KEY_EVENT_ID_FIELD_NAME, null);
        CassandraProperties properties = MapBasedCassandraPropsBuilder.build(map);
        String error = "Event ID field name does not match expected default value";
        assertThat(error, properties.getEventIdFieldName(),
                is(equalTo(MapBasedCassandraPropsBuilder.DEFAULT_EVENT_ID_FIELD_NAME)));

        map.put(MapBasedCassandraPropsBuilder.KEY_EVENT_ID_FIELD_NAME, "42");
        properties = MapBasedCassandraPropsBuilder.build(map);
        error = "Event ID field name does not match expected value";
        assertThat(error, properties.getEventIdFieldName(), is(equalTo("42")));

        CassandraProperties properties2 = new CassandraProperties(properties);
        error = "copy constructor does not copy field";
        assertThat(error, properties2.getEventIdFieldName(), is(equalTo("42")));
    }
}
