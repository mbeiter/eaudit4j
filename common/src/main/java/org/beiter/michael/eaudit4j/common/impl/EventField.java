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
import org.beiter.michael.array.Cleanser;
import org.beiter.michael.array.Converter;
import org.beiter.michael.eaudit4j.common.Encodings;
import org.beiter.michael.eaudit4j.common.Field;

/**
 * This class implements an {@link Field}.
 */
public class EventField
        implements Field {

    /**
     * The name of the field
     */
    private String name;

    /**
     * The value of the field
     */
    private byte[] value;

    /**
     * The encoding used to produce the byte[] representation of this field's value
     */
    private Encodings encoding = Encodings.PLAIN; // really making sure...

    /**
     * Creates a set a field from an existing field, making a defensive copy.
     *
     * @param field The field to copy
     * @throws NullPointerException When the {@code field} is {@code null}
     * @see EventField#EventField(String, byte[])
     */
    public EventField(final Field field) {

        Validate.notNull(field, "The validated object 'field' is null");

        this.name = field.getName();
        this.value = field.getValue().clone();
        this.encoding = field.getEncoding();
    }

    /**
     * Create a new field of the specified name, with the specified value (assuming {@link Encodings#PLAIN} encoding).
     * <p>
     * See {@link EventField#EventField(String, byte[], Encodings)}.
     *
     * @param name  The name of the field
     * @param value The initial value of the field
     * @throws NullPointerException     When the {@code name} or {@code value} are {@code null}
     * @throws IllegalArgumentException When {@code name} is empty
     */
    public EventField(final String name, final byte[] value) {

        this(name, value, Encodings.PLAIN);
    }

    /**
     * Create a new field of the specified name, with the specified value and encoding.
     * <p>
     * Note that this constructor creates a defensive copy of the provided {@code value}), and stores the copy in the
     * field. You need to explicitly clear the data provided to this method if it contains confidential information
     * that you wish to destroy.
     * <p>
     *
     * @param name     The name of the field
     * @param value    The initial value of the field
     * @param encoding The encoding of the field
     * @throws NullPointerException     When the {@code name}, {@code value}, or {@code encoding} are {@code null}
     * @throws IllegalArgumentException When {@code name} is empty
     */
    public EventField(final String name, final byte[] value, final Encodings encoding) {

        Validate.notBlank(name, "The validated character sequence 'name' is null or empty");
        Validate.notNull(value, "The validated object 'value' is null");
        Validate.notNull(encoding, "The validated object 'encoding' is null");

        // no need for defensive copies of String...
        this.name = name;

        // make a defensive copy of the value
        this.value = value.clone();

        // no need for defensive copies of ENUM
        this.encoding = encoding;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getName() {

        // no need for defensive copies of String...
        return name;
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException When {@code value} is {@code null}
     */
    @Override
    public final void setValue(final byte[] value) {

        Validate.notNull(value, "The validated object 'value' is null");

        setValue(value, Encodings.PLAIN);
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException When the {@code value} or {@code encoding} are {@code null}
     */
    @Override
    public final void setValue(final byte[] pValue, final Encodings pEncoding) {

        Validate.notNull(pValue, "The validated object 'value' is null");
        Validate.notNull(pEncoding, "The validated object 'encoding' is null");

        // make a defensive copy of the value
        this.value = pValue.clone();

        // no need for defensive copies of ENUM
        this.encoding = pEncoding;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final byte[] getValue() {

        // make a defensive copy of the value
        return value.clone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final char[] getCharValue(final String stringEncoding) {

        // the byte array already represents an encoded string,
        // we hence only have to convert it into char[] here:
        return Converter.toChars(value, stringEncoding);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Encodings getEncoding() {

        // no need for defensive copies of ENUM
        return encoding;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Field getCopy() {

        // use the copy constructor to create a defensive copy of the object
        return new EventField(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void clear() {

        // no need to clear the name of the field, but we have to wipe the value:
        Cleanser.wipe(value);
    }
}
