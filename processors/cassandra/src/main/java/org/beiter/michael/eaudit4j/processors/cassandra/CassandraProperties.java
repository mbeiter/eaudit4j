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
package org.beiter.michael.eaudit4j.processors.cassandra;

import org.apache.commons.lang3.Validate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * This class specifies properties specific to the Cassandra Processor.
 */
// suppress warnings about the long variable names
@SuppressWarnings("PMD.LongVariable")
public class CassandraProperties {

    /**
     * @see CassandraProperties#setInsertEventCqlStmt(String)
     */
    private String insertEventCqlStmt;

    /**
     * @see CassandraProperties#setEventIdCqlParam(String)
     */
    private String eventIdCqlParam;

    /**
     * @see CassandraProperties#setAuditStreamNameCqlParam(String)
     */
    private String auditStreamNameCqlParam;

    /**
     * @see CassandraProperties#setEventJsonCqlParam(String)
     */
    private String eventJsonCqlParam;

    /**
     * @see CassandraProperties#setStringEncoding(String)
     */
    private String stringEncoding;

    /**
     * @see CassandraProperties#setEventIdFieldName(String)
     */
    private String eventIdFieldName;

    /**
     * @see CassandraProperties#setSessionName(String)
     */
    private String sessionName;

    /**
     * @see CassandraProperties#setAdditionalProperties(Map)
     */
    private Map<String, String> additionalProperties = new ConcurrentHashMap<>();

    /**
     * Constructs an empty set of Cassandra properties, with most values being set to <code>null</code>, 0, or empty
     * (depending on the type of the property). Usually this constructor is used if this configuration POJO is populated
     * in an automated fashion (e.g. injection). If you need to build them manually (possibly with defaults), use or
     * create a properties builder.
     * <p>
     * You can change the defaults with the setters.
     *
     * @see org.beiter.michael.eaudit4j.processors.cassandra.propsbuilder.MapBasedCassandraPropsBuilder#buildDefault()
     * @see org.beiter.michael.eaudit4j.processors.cassandra.propsbuilder.MapBasedCassandraPropsBuilder#build(Map)
     */
    public CassandraProperties() {

        // no code here, constructor just for java docs
    }

    /**
     * Creates a set of Cassandra properties from an existing set of Cassandra properties, making a defensive copy.
     *
     * @param properties The set of properties to copy
     * @throws NullPointerException When {@code properties} is {@code null}
     * @see CassandraProperties ()
     */

    public CassandraProperties(final CassandraProperties properties) {
        this();

        Validate.notNull(properties, "The validated object 'properties' is null");

        setInsertEventCqlStmt(properties.getInsertEventCqlStmt());
        setEventIdCqlParam(properties.getEventIdCqlParam());
        setAuditStreamNameCqlParam(properties.getAuditStreamNameCqlParam());
        setEventJsonCqlParam(properties.getEventJsonCqlParam());
        setStringEncoding(properties.getStringEncoding());
        setEventIdFieldName(properties.getEventIdFieldName());
        setSessionName(properties.getSessionName());
        setAdditionalProperties(properties.getAdditionalProperties());
    }

    /**
     * @return The event CQL "INSERT" statement (event table)
     * @see CassandraProperties#setInsertEventCqlStmt(String)
     */
    public final String getInsertEventCqlStmt() {

        // no need for defensive copies of String

        return insertEventCqlStmt;
    }

    /**
     * Set the CQL statement to insert event records in the event table.
     * <p>
     * The CQL statement must be parameterized, and it must use named parameters. It must also explicitly specify the
     * keyspace as {@code keyspace.tablename}.
     *
     * @param insertEventCqlStmt The CQL statement used to insert audit events to the database
     * @throws NullPointerException     When the {@code insertEventCqlStmt} is {@code null}
     * @throws IllegalArgumentException When the {@code insertEventCqlStmt} is {@code empty}
     */
    public final void setInsertEventCqlStmt(final String insertEventCqlStmt) {

        Validate.notBlank(insertEventCqlStmt, "The validated character sequence 'insertEventCqlStmt' is null or empty");

        // no need for defensive copies of String

        this.insertEventCqlStmt = insertEventCqlStmt;
    }

    /**
     * @return The name of the "event ID" field parameter in the event CQL "INSERT" statement (event table)
     * @see CassandraProperties#setEventIdCqlParam(String)
     */
    public final String getEventIdCqlParam() {

        // no need for defensive copies of String

        return eventIdCqlParam;
    }

