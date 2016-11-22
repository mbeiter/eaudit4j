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
package org.beiter.michael.eaudit4j.processors.jdbc;

import org.apache.commons.lang3.Validate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class specifies properties specific to a JDBC Processor, independent on the underlying database connection
 * technology (e.g. pooled / direct, JNDI, data source, ...).
 */
// suppress warnings about the long variable names
@SuppressWarnings("PMD.LongVariable")
public class JdbcProperties {

    /**
     * @see JdbcProperties#setInsertEventSqlStmt(String)
     */
    private String insertEventSqlStmt;

    /**
     * @see JdbcProperties#setStringEncoding(String)
     */
    private String stringEncoding;

    /**
     * @see JdbcProperties#setEventIdFieldName(String)
     */
    private String eventIdFieldName;

    /**
     * @see JdbcProperties#setIndexedFields(String)
     */
    private String indexedFields;

    /**
     * @see JdbcProperties#setIndexedFieldsMaxLength(int)
     */
    private int indexedFieldsMaxLength;

    /**
     * @see JdbcProperties#setIndexedFieldsToLower(boolean)
     */
    private boolean indexedFieldsToLower;

    /**
     * @see JdbcProperties#setInsertIndexedFieldSqlStmt(String)
     */
    private String insertIndexedFieldSqlStmt;

    /**
     * @see JdbcProperties#setIndexedFieldSeparator(String)
     */
    private String indexedFieldSeparator;

    /**
     * @see JdbcProperties#setIndexedFieldNameSeparator(String)
     */
    private String indexedFieldNameSeparator;

    /**
     * @see JdbcProperties#setJndiConnectionName(String)
     */
    private String jndiConnectionName;

    /**
     * @see JdbcProperties#setDataSourceName(String)
     */
    private String dataSourceName;

    /**
     * @see JdbcProperties#setAdditionalProperties(Map)
     */
    private Map<String, String> additionalProperties = new ConcurrentHashMap<>();

    /**
     * Constructs an empty set of JDBC properties, with most values being set to <code>null</code>, 0, or empty
     * (depending on the type of the property). Usually this constructor is used if this configuration POJO is populated
     * in an automated fashion (e.g. injection). If you need to build them manually (possibly with defaults), use or
     * create a properties builder.
     * <p>
     * You can change the defaults with the setters.
     *
     * @see org.beiter.michael.eaudit4j.common.propsbuilder.MapBasedCommonPropsBuilder#buildDefault()
     * @see org.beiter.michael.eaudit4j.common.propsbuilder.MapBasedCommonPropsBuilder#build(Map)
     */
    public JdbcProperties() {

        // no code here, constructor just for java docs
    }

    /**
     * Creates a set of JDBC properties from an existing set of JDBC properties, making a defensive copy.
     *
     * @param properties The set of properties to copy
     * @throws NullPointerException When {@code properties} is {@code null}
     * @see JdbcProperties ()
     */

    public JdbcProperties(final JdbcProperties properties) {

        this();

        Validate.notNull(properties, "The validated object 'properties' is null");

        setInsertEventSqlStmt(properties.getInsertEventSqlStmt());
        setStringEncoding(properties.getStringEncoding());
        setEventIdFieldName(properties.getEventIdFieldName());
        setIndexedFields(properties.getIndexedFields());
        setIndexedFieldsMaxLength(properties.getIndexedFieldsMaxLength());
        setIndexedFieldsToLower(properties.isIndexedFieldsToLower());
        setInsertIndexedFieldSqlStmt(properties.getInsertIndexedFieldSqlStmt());
        setIndexedFieldSeparator(properties.getIndexedFieldSeparator());
        setIndexedFieldNameSeparator(properties.getIndexedFieldNameSeparator());
        setJndiConnectionName(properties.getJndiConnectionName());
        setDataSourceName(properties.getDataSourceName());
        setAdditionalProperties(properties.getAdditionalProperties());
    }

    /**
     * @return The event SQL "INSERT" statement (event table)
     * @see JdbcProperties#setInsertEventSqlStmt(String)
     */
    public final String getInsertEventSqlStmt() {

        // no need for defensive copies of String

        return insertEventSqlStmt;
    }

    /**
     * Set the SQL statement to insert event records in the event table.
     *
     * @param insertEventSqlStmt The SQL statement used to insert audit events to the database
     * @throws NullPointerException     When the {@code insertEventSqlStmt} is {@code null}
     * @throws IllegalArgumentException When the {@code insertEventSqlStmt} is {@code empty}
     */
    public final void setInsertEventSqlStmt(final String insertEventSqlStmt) {

        Validate.notBlank(insertEventSqlStmt, "The validated character sequence 'insertEventSqlStmt' is null or empty");

        // no need for defensive copies of String

        this.insertEventSqlStmt = insertEventSqlStmt;
    }

