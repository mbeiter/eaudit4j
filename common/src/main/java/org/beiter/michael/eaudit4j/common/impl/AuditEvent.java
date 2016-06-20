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
import org.beiter.michael.eaudit4j.common.Event;
import org.beiter.michael.eaudit4j.common.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.CharBuffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class implements an {@link Event}.
 */
public class AuditEvent
        implements Event {

    /**
     * The logger object for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(AuditEvent.class);

    /**
     * The record format version implemented by this {@link Event} implementation's JSON serializer
     */
    public static final String FORMAT_VERSION = "1.0";

    /**
     * The fields that comprise a particular audit event.
     */
    private final ConcurrentHashMap<String, Field> fields = new ConcurrentHashMap<>();

    /**
     * Create a new audit event.
     * <p>
     * Use this constructor in case you want to create an empty event, and populate it with the setters.
     */
    public AuditEvent() {

        this(new Field[]{}); // could do this(null), but that is ambiguous in var args methods
    }

    /**
     * Create a new audit event.
     * <p>
     * Use this constructor in case you want to create an event that is pre-populated with the provided fields.
     * <p>
     * You may still use the setters to add additional fields.
     * <p>
     * Note that this constructor creates defensive copies of the data in the provided fields. You need to explicitly
     * clear the fields provided to this constructor if they contain confidential information that you wish to destroy.
     *
     * @param fields The fields to set
     */
    // This constructor creates a defensive copuy of the provided fields in a loop
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public AuditEvent(final Field... fields) {

        if (fields != null && fields.length > 0) {

            for (final Field field : fields) {

                // we could simply ignore a null field here, as we did with a null argument to this method, but having
                // a null field in a list of fields seems to be a problem we should report back to the caller.
                Validate.notNull(field, "A 'field' object in the provided var args list is null");

                // create a defensive copy of the field
                final Field tmpField = new EventField(field);

                // assign the field to the existing map
                this.fields.put(tmpField.getName(), tmpField);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getRecordFormatVersion() {

        return FORMAT_VERSION;
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException When {@code field} is {@code null}
     */
    @Override
    public final String setField(final Field field) {

        Validate.notNull(field, "The validated object 'field' is null");

        // create a defensive copy of the field
        final Field tmpField = new EventField(field);

        // assign the field to the existing map
        this.fields.put(field.getName(), tmpField);

        return field.getName();
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException     When {@code fieldName} is {@code null}
     * @throws IllegalArgumentException When {@code className} is empty
     */
    @Override
    public final boolean containsField(final String fieldName) {

        Validate.notBlank(fieldName, "The validated character sequence 'fieldName' is null or empty");

        return fields.containsKey(fieldName);
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException     When {@code fieldName} is {@code null}
     * @throws IllegalArgumentException When {@code className} is empty
     */
    @Override
    public final boolean unsetField(final String fieldName) {

        Validate.notBlank(fieldName, "The validated character sequence 'fieldName' is null or empty");

        // return "true" if the field was found in the map, "false" otherwise
        return fields.remove(fieldName) != null;
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException     When {@code fieldName} is {@code null}
     * @throws IllegalArgumentException When {@code className} is empty
     */
    @Override
    public final Field getField(final String fieldName) {

        Validate.notBlank(fieldName, "The validated character sequence 'fieldName' is null or empty");

        if (!fields.containsKey(fieldName)) {
            final String error = "The field " + fieldName + " is not registered with this event.";
            LOG.info(error);
            throw new NoSuchElementException(error);
        }

        // return a defensive copy of the requested field
        return new EventField(fields.get(fieldName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final List<String> getFieldNames() {

        // Use an array list instead of a vector - much fast due to lack of synchronization,
        // and the caller must cope with the implications of the List interface anyway...
        final List<String> fieldNames = new ArrayList<>(fields.keySet().size());

        // copy the field names into a new structure, we do not want to
        // return the original enumeration that backs the internal map!
        for (final String fieldName : fields.keySet()) {
            fieldNames.add(fieldName);
        }

        // wrap the result to help the caller avoid accidental modifications...
        return Collections.unmodifiableList(fieldNames);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void clear() {

        // first remove the confidential information from each entry...
        for (final Field field : fields.values()) {

            field.clear();
        }

        // ... then clear all field references
        fields.clear();
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException     When {@code encoding} is null
     * @throws IllegalArgumentException When {@code encoding} is empty
     */
    @Override
    public final char[] toJson(final String encoding) {

        // no need to employ a builder here, this is all static data and the compiler can optimize this
        final char[] header = ("{\"version\":\"" + getRecordFormatVersion() + "\",\"fields\":").toCharArray();

        final char[] body = fieldsToJson(encoding);

        // The compiler will optimize this...
        // I find this code easier to read than creating & populating the char array directly
        final char[] footer = "}".toCharArray();

        // create a result array and copy the three pieces (header, body, footer) into that target array
        final char[] result = new char[header.length + body.length + footer.length];
        System.arraycopy(header, 0, result, 0, header.length);
        System.arraycopy(body, 0, result, header.length, body.length);
        System.arraycopy(footer, 0, result, header.length + body.length, footer.length);

        // clear the confidential information from the body
        // (header and footer do not contain confidential data, and do not have to be cleared)
        Cleanser.wipe(body);

        return result;
    }

    /**
     * Create a JSON serialized representation of the event's fields using the provided String encoding.
     * <p>
     * In this implementation, we craft the JSON string manually to avoid dependencies on a specific serializer
     * implementation - and allow us to create the char array without a String detour, allowing us (and the caller /
     * user of the char array) to clear the memory of all confidential data.
     * <p>
     * When calling this method, make sure to use the same encoding that was used when setting the field values, for
     * instance with the {@link Field#setValue(byte[], org.beiter.michael.eaudit4j.common.Encodings)} method.
     *
     * @param encoding The string encoding to use
     * @return A JSON representation of the event.
     * @throws NullPointerException     When {@code encoding} is null
     * @throws IllegalArgumentException When {@code encoding} is empty
     */
    private char[] fieldsToJson(final String encoding) {

        Validate.notBlank(encoding, "The validated character sequence 'encoding' is null or empty");

        // calculate the minimum size of the CharBuffer
        int minSize = 2; // the opening and closing bracket of the JSON map
        for (final Map.Entry<String, Field> entry : fields.entrySet()) {

            minSize += 6; // for each entry, we need four single quotes ('), a colon (:), and usually a comma (,)

            minSize += entry.getKey().length();

            // this gives us the length of the byte representation, which may be somewhat longer
            // than the char representation. It is accurate enough though for what we need here,
            // and it is always longer (never shorter), which is key.
            minSize += entry.getValue().getValue().length;
        }

        final CharBuffer charBuffer = CharBuffer.allocate(minSize);
        charBuffer.put("{");

        // the first entry is not prefixed by a comma
        boolean firstEntry = true;
        for (final Map.Entry<String, Field> entry : fields.entrySet()) {

            if (!firstEntry) {
                charBuffer.put(",");
            }
            charBuffer.put("\"");
            charBuffer.put(entry.getKey());
            charBuffer.put("\":\"");
            charBuffer.put(Converter.toChars(entry.getValue().getValue(), encoding));
            charBuffer.put("\"");
            firstEntry = false;
        }
        charBuffer.put("}");

        // copy the results
        final char[] result = Arrays.copyOfRange(charBuffer.array(), 0, charBuffer.position());

        // clear all temporary data
        Cleanser.wipe(charBuffer.array());

        return result;
    }
}
