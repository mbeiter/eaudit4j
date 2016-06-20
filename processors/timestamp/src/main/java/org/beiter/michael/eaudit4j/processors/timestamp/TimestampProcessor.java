/*
 * #%L
 * This file is part of eAudit4j, a library for creating pluggable
 * auditing solutions, providing an audit processor that retrieves
 * the system time and appends a timestamp as a field to audit events.
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
package org.beiter.michael.eaudit4j.processors.timestamp;

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
import org.beiter.michael.eaudit4j.processors.timestamp.propsbuilder.MapBasedTimestampPropsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * This processors obtains the current system time and adds it to an audit
 * event as a ({@link String}) timestamp in a configurable format.
 */
public class TimestampProcessor
        implements Processor, Reversible {

    /**
     * The logger object for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(TimestampProcessor.class);

    /**
     * A copy of the common properties
     */
    private CommonProperties commonProperties;

    /**
     * A copy of the processor specific properties
     */
    private TimestampProperties properties;

    /**
     * {@inheritDoc}
     */
    @Override
    public final void init(final CommonProperties pCommonProperties) {

        Validate.notNull(pCommonProperties, "The validated object 'pCommonProperties' is null");

        this.commonProperties = new CommonProperties(pCommonProperties);
        this.properties = MapBasedTimestampPropsBuilder.build(pCommonProperties.getAdditionalProperties());
    }

    /**
     * This method processes an event in the default audit stream.
     * <p>
     * The method obtains the system time and adds a timestamp to the event.
     * <p>
     * See {@link org.beiter.michael.eaudit4j.common.Audit#audit(Event)}.
     * See {@link TimestampProcessor#process(Event, String, ProcessingObjects)}.
     *
     * @param event The event to audit
     * @return The event after processing (for synchronous audit implementations), undefined otherwise
     * @throws AuditException when the audit operation fails (e.g. the event already contains a timestamp,
     *                        or the timestamp could not be formatted)
     */
    @Override
    public final Event process(final Event event)
            throws AuditException {

        return process(event, commonProperties.getDefaultAuditStream());
    }

    /**
     * This method processes an event in the provided audit stream.
     * <p>
     * The method obtains the system time and adds a timestamp to the event.
     * <p>
     * See {@link org.beiter.michael.eaudit4j.common.Audit#audit(Event)}.
     * See {@link TimestampProcessor#process(Event, String, ProcessingObjects)}.
     *
     * @param event           The event to audit
     * @param auditStreamName The audit stream to send events to
     * @return The event after processing (for synchronous audit implementations), undefined otherwise
     * @throws AuditException           when the audit operation fails (e.g. the event already contains a timestamp,
     *                                  or the timestamp could not be formatted)
     * @throws NullPointerException     when the {@code auditStreamName} or {@code processingObjects} are {@code null}
     * @throws IllegalArgumentException when {@code auditStreamName} is empty
     */
    @Override
    public final Event process(final Event event, final String auditStreamName)
            throws AuditException {

        return process(event, auditStreamName, new ProcessingObjects());
    }

    /**
     * This method processes an event in the provided audit stream and includes a set of {@link ProcessingObjects}.
     * <p>
     * The method obtains the system time and adds a timestamp to the event.
     * <p>
     * See {@link org.beiter.michael.eaudit4j.common.Audit#audit(Event, String)}.
     *
     * @param event             The event to audit
     * @param auditStreamName   The audit stream to send events to
     * @param processingObjects The processing objects available to the processors
     * @return The event after processing (for synchronous audit implementations), undefined otherwise
     * @throws AuditException           when the audit operation fails (e.g. the event already contains a timestamp,
     *                                  or the timestamp could not be formatted)
     * @throws NullPointerException     when the {@code auditStreamName} or {@code processingObjects} are {@code null}
     * @throws IllegalArgumentException when {@code auditStreamName} is empty
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

        // throw an exception if the event already contains a timestamp
        // At best, this is an indication that this processor has been executed repeatedly in the audit chain
        if (event.containsField(properties.getEventFieldName())) {

            final String error = "The processpr " + this.getClass().getCanonicalName()
                    + " is present multiple times in the audit chain";
            LOG.warn(error);
            throw new AuditException(AuditErrorConditions.CONFIGURATION, error);
        }

        // get a String timestamp representation of NOW()
        final String timestamp = getTimestamp(new Date());

        // add the timestamp to the event
        byte[] bytes;
        try {
            bytes = timestamp.getBytes(commonProperties.getEncoding());
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
     * This method removes the timestamp field from an event. If the event does not contain the field, then it is
     * returned unchanged.
     *
     * @param event The event to revert changes on
     * @return An event with the timestamp field removed
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
                final String error = "The timestamp field could not be removed from the event";
                LOG.warn(error);
                throw new AuditException(AuditErrorConditions.PROCESSING, error);
            }
        } else {

            return event;
        }
    }

    /**
     * Compute a String representation of the provided timestamp.
     *
     * The method uses the the processor's properties to determine the format of the String.
     *
     * @param now The timestamp to convert
     * @return The String representation of the timestamp
     */
    private String getTimestamp(final Date now) {

        // parse the timezone string and create a timezone object
        // (returns GMT if the configuration String is not understood)
        final TimeZone timeZone = TimeZone.getTimeZone(properties.getTimezone());

        // Format the current time with the provided timezone, and turn it into a String.
        // We use the platform's default locale here. This could be made configurable, but we wait until someone asks
        // for it. Using an existing object here will help a lot with performance, and the alternative of using a
        // constant (e.g. Locale.US) is not very appealing either.
        // This is a pattern used throughout the library. Search for Locale.getDefault() to find all
        // locations.
        final SimpleDateFormat dateFormat = new SimpleDateFormat(properties.getFormat(), Locale.getDefault());
        dateFormat.setTimeZone(timeZone);

        return dateFormat.format(now);
    }
}
