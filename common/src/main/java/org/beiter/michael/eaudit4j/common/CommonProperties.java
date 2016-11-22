/*
 * #%L
 * This file is part of eAudit4j, a library for creating pluggable auditing solutions.
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
package org.beiter.michael.eaudit4j.common;

import org.apache.commons.lang3.Validate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class specifies common properties.
 */
// CHECKSTYLE:OFF
// this is flagged in checkstyle with a missing whitespace before '}', which is a bug in checkstyle
// suppress warnings about the long variable names and field names count (okay for this config POJO)
@SuppressWarnings({"PMD.LongVariable", "PMD.TooManyFields"})
// CHECKSTYLE:ON
public class CommonProperties {

    /**
     * @see CommonProperties#setAuditClassName(String)
     */
    private String auditClassName;

    /**
     * @see CommonProperties#setDefaultAuditStream(String)
     */
    private String defaultAuditStream;

    /**
     * @see CommonProperties#setEncoding(String)
     */
    private String encoding;

    /**
     * @see CommonProperties#setDateFormat(String)
     */
    private String dateFormat;

    /**
     * @see CommonProperties#setProcessors(String)
     */
    private String processors;

    /**
     * @see CommonProperties#setFailOnMissingProcessors(boolean)
     */
    private boolean failOnMissingProcessors;

    /**
     * @see CommonProperties#setFieldNameEventType(String)
     */
    private String fnEventType;

    /**
     * @see CommonProperties#setFieldNameEventGroupType(String)
     */
    private String fnEventGroupType;

    /**
     * @see CommonProperties#setFieldNameSubject(String)
     */
    private String fnSubject;

    /**
     * @see CommonProperties#setFieldNameSubjectLocation(String)
     */
    private String fnSubjectLocation;

    /**
     * @see CommonProperties#setFieldNameActor(String)
     */
    private String fnActor;

    /**
     * @see CommonProperties#setFieldNameObject(String)
     */
    private String fnObject;

    /**
     * @see CommonProperties#setFieldNameObjectLocation(String)
     */
    private String fnObjectLocation;

    /**
     * @see CommonProperties#setFieldNameContentBeforeOperation(String)
     */
    private String fnObjectContentBeforeOperation;

    /**
     * @see CommonProperties#setFieldNameContentAfterOperation(String)
     */
    private String fnObjectContentAfterOperation;

    /**
     * @see CommonProperties#setFieldNameResult(String)
     */
    private String fnResult;

    /**
     * @see CommonProperties#setFieldNameResultSummary(String)
     */
    private String fnResultSummary;

    /**
     * @see CommonProperties#setFieldNameEventSummary(String)
     */
    private String fnEventSummary;


    /**
     * @see CommonProperties#setAdditionalProperties(Map<String, String>)
     */
    private Map<String, String> additionalProperties = new ConcurrentHashMap<>();

    /**
     * Constructs an empty set of common properties, with most values being set to <code>null</code>, 0, or empty
     * (depending on the type of the property). Usually this constructor is used if this configuration POJO is populated
     * in an automated fashion (e.g. injection). If you need to build them manually (possibly with defaults), use or
     * create a properties builder.
     * <p>
     * You can change the defaults with the setters.
     *
     * @see org.beiter.michael.eaudit4j.common.propsbuilder.MapBasedCommonPropsBuilder#buildDefault()
     * @see org.beiter.michael.eaudit4j.common.propsbuilder.MapBasedCommonPropsBuilder#build(java.util.Map)
     */
    public CommonProperties() {

        // no code here, constructor just for java docs
    }

