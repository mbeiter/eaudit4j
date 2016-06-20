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

import java.util.List;

/**
 * This class implements an interface to represent audit events, an event being comprised of a set of {@link Field}s.
 */
public interface Event {

    /**
     * Each implementation of the {@code Event} interface comes with a JSON serialization.
     * <p>
     * This method returns the record format provided in the implementation's JSON serializer. See the library's
     * This method returns the record format provided in the implementation's JSON serializer. See the library's
     * documentation on specifications of supported record format versions.
     *
     * @return The record format version implemented by the {@code Event} implementation's JSON serializer
     */
    String getRecordFormatVersion();

    /**
     * Add a field to the event.
     * <p>
     * Note that implementations of this method <strong>should</strong> create a defensive copy of the provided
     * {@code Field}), and stores the copy in the event. You need to explicitly clear the data provided to this method
     * if it contains confidential information that you wish to destroy.
     *
     * @param field The field to add
     * @return The name of the field that was added to the event
     */
    String setField(Field field);

    /**
     * Checks if the event contains a field with the specified name.
     *
     * @param fieldName The field name to check for
     * @return {@code true} if the field is registered with the event, {@code false} otherwise
     */
    boolean containsField(String fieldName);

    /**
     * Remove a field from the event.
     *
     * @param fieldName The name of the field to remove
     * @return {@code true} if the field could be removed, {@code false} otherwise
     */
    boolean unsetField(String fieldName);

    /**
     * Retrieve the value of a field from the event.
     *
     * @param fieldName The name of the field to retrieve.
     * @return The value of the specified field
     * @throws java.util.NoSuchElementException When the field is not part of the Event
     */
    Field getField(String fieldName);

    /**
     * Retrieve a list of all fields (i.e. a list of field names) currently registered with the event.
     * <p>
     * Note that the returned implementation of {@link List} may or may not be thread safe. If multiple threads access
     * the returned list concurrently, and at least one of the threads modifies the list structurally, it should be
     * synchronized externally.
     * <p>
     * Note that the returned implementation of {@link List} may be an unmodifiable List (e.g. wrapped into
     * {@code Collections.unmodifiableList()}.
     *
     * @return A list of field names
     */
    List<String> getFieldNames();

    /**
     * Clears the content of the field's value.
     * <p>
     * This method calls the {@link Field#clear()} method of all fields associated with this event, and then removes
     * them from the event. Use this call to wipe potentially confidential information from memory.
     */
    void clear();

    /**
     * Create a JSON serialized representation of the event using the provided String encoding. This JSON
     * representation in particular contains all fields that are registered with the event.
     * <p>
     * The event is serialized into a default format, the version of which can be obtained with the
     * {@link Event#getRecordFormatVersion()} method.
     * <p>
     * When calling this method, make sure to use the same encoding that was used when setting the field values, for
     * instance with the {@link Field#setValue(byte[], Encodings)} method.
     *
     * @param encoding The string encoding to use (e.g. UTF-8)
     * @return A JSON representation of the event.
     */
    char[] toJson(String encoding);
}
