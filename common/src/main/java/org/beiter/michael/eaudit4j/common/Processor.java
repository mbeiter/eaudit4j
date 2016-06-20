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
 * A processor can be part of an input / output sequence of processors. When an event is submitted to a processor,
 * then it processes (e.g. modifies or persists) the event. The output of a first processor may be used as the input
 * of a second processor in a audit chain.
 * <p>
 * Classes implementing this interface <b>must</b> be thread safe.
 */
public interface Processor {

    /**
     * Initializes the configuration of an audit processor.
     * <p>
     * A class implementing this interface may provide a reasonable default configuration and handle situations where
     * e.g. the {@code process()} methods are called without a previous call of {@code init()} (i.e. do not throw any
     * exception), but this is not guaranteed (i.e. calling the {@code process()} methods without proper initialization
     * may throw an exception, such as an {@link IllegalStateException} or a {@link AuditException}).
     * <p>
     * A class implementing this interface must ensure that subsequent calls to this method update the class'
     * configuration in a thread-safe way.
     *
     * @param properties The properties to initialize the audit processor with. Supported "additionalParameters" may
     *                   vary with the implementing classes.
     */
    void init(CommonProperties properties);

    /**
     * Audit an event within the default audit stream obtained from the configuration.
     * <p>
     * Implementations of this method may change the event, for instance, by adding new fields to the event, or
     * modifying existing fields in the event.
     *
     * @param event The event to be processed
     * @return The event after processing (including modifications to the original event)
     * @throws AuditException When the event cannot be processed
     */
    Event process(Event event)
            throws AuditException;

    /**
     * Audit an event within the provided audit stream.
     * <p>
     * Applications may support more than one audit streams. Audit streams are event streams that are isolated from
     * each other. As an example, an application may want to support one audit stream per tenant, plus one or more
     * additional audit streams for global events, allowing sharing of audit data with tenants without the risk of
     * compromising other tenants but still maintaining full integrity of the shared audit data.
     * <p>
     * Implementations of this method may change the event, for instance, by adding new fields to the event, or
     * modifying existing fields in the event.
     *
     * @param event           The event to be processed
     * @param auditStreamName The audit stream to send events to
     * @return The event after processing (including modifications to the original event)
     * @throws AuditException When the event cannot be processed
     */
    Event process(Event event, String auditStreamName)
            throws AuditException;

    /**
     * Audit an event within the provided audit stream and a set of {@link ProcessingObjects}.
     * <p>
     * {@link ProcessingObjects} allow an application to inject a set of named objects into the processor. This is
     * commonly used for objects that are managed by applications, and cannot or should not be created by an individual
     * {@link Processor}.
     * <p>
     * Examples for such objects include {@link javax.sql.DataSource} objects to connect to a database, with the
     * database connection pool being managed directly by the application.
     * <p>
     * Implementations of this method may change the event, for instance, by adding new fields to the event, or
     * modifying existing fields in the event.
     *
     * @param event             The event to be processed
     * @param auditStreamName   The audit stream to send events to
     * @param processingObjects The processing objects available to the processor
     * @return The event after processing (including modifications to the original event)
     * @throws AuditException When the event cannot be processed
     */
    Event process(Event event, String auditStreamName, ProcessingObjects processingObjects)
            throws AuditException;

    /**
     * Clean up temporary data / keys / etc
     */
    void cleanUp();
}