    /**
     * Creates a set of common properties from an existing set of common properties, making a defensive copy.
     *
     * @param properties The set of properties to copy
     * @throws NullPointerException When {@code properties} is {@code null}
     * @see CommonProperties#CommonProperties()
     */
    public CommonProperties(final CommonProperties properties) {

        this();

        Validate.notNull(properties, "The validated object 'properties' is null");

        setAuditClassName(properties.getAuditClassName());
        setDefaultAuditStream(properties.getDefaultAuditStream());
        setEncoding(properties.getEncoding());
        setDateFormat(properties.getDateFormat());
        setProcessors(properties.getProcessors());
        setFailOnMissingProcessors(properties.isFailOnMissingProcessors());
        setFieldNameEventType(properties.getFieldNameEventType());
        setFieldNameEventGroupType(properties.getFieldNameEventGroupType());
        setFieldNameSubject(properties.getFieldNameSubject());
        setFieldNameSubjectLocation(properties.getFieldNameSubjectLocation());
        setFieldNameActor(properties.getFieldNameActor());
        setFieldNameObject(properties.getFieldNameObject());
        setFieldNameObjectLocation(properties.getFieldNameObjectLocation());
        setFieldNameContentBeforeOperation(properties.getFieldNameContentBeforeOperation());
        setFieldNameContentAfterOperation(properties.getFieldNameContentAfterOperation());
        setFieldNameResult(properties.getFieldNameResult());
        setFieldNameResultSummary(properties.getFieldNameResultSummary());
        setFieldNameEventSummary(properties.getFieldNameEventSummary());
        setAdditionalProperties(properties.getAdditionalProperties());
    }

    /**
     * @return The audit class to use for auditing (implements {@link Audit}
     * @see CommonProperties#setAuditClassName(String)
     */
    public final String getAuditClassName() {

        // no need for defensive copies of String

        return auditClassName;
    }

    /**
     * Set the audit class to use for auditing (implements {@link Audit}.
     *
     * @param auditClassName The name of the audit class to use for auditing
     * @throws NullPointerException When the {@code auditClassName} is {@code null}
     */
    public final void setAuditClassName(final String auditClassName) {

        Validate.notNull(auditClassName, "The validated object 'auditClassName' is null");

        // no need for defensive copies of String

        this.auditClassName = auditClassName;
    }

    /**
     * @return The default audit stream to use when no audit stream has been provided
     * @see CommonProperties#setDefaultAuditStream(String)
     */
    public final String getDefaultAuditStream() {

        // no need for defensive copies of String

        return defaultAuditStream;
    }

    /**
     * Set the default audit stream to use when no audit stream has been provided.
     *
     * @param defaultAuditStream The name of the default audit stream
     * @throws NullPointerException When the {@code defaultAuditStream} is {@code null}
     */
    public final void setDefaultAuditStream(final String defaultAuditStream) {

        Validate.notNull(defaultAuditStream, "The validated object 'defaultAuditStream' is null");

        // no need for defensive copies of String

        this.defaultAuditStream = defaultAuditStream;
    }

    /**
     * @return The character encoding to use when converting @{code char} to {@code byte} and vice versa.
     * @see CommonProperties#setEncoding(String)
     */
    public final String getEncoding() {

        // no need for defensive copies of String

        return encoding;
    }

    /**
     * Set the character encoding to use when converting @{code char} to {@code byte} and vice versa.
     *
     * @param encoding The name of the encoding (e.g. UTF-8, see {@link java.nio.charset.Charset})
     * @throws NullPointerException When the {@code encoding} is {@code null}
     */
    public final void setEncoding(final String encoding) {

        Validate.notNull(encoding, "The validated object 'encoding' is null");

        // no need for defensive copies of String

        this.encoding = encoding;
    }

    /**
     * @return The date format string to use when rendering a timestamp into human readable form
     * @see CommonProperties#setDateFormat(String)
     */
    public final String getDateFormat() {

        // no need for defensive copies of String

        return dateFormat;
    }

    /**
     * Set the date format string to use when rendering a timestamp into human readable form
     *
     * @param dateFormat The date format string to use
     */
    public final void setDateFormat(final String dateFormat) {

        // no need for validation, as we cannot possible validate all date format strings and null is allowed

        // no need for defensive copies of String

        this.dateFormat = dateFormat;
    }

    /**
     * @return The processor chain to instantiate
     * @see CommonProperties#setProcessors(String)
     */
    public final String getProcessors() {

        // no need for defensive copies of String

        return processors;
    }

    /**
     * Set the processor chain to instantiate.
     * <p>
     * Provide the processor chain as a comma separated list of fully qualified class names. Classes must implement the
     * {@link Processor} interface. The order of the processors matter, the chain will be built from left to right.
     *
     * @param processors The date format string to use
     */
    public final void setProcessors(final String processors) {

        // no need for validation, as we cannot possible validate all possible processors and null is allowed

        // no need for defensive copies of String

        this.processors = processors;
    }

