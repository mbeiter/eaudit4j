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

/**
 * This interface defines specification (getters, setters, maintenance methods) for fields to be used in audit events.
 */
public interface Field {

    /**
     * Get the name of the field.
     * <p>
     * This name is set, for instance, when the field is created.
     *
     * @return The name of the field
     */
    String getName();

    /**
     * Set the value of the field.
     * <p>
     * This method sets the encoding to {@link Encodings#PLAIN} (i.e. unencoded).
     * <p>
     * See {@link Field#setValue(byte[], Encodings)}.
     *
     * @param value The value to set
     */
    void setValue(byte[] value);

    /**
     * Set the value of the field, indicating the encoding that has been used on the data represented by the value of
     * this field.
     * <p>
     * Note that, depending on the implementation, the value of the field may always be internal represented as binary
     * data, with this binary data being a binary representation of a plain text string, or a binary representation of
     * a hex-encoded string that represents some non-printable binary data.
     * <p>
     * When the encoding is set to a different value than {@link Encodings#PLAIN}, consumers of the field information
     * (e.g. processors) should attempt to decode the value of this field before further processing.
     * <p>
     * Note that implementations of this method <strong>should</strong> create a defensive copy of the provided
     * {@code value}), and store the copy in the field. You need to explicitly clear the data provided to this method
     * if it contains confidential information that you wish to destroy.
     *
     * @param value    The value to set
     * @param encoding The encoding that was used to produce the data in the byte[] array
     */
    void setValue(byte[] value, Encodings encoding);

    /**
     * Get the value of the field.
     * <p>
     * Use the {@link Field#getEncoding()} method to get an understanding if the value returned by this method
     * represents an unencoded or an encoded byte array, and what decoding mechanism would have to be used to restore
     * the plain value.
     *
     * @return The value of the field.
     */
    byte[] getValue();

    /**
     * Get the value of the field as an (encoded) char[].
     * <p>
     * Use the {@link Field#getEncoding()} method to get an understanding if the value returned by this method
     * represents an unencoded or an encoded char array, and what decoding mechanism would have to be used to restore
     * the plain value.
     *
     * @param stringEncoding The String encoding to use when converting the byte value to a char value (e.g. UTF-8)
     * @return The value of the field as an (encoded) char[].
     */
    char[] getCharValue(String stringEncoding);

    /**
     * Get an indication on whether the binary data returned by {@link Field#getValue()} represents a encoded data, and
     * would have to be encoded e.g. for display.
     *
     * @return The recommended encoding for the value of this field.
     */
    Encodings getEncoding();

    /**
     * Create a copy of the field.
     * <p>
     * All elements of the field are copied into a new instance, which is returned to the caller.
     *
     * @return A copy of the field
     */
    Field getCopy();

    /**
     * Clears the content of the field's value.
     * <p>
     * This method overwrites the internal representation of the field value in the instance. Use this call to wipe
     * potentially confidential information from memory.
     */
    void clear();
}
