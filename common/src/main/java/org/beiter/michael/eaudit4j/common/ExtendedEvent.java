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
 * This interface defines several convenience methods (setters only!) to handle audit fields that are frequently used
 * in audit events.
 */
public interface ExtendedEvent
        extends Event {

    /**
     * This method sets a field that represents an <strong>event type</strong>.
     * <p>
     * This is a convenience method if applications want to use this field, and also want to use the standard field
     * name for it. If you want to set a different field name for this field, you should use the generic
     * {@link Event#setField(Field)} method.
     * <p>
     * The name of the field will be determined by the implementation of this interface.
     * <p>
     * Implementations of this method <strong>should</strong> create a defensive copy of the provided audit data (the
     * {@code char[] value}), and stores the copy in the event. You need to explicitly clear the data provided to this
     * method if it contains confidential information that you wish to destroy.
     *
     * @param value The value of the field to set
     * @return The name of the field that was added to the event
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    String setEventType(char[] value);

    /**
     * This method sets a field that represents an <strong>event group type</strong>.
     * <p>
     * See {@link ExtendedEvent#setEventType(char[] eventType)} for more information on how to use this method.
     *
     * @param value The value of the field to set
     * @return The name of the field that was added to the event
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    String setEventGroupType(char[] value);

    /**
     * This method sets a field that represents a <strong>subject</strong>.
     * <p>
     * See {@link ExtendedEvent#setEventType(char[] eventType)} for more information on how to use this method.
     *
     * @param value The value of the field to set
     * @return The name of the field that was added to the event
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    String setSubject(char[] value);

    /**
     * This method sets a field that represents a <strong>subject location</strong>.
     * <p>
     * See {@link ExtendedEvent#setEventType(char[] eventType)} for more information on how to use this method.
     *
     * @param value The value of the field to set
     * @return The name of the field that was added to the event
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    String setSubjectLocation(char[] value);

    /**
     * This method sets a field that represents an <strong>actor</strong>.
     * <p>
     * See {@link ExtendedEvent#setEventType(char[] eventType)} for more information on how to use this method.
     *
     * @param value The value of the field to set
     * @return The name of the field that was added to the event
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    String setActor(char[] value);

    /**
     * This method sets a field that represents an <strong>object</strong>.
     * <p>
     * See {@link ExtendedEvent#setEventType(char[] eventType)} for more information on how to use this method.
     *
     * @param value The value of the field to set
     * @return The name of the field that was added to the event
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    String setObject(char[] value);

    /**
     * This method sets a field that represents an <strong>object location</strong>.
     * <p>
     * See {@link ExtendedEvent#setEventType(char[] eventType)} for more information on how to use this method.
     *
     * @param value The value of the field to set
     * @return The name of the field that was added to the event
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    String setObjectLocation(char[] value);

    /**
     * This method sets a field that represents <strong>content as it was before the audited operation was invoked
     * </strong>.
     * <p>
     * See {@link ExtendedEvent#setEventType(char[] eventType)} for more information on how to use this method.
     *
     * @param value The value of the field to set
     * @return The name of the field that was added to the event
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    String setContentBeforeOperation(char[] value);

    /**
     * This method sets a field that represents <strong>content as it was after the audited operation was completed
     * </strong>.
     * <p>
     * See {@link ExtendedEvent#setEventType(char[] eventType)} for more information on how to use this method.
     *
     * @param value The value of the field to set
     * @return The name of the field that was added to the event
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    String setContentAfterOperation(char[] value);

    /**
     * This method sets a field that represents a <strong>result of an audited operation</strong>.
     * <p>
     * See {@link ExtendedEvent#setEventType(char[] eventType)} for more information on how to use this method.
     *
     * @param value The value of the field to set
     * @return The name of the field that was added to the event
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    String setResult(char[] value);

    /**
     * This method sets a field that represents a (human readable) <strong>result summary of an audited operation
     * </strong>.
     * <p>
     * See {@link ExtendedEvent#setEventType(char[] eventType)} for more information on how to use this method.
     *
     * @param value The value of the field to set
     * @return The name of the field that was added to the event
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    String setResultSummary(char[] value);

    /**
     * This method sets a field that represents a (human readable) <strong>event summary of an audited operation
     * </strong>.
     * <p>
     * See {@link ExtendedEvent#setEventType(char[] eventType)} for more information on how to use this method.
     *
     * @param value The value of the field to set
     * @return The name of the field that was added to the event
     */
    // Cannot use varargs here, this would be against the point!
    @SuppressWarnings("PMD.UseVarargs")
    String setEventSummary(char[] value);

    /**
     * Create a JSON serialized representation of the event using String encoding specified in
     * {@link CommonProperties#getEncoding()}. This JSON representation in particular contains all fields that are
     * registered with the event.
     * <p>
     * The event is serialized into a default format, the version of which can be obtained with the
     * {@link Event#getRecordFormatVersion()} method.
     *
     * @return A JSON representation of the event.
     */
    char[] toJson();
}