    /**
     * @return The String encoding
     * @see JdbcProperties#setStringEncoding(String)
     */
    public final String getStringEncoding() {

        // no need for defensive copies of String

        return stringEncoding;
    }

    /**
     * Set the String encoding to use when converting bytes to a String
     *
     * @param stringEncoding The String encoding to use
     * @throws NullPointerException     When the {@code stringEncoding} is {@code null}
     * @throws IllegalArgumentException When the {@code stringEncoding} is {@code empty}
     */
    public final void setStringEncoding(final String stringEncoding) {

        Validate.notBlank(stringEncoding, "The validated character sequence 'stringEncoding' is null or empty");

        // no need for defensive copies of String

        this.stringEncoding = stringEncoding;
    }

    /**
     * @return the name of the field holding a unique event ID
     * @see JdbcProperties#setEventIdFieldName(String)
     */
    public final String getEventIdFieldName() {

        // no need for defensive copies of String

        return eventIdFieldName;
    }

    /**
     * The name of the field in the event that holds a unique event ID.
     * <p>
     * This event ID can be created e.g. with the `org.beiter.michael.eaudit4j.processors.eventid.EventIdProcessor`
     * processor.
     * <p>
     * Note that the field (referenced in the INSERT SQL statements both for the events table and the search table)
     * that holds this value must be long enough to accept a value of the maximum length possible for the value
     * configured here.
     *
     * @param eventIdFieldName the length of the (random) event ID generated by this processor (must be greater 0)
     * @throws NullPointerException     When the {@code eventIdFieldName} is {@code null}
     * @throws IllegalArgumentException When the {@code eventIdFieldName} is {@code empty}
     */
    public final void setEventIdFieldName(final String eventIdFieldName) {

        Validate.notBlank(stringEncoding, "The validated character sequence 'eventIdFieldName' is null or empty");

        // no need for defensive copies of String

        this.eventIdFieldName = eventIdFieldName;
    }

    /**
     * @return The event fields to be added to the search / index table. May be {@code null} or empty if no fields have
     * been configured
     * @see JdbcProperties#setIndexedFields(String)
     */
    public final String getIndexedFields() {

        // no need for defensive copies of String

        return indexedFields;
    }

    /**
     * Set the fields to be added to the search / index table.
     * <p>
     * Provide a list of fields that is separated with the separation character specified in
     * {@link JdbcProperties#setIndexedFieldSeparator}. Define a mapping of
     * {@link org.beiter.michael.eaudit4j.common.Event} field names to search index field names (i.e. keys / names under
     * which the provided {@link org.beiter.michael.eaudit4j.common.Event} field will be stored in the index table) as
     * shown in the example below, using the character specified in the
     * {@link JdbcProperties#setIndexedFieldNameSeparator}.
     * <p>
     * Note that the mapping of an event field name to an search index field name is optional. If no indexed field names
     * separator is used for a specific field, then the event field name is used to store the field in the search table.
     * <p>
     * Set this to {@code null} or empty if none of the event fields should be included in the search table.
     * <p>
     * This example uses {@code ,} as the indexed fields separator, and {@code :} as the indexed field names separator.
     * <p>
     * Example: {@code eventActor:indexedActor,eventSubject,eventObject:indexedObject}
     *
     * @param indexedFields The list of fields to be included in the search table / index
     */
    public final void setIndexedFields(final String indexedFields) {

        // no need for validation, as we cannot possible validate all field names and null is allowed

        // no need for defensive copies of String

        this.indexedFields = indexedFields;
    }

    /**
     * @return the maximum length of an indexed event field to be persisted to the indexed fields database table
     * @see JdbcProperties#setIndexedFieldsMaxLength(int)
     */
    public final int getIndexedFieldsMaxLength() {

        // no need for defensive copies of int

        return indexedFieldsMaxLength;
    }

