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
package org.beiter.michael.eaudit4j.common.propsbuilder;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.beiter.michael.eaudit4j.common.CommonProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class builds a set of {@link CommonProperties} using the settings obtained from a Map.
 * <p>
 * Use the keys from the various KEY_* fields to properly populate the Map before calling this class' methods.
 */
// CHECKSTYLE:OFF
// this is flagged in checkstyle with a missing whitespace before '}', which is a bug in checkstyle
// suppress warnings about the long variable names
@SuppressWarnings({"PMD.LongVariable"})
// CHECKSTYLE:ON
public final class MapBasedCommonPropsBuilder {

    /**
     * The logger object for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(MapBasedCommonPropsBuilder.class);

    // #################
    // # Default values
    // #################

    /**
     * @see CommonProperties#setAuditClassName(String)
     */
    public static final String DEFAULT_AUDIT_CLASS_NAME =
            org.beiter.michael.eaudit4j.common.impl.SyncAudit.class.getCanonicalName();

    /**
     * @see CommonProperties#setDefaultAuditStream(String)
     */
    public static final String DEFAULT_AUDIT_STREAM = "Default audit stream - CONFIGURE ME!";

    /**
     * @see CommonProperties#setEncoding(String)
     */
    public static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * @see CommonProperties#setDateFormat(String)
     */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    /**
     * @see CommonProperties#setProcessors(String)
     */
    public static final String DEFAULT_PROCESSORS = null;

    /**
     * @see CommonProperties#setFailOnMissingProcessors(boolean)
     */
    public static final boolean DEFAULT_FAIL_ON_MISSING_PROCESSORS = true;

    /**
     * @see CommonProperties#setFieldNameEventType(String)
     */
    public static final String DEFAULT_FIELD_NAME_EVENT_TYPE = "eventType";

    /**
     * @see CommonProperties#setFieldNameEventGroupType(String)
     */
    public static final String DEFAULT_FIELD_NAME_EVENT_GROUP_TYPE = "eventGroupType";

    /**
     * @see CommonProperties#setFieldNameSubject(String)
     */
    public static final String DEFAULT_FIELD_NAME_SUBJECT = "subject";

    /**
     * @see CommonProperties#setFieldNameSubjectLocation(String)
     */
    public static final String DEFAULT_FIELD_NAME_SUBJECT_LOCATION = "subjectLocation";

    /**
     * @see CommonProperties#setFieldNameActor(String)
     */
    public static final String DEFAULT_FIELD_NAME_ACTOR = "actor";

    /**
     * @see CommonProperties#setFieldNameObject(String)
     */
    public static final String DEFAULT_FIELD_NAME_OBJECT = "object";

    /**
     * @see CommonProperties#setFieldNameObjectLocation(String)
     */
    public static final String DEFAULT_FIELD_NAME_OBJECT_LOCATION = "objectLocation";

    /**
     * @see CommonProperties#setFieldNameContentBeforeOperation(String)
     */
    public static final String DEFAULT_FIELD_NAME_CONTENT_BEFORE_OPERATION = "contentBeforeOperation";

    /**
     * @see CommonProperties#setFieldNameContentAfterOperation(String)
     */
    public static final String DEFAULT_FIELD_NAME_CONTENT_AFTER_OPERATION = "contentAfterOperation";

    /**
     * @see CommonProperties#setFieldNameResult(String)
     */
    public static final String DEFAULT_FIELD_NAME_RESULT = "result";

    /**
     * @see CommonProperties#setFieldNameResultSummary(String)
     */
    public static final String DEFAULT_FIELD_NAME_RESULT_SUMMARY = "resultSummary";

    /**
     * @see CommonProperties#setFieldNameEventSummary(String)
     */
    public static final String DEFAULT_FIELD_NAME_EVENT_SUMMARY = "eventSummary";

    // #####################
    // # Configuration Keys
    // #####################

    /**
     * @see CommonProperties#setAuditClassName(String)
     */
    public static final String KEY_AUDIT_CLASS_NAME = "audit.auditClassName";

    /**
     * @see CommonProperties#setDefaultAuditStream(String)
     */
    public static final String KEY_DEFAULT_AUDIT_STREAM = "audit.defaultAuditStreamName";

    /**
     * @see CommonProperties#setEncoding(String)
     */
    public static final String KEY_ENCODING = "audit.encoding";

