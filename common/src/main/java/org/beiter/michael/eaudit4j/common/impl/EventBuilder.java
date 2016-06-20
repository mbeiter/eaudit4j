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
import org.beiter.michael.eaudit4j.common.Event;
import org.beiter.michael.eaudit4j.common.Field;

/**
 * Build audit {@link Event}s, and maybe be found to be a more convenient method to building audit events than crafting
 * them manually from {@link Field} instances.
 * <p>
 * Note that this class provides several convenience methods to add frequently used fields to an audit event. The names
 * under which these fields will be stored in the event can be configured through the configuration object provided in
 * the constructor.
 */
public class EventBuilder {

    /**
     * A copy of the common properties
     */
    private final CommonProperties properties;

    /**
     * The object under construction by this builder.
     */
    // Note that we are using the {@link AuditEvent} implementation here, rather than the {@link ExtendedAuditEvent}.
    // We miss out on some convenience methods defined in {@link org.beiter.michael.eaudit4j.common.ExtendedEvent}.
    // However, using these methods may introduce too tight coupling (i.e. this class would always return an
    // {@link org.beiter.michael.eaudit4j.common.ExtendedEvent} rather than an
    // {@link org.beiter.michael.eaudit4j.common.Event}, which may not be desirable.
    private final Event event;

    /**
     * Create a new instance of the event builder
     *
     * @param properties The configuration properties to be used by this builder instance
     * @throws NullPointerException When {@code properties} is {@code null}
     */
    public EventBuilder(final CommonProperties properties) {

        Validate.notNull(properties, "The validated object 'properties' is null");

        this.properties = new CommonProperties(properties);
        this.event = new AuditEvent();
    }

    /**
     * Set a new field in the event with the specified properties.
     * <p>
     * The method creates a defensive copy of the provided audit data (the {@code byte[] value}), and stores the copy
     * in the event. You need to explicitly clear the data provided to this method if it contains confidential
     * information that you wish to destroy.
     *
     * @param name  The name of the field to set
     * @param value The value of the field to set
     * @return The event builder
     * @throws NullPointerException     When {@code value} is {@code null}
     * @throws IllegalArgumentException When {@code value} is empty
     */
    public final EventBuilder setField(final String name, final byte[] value) {

        Validate.notBlank(name, "The validated character sequence 'name' is null or empty");
        Validate.notNull(value, "The validated object 'value' is null");

        // EventField creates defensive copies, which means we do not have to create (and destroy)
        // yet another defensive copy of the data array before submitting to EventField
        final Field field = new EventField(name, value);

        event.setField(field);

        // clean up temporary data
        field.clear();

        return this;
    }

    /**
     * Set a new field in the event with the specified properties.
     * <p>
     * The method creates a defensive copy of the provided audit data (the {@code byte[] value}), and stores the copy
     * in the event. You need to explicitly clear the data provided to this method if it contains confidential
     * information that you wish to destroy.
     * <p>
     * Note that the value of the provided char is encoded using the charset specified in
     * {@link CommonProperties#getEncoding()}
     *
     * @param name  The name of the field to set
     * @param value The value of the field to set
     * @return The event builder
     * @throws NullPointerException     When {@code value} is {@code null}
     * @throws IllegalArgumentException When {@code value} is empty
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    public final EventBuilder setField(final String name, final char[] value) {

        Validate.notBlank(name, "The validated character sequence 'name' is null or empty");
        Validate.notNull(value, "The validated object 'value' is null");

        // EventField creates defensive copies, which means we do not have to create (and destroy)
        // yet another defensive copy of the data array before submitting to EventField
        final Field field = new EventField(name, Converter.toBytes(value, properties.getEncoding()));

        event.setField(field);

        // clean up temporary data
        field.clear();

        return this;
    }

    /**
     * Set a new field in the event.
     * <p>
     * The method creates a defensive copy of the provided audit data (the content of {@code Field}), and stores the
     * copy in the event. You need to explicitly clear the data provided to this method if it contains confidential
     * information that you wish to destroy.
     *
     * @param field The field to set
     * @return The event builder
     * @throws NullPointerException When {@code value} is {@code null}
     */
    public final EventBuilder setField(final Field field) {

        Validate.notNull(field, "The validated object 'field' is null");

        // Use the EventField copy method to create a defensive copy of field
        final Field tmpField = field.getCopy();

        event.setField(tmpField);


        // clean up temporary data
        tmpField.clear();


        return this;
    }

