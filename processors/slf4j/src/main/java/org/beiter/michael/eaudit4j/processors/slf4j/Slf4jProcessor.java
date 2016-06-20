/*
 * #%L
 * This file is part of eAudit4j, a library for creating pluggable
 * auditing solutions, providing an audit processor that logs audit
 * events using slf4j.
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
package org.beiter.michael.eaudit4j.processors.slf4j;

import org.apache.commons.lang3.Validate;
import org.beiter.michael.eaudit4j.common.AuditErrorConditions;
import org.beiter.michael.eaudit4j.common.AuditException;
import org.beiter.michael.eaudit4j.common.CommonProperties;
import org.beiter.michael.eaudit4j.common.Event;
import org.beiter.michael.eaudit4j.common.Field;
import org.beiter.michael.eaudit4j.common.ProcessingObjects;
import org.beiter.michael.eaudit4j.common.Processor;
import org.beiter.michael.eaudit4j.common.Reversible;
import org.beiter.michael.eaudit4j.processors.slf4j.propsbuilder.MapBasedSlf4jPropsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.spi.MDCAdapter;

/**
 * This processors persists events to slf4j.
 * <p>
 * The lines that are submitted to slf4j are prefixed with a configurable "audit marker" and the name of the audit
 * stream as provided, or the default audit stream if none was provided.
 * <p>
 * If the underlying logger supports MDC, the following fields are stored in the MDC:
 * <ul>
 * <li>A JSON-serialized representation of the event, using a configured field name as the field identifier</li>
 * <li>The audit stream name, using a configured field name as the field identifier</li>
 * </ul>
 * <p>
 * If so configured, then this processor will additionally include specific fields in the MDC map, making them
 * directly available for further processing by the underlying logger.
 * <p>
 * This implementation supports storing specific {@link Event} fields in the MDC, either under their {@link Event}
 * field name, or through an "alias", which allows storing {@link Event} fields in the MDC using an alternative name.
 */