    /**
     * @see CommonProperties#setDateFormat(String)
     */
    public static final String KEY_DATE_FORMAT = "audit.dateFormat";

    /**
     * @see CommonProperties#setProcessors(String)
     */
    public static final String KEY_PROCESSORS = "audit.processors";

    /**
     * @see CommonProperties#setFailOnMissingProcessors(boolean) (boolean)
     */
    public static final String KEY_FAIL_ON_MISSING_PROCESSORS = "audit.failOnMissingProcessors";

    /**
     * @see CommonProperties#setFieldNameEventType(String)
     */
    public static final String KEY_FIELD_NAME_EVENT_TYPE = "audit.fieldName.eventType";

    /**
     * @see CommonProperties#setFieldNameEventGroupType(String)
     */
    public static final String KEY_FIELD_NAME_EVENT_GROUP_TYPE = "audit.fieldName.eventGroupType";

    /**
     * @see CommonProperties#setFieldNameSubject(String)
     */
    public static final String KEY_FIELD_NAME_SUBJECT = "audit.fieldName.subject";

    /**
     * @see CommonProperties#setFieldNameSubjectLocation(String)
     */
    public static final String KEY_FIELD_NAME_SUBJECT_LOCATION = "audit.fieldName.subjectLocation";

    /**
     * @see CommonProperties#setFieldNameActor(String)
     */
    public static final String KEY_FIELD_NAME_ACTOR = "audit.fieldName.actor";

    /**
     * @see CommonProperties#setFieldNameObject(String)
     */
    public static final String KEY_FIELD_NAME_OBJECT = "audit.fieldName.object";

    /**
     * @see CommonProperties#setFieldNameObjectLocation(String)
     */
    public static final String KEY_FIELD_NAME_OBJECT_LOCATION = "audit.fieldName.objectLocation";

    /**
     * @see CommonProperties#setFieldNameContentBeforeOperation(String)
     */
    public static final String KEY_FIELD_NAME_CONTENT_BEFORE_OPERATION = "audit.fieldName.contentBeforeOperation";

    /**
     * @see CommonProperties#setFieldNameContentAfterOperation(String)
     */
    public static final String KEY_FIELD_NAME_CONTENT_AFTER_OPERATION = "audit.fieldName.contentAfterOperation";

    /**
     * @see CommonProperties#setFieldNameResult(String)
     */
    public static final String KEY_FIELD_NAME_RESULT = "audit.fieldName.result";

    /**
     * @see CommonProperties#setFieldNameResultSummary(String)
     */
    public static final String KEY_FIELD_NAME_RESULT_SUMMARY = "audit.fieldName.resultSummary";

    /**
     * @see CommonProperties#setFieldNameEventSummary(String)
     */
    public static final String KEY_FIELD_NAME_EVENT_SUMMARY = "audit.fieldName.eventSummary";

    /**
     * A private constructor to prevent instantiation of this class
     */
    private MapBasedCommonPropsBuilder() {
    }

    /**
     * Creates a set of common properties that use the defaults as specified in this class.
     *
     * @return A set of common properties with (reasonable) defaults
     * @see MapBasedCommonPropsBuilder
     */
    public static CommonProperties buildDefault() {

        return build(new ConcurrentHashMap<String, String>());
    }

