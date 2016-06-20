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
package org.beiter.michael.eaudit4j.common.impl;

import org.apache.commons.lang3.Validate;
import org.beiter.michael.array.Converter;
import org.beiter.michael.eaudit4j.common.CommonProperties;
import org.beiter.michael.eaudit4j.common.ExtendedEvent;
import org.beiter.michael.eaudit4j.common.Field;

/**
 * This class implements an {@link ExtendedEvent}. In addition to the basic properties defined in
 * {@link org.beiter.michael.eaudit4j.common.Event}, it also implements the extended (frequently used) audit event
 * properties defined in {@link ExtendedEvent}.
 */
public class ExtendedAuditEvent
        extends AuditEvent
        implements ExtendedEvent {

    /**
     * A copy of the common properties
     */
    private final CommonProperties properties;

    /**
     * Create a new (extended) audit event.
     * <p>
     * Use this constructor in case you want to create an empty event, and populate it with the setters.
     *
     * @param properties The configuration properties to be used by this event instance
     * @throws NullPointerException When {@code value} is {@code null}
     */
    public ExtendedAuditEvent(final CommonProperties properties) {

        super();

        Validate.notNull(properties, "The validated object 'properties' is null");

        this.properties = new CommonProperties(properties);
    }

    /**
     * Create a new (extended) audit event.
     * <p>
     * Use this constructor in case you want to create an event that is pre-populated with the provided fields.
     * <p>
     * You may still use the setters to add additional fields.
     * <p>
     * Note that this constructor creates defensive copies of the data in the provided fields. You need to explicitly
     * clear the fields provided to this constructor if they contain confidential information that you wish to destroy.
     *
     * @param properties The configuration properties to be used by this event instance
     * @param fields     The fields to set
     * @throws NullPointerException When {@code value} is {@code null}
     */
    public ExtendedAuditEvent(final CommonProperties properties, final Field... fields) {

        super(fields);

        Validate.notNull(properties, "The validated object 'properties' is null");

        this.properties = new CommonProperties(properties);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The field is named according to the well-known field names defined in {@link CommonProperties} as provided at
     * creation of this object, the value for the field is taken from the provided parameter.
     * <p>
     * Note that the value of the provided char is encoded using the charset specified in
     * {@link CommonProperties#getEncoding()}
     *
     * @throws NullPointerException When {@code value} is {@code null}
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    @Override
    public final String setEventType(final char[] value) {

        Validate.notNull(value, "The validated object 'value' is null");

        final Field field = buildField(properties.getFieldNameEventType(), value);
        final String name = this.setField(field);
        field.clear(); // clean up temporary data

        return name;
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException When {@code value} is {@code null}
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    @Override
    public final String setEventGroupType(final char[] value) {

        Validate.notNull(value, "The validated object 'value' is null");

        final Field field = buildField(properties.getFieldNameEventGroupType(), value);
        final String name = this.setField(field);
        field.clear(); // clean up temporary data

        return name;
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException When {@code value} is {@code null}
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    @Override
    public final String setSubject(final char[] value) {

        Validate.notNull(value, "The validated object 'value' is null");

        final Field field = buildField(properties.getFieldNameSubject(), value);
        final String name = this.setField(field);
        field.clear(); // clean up temporary data

        return name;
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException When {@code value} is {@code null}
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    @Override
    public final String setSubjectLocation(final char[] value) {

        Validate.notNull(value, "The validated object 'value' is null");

        final Field field = buildField(properties.getFieldNameSubjectLocation(), value);
        final String name = this.setField(field);
        field.clear(); // clean up temporary data

        return name;
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException When {@code value} is {@code null}
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    @Override
    public final String setActor(final char[] value) {

        Validate.notNull(value, "The validated object 'value' is null");

        final Field field = buildField(properties.getFieldNameActor(), value);
        final String name = this.setField(field);
        field.clear(); // clean up temporary data

        return name;
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException When {@code value} is {@code null}
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    @Override
    public final String setObject(final char[] value) {

        Validate.notNull(value, "The validated object 'value' is null");

        final Field field = buildField(properties.getFieldNameObject(), value);
        final String name = this.setField(field);
        field.clear(); // clean up temporary data

        return name;
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException When {@code value} is {@code null}
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    @Override
    public final String setObjectLocation(final char[] value) {

        Validate.notNull(value, "The validated object 'value' is null");

        final Field field = buildField(properties.getFieldNameObjectLocation(), value);
        final String name = this.setField(field);
        field.clear(); // clean up temporary data

        return name;
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException When {@code value} is {@code null}
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    @Override
    public final String setContentBeforeOperation(final char[] value) {

        Validate.notNull(value, "The validated object 'value' is null");

        final Field field = buildField(properties.getFieldNameContentBeforeOperation(), value);
        final String name = this.setField(field);
        field.clear(); // clean up temporary data

        return name;
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException When {@code value} is {@code null}
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    @Override
    public final String setContentAfterOperation(final char[] value) {

        Validate.notNull(value, "The validated object 'value' is null");

        final Field field = buildField(properties.getFieldNameContentAfterOperation(), value);
        final String name = this.setField(field);
        field.clear(); // clean up temporary data

        return name;
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException When {@code value} is {@code null}
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    @Override
    public final String setResult(final char[] value) {

        Validate.notNull(value, "The validated object 'value' is null");

        final Field field = buildField(properties.getFieldNameResult(), value);
        final String name = this.setField(field);
        field.clear(); // clean up temporary data

        return name;
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException When {@code value} is {@code null}
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    @Override
    public final String setResultSummary(final char[] value) {

        Validate.notNull(value, "The validated object 'value' is null");

        final Field field = buildField(properties.getFieldNameResultSummary(), value);
        final String name = this.setField(field);
        field.clear(); // clean up temporary data

        return name;
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException When {@code value} is {@code null}
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    @Override
    public final String setEventSummary(final char[] value) {

        Validate.notNull(value, "The validated object 'value' is null");

        final Field field = buildField(properties.getFieldNameEventSummary(), value);
        final String name = this.setField(field);
        field.clear(); // clean up temporary data

        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final char[] toJson() {

        return toJson(properties.getEncoding());
    }

    /**
     * Build a field with the provided information.
     * <p>
     * The field value is converted from char to byte with the encoding configured in the properties. The name of the
     * field is set as provided.
     *
     * @param fieldName The field's well known name
     * @param value     The value to assign
     * @return The field created from the provided information
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    private Field buildField(final String fieldName, final char[] value) {

        // EventField creates defensive copies, which means we do not have to create (and destroy)
        // yet another defensive copy of the data array before submitting to EventField
        return new EventField(fieldName, Converter.toBytes(value, properties.getEncoding()));
    }
}
