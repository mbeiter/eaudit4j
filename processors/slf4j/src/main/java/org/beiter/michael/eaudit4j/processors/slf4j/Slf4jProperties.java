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
package org.beiter.michael.eaudit4j.processors.slf4j;

import org.apache.commons.lang3.Validate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class specifies properties specific to the slf4j Processor.
 */
// suppress warnings about the long variable names
@SuppressWarnings("PMD.LongVariable")
public class Slf4jProperties {

    /**
     * @see Slf4jProperties#setMarker(String)
     */
    private String marker;

    /**
     * @see Slf4jProperties#setStringEncoding(String)
     */
    private String stringEncoding;

    /**
     * @see Slf4jProperties#setAuditStreamFieldName(String)
     */
    private String auditStreamFieldName;

    /**
     * @see Slf4jProperties#setSerializedEventFieldName(String)
     */
    private String serializedEventFieldName;


    /**
     * @see Slf4jProperties#setMdcFields(String)
     */
    private String mdcFields;

    /**
     * @see Slf4jProperties#setMdcFieldSeparator(String)
     */
    private String mdcFieldSeparator;

    /**
     * @see Slf4jProperties#setMdcFieldNameSeparator(String)
     */
    private String mdcFieldNameSeparator;

    /**
     * @see Slf4jProperties#setAdditionalProperties(Map)
     */
    private Map<String, String> additionalProperties = new ConcurrentHashMap<>();

    /**
     * Constructs an empty set of slf4j properties, with most values being set to <code>null</code>, 0, or empty
     * (depending on the type of the property). Usually this constructor is used if this configuration POJO is populated
     * in an automated fashion (e.g. injection). If you need to build them manually (possibly with defaults), use or
     * create a properties builder.
     * <p>
     * You can change the defaults with the setters.
     *
     * @see org.beiter.michael.eaudit4j.common.propsbuilder.MapBasedCommonPropsBuilder#buildDefault()
     * @see org.beiter.michael.eaudit4j.common.propsbuilder.MapBasedCommonPropsBuilder#build(Map)
     */
    public Slf4jProperties() {

        // no code here, constructor just for java docs
    }

    /**
     * Creates a set of slf4j properties from an existing set of slf4j properties, making a defensive copy.
     *
     * @param properties The set of properties to copy
     * @throws NullPointerException When {@code properties} is {@code null}
     * @see Slf4jProperties ()
     */

    public Slf4jProperties(final Slf4jProperties properties) {

        this();

        Validate.notNull(properties, "The validated object 'properties' is null");

        setMarker(properties.getMarker());
        setStringEncoding(properties.getStringEncoding());
        setAuditStreamFieldName(properties.getAuditStreamFieldName());
        setSerializedEventFieldName(properties.getSerializedEventFieldName());
        setMdcFields(properties.getMdcFields());
        setMdcFieldSeparator(properties.getMdcFieldSeparator());
        setMdcFieldNameSeparator(properties.getMdcFieldNameSeparator());
        setAdditionalProperties(properties.getAdditionalProperties());
    }

    /**
     * @return The marker
     * @see Slf4jProperties#setMarker(String)
     */
    public final String getMarker() {

        // no need for defensive copies of String

        return marker;
    }

    /**
     * Set the marker to identify serialized audit events in slf4j.
     * <p>
     * The marker is used as a prefix that will be added to all events logged to slf4j.
     *
     * @param marker The marker used to mark audit message in slf4j
     * @throws NullPointerException     When the {@code marker} is {@code null}
     * @throws IllegalArgumentException When the {@code marker} is {@code empty}
     */
    public final void setMarker(final String marker) {

        Validate.notBlank(marker, "The validated character sequence 'marker' is null or empty");

        // no need for defensive copies of String

        this.marker = marker;
    }

    /**
     * @return The String encoding
     * @see Slf4jProperties#setStringEncoding(String)
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

        Validate.notBlank(marker, "The validated character sequence 'stringEncoding' is null or empty");

        // no need for defensive copies of String

        this.stringEncoding = stringEncoding;
    }

    /**
     * @return The name to use for the audit stream field in the MDC
     * @see Slf4jProperties#setAuditStreamFieldName(String)
     */
    public final String getAuditStreamFieldName() {

        // no need for defensive copies of String

        return auditStreamFieldName;
    }

    /**
     * Set the name to use for the audit stream field in the MDC
     *
     * @param auditStreamFieldName The name of the audit stream field
     * @throws NullPointerException     When the {@code stringEncoding} is {@code null}
     * @throws IllegalArgumentException When the {@code stringEncoding} is {@code empty}
     */
    public final void setAuditStreamFieldName(final String auditStreamFieldName) {

        Validate.notBlank(auditStreamFieldName,
                "The validated character sequence 'auditStreamFieldName' is null or empty");

        // no need for defensive copies of String

        this.auditStreamFieldName = auditStreamFieldName;
    }

    /**
     * @return The name to use for the serialized event field in the MDC
     * @see Slf4jProperties#setSerializedEventFieldName(String)
     */
    public final String getSerializedEventFieldName() {

        // no need for defensive copies of String

        return serializedEventFieldName;
    }

