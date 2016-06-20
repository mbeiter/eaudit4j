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
package org.beiter.michael.eaudit4j.common.demo;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.beiter.michael.eaudit4j.common.*;
import org.beiter.michael.eaudit4j.common.impl.*;
import org.beiter.michael.eaudit4j.common.propsbuilder.MapBasedCommonPropsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

public class DemoTest {

    /**
     * The logger object for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(DemoTest.class);

    /**
     * The encoding we will use for Strings in this demo
     */
    private final String encoding = "UTF-8";

    /**
     * The library configuration
     */
    private CommonProperties properties;

    @Before
    public void resetConfiguration() {

        // start with some default properties
        properties = MapBasedCommonPropsBuilder.buildDefault();

        // set additional properties
        properties.setDefaultAuditStream("1234567890");
        properties.setEncoding(encoding);
        properties.setFailOnMissingProcessors(false);
    }

    /**
     * This is how to use the library when creating events with the "Event" interface constructor and Field setter.
     * <p>
     * This is the most basic way to use the library. The direct creation of fields and usage of the basic {@link Event}
     * interface only allows you to create "unnamed" fields directly, but you can create "named" fields by using the
     * {@link EventField} constructor.
     * <p>
     * This is the most flexible way to use the audit library, because you are not restricted in any way by the
     * library's interface assumptions on field names to use in your application. You can use this approach to create
     * application specific wrappers and customized "convenience" methods to make calls to the audit subsystem.
     * <p>
     * Note that this approach is using fairly low-level APIs of the audit library, which makes it necessary to handle
     * the conversion of audit payload to binary data (which the library processes) yourself.
     */
    @Test
    public void auditWithAuditLibraryPrimitivesTest()
            throws UnsupportedEncodingException {

        // First create some fields we want to audit
        // Note that the field content is submitted as a binary value (see other options to use human readable formats)
        Field field1 = new EventField("field_1_name", "field 1 value".getBytes(encoding));
        // Fields may have multi-byte chars, of course:
        Field field2 = new EventField("field_2_name", "field 2 value: \u00C4-\u00D6-\u00DC".getBytes(encoding));
        // You can audit binary data, too.
        // The examples assume some binary data that has been encoded in its byte representation:
        Field field3 = new EventField(
                "field_3_name",
                Base64.encodeBase64("field 3 value".getBytes(encoding)),
                Encodings.BASE64);
        Field field4 = new EventField(
                "field_4_name",
                new Hex().encode("field 4 value".getBytes(encoding)),
                Encodings.HEX);

        // The library defines some "common" (standardized) field names in the properties (which you can override
        // there), that  you can use like so:
        Field field5 = new EventField(
                properties.getFieldNameActor(),
                "actor ID".getBytes(encoding));
        Field field6 = new EventField(
                properties.getFieldNameSubject(),
                "subject ID".getBytes(encoding));

        // create an event, and set some fields in the constructor (optional)
        Event event = new AuditEvent(field1, field2, field3);
        // you may set more fields with the Field setter method
        event.setField(field4);
        event.setField(field5);
        event.setField(field6);

        // this is where you pass the event into the audit processing chain
        auditEvent(event);

        // Clear the confidential information from the event
        // (if you always want to do that, feel free to move this to the {@code auditEvent()} method.)
        event.clear();

        // Clear the confidential information from the fields
        field1.clear();
        field2.clear();
        field3.clear();
        field4.clear();
        field5.clear();
        field6.clear();
    }

    /**
     * This is how to use the library when creating events with the "ExtendedEvent" interface constructor and advanced
     * Field setters.
     * <p>
     * This is a more advanced way to use the library. Using the {@link ExtendedEvent} interface allows you to create
     * commonly used / "named" fields directly.
     * <p>
     * Unless you want to audit custom fields for your application, you will not have to create {@link Field}s directly
     * and you will hence not have to worry about proper encoding of binary data.
     */
    @Test
    public void auditWithAuditLibraryExtensionsTest()
            throws UnsupportedEncodingException {

        // no need to create fields before creating the event unless you want to add some custom
        // fields (including binary data, see auditWithAuditLibraryPrimitivesTest()), like so:
        Field field1 = new EventField("a_custom_field_1_name", "field 1 value".getBytes(encoding));
        // Fields may have multi-byte chars, of course:
        Field field2 = new EventField("a_custom_field_2_name",
                "field 2 value: \u00C4-\u00D6-\u00DC".getBytes(encoding));

        // If you are not using custom fields, you may use the default constructor and add data to the event using the
        // named setters. You will have to pass in the application properties, because the ExtendedAuditEvent is more
        // sophisticated than the basic AuditEvent and needs some configuration.
        // You can additionally still use the Field constructor, and the generic Field setter, to add custom events,
        // like so:
        ExtendedEvent event = new ExtendedAuditEvent(properties, field1);
        // the generic setter for the custom field
        event.setField(field2);
        // named setters for commonly used fields
        event.setActor("actor ID".toCharArray());
        event.setSubject("subject ID".toCharArray());


        // If you do not need custom fields, creating and populating an event can be as easy as:
        ExtendedEvent event2 = new ExtendedAuditEvent(properties);
        event.setActor("actor ID".toCharArray());
        event.setSubject("subject ID".toCharArray());

        // this is where you pass the event into the audit processing chain
        auditEvent(event);
        auditEvent(event2);

        // Clear the confidential information from the event
        // (if you always want to do that, feel free to move this to the {@code auditEvent()} method.)
        event.clear();
        event2.clear();

        // Clear the confidential information from the fields
        field1.clear();
        field2.clear();
    }