    /**
     * Initialize a set of common properties based on key / values in a <code>HashMap</code>.
     *
     * @param properties A <code>HashMap</code> with configuration properties, using the keys as specified in this class
     * @return A {@link CommonProperties} object with default values, plus the provided parameters
     * @throws NullPointerException When {@code properties} is {@code null}
     */
    // CHECKSTYLE:OFF
    // this is flagged in checkstyle with a missing whitespace before '}', which is a bug in checkstyle
    // suppress warnings about this method being too long (not much point in splitting up this one!)
    // suppress warnings about this method being too complex (can't extract a generic subroutine to reduce exec paths)
    @SuppressWarnings({"PMD.ExcessiveMethodLength", "PMD.NPathComplexity", "PMD.CyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.NcssMethodCount"})
    // CHECKSTYLE:ON
    public static CommonProperties build(final Map<String, String> properties) {

        Validate.notNull(properties, "The validated object 'properties' is null");

        final CommonProperties commonProps = new CommonProperties();
        String tmp = properties.get(KEY_AUDIT_CLASS_NAME);
        if (StringUtils.isNotEmpty(tmp)) {
            commonProps.setAuditClassName(tmp);
            logValue(KEY_AUDIT_CLASS_NAME, tmp);
        } else {
            commonProps.setAuditClassName(DEFAULT_AUDIT_CLASS_NAME);
            logDefault(KEY_AUDIT_CLASS_NAME, DEFAULT_AUDIT_CLASS_NAME);
        }

        tmp = properties.get(KEY_DEFAULT_AUDIT_STREAM);
        if (StringUtils.isNotEmpty(tmp)) {
            commonProps.setDefaultAuditStream(tmp);
            logValue(KEY_DEFAULT_AUDIT_STREAM, tmp);
        } else {
            commonProps.setDefaultAuditStream(DEFAULT_AUDIT_STREAM);
            logDefault(KEY_DEFAULT_AUDIT_STREAM, DEFAULT_AUDIT_STREAM);
        }

        tmp = properties.get(KEY_ENCODING);
        if (StringUtils.isNotEmpty(tmp)) {
            commonProps.setEncoding(tmp);
            logValue(KEY_ENCODING, tmp);
        } else {
            commonProps.setEncoding(DEFAULT_ENCODING);
            logDefault(KEY_ENCODING, DEFAULT_ENCODING);
        }

        tmp = properties.get(KEY_DATE_FORMAT);
        if (StringUtils.isNotEmpty(tmp)) {
            commonProps.setDateFormat(tmp);
            logValue(KEY_DATE_FORMAT, tmp);
        } else {
            commonProps.setDateFormat(DEFAULT_DATE_FORMAT);
            logDefault(KEY_DATE_FORMAT, DEFAULT_DATE_FORMAT);
        }

        tmp = properties.get(KEY_PROCESSORS);
        if (StringUtils.isNotEmpty(tmp)) {
            commonProps.setProcessors(tmp);
            logValue(KEY_PROCESSORS, tmp);
        } else {
            commonProps.setProcessors(DEFAULT_PROCESSORS);
            logDefault(KEY_PROCESSORS, DEFAULT_PROCESSORS);
        }

        tmp = properties.get(KEY_FAIL_ON_MISSING_PROCESSORS);
        if (StringUtils.isNotEmpty(tmp)) {
            commonProps.setFailOnMissingProcessors(Boolean.parseBoolean(tmp));
            logValue(KEY_FAIL_ON_MISSING_PROCESSORS, tmp);
        } else {
            commonProps.setFailOnMissingProcessors(DEFAULT_FAIL_ON_MISSING_PROCESSORS);
            logDefault(KEY_FAIL_ON_MISSING_PROCESSORS, String.valueOf(DEFAULT_FAIL_ON_MISSING_PROCESSORS));
        }

        tmp = properties.get(KEY_FIELD_NAME_EVENT_TYPE);
        if (StringUtils.isNotEmpty(tmp)) {
            commonProps.setFieldNameEventType(tmp);
            logValue(KEY_FIELD_NAME_EVENT_TYPE, tmp);
        } else {
            commonProps.setFieldNameEventType(DEFAULT_FIELD_NAME_EVENT_TYPE);
            logDefault(KEY_FIELD_NAME_EVENT_TYPE, DEFAULT_FIELD_NAME_EVENT_TYPE);
        }

        tmp = properties.get(KEY_FIELD_NAME_EVENT_GROUP_TYPE);
        if (StringUtils.isNotEmpty(tmp)) {
            commonProps.setFieldNameEventGroupType(tmp);
            logValue(KEY_FIELD_NAME_EVENT_GROUP_TYPE, tmp);
        } else {
            commonProps.setFieldNameEventGroupType(DEFAULT_FIELD_NAME_EVENT_GROUP_TYPE);
            logDefault(KEY_FIELD_NAME_EVENT_GROUP_TYPE, DEFAULT_FIELD_NAME_EVENT_GROUP_TYPE);
        }

        tmp = properties.get(KEY_FIELD_NAME_SUBJECT);
        if (StringUtils.isNotEmpty(tmp)) {
            commonProps.setFieldNameSubject(tmp);
            logValue(KEY_FIELD_NAME_SUBJECT, tmp);
        } else {
            commonProps.setFieldNameSubject(DEFAULT_FIELD_NAME_SUBJECT);
            logDefault(KEY_FIELD_NAME_SUBJECT, DEFAULT_FIELD_NAME_SUBJECT);
        }

        tmp = properties.get(KEY_FIELD_NAME_SUBJECT_LOCATION);
        if (StringUtils.isNotEmpty(tmp)) {
            commonProps.setFieldNameSubjectLocation(tmp);
            logValue(KEY_FIELD_NAME_SUBJECT_LOCATION, tmp);
        } else {
            commonProps.setFieldNameSubjectLocation(DEFAULT_FIELD_NAME_SUBJECT_LOCATION);
            logDefault(KEY_FIELD_NAME_SUBJECT_LOCATION, DEFAULT_FIELD_NAME_SUBJECT_LOCATION);
        }

        tmp = properties.get(KEY_FIELD_NAME_ACTOR);
        if (StringUtils.isNotEmpty(tmp)) {
            commonProps.setFieldNameActor(tmp);
            logValue(KEY_FIELD_NAME_ACTOR, tmp);
        } else {
            commonProps.setFieldNameActor(DEFAULT_FIELD_NAME_ACTOR);
            logDefault(KEY_FIELD_NAME_ACTOR, DEFAULT_FIELD_NAME_ACTOR);
        }

        tmp = properties.get(KEY_FIELD_NAME_OBJECT);
        if (StringUtils.isNotEmpty(tmp)) {
            commonProps.setFieldNameObject(tmp);
            logValue(KEY_FIELD_NAME_OBJECT, tmp);
        } else {
            commonProps.setFieldNameObject(DEFAULT_FIELD_NAME_OBJECT);
            logDefault(KEY_FIELD_NAME_OBJECT, DEFAULT_FIELD_NAME_OBJECT);
        }

        tmp = properties.get(KEY_FIELD_NAME_OBJECT_LOCATION);
        if (StringUtils.isNotEmpty(tmp)) {
            commonProps.setFieldNameObjectLocation(tmp);
            logValue(KEY_FIELD_NAME_OBJECT_LOCATION, tmp);
        } else {
            commonProps.setFieldNameObjectLocation(DEFAULT_FIELD_NAME_OBJECT_LOCATION);
            logDefault(KEY_FIELD_NAME_OBJECT_LOCATION, DEFAULT_FIELD_NAME_OBJECT_LOCATION);
        }

        tmp = properties.get(KEY_FIELD_NAME_CONTENT_BEFORE_OPERATION);
        if (StringUtils.isNotEmpty(tmp)) {
            commonProps.setFieldNameContentBeforeOperation(tmp);
            logValue(KEY_FIELD_NAME_CONTENT_BEFORE_OPERATION, tmp);
        } else {
            commonProps.setFieldNameContentBeforeOperation(DEFAULT_FIELD_NAME_CONTENT_BEFORE_OPERATION);
            logDefault(KEY_FIELD_NAME_CONTENT_BEFORE_OPERATION, DEFAULT_FIELD_NAME_CONTENT_BEFORE_OPERATION);
        }

        tmp = properties.get(KEY_FIELD_NAME_CONTENT_AFTER_OPERATION);
        if (StringUtils.isNotEmpty(tmp)) {
            commonProps.setFieldNameContentAfterOperation(tmp);
            logValue(KEY_FIELD_NAME_CONTENT_AFTER_OPERATION, tmp);
        } else {
            commonProps.setFieldNameContentAfterOperation(DEFAULT_FIELD_NAME_CONTENT_AFTER_OPERATION);
            logDefault(KEY_FIELD_NAME_CONTENT_AFTER_OPERATION, DEFAULT_FIELD_NAME_CONTENT_AFTER_OPERATION);
        }

        tmp = properties.get(KEY_FIELD_NAME_RESULT);
        if (StringUtils.isNotEmpty(tmp)) {
            commonProps.setFieldNameResult(tmp);
            logValue(KEY_FIELD_NAME_RESULT, tmp);
        } else {
            commonProps.setFieldNameResult(DEFAULT_FIELD_NAME_RESULT);
            logDefault(KEY_FIELD_NAME_RESULT, DEFAULT_FIELD_NAME_RESULT);
        }

        tmp = properties.get(KEY_FIELD_NAME_RESULT_SUMMARY);
        if (StringUtils.isNotEmpty(tmp)) {
            commonProps.setFieldNameResultSummary(tmp);
            logValue(KEY_FIELD_NAME_RESULT_SUMMARY, tmp);
        } else {
            commonProps.setFieldNameResultSummary(DEFAULT_FIELD_NAME_RESULT_SUMMARY);
            logDefault(KEY_FIELD_NAME_RESULT_SUMMARY, DEFAULT_FIELD_NAME_RESULT_SUMMARY);
        }

        tmp = properties.get(KEY_FIELD_NAME_EVENT_SUMMARY);
        if (StringUtils.isNotEmpty(tmp)) {
            commonProps.setFieldNameEventSummary(tmp);
            logValue(KEY_FIELD_NAME_EVENT_SUMMARY, tmp);
        } else {
            commonProps.setFieldNameEventSummary(DEFAULT_FIELD_NAME_EVENT_SUMMARY);
            logDefault(KEY_FIELD_NAME_EVENT_SUMMARY, DEFAULT_FIELD_NAME_EVENT_SUMMARY);
        }

        // set the additional properties, preserving the originally provided properties
        // create a defensive copy of the map and all its properties
        // the code looks a little complicated that "putAll()", but it catches situations where a Map is provided that
        // supports null values (e.g. a HashMap) vs Map implementations that do not (e.g. ConcurrentHashMap).
        final Map<String, String> tempMap = new ConcurrentHashMap<>();
        for (final Map.Entry<String, String> entry : properties.entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();

            if (value != null) {
                tempMap.put(key, value);
            }
        }
        commonProps.setAdditionalProperties(tempMap);

        return commonProps;
    }