    /**
     * The maximum length of the values to be stored in indexed fields.
     * <p>
     * If the value of a specific indexed field in an event is longer than this setting, then the value of the field
     * is truncated to this length before it is inserted in the indexed fields table.
     *
     * @param indexedFieldsMaxLength the maximum length of the values to be stored in indexed fields (must be greater 0)
     * @throws IllegalArgumentException When the provided value of {@code indexedFieldsMaxLength} is out of range
     */
    public final void setIndexedFieldsMaxLength(final int indexedFieldsMaxLength) {

        Validate.inclusiveBetween(1, Integer.MAX_VALUE, indexedFieldsMaxLength);

        // no need for defensive copies of int

        this.indexedFieldsMaxLength = indexedFieldsMaxLength;
    }

    /**
     * @return whether an indexed event field should be converted to lowercase before being stored in the database
     * @see JdbcProperties#setIndexedFieldsToLower(boolean)
     */
    public final boolean isIndexedFieldsToLower() {

        // no need for defensive copies of int

        return indexedFieldsToLower;
    }

    /**
     * Whether to convert all characters in indexed fields to lowercase before they are stored in the indexed fields
     * database table.
     * <p>
     * Set this to `true` when using a database that only supports case-sensitive search, or if the case-insensitive
     * search on the database is not as performant as the case-sensitive search.
     *
     * @param indexedFieldsToLower whether to convert all characters in indexed fields to lowercase
     */
    public final void setIndexedFieldsToLower(final boolean indexedFieldsToLower) {

        // no need for validation on boolean

        // no need for defensive copies of int

        this.indexedFieldsToLower = indexedFieldsToLower;
    }

    /**
     * @return The indexed field SQL "INSERT" statement (search / indextable)
     * @see JdbcProperties#setInsertIndexedFieldSqlStmt(String)
     */
    public final String getInsertIndexedFieldSqlStmt() {

        // no need for defensive copies of String

        return insertIndexedFieldSqlStmt;
    }

    /**
     * Set the SQL statement to insert event records in the search / index table.
     *
     * @param insertIndexedFieldSqlStmt The SQL statement used to insert indexed audit fields to the database
     * @throws NullPointerException     When the {@code insertIndexedFieldSqlStmt} is {@code null}
     * @throws IllegalArgumentException When the {@code insertIndexedFieldSqlStmt} is {@code empty}
     */
    public final void setInsertIndexedFieldSqlStmt(final String insertIndexedFieldSqlStmt) {

        Validate.notBlank(insertEventSqlStmt,
                "The validated character sequence 'insertIndexedFieldSqlStmt' is null or empty");

        // no need for defensive copies of String

        this.insertIndexedFieldSqlStmt = insertIndexedFieldSqlStmt;
    }

    /**
     * @return The indexed field separator
     * @see JdbcProperties#setIndexedFieldSeparator(String)
     */
    public final String getIndexedFieldSeparator() {

        // no need for defensive copies of String

        return indexedFieldSeparator;
    }

    /**
     * Set the indexed fields separator character used in {@link JdbcProperties#setIndexedFields(String)}.
     * <p>
     * The length of this parameter must be equal to {@code 1}.
     *
     * @param indexedFieldSeparator The character used to separate fields
     * @throws NullPointerException     When the {@code mdcFieldSeparator} is {@code null}
     * @throws IllegalArgumentException When the {@code mdcFieldSeparator} is {@code empty} or its length is not 1
     */
    public final void setIndexedFieldSeparator(final String indexedFieldSeparator) {

        Validate.notBlank(indexedFieldSeparator,
                "The validated character sequence 'indexedFieldSeparator' is null or empty");
        Validate.inclusiveBetween(1, 1, indexedFieldSeparator.length(),
                "The length of the validated character sequence 'indexedFieldSeparator' is invalid. "
                        + "Expected 1, was " + indexedFieldSeparator.length());

        // no need for defensive copies of String

        this.indexedFieldSeparator = indexedFieldSeparator;
    }

    /**
     * @return The indexed field name separator
     * @see JdbcProperties#setIndexedFieldNameSeparator(String)
     */
    public final String getIndexedFieldNameSeparator() {

        // no need for defensive copies of String

        return indexedFieldNameSeparator;
    }

    /**
     * Set the field name separator character used in {@link JdbcProperties#setIndexedFields(String)}} to separate field
     * names in the search / index table from the event field name. This allows using a different field name in the
     * index / search table than in the event.
     * <p>
     * The length of this parameter must be equal to {@code 1}.
     *
     * @param indexedFieldNameSeparator The character used to separate field names in the search / index table from the
     *                                  event field name
     * @throws NullPointerException     When the {@code mdcFieldSeparator} is {@code null}
     * @throws IllegalArgumentException When the {@code mdcFieldSeparator} is {@code empty} or its length is not 1
     */
    public final void setIndexedFieldNameSeparator(final String indexedFieldNameSeparator) {

        Validate.notBlank(indexedFieldNameSeparator,
                "The validated character sequence 'indexedFieldNameSeparator' is null or empty");
        Validate.inclusiveBetween(1, 1, indexedFieldNameSeparator.length(),
                "The length of the validated characeter sequence 'indexedFieldNameSeparator' is invalid. "
                        + "Expected 1, was " + indexedFieldNameSeparator.length());

        // no need for defensive copies of String

        this.indexedFieldNameSeparator = indexedFieldNameSeparator;
    }

