/*
 * #%L
 * This file is part of eAudit4j, a library for creating pluggable
 * auditing solutions, providing an audit processor that persists
 * audit events to a JDBC database.
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
package org.beiter.michael.eaudit4j.processors.jdbc.propsbuilder;

import org.beiter.michael.eaudit4j.processors.jdbc.JdbcProperties;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

public class MapPropsJdbcPropsBuilderTest {

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
        JdbcProperties properties = MapBasedJdbcPropsBuilder.build(map);

        String error = "The properties builder returns a singleton";
        assertThat(error, map, is(not(sameInstance(properties.getAdditionalProperties()))));
    }


    ///////////////////////////////////////////////////////////////////////////
    // Named Properties Tests
    //   (test the explicitly named properties)
    ///////////////////////////////////////////////////////////////////////////

    /**
     * default event INSERT SQL statement test
     */
    @Test
    public void defaultInsertEventSqlStmtTest() {

        JdbcProperties properties = MapBasedJdbcPropsBuilder.buildDefault();

        String error = "INSERT Event SQL statement does not match expected default value";
        assertThat(error, properties.getInsertEventSqlStmt(),
                is(equalTo(MapBasedJdbcPropsBuilder.DEFAULT_INSERT_EVENT_SQL_STMT)));
        error = "INSERT Event SQL statement  does not match expected value";
        properties.setInsertEventSqlStmt("42");
        assertThat(error, properties.getInsertEventSqlStmt(), is(equalTo("42")));
    }

    /**
     * Event INSERT SQL statement test
     */
    @Test
    public void insertEventSqlStmtTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedJdbcPropsBuilder.KEY_INSERT_EVENT_SQL_STMT, null);
        JdbcProperties properties = MapBasedJdbcPropsBuilder.build(map);
        String error = "INSERT Event SQL statement does not match expected default value";
        assertThat(error, properties.getInsertEventSqlStmt(),
                is(equalTo(MapBasedJdbcPropsBuilder.DEFAULT_INSERT_EVENT_SQL_STMT)));

        map.put(MapBasedJdbcPropsBuilder.KEY_INSERT_EVENT_SQL_STMT, "42");
        properties = MapBasedJdbcPropsBuilder.build(map);
        error = "INSERT Event SQL statement does not match expected value";
        assertThat(error, properties.getInsertEventSqlStmt(), is(equalTo("42")));

        // copy constructor test
        JdbcProperties properties2 = new JdbcProperties(properties);
        error = "copy constructor does not copy field";
        assertThat(error, properties2.getInsertEventSqlStmt(), is(equalTo("42")));
    }

    /**
     * default string encoding test
     */
    @Test
    public void defaultStringEncodingTest() {

        JdbcProperties properties = MapBasedJdbcPropsBuilder.buildDefault();

        String error = "String encoding does not match expected default value";
        assertThat(error, properties.getStringEncoding(),
                is(equalTo(MapBasedJdbcPropsBuilder.DEFAULT_STRING_ENCODING)));
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

        map.put(MapBasedJdbcPropsBuilder.KEY_STRING_ENCODING, null);
        JdbcProperties properties = MapBasedJdbcPropsBuilder.build(map);
        String error = "String encoding does not match expected default value";
        assertThat(error, properties.getStringEncoding(),
                is(equalTo(MapBasedJdbcPropsBuilder.DEFAULT_STRING_ENCODING)));

        map.put(MapBasedJdbcPropsBuilder.KEY_STRING_ENCODING, "42");
        properties = MapBasedJdbcPropsBuilder.build(map);
        error = "String encoding does not match expected value";
        assertThat(error, properties.getStringEncoding(), is(equalTo("42")));

        // copy constructor test
        JdbcProperties properties2 = new JdbcProperties(properties);
        error = "copy constructor does not copy field";
        assertThat(error, properties2.getStringEncoding(), is(equalTo("42")));
    }

    /**
     * default event ID field name test
     */
    @Test
    public void defaultEventIdFieldNameTest() {

        JdbcProperties properties = MapBasedJdbcPropsBuilder.buildDefault();

        String error = "Event ID field name does not match expected default value";
        assertThat(error, properties.getEventIdFieldName(),
                is(equalTo(MapBasedJdbcPropsBuilder.DEFAULT_EVENT_ID_FIELD_NAME)));
        error = "Event ID field name does not match expected value";
        properties.setEventIdFieldName("42");
        assertThat(error, properties.getEventIdFieldName(), is(equalTo("42")));
    }

    /**
     * event ID field name test
     */
    @Test
    public void eventIdFieldNameTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedJdbcPropsBuilder.KEY_EVENT_ID_FIELD_NAME, null);
        JdbcProperties properties = MapBasedJdbcPropsBuilder.build(map);
        String error = "Event ID field name does not match expected default value";
        assertThat(error, properties.getEventIdFieldName(),
                is(equalTo(MapBasedJdbcPropsBuilder.DEFAULT_EVENT_ID_FIELD_NAME)));

        map.put(MapBasedJdbcPropsBuilder.KEY_EVENT_ID_FIELD_NAME, "42");
        properties = MapBasedJdbcPropsBuilder.build(map);
        error = "Event ID field name does not match expected value";
        assertThat(error, properties.getEventIdFieldName(), is(equalTo("42")));

        // copy constructor test
        JdbcProperties properties2 = new JdbcProperties(properties);
        error = "copy constructor does not copy field";
        assertThat(error, properties2.getEventIdFieldName(), is(equalTo("42")));
    }

    /**
     * default indexed fields test
     */
    @Test
    public void defaultIndexedFieldsTest() {

        JdbcProperties properties = MapBasedJdbcPropsBuilder.buildDefault();

        String error = "Indexed fields does not match expected default value";
        assertThat(error, properties.getIndexedFields(),
                is(equalTo(MapBasedJdbcPropsBuilder.DEFAULT_INDEXED_FIELDS)));
        error = "Indexed fields does not match expected value";
        properties.setIndexedFields("42");
        assertThat(error, properties.getIndexedFields(), is(equalTo("42")));
    }

    /**
     * Indexed fields test
     */
    @Test
    public void indexedFieldsTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedJdbcPropsBuilder.KEY_INDEXED_FIELDS, null);
        JdbcProperties properties = MapBasedJdbcPropsBuilder.build(map);
        String error = "Indexed fields does not match expected default value";
        assertThat(error, properties.getIndexedFields(),
                is(equalTo(MapBasedJdbcPropsBuilder.DEFAULT_INDEXED_FIELDS)));

        map.put(MapBasedJdbcPropsBuilder.KEY_INDEXED_FIELDS, "42");
        properties = MapBasedJdbcPropsBuilder.build(map);
        error = "Indexed fields does not match expected value";
        assertThat(error, properties.getIndexedFields(), is(equalTo("42")));

        // copy constructor test
        JdbcProperties properties2 = new JdbcProperties(properties);
        error = "copy constructor does not copy field";
        assertThat(error, properties2.getIndexedFields(), is(equalTo("42")));
    }

    /**
     * default indexed fields max length test
     */
    @Test
    public void defaultIndexedFieldsMaxLengthTest() {

        JdbcProperties properties = MapBasedJdbcPropsBuilder.buildDefault();

        String error = "field max length does not match expected default value";
        assertThat(error, properties.getIndexedFieldsMaxLength(), is(equalTo(MapBasedJdbcPropsBuilder.DEFAULT_INDEXED_FIELDS_MAX_LENGTH)));
        error = "field max length does not match expected value";
        properties.setIndexedFieldsMaxLength(42);
        assertThat(error, properties.getIndexedFieldsMaxLength(), is(equalTo(42)));
    }

    /**
     * indexed fields max length test
     */
    @Test
    public void indexedFieldsMaxLengthTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedJdbcPropsBuilder.KEY_INDEXED_FIELDS_MAX_LENGTH, null);
        JdbcProperties properties = MapBasedJdbcPropsBuilder.build(map);
        String error = "field max length does not match expected default value";
        assertThat(error, properties.getIndexedFieldsMaxLength(), is(equalTo(MapBasedJdbcPropsBuilder.DEFAULT_INDEXED_FIELDS_MAX_LENGTH)));

        map.put(MapBasedJdbcPropsBuilder.KEY_INDEXED_FIELDS_MAX_LENGTH, "asdf");
        properties = MapBasedJdbcPropsBuilder.build(map);
        error = "field max length does not match expected value";
        assertThat(error, properties.getIndexedFieldsMaxLength(), is(equalTo(MapBasedJdbcPropsBuilder.DEFAULT_INDEXED_FIELDS_MAX_LENGTH)));

        map.put(MapBasedJdbcPropsBuilder.KEY_INDEXED_FIELDS_MAX_LENGTH, "42");
        properties = MapBasedJdbcPropsBuilder.build(map);
        error = "field max length does not match expected value";
        assertThat(error, properties.getIndexedFieldsMaxLength(), is(equalTo(42)));

        // copy constructor test
        JdbcProperties properties2 = new JdbcProperties(properties);
        error = "copy constructor does not copy field";
        assertThat(error, properties2.getIndexedFieldsMaxLength(), is(equalTo(42)));
    }

    /**
     * default indexed fields to lower test
     */
    @Test
    public void defaultIndexedFieldsToLowerTest() {

        JdbcProperties properties = MapBasedJdbcPropsBuilder.buildDefault();

        String error = "field to lower does not match expected default value";
        assertThat(error, properties.isIndexedFieldsToLower(), is(equalTo(false)));
        error = "field to lower  does not match expected value";
        properties.setIndexedFieldsToLower(true);
        assertThat(error, properties.isIndexedFieldsToLower(), is(equalTo(true)));
    }

    /**
     * indexed fields to lower test
     */
    @Test
    public void indexedFieldsToLowerTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedJdbcPropsBuilder.KEY_INDEXED_FIELDS_TO_LOWER, null);
        JdbcProperties properties = MapBasedJdbcPropsBuilder.build(map);
        String error = "field to lower does not match expected default value";
        assertThat(error, properties.isIndexedFieldsToLower(), is(equalTo(false)));

        map.put(MapBasedJdbcPropsBuilder.KEY_INDEXED_FIELDS_TO_LOWER, "asdf");
        properties = MapBasedJdbcPropsBuilder.build(map);
        error = "field to lower does not match expected value";
        assertThat(error, properties.isIndexedFieldsToLower(), is(equalTo(false)));

        map.put(MapBasedJdbcPropsBuilder.KEY_INDEXED_FIELDS_TO_LOWER, "tRuE");
        properties = MapBasedJdbcPropsBuilder.build(map);
        error = "field to lower does not match expected value";
        assertThat(error, properties.isIndexedFieldsToLower(), is(equalTo(true)));

        // copy constructor test
        JdbcProperties properties2 = new JdbcProperties(properties);
        error = "copy constructor does not copy field";
        assertThat(error, properties2.isIndexedFieldsToLower(), is(equalTo(true)));
    }


    /**
     * default indexed field INSERT SQL statement test
     */
    @Test
    public void defaultInsertIndexedFieldSqlStmtTest() {

        JdbcProperties properties = MapBasedJdbcPropsBuilder.buildDefault();

        String error = "INSERT indexed field SQL statement does not match expected default value";
        assertThat(error, properties.getInsertIndexedFieldSqlStmt(),
                is(equalTo(MapBasedJdbcPropsBuilder.DEFAULT_INSERT_INDEXED_FIELD_SQL_STMT)));
        error = "INSERT indexed field SQL statement  does not match expected value";
        properties.setInsertIndexedFieldSqlStmt("42");
        assertThat(error, properties.getInsertIndexedFieldSqlStmt(), is(equalTo("42")));
    }

    /**
     * Indexed field INSERT SQL statement test
     */
    @Test
    public void insertIndexedFieldSqlStmtTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedJdbcPropsBuilder.KEY_INSERT_INDEXED_FIELD_SQL_STMT, null);
        JdbcProperties properties = MapBasedJdbcPropsBuilder.build(map);
        String error = "INSERT indexed field SQL statement does not match expected default value";
        assertThat(error, properties.getInsertIndexedFieldSqlStmt(),
                is(equalTo(MapBasedJdbcPropsBuilder.DEFAULT_INSERT_INDEXED_FIELD_SQL_STMT)));

        map.put(MapBasedJdbcPropsBuilder.KEY_INSERT_INDEXED_FIELD_SQL_STMT, "42");
        properties = MapBasedJdbcPropsBuilder.build(map);
        error = "INSERT indexed field SQL statement does not match expected value";
        assertThat(error, properties.getInsertIndexedFieldSqlStmt(), is(equalTo("42")));

        // copy constructor test
        JdbcProperties properties2 = new JdbcProperties(properties);
        error = "copy constructor does not copy field";
        assertThat(error, properties2.getInsertIndexedFieldSqlStmt(), is(equalTo("42")));
    }

    /**
     * default indexed field separator test
     */
    @Test
    public void defaultIndexedFieldSeparatorTest() {

        JdbcProperties properties = MapBasedJdbcPropsBuilder.buildDefault();

        String error = "Indexed field separator does not match expected default value";
        assertThat(error, properties.getIndexedFieldSeparator(),
                is(equalTo(MapBasedJdbcPropsBuilder.DEFAULT_INDEXED_FIELD_SEPARATOR)));
        error = "Indexed field separator does not match expected value";
        properties.setIndexedFieldSeparator("!");
        assertThat(error, properties.getIndexedFieldSeparator(), is(equalTo("!")));
    }

    /**
     * Indexed field separator test
     */
    @Test
    public void mdcFieldSeparatorTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedJdbcPropsBuilder.KEY_INDEXED_FIELD_SEPARATOR, null);
        JdbcProperties properties = MapBasedJdbcPropsBuilder.build(map);
        String error = "Indexed field separator does not match expected default value";
        assertThat(error, properties.getIndexedFieldSeparator(),
                is(equalTo(MapBasedJdbcPropsBuilder.DEFAULT_INDEXED_FIELD_SEPARATOR)));

        map.put(MapBasedJdbcPropsBuilder.KEY_INDEXED_FIELD_SEPARATOR, "!");
        properties = MapBasedJdbcPropsBuilder.build(map);
        error = "Indexed field separator does not match expected value";
        assertThat(error, properties.getIndexedFieldSeparator(), is(equalTo("!")));

        // copy constructor test
        JdbcProperties properties2 = new JdbcProperties(properties);
        error = "copy constructor does not copy field";
        assertThat(error, properties2.getIndexedFieldSeparator(), is(equalTo("!")));
    }

    /**
     * Indexed field separator length
     */
    @Test(expected = IllegalArgumentException.class)
    public void mdcFieldSeparatorLengthTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedJdbcPropsBuilder.KEY_INDEXED_FIELD_SEPARATOR, "42");

        // this should throw an Exception (length "42" is too big, only allow single chars here)
        JdbcProperties properties = MapBasedJdbcPropsBuilder.build(map);
    }

    /**
     * default indexed field name separator
     */
    @Test
    public void defaultMdcFieldNameSeparatorTest() {

        JdbcProperties properties = MapBasedJdbcPropsBuilder.buildDefault();

        String error = "Indexed field name separator does not match expected default value";
        assertThat(error, properties.getIndexedFieldNameSeparator(),
                is(equalTo(MapBasedJdbcPropsBuilder.DEFAULT_INDEXED_FIELD_NAME_SEPARATOR)));
        error = "Indexed field name separator does not match expected value";
        properties.setIndexedFieldNameSeparator("!");
        assertThat(error, properties.getIndexedFieldNameSeparator(), is(equalTo("!")));
    }

    /**
     * MDC field name separator
     */
    @Test
    public void mdcFieldNameSeparatorTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedJdbcPropsBuilder.KEY_INDEXED_FIELD_NAME_SEPARATOR, null);
        JdbcProperties properties = MapBasedJdbcPropsBuilder.build(map);
        String error = "Indexed field name separator does not match expected default value";
        assertThat(error, properties.getIndexedFieldNameSeparator(),
                is(equalTo(MapBasedJdbcPropsBuilder.DEFAULT_INDEXED_FIELD_NAME_SEPARATOR)));

        map.put(MapBasedJdbcPropsBuilder.KEY_INDEXED_FIELD_NAME_SEPARATOR, "!");
        properties = MapBasedJdbcPropsBuilder.build(map);
        error = "Indexed field name separator does not match expected value";
        assertThat(error, properties.getIndexedFieldNameSeparator(), is(equalTo("!")));

        // copy constructor test
        JdbcProperties properties2 = new JdbcProperties(properties);
        error = "copy constructor does not copy field";
        assertThat(error, properties2.getIndexedFieldNameSeparator(), is(equalTo("!")));
    }

    /**
     * MDC field name separator length
     */
    @Test(expected = IllegalArgumentException.class)
    public void mdcFieldNameSeparatorLengthTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedJdbcPropsBuilder.KEY_INDEXED_FIELD_NAME_SEPARATOR, "42");

        // this should throw an Exception (length "42" is too big, only allow single chars here)
        JdbcProperties properties = MapBasedJdbcPropsBuilder.build(map);
    }

    /**
     * default JNDI connection name test
     */
    @Test
    public void defaultJndiConnectionNameTest() {

        JdbcProperties properties = MapBasedJdbcPropsBuilder.buildDefault();

        String error = "JNDI connection name does not match expected default value";
        assertThat(error, properties.getJndiConnectionName(),
                is(equalTo(MapBasedJdbcPropsBuilder.DEFAULT_JNDI_CONNECTION_NAME)));
        error = "JNDI connection name does not match expected value";
        properties.setJndiConnectionName("42");
        assertThat(error, properties.getJndiConnectionName(), is(equalTo("42")));
    }

    /**
     * JNDI connection name test
     */
    @Test
    public void jndiConnectionNameTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedJdbcPropsBuilder.KEY_JNDI_CONNECTION_NAME, null);
        JdbcProperties properties = MapBasedJdbcPropsBuilder.build(map);
        String error = "JNDI connection name does not match expected default value";
        assertThat(error, properties.getJndiConnectionName(),
                is(equalTo(MapBasedJdbcPropsBuilder.DEFAULT_JNDI_CONNECTION_NAME)));

        map.put(MapBasedJdbcPropsBuilder.KEY_JNDI_CONNECTION_NAME, "42");
        properties = MapBasedJdbcPropsBuilder.build(map);
        error = "JNDI connection name does not match expected value";
        assertThat(error, properties.getJndiConnectionName(), is(equalTo("42")));

        // copy constructor test
        JdbcProperties properties2 = new JdbcProperties(properties);
        error = "copy constructor does not copy field";
        assertThat(error, properties2.getJndiConnectionName(), is(equalTo("42")));
    }

    /**
     * default data source name test
     */
    @Test
    public void defaultDataSourceNameTest() {

        JdbcProperties properties = MapBasedJdbcPropsBuilder.buildDefault();

        String error = "Data source name does not match expected default value";
        assertThat(error, properties.getDataSourceName(),
                is(equalTo(MapBasedJdbcPropsBuilder.DEFAULT_DATA_SOURCE_NAME)));
        error = "Data source name does not match expected value";
        properties.setDataSourceName("42");
        assertThat(error, properties.getDataSourceName(), is(equalTo("42")));
    }

    /**
     * Data source name test
     */
    @Test
    public void dataSourceNameTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedJdbcPropsBuilder.KEY_DATA_SOURCE_NAME, null);
        JdbcProperties properties = MapBasedJdbcPropsBuilder.build(map);
        String error = "Data source name does not match expected default value";
        assertThat(error, properties.getDataSourceName(),
                is(equalTo(MapBasedJdbcPropsBuilder.DEFAULT_DATA_SOURCE_NAME)));

        map.put(MapBasedJdbcPropsBuilder.KEY_DATA_SOURCE_NAME, "42");
        properties = MapBasedJdbcPropsBuilder.build(map);
        error = "Data source name does not match expected value";
        assertThat(error, properties.getDataSourceName(), is(equalTo("42")));

        // copy constructor test
        JdbcProperties properties2 = new JdbcProperties(properties);
        error = "copy constructor does not copy field";
        assertThat(error, properties2.getDataSourceName(), is(equalTo("42")));
    }
}
