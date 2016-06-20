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

import org.beiter.michael.eaudit4j.common.CommonProperties;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class MapPropsCommonPropsBuilderTest {

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
        CommonProperties commonProps = MapBasedCommonPropsBuilder.build(map);

        String error = "The properties builder returns a singleton";
        assertThat(error, map, is(not(sameInstance(commonProps.getAdditionalProperties()))));
    }


    ///////////////////////////////////////////////////////////////////////////
    // Named Properties Tests
    //   (test the explicitly named properties)
    ///////////////////////////////////////////////////////////////////////////

    /**
     * default audit class name test
     */
    @Test
    public void defaultAuditClassNameTest() {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();

        String error = "audit class name does not match expected default value";
        assertThat(error, commonProps.getAuditClassName(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_AUDIT_CLASS_NAME)));
        error = "audit class name does not match expected value";
        commonProps.setAuditClassName("42");
        assertThat(error, commonProps.getAuditClassName(), is(equalTo("42")));
    }

    /**
     * audit class name test
     */
    @Test
    public void auditClassNameTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedCommonPropsBuilder.KEY_AUDIT_CLASS_NAME, null);
        CommonProperties commonProps = MapBasedCommonPropsBuilder.build(map);
        String error = "audit class name does not match expected default value";
        assertThat(error, commonProps.getAuditClassName(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_AUDIT_CLASS_NAME)));

        map.put(MapBasedCommonPropsBuilder.KEY_AUDIT_CLASS_NAME, "42");
        commonProps = MapBasedCommonPropsBuilder.build(map);
        error = "audit class name does not match expected value";
        assertThat(error, commonProps.getAuditClassName(), is(equalTo("42")));
    }

    /**
     * default default audit stream test
     */
    @Test
    public void defaultAuditStreamTest() {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();

        String error = "default audit stream name does not match expected default value";
        assertThat(error, commonProps.getDefaultAuditStream(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_AUDIT_STREAM)));
        error = "audit class name does not match expected value";
        commonProps.setDefaultAuditStream("42");
        assertThat(error, commonProps.getDefaultAuditStream(), is(equalTo("42")));
    }

    /**
     * default audit stream test
     */
    @Test
    public void defaultAuditTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedCommonPropsBuilder.KEY_DEFAULT_AUDIT_STREAM, null);
        CommonProperties commonProps = MapBasedCommonPropsBuilder.build(map);
        String error = "default audit stream name does not match expected default value";
        assertThat(error, commonProps.getDefaultAuditStream(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_AUDIT_STREAM)));

        map.put(MapBasedCommonPropsBuilder.KEY_DEFAULT_AUDIT_STREAM, "42");
        commonProps = MapBasedCommonPropsBuilder.build(map);
        error = "default audit stream does not match expected value";
        assertThat(error, commonProps.getDefaultAuditStream(), is(equalTo("42")));
    }

    /**
     * default encoding test
     */
    @Test
    public void defaultEncodingTest() {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();

        String error = "encoding does not match expected default value";
        assertThat(error, commonProps.getEncoding(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_ENCODING)));
        error = "encoding does not match expected value";
        commonProps.setEncoding("42");
        assertThat(error, commonProps.getEncoding(), is(equalTo("42")));
    }

    /**
     * encoding test
     */
    @Test
    public void encodingTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedCommonPropsBuilder.KEY_ENCODING, null);
        CommonProperties commonProps = MapBasedCommonPropsBuilder.build(map);
        String error = "encoding does not match expected default value";
        assertThat(error, commonProps.getEncoding(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_ENCODING)));

        map.put(MapBasedCommonPropsBuilder.KEY_ENCODING, "42");
        commonProps = MapBasedCommonPropsBuilder.build(map);
        error = "encoding does not match expected value";
        assertThat(error, commonProps.getEncoding(), is(equalTo("42")));
    }

    /**
     * default date format test
     */
    @Test
    public void defaultDateFormatTest() {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();

        String error = "date format does not match expected default value";
        assertThat(error, commonProps.getDateFormat(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_DATE_FORMAT)));
        error = "date format does not match expected value";
        commonProps.setDateFormat("42");
        assertThat(error, commonProps.getDateFormat(), is(equalTo("42")));
    }

    /**
     * date format test
     */
    @Test
    public void dateFormatTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedCommonPropsBuilder.KEY_DATE_FORMAT, null);
        CommonProperties commonProps = MapBasedCommonPropsBuilder.build(map);
        String error = "date format does not match expected default value";
        assertThat(error, commonProps.getDateFormat(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_DATE_FORMAT)));

        map.put(MapBasedCommonPropsBuilder.KEY_DATE_FORMAT, "42");
        commonProps = MapBasedCommonPropsBuilder.build(map);
        error = "date format does not match expected value";
        assertThat(error, commonProps.getDateFormat(), is(equalTo("42")));
    }

    /**
     * default processors list test
     */
    @Test
    public void defaultProcessorsTest() {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();

        String error = "processors list does not match expected default value";
        assertThat(error, commonProps.getProcessors(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_PROCESSORS)));
        error = "processors list does not match expected value";
        commonProps.setProcessors("42");
        assertThat(error, commonProps.getProcessors(), is(equalTo("42")));
    }

    /**
     * processors list test
     */
    @Test
    public void processorsTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedCommonPropsBuilder.KEY_PROCESSORS, null);
        CommonProperties commonProps = MapBasedCommonPropsBuilder.build(map);
        String error = "processors list does not match expected default value";
        assertThat(error, commonProps.getProcessors(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_PROCESSORS)));

        map.put(MapBasedCommonPropsBuilder.KEY_PROCESSORS, "42");
        commonProps = MapBasedCommonPropsBuilder.build(map);
        error = "processors list does not match expected value";
        assertThat(error, commonProps.getProcessors(), is(equalTo("42")));
    }

    /**
     * default is_failOnMissingProcessors test
     */
    @Test
    public void defaultFailOnMissingProcessorsTest() {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();

        String error = "is_failOnMissingProcessors does not match expected default value";
        assertThat(error, commonProps.isFailOnMissingProcessors(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_FAIL_ON_MISSING_PROCESSORS)));
        error = "is_failOnMissingProcessors does not match expected value";
        commonProps.setFailOnMissingProcessors(true);
        assertThat(error, commonProps.isFailOnMissingProcessors(), is(equalTo(true)));
        error = "is_failOnMissingProcessors does not match expected value";
        commonProps.setFailOnMissingProcessors(false);
        assertThat(error, commonProps.isFailOnMissingProcessors(), is(equalTo(false)));
    }

    /**
     * is_failOnMissingProcessors test
     */
    @Test
    public void failOnMissingProcessorsTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedCommonPropsBuilder.KEY_FAIL_ON_MISSING_PROCESSORS, null);
        CommonProperties commonProps = MapBasedCommonPropsBuilder.build(map);
        String error = "is_failOnMissingProcessors does not match expected default value";
        assertThat(error, commonProps.isFailOnMissingProcessors(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_FAIL_ON_MISSING_PROCESSORS)));

        map.put(MapBasedCommonPropsBuilder.KEY_FAIL_ON_MISSING_PROCESSORS, "asdf");
        commonProps = MapBasedCommonPropsBuilder.build(map);
        error = "is_failOnMissingProcessors does not match expected value";
        assertThat(error, commonProps.isFailOnMissingProcessors(), is(equalTo(false)));

        map.put(MapBasedCommonPropsBuilder.KEY_FAIL_ON_MISSING_PROCESSORS, "tRuE");
        commonProps = MapBasedCommonPropsBuilder.build(map);
        error = "is_failOnMissingProcessors does not match expected value";
        assertThat(error, commonProps.isFailOnMissingProcessors(), is(equalTo(true)));
    }

    /**
     * default field name (event type) test
     */
    @Test
    public void defaultFieldNameEventTypeTest() {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();

        String error = "field name does not match expected default value";
        assertThat(error, commonProps.getFieldNameEventType(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_FIELD_NAME_EVENT_TYPE)));
        error = "field name does not match expected value";
        commonProps.setFieldNameEventType("42");
        assertThat(error, commonProps.getFieldNameEventType(), is(equalTo("42")));
    }

    /**
     * field name (event type) test
     */
    @Test
    public void fieldNameEventTypeTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedCommonPropsBuilder.KEY_FIELD_NAME_EVENT_TYPE, null);
        CommonProperties commonProps = MapBasedCommonPropsBuilder.build(map);
        String error = "field name does not match expected default value";
        assertThat(error, commonProps.getFieldNameEventType(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_FIELD_NAME_EVENT_TYPE)));

        map.put(MapBasedCommonPropsBuilder.KEY_FIELD_NAME_EVENT_TYPE, "42");
        commonProps = MapBasedCommonPropsBuilder.build(map);
        error = "field name does not match expected value";
        assertThat(error, commonProps.getFieldNameEventType(), is(equalTo("42")));
    }

    /**
     * default field name (event group type) test
     */
    @Test
    public void defaultFieldNameEventGroupTypeTest() {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();

        String error = "field name does not match expected default value";
        assertThat(error, commonProps.getFieldNameEventGroupType(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_FIELD_NAME_EVENT_GROUP_TYPE)));
        error = "field name does not match expected value";
        commonProps.setFieldNameEventGroupType("42");
        assertThat(error, commonProps.getFieldNameEventGroupType(), is(equalTo("42")));
    }

    /**
     * field name (event group type) test
     */
    @Test
    public void fieldNameEventGroupTypeTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedCommonPropsBuilder.KEY_FIELD_NAME_EVENT_GROUP_TYPE, null);
        CommonProperties commonProps = MapBasedCommonPropsBuilder.build(map);
        String error = "field name does not match expected default value";
        assertThat(error, commonProps.getFieldNameEventGroupType(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_FIELD_NAME_EVENT_GROUP_TYPE)));

        map.put(MapBasedCommonPropsBuilder.KEY_FIELD_NAME_EVENT_GROUP_TYPE, "42");
        commonProps = MapBasedCommonPropsBuilder.build(map);
        error = "field name does not match expected value";
        assertThat(error, commonProps.getFieldNameEventGroupType(), is(equalTo("42")));
    }

    /**
     * default field name (subject) test
     */
    @Test
    public void defaultFieldNameSubjectTest() {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();

        String error = "field name does not match expected default value";
        assertThat(error, commonProps.getFieldNameSubject(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_FIELD_NAME_SUBJECT)));
        error = "field name does not match expected value";
        commonProps.setFieldNameSubject("42");
        assertThat(error, commonProps.getFieldNameSubject(), is(equalTo("42")));
    }

    /**
     * field name (subject) test
     */
    @Test
    public void fieldNameSubjectTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedCommonPropsBuilder.KEY_FIELD_NAME_SUBJECT, null);
        CommonProperties commonProps = MapBasedCommonPropsBuilder.build(map);
        String error = "field name does not match expected default value";
        assertThat(error, commonProps.getFieldNameSubject(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_FIELD_NAME_SUBJECT)));

        map.put(MapBasedCommonPropsBuilder.KEY_FIELD_NAME_SUBJECT, "42");
        commonProps = MapBasedCommonPropsBuilder.build(map);
        error = "field name does not match expected value";
        assertThat(error, commonProps.getFieldNameSubject(), is(equalTo("42")));
    }

    /**
     * default field name (subject location) test
     */
    @Test
    public void defaultFieldNameSubjectLocationTest() {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();

        String error = "field name does not match expected default value";
        assertThat(error, commonProps.getFieldNameSubjectLocation(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_FIELD_NAME_SUBJECT_LOCATION)));
        error = "field name does not match expected value";
        commonProps.setFieldNameSubjectLocation("42");
        assertThat(error, commonProps.getFieldNameSubjectLocation(), is(equalTo("42")));
    }

    /**
     * field name (subject location) test
     */
    @Test
    public void fieldNameSubjectLocationTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedCommonPropsBuilder.KEY_FIELD_NAME_SUBJECT_LOCATION, null);
        CommonProperties commonProps = MapBasedCommonPropsBuilder.build(map);
        String error = "field name does not match expected default value";
        assertThat(error, commonProps.getFieldNameSubjectLocation(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_FIELD_NAME_SUBJECT_LOCATION)));

        map.put(MapBasedCommonPropsBuilder.KEY_FIELD_NAME_SUBJECT_LOCATION, "42");
        commonProps = MapBasedCommonPropsBuilder.build(map);
        error = "field name does not match expected value";
        assertThat(error, commonProps.getFieldNameSubjectLocation(), is(equalTo("42")));
    }

    /**
     * default field name (actor) test
     */
    @Test
    public void defaultFieldNameActorTest() {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();

        String error = "field name does not match expected default value";
        assertThat(error, commonProps.getFieldNameActor(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_FIELD_NAME_ACTOR)));
        error = "field name does not match expected value";
        commonProps.setFieldNameActor("42");
        assertThat(error, commonProps.getFieldNameActor(), is(equalTo("42")));
    }

    /**
     * field name (actor) test
     */
    @Test
    public void fieldNameActorTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedCommonPropsBuilder.KEY_FIELD_NAME_ACTOR, null);
        CommonProperties commonProps = MapBasedCommonPropsBuilder.build(map);
        String error = "field name does not match expected default value";
        assertThat(error, commonProps.getFieldNameActor(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_FIELD_NAME_ACTOR)));

        map.put(MapBasedCommonPropsBuilder.KEY_FIELD_NAME_ACTOR, "42");
        commonProps = MapBasedCommonPropsBuilder.build(map);
        error = "field name does not match expected value";
        assertThat(error, commonProps.getFieldNameActor(), is(equalTo("42")));
    }

    /**
     * default field name (object) test
     */
    @Test
    public void defaultFieldNameObjectTest() {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();

        String error = "field name does not match expected default value";
        assertThat(error, commonProps.getFieldNameObject(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_FIELD_NAME_OBJECT)));
        error = "field name does not match expected value";
        commonProps.setFieldNameObject("42");
        assertThat(error, commonProps.getFieldNameObject(), is(equalTo("42")));
    }

    /**
     * field name (object) test
     */
    @Test
    public void fieldNameObjectTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedCommonPropsBuilder.KEY_FIELD_NAME_OBJECT, null);
        CommonProperties commonProps = MapBasedCommonPropsBuilder.build(map);
        String error = "field name does not match expected default value";
        assertThat(error, commonProps.getFieldNameObject(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_FIELD_NAME_OBJECT)));

        map.put(MapBasedCommonPropsBuilder.KEY_FIELD_NAME_OBJECT, "42");
        commonProps = MapBasedCommonPropsBuilder.build(map);
        error = "field name does not match expected value";
        assertThat(error, commonProps.getFieldNameObject(), is(equalTo("42")));
    }

    /**
     * default field name (object location) test
     */
    @Test
    public void defaultFieldNameObjectLocationTest() {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();

        String error = "field name does not match expected default value";
        assertThat(error, commonProps.getFieldNameObjectLocation(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_FIELD_NAME_OBJECT_LOCATION)));
        error = "field name does not match expected value";
        commonProps.setFieldNameObjectLocation("42");
        assertThat(error, commonProps.getFieldNameObjectLocation(), is(equalTo("42")));
    }

    /**
     * field name (object location) test
     */
    @Test
    public void fieldNameObjectLocationTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedCommonPropsBuilder.KEY_FIELD_NAME_OBJECT_LOCATION, null);
        CommonProperties commonProps = MapBasedCommonPropsBuilder.build(map);
        String error = "field name does not match expected default value";
        assertThat(error, commonProps.getFieldNameObjectLocation(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_FIELD_NAME_OBJECT_LOCATION)));

        map.put(MapBasedCommonPropsBuilder.KEY_FIELD_NAME_OBJECT_LOCATION, "42");
        commonProps = MapBasedCommonPropsBuilder.build(map);
        error = "field name does not match expected value";
        assertThat(error, commonProps.getFieldNameObjectLocation(), is(equalTo("42")));
    }

    /**
     * default field name (content before operation) test
     */
    @Test
    public void defaultFieldNameContentBeforeOperationTest() {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();

        String error = "field name does not match expected default value";
        assertThat(error, commonProps.getFieldNameContentBeforeOperation(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_FIELD_NAME_CONTENT_BEFORE_OPERATION)));
        error = "field name does not match expected value";
        commonProps.setFieldNameContentBeforeOperation("42");
        assertThat(error, commonProps.getFieldNameContentBeforeOperation(), is(equalTo("42")));
    }

    /**
     * field name (content before operation) test
     */
    @Test
    public void fieldNameContentBeforeOperationTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedCommonPropsBuilder.KEY_FIELD_NAME_CONTENT_BEFORE_OPERATION, null);
        CommonProperties commonProps = MapBasedCommonPropsBuilder.build(map);
        String error = "field name does not match expected default value";
        assertThat(error, commonProps.getFieldNameContentBeforeOperation(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_FIELD_NAME_CONTENT_BEFORE_OPERATION)));

        map.put(MapBasedCommonPropsBuilder.KEY_FIELD_NAME_CONTENT_BEFORE_OPERATION, "42");
        commonProps = MapBasedCommonPropsBuilder.build(map);
        error = "field name does not match expected value";
        assertThat(error, commonProps.getFieldNameContentBeforeOperation(), is(equalTo("42")));
    }

    /**
     * default field name (content after operation) test
     */
    @Test
    public void defaultFieldNameContentAfterOperationTest() {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();

        String error = "field name does not match expected default value";
        assertThat(error, commonProps.getFieldNameContentAfterOperation(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_FIELD_NAME_CONTENT_AFTER_OPERATION)));
        error = "field name does not match expected value";
        commonProps.setFieldNameContentAfterOperation("42");
        assertThat(error, commonProps.getFieldNameContentAfterOperation(), is(equalTo("42")));
    }

    /**
     * field name (content after operation) test
     */
    @Test
    public void fieldNameContentAfterOperationTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedCommonPropsBuilder.KEY_FIELD_NAME_CONTENT_AFTER_OPERATION, null);
        CommonProperties commonProps = MapBasedCommonPropsBuilder.build(map);
        String error = "field name does not match expected default value";
        assertThat(error, commonProps.getFieldNameContentAfterOperation(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_FIELD_NAME_CONTENT_AFTER_OPERATION)));

        map.put(MapBasedCommonPropsBuilder.KEY_FIELD_NAME_CONTENT_AFTER_OPERATION, "42");
        commonProps = MapBasedCommonPropsBuilder.build(map);
        error = "field name does not match expected value";
        assertThat(error, commonProps.getFieldNameContentAfterOperation(), is(equalTo("42")));
    }

    /**
     * default field name (result) test
     */
    @Test
    public void defaultFieldNameResultTest() {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();

        String error = "field name does not match expected default value";
        assertThat(error, commonProps.getFieldNameResult(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_FIELD_NAME_RESULT)));
        error = "field name does not match expected value";
        commonProps.setFieldNameResult("42");
        assertThat(error, commonProps.getFieldNameResult(), is(equalTo("42")));
    }

    /**
     * field name (result) test
     */
    @Test
    public void fieldNameResultTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedCommonPropsBuilder.KEY_FIELD_NAME_RESULT, null);
        CommonProperties commonProps = MapBasedCommonPropsBuilder.build(map);
        String error = "field name does not match expected default value";
        assertThat(error, commonProps.getFieldNameResult(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_FIELD_NAME_RESULT)));

        map.put(MapBasedCommonPropsBuilder.KEY_FIELD_NAME_RESULT, "42");
        commonProps = MapBasedCommonPropsBuilder.build(map);
        error = "field name does not match expected value";
        assertThat(error, commonProps.getFieldNameResult(), is(equalTo("42")));
    }

    /**
     * default field name (result summary) test
     */
    @Test
    public void defaultFieldNameResultSummaryTest() {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();

        String error = "field name does not match expected default value";
        assertThat(error, commonProps.getFieldNameResultSummary(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_FIELD_NAME_RESULT_SUMMARY)));
        error = "field name does not match expected value";
        commonProps.setFieldNameResultSummary("42");
        assertThat(error, commonProps.getFieldNameResultSummary(), is(equalTo("42")));
    }

    /**
     * field name (result summary) test
     */
    @Test
    public void fieldNameResultSummaryTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedCommonPropsBuilder.KEY_FIELD_NAME_RESULT_SUMMARY, null);
        CommonProperties commonProps = MapBasedCommonPropsBuilder.build(map);
        String error = "field name does not match expected default value";
        assertThat(error, commonProps.getFieldNameResultSummary(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_FIELD_NAME_RESULT_SUMMARY)));

        map.put(MapBasedCommonPropsBuilder.KEY_FIELD_NAME_RESULT_SUMMARY, "42");
        commonProps = MapBasedCommonPropsBuilder.build(map);
        error = "field name does not match expected value";
        assertThat(error, commonProps.getFieldNameResultSummary(), is(equalTo("42")));
    }

    /**
     * default field name (event summary) test
     */
    @Test
    public void defaultFieldNameEventSummaryTest() {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();

        String error = "field name does not match expected default value";
        assertThat(error, commonProps.getFieldNameEventSummary(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_FIELD_NAME_EVENT_SUMMARY)));
        error = "field name does not match expected value";
        commonProps.setFieldNameEventSummary("42");
        assertThat(error, commonProps.getFieldNameEventSummary(), is(equalTo("42")));
    }

    /**
     * field name (event summary) test
     */
    @Test
    public void fieldNameEventSummaryTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedCommonPropsBuilder.KEY_FIELD_NAME_EVENT_SUMMARY, null);
        CommonProperties commonProps = MapBasedCommonPropsBuilder.build(map);
        String error = "field name does not match expected default value";
        assertThat(error, commonProps.getFieldNameEventSummary(),
                is(equalTo(MapBasedCommonPropsBuilder.DEFAULT_FIELD_NAME_EVENT_SUMMARY)));

        map.put(MapBasedCommonPropsBuilder.KEY_FIELD_NAME_EVENT_SUMMARY, "42");
        commonProps = MapBasedCommonPropsBuilder.build(map);
        error = "field name does not match expected value";
        assertThat(error, commonProps.getFieldNameEventSummary(), is(equalTo("42")));
    }

}