    /**
     * @return the name of the JNDI connection (when connecting through JNDI)
     * @see JdbcProperties#setJndiConnectionName(String)
     */
    public final String getJndiConnectionName() {

        // no need for defensive copies of String

        return jndiConnectionName;
    }

    /**
     * The name of the JNDI connection (when connecting through JNDI).
     *
     * @param jndiConnectionName the name of the JNDI connection to use
     * @throws NullPointerException     When the {@code jndiConnectionName} is {@code null}
     * @throws IllegalArgumentException When the {@code jndiConnectionName} is {@code empty}
     */
    public final void setJndiConnectionName(final String jndiConnectionName) {

        Validate.notBlank(stringEncoding, "The validated character sequence 'jndiConnectionName' is null or empty");

        // no need for defensive copies of String

        this.jndiConnectionName = jndiConnectionName;
    }

    /**
     * @return the name of the data source attribute (when connecting through a provided data source)
     * @see JdbcProperties#setJndiConnectionName(String)
     */
    public final String getDataSourceName() {

        // no need for defensive copies of String

        return dataSourceName;
    }

    /**
     * The name of the data source attribute in the {@link org.beiter.michael.eaudit4j.common.ProcessingObjects} of
     * the {@link AbstractJdbcProcessor#process(org.beiter.michael.eaudit4j.common.Event, String,
     * org.beiter.michael.eaudit4j.common.ProcessingObjects)} method (when connecting through a
     * data source that is provided by the integrating application).
     *
     * @param dataSourceName the name of the data source attribute
     * @throws NullPointerException     When the {@code jndiConnectionName} is {@code null}
     * @throws IllegalArgumentException When the {@code jndiConnectionName} is {@code empty}
     */
    public final void setDataSourceName(final String dataSourceName) {

        Validate.notBlank(stringEncoding, "The validated character sequence 'dataSourceName' is null or empty");

        // no need for defensive copies of String

        this.dataSourceName = dataSourceName;
    }

    /**
     * @return Any additional properties stored in this object that have not explicitly been parsed
     * @see JdbcProperties#setAdditionalProperties(Map)
     */
    public final Map<String, String> getAdditionalProperties() {

        // create a defensive copy of the map and all its properties
        if (this.additionalProperties == null) {
            // this should never happen!
            return new ConcurrentHashMap<>();
        } else {
            final Map<String, String> tempMap = new ConcurrentHashMap<>();
            // putAll() is safe here, because we always apply it on a ConcurrentHashMap
            tempMap.putAll(additionalProperties);

            return tempMap;
        }
    }

    /**
     * Any additional properties which have not been parsed, and for which no getter/setter exists, but are to be
     * stored in this object nevertheless.
     * <p>
     * This property is commonly used to preserve original properties from upstream components that are to be passed
     * on to downstream components unchanged. This properties set may or may not include properties that have been
     * extracted from the map, and been made available through this POJO.
     * <p>
     * Note that these additional properties may be <code>null</code> or empty, even in a fully populated POJO where
     * other properties commonly have values assigned to.
     *
     * @param additionalProperties The additional properties to store
     */
    public final void setAdditionalProperties(final Map<String, String> additionalProperties) {

        // create a defensive copy of the map and all its properties
        if (additionalProperties == null) {
            // create a new (empty) properties map if the provided parameter was null
            this.additionalProperties = new ConcurrentHashMap<>();
        } else {
            // create a defensive copy of the map and all its properties
            // the code looks a little more complicated than a simple "putAll()", but it catches situations
            // where a Map is provided that supports null values (e.g. a HashMap) vs Map implementations
            // that do not (e.g. ConcurrentHashMap).
            this.additionalProperties = new ConcurrentHashMap<>();
            for (final Map.Entry<String, String> entry : additionalProperties.entrySet()) {
                final String key = entry.getKey();
                final String value = entry.getValue();

                if (value != null) {
                    this.additionalProperties.put(key, value);
                }
            }
        }
    }
}