public class Slf4jProcessor
        implements Processor, Reversible {

    /**
     * The logger object for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(Slf4jProcessor.class);

    /**
     * A copy of the common properties
     */
    private CommonProperties commonProperties;

    /**
     * A copy of the processor specific properties
     */
    private Slf4jProperties properties;


    /**
     * {@inheritDoc}
     */
    @Override
    public final void init(final CommonProperties pCommonProperties) {

        Validate.notNull(pCommonProperties, "The validated object 'pCommonProperties' is null");

        this.commonProperties = new CommonProperties(pCommonProperties);
        this.properties = MapBasedSlf4jPropsBuilder.build(pCommonProperties.getAdditionalProperties());
    }

    /**
     * This method processes an event in the default audit stream.
     * <p>
     * See {@link org.beiter.michael.eaudit4j.common.Audit#audit(Event)}.
     * See {@link Slf4jProcessor#process(Event, String, ProcessingObjects)}.
     *
     * @param event The event to audit
     * @return The event after processing (for synchronous audit implementations), undefined otherwise
     * @throws AuditException when the audit operation fails (e.g. no machine ID could be obtained)
     */
    @Override
    public final Event process(final Event event)
            throws AuditException {

        return process(event, commonProperties.getDefaultAuditStream());
    }

    /**
     * This method processes an event in the provided audit stream.
     * <p>
     * See {@link org.beiter.michael.eaudit4j.common.Audit#audit(Event)}.
     * See {@link Slf4jProcessor#process(Event, String, ProcessingObjects)}.
     *
     * @param event           The event to audit
     * @param auditStreamName The audit stream to send events to
     * @return The event after processing (for synchronous audit implementations), undefined otherwise
     * @throws AuditException           when the audit operation fails (e.g. no machine ID could be obtained)
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
     * See {@link org.beiter.michael.eaudit4j.common.Audit#audit(Event, String)}.
     *
     * @param event             The event to audit
     * @param auditStreamName   The audit stream to send events to
     * @param processingObjects The processing objects available to the processors
     * @return The event after processing (for synchronous audit implementations), undefined otherwise
     * @throws AuditException           when the audit operation fails (e.g. the event already contains a machine ID,
     *                                  or no machine ID could be obtained)
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

        // Store the audit stream in the MDC
        addAuditStreamNameToMdc(auditStreamName, properties);

        // populate the MDC with specific fields from the event, as configured in the properties
        addEventFieldsToMdc(event, properties);

        // add the JSON representing the event to the MDC
        final String json = String.valueOf(event.toJson(properties.getStringEncoding()));
        addSerializedEventToMdc(json, properties);

        // Log the JSON:
        LOG.info(properties.getMarker() + auditStreamName + ": " + json);

        // clear the MDC
        MDC.clear();

        // return the event unchanged
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
     * This processor does not alter the {@link Event}.
     *
     * @param event The event to revert changes on
     * @return An event with the machine ID field removed
     * @throws AuditException When the operation fails
     */
    @Override
    public final Event revert(final Event event)
            throws AuditException {

        Validate.notNull(event, "The validated object 'event' is null");

        // do nothing
        return event;
    }

    /**
     * Add the audit stream name to the MDC
     *
     * @param auditStreamName The audit stream name to add to the MDC
     * @param pProperties     The processor configuration
     * @return The {@code MDCAdapter} that is used in the modified MDC
     */
    private MDCAdapter addAuditStreamNameToMdc(final String auditStreamName, final Slf4jProperties pProperties) {

        MDC.put(pProperties.getAuditStreamFieldName(), auditStreamName);

        // return the MDC Adapter
        // (the MDC is global, so the MDC Adapter is not really used in this class, but it is very helpful in unit
        // testing to get access to the underlying map without making costly copies)
        return MDC.getMDCAdapter();
    }

    /**
     * Add the serialized event JSON to the MDC
     *
     * @param serializedEvent The serialized event to add to the MDC
     * @param pProperties      The processor configuration
     * @return The {@code MDCAdapter} that is used in the modified MDC
     */
    private MDCAdapter addSerializedEventToMdc(final String serializedEvent, final Slf4jProperties pProperties) {

        MDC.put(pProperties.getSerializedEventFieldName(), serializedEvent);

        // return the MDC Adapter
        // (the MDC is global, so the MDC Adapter is not really used in this class, but it is very helpful in unit
        // testing to get access to the underlying map without making costly copies)
        return MDC.getMDCAdapter();
    }

    /**
     * Add the fields to the MDC as configured
     *
     * @param event       The event to take the fields from
     * @param pProperties The processor configuration
     * @return The {@code MDCAdapter} that is used in the modified MDC
     * @throws AuditException When there is an invalid MDC field list configuration
     */
    // Need to validate sizes of the mdcField split to decide whether we use a custom name, or the field name
    // We could do that with a null check, but that seems to be awkward.
    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    private MDCAdapter addEventFieldsToMdc(final Event event, final Slf4jProperties pProperties)
            throws AuditException {

        // split the configured event name list, and see what we need to add to the MDC
        if (pProperties.getMdcFields() != null && !pProperties.getMdcFields().isEmpty()) {

            // split the list of fields that we need to add to the MDC
            final String[] mdcFieldNames = pProperties.getMdcFields().split(pProperties.getMdcFieldSeparator());

            // Go to the list of configured fields, and check if there is an alias configured.
            // If so, use that alias - otherwise, use the field name directly.
            // Only add the field if it exists in the event (not all events necessarily contain all fields)
            for (final String mdcFieldName : mdcFieldNames) {

                final String[] mdcField = mdcFieldName.split(pProperties.getMdcFieldNameSeparator());
                final Field field; // the actual field
                final String fieldName; // the name we use for the field in the MDC

                if (mdcField.length == 1) {

                    // we do not have a dedicated MDC name configured, hence use the field name.
                    // first, we check if the field has been set:
                    if (event.containsField(mdcField[0])) {

                        // the field exists for this event, which means we can pull it
                        field = event.getField(mdcField[0]);
                        fieldName = field.getName();
                    } else {

                        // the field does not exist in this event, hence proceed to the next field
                        continue;
                    }
                } else if (mdcField.length == 2) {

                    // we do have a dedicated MDC name configured
                    // first, we check if the field has been set:
                    if (event.containsField(mdcField[0])) {

                        // the field exists for this event, which means we can pull it
                        field = event.getField(mdcField[0]);
                        fieldName = mdcField[1];
                    } else {

                        // the field does not exist in this event, hence proceed to the next field
                        continue;
                    }
                } else {
                    // We have less than 1 and more than 2 field name components.
                    // This should never happen.
                    final String error = "The Event field name / MDC mapping is invalid. ";
                    LOG.warn(error);
                    throw new AuditException(AuditErrorConditions.CONFIGURATION, error);
                }

                // write the field to the MDC
                MDC.put(fieldName, String.valueOf(field.getCharValue(pProperties.getStringEncoding())));
            }
        }

        // return the MDC Adapter
        // (the MDC is global, so the MDC Adapter is not really used in this class, but it is very helpful in unit
        // testing to get access to the underlying map without making costly copies)
        return MDC.getMDCAdapter();
    }
}
