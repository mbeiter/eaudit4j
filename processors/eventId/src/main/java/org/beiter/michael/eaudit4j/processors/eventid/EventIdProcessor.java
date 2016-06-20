/*
 * #%L
 * This file is part of eAudit4j, a library for creating pluggable
 * auditing solutions, providing an audit processor that creates
 * a random event ID that is appended as a field to audit events.
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
package org.beiter.michael.eaudit4j.processors.eventid;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.Validate;
import org.beiter.michael.array.Cleanser;
import org.beiter.michael.eaudit4j.common.AuditErrorConditions;
import org.beiter.michael.eaudit4j.common.AuditException;
import org.beiter.michael.eaudit4j.common.CommonProperties;
import org.beiter.michael.eaudit4j.common.Event;
import org.beiter.michael.eaudit4j.common.Field;
import org.beiter.michael.eaudit4j.common.ProcessingObjects;
import org.beiter.michael.eaudit4j.common.Processor;
import org.beiter.michael.eaudit4j.common.Reversible;
import org.beiter.michael.eaudit4j.common.impl.EventField;
import org.beiter.michael.eaudit4j.processors.eventid.propsbuilder.MapBasedEventIdPropsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;

/**
 * This processors generates a random event identifier of configurable length
 * (and entropy), and appends it to the event.
 */