    /**
     * @return the indication of whether to fail auditing if no processors are configured in the processing chain
     * @see CommonProperties#setFailOnMissingProcessors(boolean)
     */
    public final boolean isFailOnMissingProcessors() {

        // no need for defensive copies of boolean

        return failOnMissingProcessors;
    }

    /**
     * The indication of whether at least one processor is required in the processing chain, that is, whether to fail
     * the audit operation if not processors have been configured.
     *
     * @param failOnMissingProcessors the indication of whether at least one processor is required
     */
    public final void setFailOnMissingProcessors(final boolean failOnMissingProcessors) {

        // no need for validation, as boolean cannot be null and all possible values are allowed
        // no need for defensive copies of boolean

        this.failOnMissingProcessors = failOnMissingProcessors;
    }

    /**
     * @return The field name of the named field "Event Type"
     * @see CommonProperties#setFieldNameEventType(String)
     */
    public final String getFieldNameEventType() {

        // no need for defensive copies of String

        return fnEventType;
    }

    /**
     * The field name of the named field "Event Type".
     * <p>
     * This field name will be used when instantiating the named field.
     *
     * @param fieldName The name of the named field.
     */
    public final void setFieldNameEventType(final String fieldName) {

        // no need for validation, as we cannot possible validate all possible processors and null is allowed

        // no need for defensive copies of String

        this.fnEventType = fieldName;
    }

    /**
     * @return The field name of the named field "Event Group Type"
     * @see CommonProperties#setFieldNameEventGroupType(String)
     */
    public final String getFieldNameEventGroupType() {

        // no need for defensive copies of String

        return fnEventGroupType;
    }

    /**
     * The field name of the named field "Event Group Type".
     * <p>
     * This field name will be used when instantiating the named field.
     *
     * @param fieldName The name of the named field.
     */
    public final void setFieldNameEventGroupType(final String fieldName) {

        // no need for validation, as we cannot possible validate all possible processors and null is allowed

        // no need for defensive copies of String

        this.fnEventGroupType = fieldName;
    }

    /**
     * @return The field name of the named field "Subject"
     * @see CommonProperties#setFieldNameSubject(String)
     */
    public final String getFieldNameSubject() {

        // no need for defensive copies of String

        return fnSubject;
    }

    /**
     * The field name of the named field "Subject".
     * <p>
     * This field name will be used when instantiating the named field.
     *
     * @param fieldName The name of the named field.
     */
    public final void setFieldNameSubject(final String fieldName) {

        // no need for validation, as we cannot possible validate all possible processors and null is allowed

        // no need for defensive copies of String

        this.fnSubject = fieldName;
    }

    /**
     * @return The field name of the named field "SubjectLocation"
     * @see CommonProperties#setFieldNameSubjectLocation(String)
     */
    public final String getFieldNameSubjectLocation() {

        // no need for defensive copies of String

        return fnSubjectLocation;
    }

    /**
     * The field name of the named field "Subject Location".
     * <p>
     * This field name will be used when instantiating the named field.
     *
     * @param fieldName The name of the named field.
     */
    public final void setFieldNameSubjectLocation(final String fieldName) {

        // no need for validation, as we cannot possible validate all possible processors and null is allowed

        // no need for defensive copies of String

        this.fnSubjectLocation = fieldName;
    }

    /**
     * @return The field name of the named field "Actor"
     * @see CommonProperties#setFieldNameActor(String)
     */
    public final String getFieldNameActor() {

        // no need for defensive copies of String

        return fnActor;
    }

    /**
     * The field name of the named field "Actor".
     * <p>
     * This field name will be used when instantiating the named field.
     *
     * @param fieldName The name of the named field.
     */
    public final void setFieldNameActor(final String fieldName) {

        // no need for validation, as we cannot possible validate all possible processors and null is allowed

        // no need for defensive copies of String

        this.fnActor = fieldName;
    }

    /**
     * @return The field name of the named field "Object"
     * @see CommonProperties#setFieldNameObject(String)
     */
    public final String getFieldNameObject() {

        // no need for defensive copies of String

        return fnObject;
    }