    /**
     * Set the name to use for the serialized event field in the MDC
     *
     * @param serializedEventFieldName The name of the serialized event field
     * @throws NullPointerException     When the {@code stringEncoding} is {@code null}
     * @throws IllegalArgumentException When the {@code stringEncoding} is {@code empty}
     */
    public final void setSerializedEventFieldName(final String serializedEventFieldName) {

        Validate.notBlank(serializedEventFieldName,
                "The validated character sequence 'serializedEventFieldName' is null or empty");

        // no need for defensive copies of String

        this.serializedEventFieldName = serializedEventFieldName;
    }

    /**
     * @return The fields to be included in the MDC. May be {@code null} or empty if no MDC fields have been configured
     * @see Slf4jProperties#setMdcFields(String)
     */
    public final String getMdcFields() {

        // no need for defensive copies of String

        return mdcFields;
    }

    /**
     * Set the fields to be included in the MDC, if the underlying logger supports MDC.
     * <p>
     * Provide a list of fields that is separated with the separation character specified in
     * {@link Slf4jProperties#setMdcFieldSeparator}. Define a mapping of
     * {@link org.beiter.michael.eaudit4j.common.Event} field names to MDC field names (i.e. names under which the
     * provided {@link org.beiter.michael.eaudit4j.common.Event} field will be made known to the MDC) as shown in the
     * example below, using the character specified in the {@link Slf4jProperties#setMdcFieldNameSeparator}.
     * <p>
     * Note that the mapping of an event field name to an MDC field name is optional. If no MDC field names separator
     * is used for a specific field, then the event field name is used to store the field in the MDC.
     * <p>
     * Set this to {@code null} or empty if none of the event fields should be included in the MDC.
     * <p>
     * This example uses {@code ,} as the MDC fields separator, and {@code :} as the MDC field names separator.
     * <p>
     * Example: {@code eventActor:mdcActor,eventSubject,eventObject:mdcObject}
     *
     * @param mdcFields The list of fields to be included in the MDC
     */
    public final void setMdcFields(final String mdcFields) {

        // no need for validation, as we cannot possible validate all field names and null is allowed

        // no need for defensive copies of String

        this.mdcFields = mdcFields;
    }

    /**
     * @return The MDC field separator
     * @see Slf4jProperties#setMdcFieldSeparator(String)
     */
    public final String getMdcFieldSeparator() {

        // no need for defensive copies of String

        return mdcFieldSeparator;
    }

    /**
     * Set the MDC field separator character used in {@link Slf4jProperties#setMdcFields(String)}}.
     * <p>
     * The length of this parameter must be equal to {@code 1}.
     *
     * @param mdcFieldSeparator The character used to separate fields
     * @throws NullPointerException     When the {@code mdcFieldSeparator} is {@code null}
     * @throws IllegalArgumentException When the {@code mdcFieldSeparator} is {@code empty} or its length is not 1
     */
    public final void setMdcFieldSeparator(final String mdcFieldSeparator) {

        Validate.notBlank(mdcFieldSeparator, "The validated character sequence 'mdcFieldSeparator' is null or empty");
        Validate.inclusiveBetween(1, 1, mdcFieldSeparator.length(),
                "The length of the validated character sequence 'mdcFieldSeparator' is invalid. Expected 1, was "
                        + mdcFieldSeparator.length());

        // no need for defensive copies of String

        this.mdcFieldSeparator = mdcFieldSeparator;
    }

    /**
     * @return The MDC field name separator
     * @see Slf4jProperties#setMdcFieldNameSeparator(String)
     */
    public final String getMdcFieldNameSeparator() {

        // no need for defensive copies of String

        return mdcFieldNameSeparator;
    }

    /**
     * Set the field name separator character used in {@link Slf4jProperties#setMdcFields(String)}} to separate field
     * names in the MDC from the event field name. This allows using a different field name in the MDC than in the
     * event.
     * <p>
     * The length of this parameter must be equal to {@code 1}.
     *
     * @param mdcFieldNameSeparator The character used to separate field names in the MDC from the event field name
     * @throws NullPointerException     When the {@code mdcFieldSeparator} is {@code null}
     * @throws IllegalArgumentException When the {@code mdcFieldSeparator} is {@code empty} or its length is not 1
     */
    public final void setMdcFieldNameSeparator(final String mdcFieldNameSeparator) {

        Validate.notBlank(mdcFieldNameSeparator,
                "The validated character sequence 'mdcFieldNameSeparator' is null or empty");
        Validate.inclusiveBetween(1, 1, mdcFieldNameSeparator.length(),
                "The length of the validated characeter sequence 'mdcFieldNameSeparator' is invalid. Expected 1, was "
                        + mdcFieldNameSeparator.length());

        // no need for defensive copies of String

        this.mdcFieldNameSeparator = mdcFieldNameSeparator;
    }

    /**
     * @return Any additional properties stored in this object that have not explicitly been parsed
     * @see Slf4jProperties#setAdditionalProperties(Map)
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