    /**
     * Set the name of the "event ID" parameter in the event CQL "INSERT" parameterized statement when creating new
     * event records in the event table.
     * <p>
     * The value of this configuration setting must match a named parameter in the CQL "INSERT" parameterized statement.
     * <p>
     * Note that this field is <b>not</b> necessarily equal to the column name.
     *
     * @param eventIdCqlField The name of the "event ID" field in the event CQL "INSERT" statement (event table)
     * @throws NullPointerException     When the {@code insertEventCqlStmt} is {@code null}
     * @throws IllegalArgumentException When the {@code insertEventCqlStmt} is {@code empty}
     */
    public final void setEventIdCqlParam(final String eventIdCqlField) {

        Validate.notBlank(insertEventCqlStmt, "The validated character sequence 'eventIdCqlParam' is null or empty");

        // no need for defensive copies of String

        this.eventIdCqlParam = eventIdCqlField;
    }

    /**
     * @return The name of the "audit stream name" field parameter in the event CQL "INSERT" statement (event table)
     * @see CassandraProperties#setAuditStreamNameCqlParam(String)
     */
    public final String getAuditStreamNameCqlParam() {

        // no need for defensive copies of String

        return auditStreamNameCqlParam;
    }

    /**
     * Set the name of the "audit stream name" parameter in the event CQL "INSERT" parameterized statement when
     * creating new event records in the event table.
     * <p>
     * The value of this configuration setting must match a named parameter in the CQL "INSERT" parameterized statement.
     * <p>
     * Note that this field is <b>not</b> necessarily equal to the column name.
     *
     * @param auditStreamNameCqlField The name of the "audit stream name" field in the event CQL "INSERT" statement
     * @throws NullPointerException     When the {@code insertEventCqlStmt} is {@code null}
     * @throws IllegalArgumentException When the {@code insertEventCqlStmt} is {@code empty}
     */
    public final void setAuditStreamNameCqlParam(final String auditStreamNameCqlField) {

        Validate.notBlank(insertEventCqlStmt,
                "The validated character sequence 'auditStreamNameCqlParam' is null or empty");

        // no need for defensive copies of String

        this.auditStreamNameCqlParam = auditStreamNameCqlField;
    }

    /**
     * @return The name of the "event JSON" field parameter in the event CQL "INSERT" statement (event table)
     * @see CassandraProperties#setEventJsonCqlParam(String)
     */
    public final String getEventJsonCqlParam() {

        // no need for defensive copies of String

        return eventJsonCqlParam;
    }

    /**
     * Set the name of the "event JSON" parameter in the event CQL "INSERT" parameterized statement when creating new
     * event records in the event table.
     * <p>
     * The value of this configuration setting must match a named parameter in the CQL "INSERT" parameterized statement.
     * <p>
     * Note that this field is <b>not</b> necessarily equal to the column name.
     *
     * @param eventJsonCqlField The name of the "event JSON" field in the event CQL "INSERT" statement (event table)
     * @throws NullPointerException     When the {@code insertEventCqlStmt} is {@code null}
     * @throws IllegalArgumentException When the {@code insertEventCqlStmt} is {@code empty}
     */
    public final void setEventJsonCqlParam(final String eventJsonCqlField) {

        Validate.notBlank(insertEventCqlStmt, "The validated character sequence 'eventJsonCqlParam' is null or empty");

        // no need for defensive copies of String

        this.eventJsonCqlParam = eventJsonCqlField;
    }

    /**
     * @return The String encoding
     * @see CassandraProperties#setStringEncoding(String)
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
     * @see CassandraProperties#setEventIdFieldName(String)
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
     * Note that the field (referenced in the INSERT CQL statements both for the events table and the search table)
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
     * @return the name of the Cassandra session attribute
     * @see CassandraProperties#setSessionName(String)
     */
    public final String getSessionName() {

        // no need for defensive copies of String

        return sessionName;
    }

    /**
     * The name of the Cassandra session attribute in the {@link org.beiter.michael.eaudit4j.common.ProcessingObjects}
     * of the {@link CassandraProcessor#process(org.beiter.michael.eaudit4j.common.Event, String,
     * org.beiter.michael.eaudit4j.common.ProcessingObjects)} method (this processor is always connecting to a
     * Cassandra session that is provided by the integrating application).
     *
     * @param sessionName the name of the session attribute
     * @throws NullPointerException     When the {@code sessionName} is {@code null}
     * @throws IllegalArgumentException When the {@code sessionName} is {@code empty}
     */
    public final void setSessionName(final String sessionName) {

        Validate.notBlank(stringEncoding, "The validated character sequence 'sessionName' is null or empty");

        // no need for defensive copies of String

        this.sessionName = sessionName;
    }

    /**
     * @return Any additional properties stored in this object that have not explicitly been parsed
     * @see CassandraProperties#setAdditionalProperties(Map)
     */
    public final Map<String, String> getAdditionalProperties() {

        // create a defensive copy of the map and all its properties
        if (this.additionalProperties == null) {
            // this should never happen!
            return new ConcurrentHashMap<>();
        } else {
            final Map<String, String> tempMap = new ConcurrentHashMap<>();
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
            this.additionalProperties = new ConcurrentHashMap<>();
        } else {
            this.additionalProperties = new ConcurrentHashMap<>();
            this.additionalProperties.putAll(additionalProperties);
        }
    }
}