    /**
     * This method sets a field that represents an <strong>event type</strong>.
     * <p>
     * This is a convenience method if applications want to use this field, and also want to use the standard field
     * name for it. If you want to set a different field name for this field, you should use the generic
     * {@link EventBuilder#setField(Field)} method.
     * <p>
     * The field is named according to the well-known field names defined in {@link CommonProperties} as provided at
     * creation of this object, the value for the field is taken from the provided parameter.
     * <p>
     * The method creates a defensive copy of the provided audit data (the {@code char[] value}), and stores the copy
     * in the event. You need to explicitly clear the data provided to this method if it contains confidential
     * information that you wish to destroy.
     * <p>
     * Note that the value of the provided char is encoded using the charset specified in
     * {@link CommonProperties#getEncoding()}
     *
     * @param value The value of the field to set
     * @return The event builder
     * @throws NullPointerException When {@code value} is {@code null}
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    public final EventBuilder setEventType(final char[] value) {

        Validate.notNull(value, "The validated object 'value' is null");

        final Field field = buildField(properties.getFieldNameEventType(), value);
        event.setField(field);
        field.clear(); // clean up temporary data

        return this;
    }

    /**
     * This method sets a field that represents an <strong>event group type</strong>.
     * <p>
     * See {@link EventBuilder#setEventType(char[] eventType)} for more information on how to use this method.
     *
     * @param value The value of the field to set
     * @return The event builder
     * @throws NullPointerException When {@code value} is {@code null}
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    public final EventBuilder setEventGroupType(final char[] value) {

        Validate.notNull(value, "The validated object 'value' is null");

        final Field field = buildField(properties.getFieldNameEventGroupType(), value);
        event.setField(field);
        field.clear(); // clean up temporary data

        return this;
    }

    /**
     * This method sets a field that represents a <strong>subject</strong>.
     * <p>
     * See {@link EventBuilder#setEventType(char[] eventType)} for more information on how to use this method.
     *
     * @param value The value of the field to set
     * @return The event builder
     * @throws NullPointerException When {@code value} is {@code null}
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    public final EventBuilder setSubject(final char[] value) {

        Validate.notNull(value, "The validated object 'value' is null");

        final Field field = buildField(properties.getFieldNameSubject(), value);
        event.setField(field);
        field.clear(); // clean up temporary data

        return this;
    }

    /**
     * This method sets a field that represents a <strong>subject location</strong>.
     * <p>
     * See {@link EventBuilder#setEventType(char[] eventType)} for more information on how to use this method.
     *
     * @param value The value of the field to set
     * @return The event builder
     * @throws NullPointerException When {@code value} is {@code null}
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    public final EventBuilder setSubjectLocation(final char[] value) {

        Validate.notNull(value, "The validated object 'value' is null");

        final Field field = buildField(properties.getFieldNameSubjectLocation(), value);
        event.setField(field);
        field.clear(); // clean up temporary data

        return this;
    }

    /**
     * This method sets a field that represents an <strong>actor</strong>.
     * <p>
     * See {@link EventBuilder#setEventType(char[] eventType)} for more information on how to use this method.
     *
     * @param value The value of the field to set
     * @return The event builder
     * @throws NullPointerException When {@code value} is {@code null}
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    public final EventBuilder setActor(final char[] value) {

        Validate.notNull(value, "The validated object 'value' is null");

        final Field field = buildField(properties.getFieldNameActor(), value);
        event.setField(field);
        field.clear(); // clean up temporary data

        return this;
    }

    /**
     * This method sets a field that represents an <strong>object</strong>.
     * <p>
     * See {@link EventBuilder#setEventType(char[] eventType)} for more information on how to use this method.
     *
     * @param value The value of the field to set
     * @return The event builder
     * @throws NullPointerException When {@code value} is {@code null}
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    public final EventBuilder setObject(final char[] value) {

        Validate.notNull(value, "The validated object 'value' is null");

        final Field field = buildField(properties.getFieldNameObject(), value);
        event.setField(field);
        field.clear(); // clean up temporary data

        return this;
    }

    /**
     * This method sets a field that represents an <strong>object location</strong>.
     * <p>
     * See {@link EventBuilder#setEventType(char[] eventType)} for more information on how to use this method.
     *
     * @param value The value of the field to set
     * @return The event builder
     * @throws NullPointerException When {@code value} is {@code null}
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    public final EventBuilder setObjectLocation(final char[] value) {

        Validate.notNull(value, "The validated object 'value' is null");

        final Field field = buildField(properties.getFieldNameObjectLocation(), value);
        event.setField(field);
        field.clear(); // clean up temporary data

        return this;
    }

    /**
     * This method sets a field that represents <strong>content as it was before the audited operation was invoked
     * </strong>.
     * <p>
     * See {@link EventBuilder#setEventType(char[] eventType)} for more information on how to use this method.
     *
     * @param value The value of the field to set
     * @return The event builder
     * @throws NullPointerException When {@code value} is {@code null}
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    public final EventBuilder setContentBeforeOperation(final char[] value) {

        Validate.notNull(value, "The validated object 'value' is null");

        final Field field = buildField(properties.getFieldNameContentBeforeOperation(), value);
        event.setField(field);
        field.clear(); // clean up temporary data

        return this;
    }

    /**
     * This method sets a field that represents <strong>content as it was after the audited operation was completed
     * </strong>.
     * <p>
     * See {@link EventBuilder#setEventType(char[] eventType)} for more information on how to use this method.
     *
     * @param value The value of the field to set
     * @return The event builder
     * @throws NullPointerException When {@code value} is {@code null}
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    public final EventBuilder setContentAfterOperation(final char[] value) {

        Validate.notNull(value, "The validated object 'value' is null");

        final Field field = buildField(properties.getFieldNameContentAfterOperation(), value);
        event.setField(field);
        field.clear(); // clean up temporary data

        return this;
    }

    /**
     * This method sets a field that represents a <strong>result of an audited operation</strong>.
     * <p>
     * See {@link EventBuilder#setEventType(char[] eventType)} for more information on how to use this method.
     *
     * @param value The value of the field to set
     * @return The event builder
     * @throws NullPointerException When {@code value} is {@code null}
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    public final EventBuilder setResult(final char[] value) {

        Validate.notNull(value, "The validated object 'value' is null");

        final Field field = buildField(properties.getFieldNameResult(), value);
        event.setField(field);
        field.clear(); // clean up temporary data

        return this;
    }

    /**
     * This method sets a field that represents a (human readable) <strong>result summary of an audited operation
     * </strong>.
     * <p>
     * See {@link EventBuilder#setEventType(char[] eventType)} for more information on how to use this method.
     *
     * @param value The value of the field to set
     * @return The event builder
     * @throws NullPointerException When {@code value} is {@code null}
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    public final EventBuilder setResultSummary(final char[] value) {

        Validate.notNull(value, "The validated object 'value' is null");

        final Field field = buildField(properties.getFieldNameResultSummary(), value);
        event.setField(field);
        field.clear(); // clean up temporary data

        return this;
    }

    /**
     * This method sets a field that represents a (human readable) <strong>event summary of an audited operation
     * </strong>.
     * <p>
     * See {@link EventBuilder#setEventType(char[] eventType)} for more information on how to use this method.
     *
     * @param value The value of the field to set
     * @return The event builder
     * @throws NullPointerException When {@code value} is {@code null}
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    public final EventBuilder setEventSummary(final char[] value) {

        Validate.notNull(value, "The validated object 'value' is null");

        final Field field = buildField(properties.getFieldNameEventSummary(), value);
        event.setField(field);
        field.clear(); // clean up temporary data

        return this;
    }

    /**
     * Builds the event.
     *
     * @return The event created by this builder.
     */
    public final Event build() {
        return event;
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