    /**
     * The field name of the named field "Object".
     * <p>
     * This field name will be used when instantiating the named field.
     *
     * @param fieldName The name of the named field.
     */
    public final void setFieldNameObject(final String fieldName) {

        // no need for validation, as we cannot possible validate all possible processors and null is allowed

        // no need for defensive copies of String

        this.fnObject = fieldName;
    }

    /**
     * @return The field name of the named field "Object Location"
     * @see CommonProperties#setFieldNameObjectLocation(String)
     */
    public final String getFieldNameObjectLocation() {

        // no need for defensive copies of String

        return fnObjectLocation;
    }

    /**
     * The field name of the named field "Object Location".
     * <p>
     * This field name will be used when instantiating the named field.
     *
     * @param fieldName The name of the named field.
     */
    public final void setFieldNameObjectLocation(final String fieldName) {

        // no need for validation, as we cannot possible validate all possible processors and null is allowed

        // no need for defensive copies of String

        this.fnObjectLocation = fieldName;
    }

    /**
     * @return The field name of the named field "Content Before Operation"
     * @see CommonProperties#setFieldNameContentBeforeOperation(String)
     */
    public final String getFieldNameContentBeforeOperation() {

        // no need for defensive copies of String

        return fnObjectContentBeforeOperation;
    }

    /**
     * The field name of the named field "Content Before Operation".
     * <p>
     * This field name will be used when instantiating the named field.
     *
     * @param fieldName The name of the named field.
     */
    public final void setFieldNameContentBeforeOperation(final String fieldName) {

        // no need for validation, as we cannot possible validate all possible processors and null is allowed

        // no need for defensive copies of String

        this.fnObjectContentBeforeOperation = fieldName;
    }

    /**
     * @return The field name of the named field "Content After Operation"
     * @see CommonProperties#setFieldNameContentAfterOperation(String)
     */
    public final String getFieldNameContentAfterOperation() {

        // no need for defensive copies of String

        return fnObjectContentAfterOperation;
    }

    /**
     * The field name of the named field "Content After Operation".
     * <p>
     * This field name will be used when instantiating the named field.
     *
     * @param fieldName The name of the named field.
     */
    public final void setFieldNameContentAfterOperation(final String fieldName) {

        // no need for validation, as we cannot possible validate all possible processors and null is allowed

        // no need for defensive copies of String

        this.fnObjectContentAfterOperation = fieldName;
    }

    /**
     * @return The field name of the named field "Result"
     * @see CommonProperties#setFieldNameResult(String)
     */
    public final String getFieldNameResult() {

        // no need for defensive copies of String

        return fnResult;
    }

    /**
     * The field name of the named field "Result".
     * <p>
     * This field name will be used when instantiating the named field.
     *
     * @param fieldName The name of the named field.
     */
    public final void setFieldNameResult(final String fieldName) {

        // no need for validation, as we cannot possible validate all possible processors and null is allowed

        // no need for defensive copies of String

        this.fnResult = fieldName;
    }

    /**
     * @return The field name of the named field "Result Summary"
     * @see CommonProperties#setFieldNameResultSummary(String)
     */
    public final String getFieldNameResultSummary() {

        // no need for defensive copies of String

        return fnResultSummary;
    }

    /**
     * The field name of the named field "Result Summary".
     * <p>
     * This field name will be used when instantiating the named field.
     *
     * @param fieldName The name of the named field.
     */
    public final void setFieldNameResultSummary(final String fieldName) {

        // no need for validation, as we cannot possible validate all possible processors and null is allowed

        // no need for defensive copies of String

        this.fnResultSummary = fieldName;
    }

    /**
     * @return The field name of the named field "Event Summary"
     * @see CommonProperties#setFieldNameEventSummary(String)
     */
    public final String getFieldNameEventSummary() {

        // no need for defensive copies of String

        return fnEventSummary;
    }

    /**
     * The field name of the named field "Event Summary".
     * <p>
     * This field name will be used when instantiating the named field.
     *
     * @param fieldName The name of the named field.
     */
    public final void setFieldNameEventSummary(final String fieldName) {

        // no need for validation, as we cannot possible validate all possible processors and null is allowed

        // no need for defensive copies of String

        this.fnEventSummary = fieldName;
    }

    /**
     * @return Any additional properties stored in this object that have not explicitly been parsed
     * @see CommonProperties#setAdditionalProperties(Map)
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