public class EventIdProcessor
        implements Processor, Reversible {

    /**
     * The logger object for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(EventIdProcessor.class);

    /**
     * A copy of the common properties
     */
    private CommonProperties commonProperties;

    /**
     * A copy of the processor specific properties
     */
    private EventIdProperties properties;

    /**
     * Initializing SecureRandom can be very time consuming, and (based on the local
     * Java security configuration) may be even blocking. Using a ThreadLocal instance
     * can reduce the impact of problem. Using a singleton SecureRandom would be possible
     * (it is threadsafe), but it may get congested if too many threads are accessing
     * it in parallel.
     */
    private static final ThreadLocal<SecureRandom> SECURE_RANDOM = new ThreadLocal<SecureRandom>();

    /**
     * {@inheritDoc}
     */
    @Override
    public final void init(final CommonProperties pCommonProperties) {

        Validate.notNull(pCommonProperties, "The validated object 'pCommonProperties' is null");

        this.commonProperties = new CommonProperties(pCommonProperties);
        this.properties = MapBasedEventIdPropsBuilder.build(pCommonProperties.getAdditionalProperties());
    }

    /**
     * This method processes an event in the default audit stream.
     * <p>
     * The method creates an event ID, and adds that event ID to the event.
     * <p>
     * See {@link org.beiter.michael.eaudit4j.common.Audit#audit(Event)}.
     * See {@link EventIdProcessor#process(Event, String, ProcessingObjects)}.
     *
     * @param event The event to audit
     * @return The event after processing (for synchronous audit implementations), undefined otherwise
     * @throws AuditException when the audit operation fails (e.g. no event ID could be created)
     */
    @Override
    public final Event process(final Event event)
            throws AuditException {

        return process(event, commonProperties.getDefaultAuditStream());
    }

    /**
     * This method processes an event in the provided audit stream.
     * <p>
     * The method creates an event ID, and adds that event ID to the event.
     * <p>
     * See {@link org.beiter.michael.eaudit4j.common.Audit#audit(Event)}.
     * See {@link EventIdProcessor#process(Event, String, ProcessingObjects)}.
     *
     * @param event           The event to audit
     * @param auditStreamName The audit stream to send events to
     * @return The event after processing (for synchronous audit implementations), undefined otherwise
     * @throws AuditException           When the audit operation fails (e.g. no event ID could be created)
     * @throws NullPointerException     When the {@code auditStreamName} or {@code processingObjects} are {@code null}
     * @throws IllegalArgumentException When {@code auditStreamName} is empty
     */
    @Override
    public final Event process(final Event event, final String auditStreamName)
            throws AuditException {

        return process(event, auditStreamName, new ProcessingObjects());
    }

    /**
     * This method processes an event in the provided audit stream and includes a set of {@link ProcessingObjects}.
     * <p>
     * The method creates an event ID, and adds that event ID to the event.
     * <p>
     * The event ID is created with a cryptographically secure pseudo random number generator.
     * <p>
     * See {@link org.beiter.michael.eaudit4j.common.Audit#audit(Event, String)}.
     *
     * @param event             The event to audit
     * @param auditStreamName   The audit stream to send events to
     * @param processingObjects The processing objects available to the processors
     * @return The event after processing (for synchronous audit implementations), undefined otherwise
     * @throws AuditException           when the audit operation fails (e.g. the event already contains a event ID,
     *                                  or no event ID could be created)
     * @throws NullPointerException     When the {@code auditStreamName} or {@code processingObjects} are {@code null}
     * @throws IllegalArgumentException When {@code auditStreamName} is empty
     */
    @Override
    public final Event process(final Event event,
                               final String auditStreamName,
                               final ProcessingObjects processingObjects)
            throws AuditException {

        if (event == null) {
            final String error = "The validated object 'event' is null";
            LOG.warn(error);
            throw new AuditException(AuditErrorConditions.INVALID_EVENT, error);
        }

        // this implementation does not use audit streams, but validate anyway to ensure library API compliance
        Validate.notBlank(auditStreamName, "The validated character sequence 'auditStreamName' is null or empty");

        // this implementation does not use processing objects, but validate anyway to ensure library API compliance
        Validate.notNull(processingObjects, "The validated object 'processingObjects' is null");

        // Make sure that the properties we need are available
        if (properties == null) {

            final String error = "eAudit4j processor specific properties for " + this.getClass().getCanonicalName()
                    + " have not been initialized.";
            LOG.warn(error);
            throw new AuditException(AuditErrorConditions.INITIALIZATION, error);
        }

        // throw an exception if the event already contains an event ID.
        // At best, this is an indication that this processor has been executed repeatedly in the audit chain
        if (event.containsField(properties.getEventFieldName())) {

            final String error = "The processor " + this.getClass().getCanonicalName()
                    + " is present multiple times in the audit chain";
            LOG.warn(error);
            throw new AuditException(AuditErrorConditions.CONFIGURATION, error);
        }

        final String eventId = createEventId(properties);

        // add the event ID to the event
        // we are operating off the String's byte representation, instead of the original byte array, because this
        // gives us the correct length after encoding.
        byte[] bytes;
        try {
            bytes = eventId.getBytes(commonProperties.getEncoding());
        } catch (UnsupportedEncodingException e) {
            final String error = "Encoding is not supported: " + commonProperties.getEncoding();
            LOG.warn(error);
            throw new AuditException(AuditErrorConditions.PROCESSING, error, e);
        }

        final Field field = new EventField(properties.getEventFieldName(), bytes);

        event.setField(field);

        // clear confidential data from local variables
        Cleanser.wipe(bytes);
        field.clear();

        return event;
    }

    /**
     * This processor does not store any confidential information.
     * <p>
     * The implementation of this method does nothing.
     */
    @Override
    public void cleanUp() {

        // do nothing
    }

    /**
     * This method removes the event ID field from an event. If the event does not contain the field, then it is
     * returned unchanged.
     *
     * @param event The event to revert changes on
     * @return An event with the event ID field removed
     * @throws AuditException When the operation fails
     */
    @Override
    public final Event revert(final Event event)
            throws AuditException {

        Validate.notNull(event, "The validated object 'event' is null");

        if (event.containsField(properties.getEventFieldName())) {

            final boolean result = event.unsetField(properties.getEventFieldName());

            if (result) {
                return event;
            } else {
                final String error = "The event ID field could not be removed from the event";
                LOG.warn(error);
                throw new AuditException(AuditErrorConditions.PROCESSING, error);
            }
        } else {

            return event;
        }
    }

    /**
     * Creates an event ID with the configured String length
     *
     * @param pProperties The processor configuration
     * @return An event ID with the configured String length
     */
    private String createEventId(final EventIdProperties pProperties) {

        if (SECURE_RANDOM.get() == null) {
            LOG.info("Trying to create a new instance of SecureRandom for thread ID '" + Thread.currentThread().getId()
                    + "'. If this operation blocks, your entropy pool is depleted and needs to refill first.");
            SECURE_RANDOM.set(new SecureRandom());
            LOG.info("Successfully created a new instance of SecureRandom for thread ID '"
                    + Thread.currentThread().getId() + "'.");
        }

        // we will create too many random bytes here, but it is slower to create a precise estimate
        // than simply creating a longer byte array (and String) and then later cutting it.
        final byte[] eventId = new byte[pProperties.getLength()];
        SECURE_RANDOM.get().nextBytes(eventId);

        // truncate the ID here to the requested length
        return Base64.encodeBase64URLSafeString(eventId).substring(0, pProperties.getLength());
    }
}