    /**
     * When you are mostly using the "common" fields in your events, but you do not want to bother with the difference
     * between {@link Event}s and {@link ExtendedEvent}s, then you may want to use the {@link EventBuilder}.
     * <p>
     * The {@link EventBuilder} is a convenient way to create {@link Event}s and populate the "commonly used" fields,
     * but still being able to add "custom fields":
     * <ul>
     *      <li>Before invocation of the event builder (create a {@link Field} and add it to the event builder)</li>
     *      <li>During invocation of the event builder (specify custom field information, including binary payload, to the event builder)</li>
     *      <li>After invocation of the event builder (create a {@link Field} and add it to the event that has been created by the event builder)</li>
     * </ul>
     * {@link Event#setField(Field)} method.
     * <p>
     */
    @Test
    public void auditWithAuditEventBuilderTest()
            throws UnsupportedEncodingException {

        // The event builder can use some pre-created fields
        Field field1 = new EventField("a_custom_field_1_name", "field 1 value".getBytes(encoding));

        // The event builder will need access to the configuration properties
        Event event = new EventBuilder(properties)
                .setActor("actor ID".toCharArray())
                .setSubject("subject ID".toCharArray())
                .setField(field1) // use a pre-created field
                .setField("a_custom_field_3_name", "field 3 value".getBytes(encoding)) // use a custom field, with binary data
                .setField("a_custom_field_4_name", "field 4 value".toCharArray()) // use a custom field, with char data
                .setField(properties.getFieldNameObject(), "field 5 value".getBytes(encoding)) // use a custom field (named), with binary data
                .setField(properties.getFieldNameResult(), "field 6 value".toCharArray()) // use a custom field (named), with binary data
                .build();

        // you can optionally still use the primitives with the Event created by the builder to add more custom fields,
        // including binary data (see auditWithAuditLibraryPrimitivesTest())...
        Field field2 = new EventField("a_custom_field_2_name",
                "field 2 value: \u00C4-\u00D6-\u00DC".getBytes(encoding));
        // ...and add them to the event
        event.setField(field1);
        event.setField(field2);

        // this is where you pass the event into the audit processing chain
        auditEvent(event);

        // Clear the confidential information from the event
        // (if you always want to do that, feel free to move this to the {@code auditEvent()} method.)
        event.clear();

        // Clear the confidential information from the fields that we have created manually
        field1.clear();
        field2.clear();
    }

    /**
     * This is an example on how to get an instance of the audit processing chain, handing off an even for processing,
     * and handling errors that may occur during processing.
     *
     * @param event The event to audit
     */
    private void auditEvent(Event event) {

        // get an instance of Audit. You may want to use the getSinlgeton() method, or use getInstance()
        // and manage the your own Audit singleton(s) to improve performance...
        final Audit audit;
        try {
            audit = AuditFactory.getInstance(properties.getAuditClassName(), properties);
        } catch (FactoryException e) {
            // your error handling in case of instantiation failures goes here
            LOG.error("Failed to retrieve an instance of the configured Audit implementation ("
                    + properties.getAuditClassName() + ")", e);
            return;
        }

        // audit to the default audit stream for this demo
        try {
            audit.audit(event);
        } catch (AuditException e) {

            // In case that auditing fails, you have access to an enum of error conditions.
            // See {@link AuditErrorConditions} for a complete list.
            switch (e.getErrorCondition()) {
                case CONFIGURATION:
                    LOG.error("Audit failed because of a config error: " + AuditErrorConditions.CONFIGURATION, e);
                    break;
                case UNKNOWN:
                    LOG.error("Audit failed because of an unknown error: " + AuditErrorConditions.UNKNOWN, e);
                    break;
                default:
                    LOG.error("Auditing failed, and we don't know why. " +
                            "Maybe new fields were added to the enum, and we have not updated our code yet", e);
                    break;
            }

            // typically you want to re-throw an exception here to prevent further execution of your code
            // (assuming that you do not want to execute any operation without it being audited)
        }
    }
}