    /**
     * Create a log entry when a value has been successfully configured.
     *
     * @param key   The configuration key
     * @param value The value that is being used
     */
    private static void logValue(final String key, final String value) {

        // Fortify will report a violation here because of disclosure of potentially confidential information.
        // However, the configuration keys are not confidential, which makes this a non-issue / false positive.
        if (LOG.isInfoEnabled()) {
            final StringBuilder msg = new StringBuilder("Key found in configuration ('")
                    .append(key)
                    .append("'), using configured value (not disclosed here for security reasons)");
            LOG.info(msg.toString());
        }

        // Fortify will report a violation here because of disclosure of potentially confidential information.
        // The configuration VALUES are confidential. DO NOT activate DEBUG logging in production.
        if (LOG.isDebugEnabled()) {
            final StringBuilder msg = new StringBuilder("Key found in configuration ('")
                    .append(key)
                    .append("'), using configured value ('");
            if (value == null) {
                msg.append("null')");
            } else {
                msg.append(value).append("')");
            }
            LOG.debug(msg.toString());
        }
    }

    /**
     * Create a log entry when a default value is being used in case the propsbuilder key has not been provided in the
     * configuration.
     *
     * @param key          The configuration key
     * @param defaultValue The default value that is being used
     */
    private static void logDefault(final String key, final String defaultValue) {

        // Fortify will report a violation here because of disclosure of potentially confidential information.
        // However, neither the configuration keys nor the default propsbuilder values are confidential, which makes
        // this a non-issue / false positive.
        if (LOG.isInfoEnabled()) {
            final StringBuilder msg = new StringBuilder("Key is not configured ('")
                    .append(key)
                    .append("'), using default value ('");
            if (defaultValue == null) {
                msg.append("null')");
            } else {
                msg.append(defaultValue).append("')");
            }
            LOG.info(msg.toString());
        }
    }

    /**
     * Create a log entry when a default value is being used in case that an invalid configuration value has been
     * provided in the configuration for the propsbuilder key.
     *
     * @param key             The configuration key
     * @param invalidValue    The invalid value that cannot be used
     * @param validationError The validation error that caused the invalid value to be refused
     * @param defaultValue    The default value that is being used
     */
    /* This method is not needed anymore, but keep it around for debugging in case we will need it again
    // suppress warnings about not using an object for the four strings in this PRIVATE method
    @SuppressWarnings("PMD.UseObjectForClearerAPI")
    private static void logDefault(final String key,
                                   final String invalidValue,
                                   final String validationError,
                                   final String defaultValue) {

        if (LOG.isWarnEnabled()) {
            final StringBuilder msg = new StringBuilder("Invalid value ('")
                    .append(invalidValue)
                    .append("', ")
                    .append(validationError)
                    .append(") for key '")
                    .append(key)
                    .append("', using default instead ('");
            if (defaultValue == null) {
                msg.append("null')");
            } else {
                msg.append(defaultValue).append("')");
            }
            LOG.warn(msg.toString());
        }
    }
    */
}
