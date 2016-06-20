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

import org.apache.commons.lang3.Validate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class specifies properties specific to the Timestamp Processor.
 */
// suppress warnings about the long variable names
@SuppressWarnings("PMD.LongVariable")
public class TimestampProperties {

    /**
     * @see TimestampProperties#setTimezone(String)
     */
    private String timezone;

    /**
     * @see TimestampProperties#setFormat(String)
     */
    private String format;

    /**
     * @see TimestampProperties#setEventFieldName(String)
     */
    private String eventFieldName;

    /**
     * @see TimestampProperties#setAdditionalProperties(Map)
     */
    private Map<String, String> additionalProperties = new ConcurrentHashMap<>();

    /**
     * Constructs an empty set of timestamp properties, with most values being set to <code>null</code>, 0, or empty
     * (depending on the type of the property). Usually this constructor is used if this configuration POJO is populated
     * in an automated fashion (e.g. injection). If you need to build them manually (possibly with defaults), use or
     * create a properties builder.
     * <p>
     * You can change the defaults with the setters.
     *
     * @see org.beiter.michael.eaudit4j.common.propsbuilder.MapBasedCommonPropsBuilder#buildDefault()
     * @see org.beiter.michael.eaudit4j.common.propsbuilder.MapBasedCommonPropsBuilder#build(Map)
     */
    public TimestampProperties() {

        // no code here, constructor just for java docs
    }

    /**
     * Creates a set of timestamp properties from an existing set of timestamp properties, making a defensive copy.
     *
     * @param properties The set of properties to copy
     * @throws NullPointerException When {@code properties} is {@code null}
     * @see TimestampProperties ()
     */
    public TimestampProperties(final TimestampProperties properties) {

        this();

        Validate.notNull(properties, "The validated object 'properties' is null");

        setTimezone(properties.getTimezone());
        setFormat(properties.getFormat());
        setEventFieldName(properties.getEventFieldName());
        setAdditionalProperties(properties.getAdditionalProperties());
    }

    /**
     * @return The timezone for the timestamp
     * @see TimestampProperties#setTimezone(String)
     */
    public final String getTimezone() {

        // no need for defensive copies of String

        return timezone;
    }

    /**
     * Set the timezone for the timestamps.
     * <p>
     * The timezone of the timestamp mey be configured either as:
     * <ul>
     * <li>an abbreviation (e.g. "PST") or </li>
     * <li>a full name (e.g. "America/Los_Angeles") or</li>
     * <li>a custom ID such as "GMT-8:00"</li>
     * </ul>
     * <p>
     * The support of abbreviations is deprecated and only and full names should
     * be used. See {@link java.util.TimeZone} for a list of available options.
     * <p>
     * If the provided timezone configuration is unknown, then "GMT" is being used
     * as a fallback.
     * <p>
     * Note that in most distributed deployments, it is commonly a good approach
     * to use a common timezone (e.g. "UTC") as the timezone, instead of the local
     * machine's timezone.
     *
     * @param timezone The timezone of this event
     */
    public final void setTimezone(final String timezone) {

        Validate.notBlank(timezone, "The validated character sequence 'timezone' is null or empty");

        // no need for defensive copies of String

        this.timezone = timezone;
    }

    /**
     * @return The format for the timestamp
     * @see TimestampProperties#setTimezone(String)
     */
    public final String getFormat() {

        // no need for defensive copies of String

        return format;
    }

    /**
     * Set the format for the timestamps.
     * <p>
     * Note that the timezone is nor added as a separate field to the timestamp. Hence,
     * in most deployments, it is a good idea to include the timezone in the timestamp
     * format string.
     * <p>
     * See {@link java.text.SimpleDateFormat} for a list of available options.
     *
     * @param format The format string to use for formatting the timestamp string
     */
    public final void setFormat(final String format) {

        Validate.notBlank(timezone, "The validated character sequence 'format' is null or empty");

        // no need for defensive copies of String

        this.format = format;
    }

    /**
     * @return The field name used to store the timestamp in events
     * @see TimestampProperties#setEventFieldName(String)
     */
    public final String getEventFieldName() {

        // no need for defensive copies of String

        return eventFieldName;
    }

    /**
     * Set the field name to be used when storing the timestamp in audit events
     *
     * @param eventFieldName The field name to store the timestamp in
     * @throws NullPointerException     When the {@code eventFieldName} is {@code null}
     * @throws IllegalArgumentException When the {@code eventFieldName} is {@code empty}
     */
    public final void setEventFieldName(final String eventFieldName) {

        Validate.notBlank(eventFieldName, "The validated character sequence 'eventFieldName' is null or empty");

        // no need for defensive copies of String

        this.eventFieldName = eventFieldName;
    }

    /**
     * @return Any additional properties stored in this object that have not explicitly been parsed
     * @see TimestampProperties#setAdditionalProperties(Map)
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
