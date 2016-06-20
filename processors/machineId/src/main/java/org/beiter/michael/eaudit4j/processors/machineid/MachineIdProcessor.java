/*
 * #%L
 * This file is part of eAudit4j, a library for creating pluggable
 * auditing solutions, providing an audit processor that creates
 * a unique machine ID for the machine executing the library and
 * appends it as a field to audit events.
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
package org.beiter.michael.eaudit4j.processors.machineid;

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
import org.beiter.michael.eaudit4j.processors.machineid.propsbuilder.MapBasedMachineIdPropsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This processors allows to either configure a machine identifier through
 * a configuration property, or to determine it from an environment variable,
 * from the hostname, or fall back on a randomly generated ID.
 * <p>
 * Note that a specific instance of this class will always return the same
 * machine ID once it has been determined.
 */
public class MachineIdProcessor
        implements Processor, Reversible {

    /**
     * The logger object for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(MachineIdProcessor.class);

    /**
     * A copy of the common properties
     */
    private CommonProperties commonProperties;

    /**
     * A copy of the processor specific properties
     */
    private MachineIdProperties properties;

    /**
     * The machine ID
     * (not static, because we want applications to use more than one machine ID configuration if they need to)
     */
    private final AtomicReference<String> machineId = new AtomicReference<>();


    /**
     * {@inheritDoc}
     */
    @Override
    public final void init(final CommonProperties pCommonProperties) {

        Validate.notNull(pCommonProperties, "The validated object 'pCommonProperties' is null");

        this.commonProperties = new CommonProperties(pCommonProperties);
        this.properties = MapBasedMachineIdPropsBuilder.build(pCommonProperties.getAdditionalProperties());
    }

    /**
     * This method processes an event in the default audit stream.
     * <p>
     * The method obtains a machine ID, and adds that machine ID to the event.
     * <p>
     * See {@link org.beiter.michael.eaudit4j.common.Audit#audit(Event)}.
     * See {@link MachineIdProcessor#process(Event, String, ProcessingObjects)}.
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
     * The method obtains a machine ID, and adds that machine ID to the event.
     * <p>
     * See {@link org.beiter.michael.eaudit4j.common.Audit#audit(Event)}.
     * See {@link MachineIdProcessor#process(Event, String, ProcessingObjects)}.
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
     * The method obtains a machine ID, and adds that machine ID to the event.
     * <p>
     * The machine ID is resolved as follows:
     * <ul>
     * <li>First, check if a machine ID has been explicitly configured, and use that ID</li>
     * <li>If no machine ID has been configured, try to retrieve the machine ID from an environment variable.</li>
     * <li>If this failed (e.g. the processor is not configured to retrieve the machine ID from the environment,
     * or that retrieval operation failed), then try build a machine ID from a combination of the canonical host name
     * and a timestamp</li>
     * <li>If this failed (e.g. the processor is not configured to use the canonical hostname, or the canonical
     * hostname could not be resolved), then the processor will create a pseudo-random machine ID and log that
     * machine ID in the log.</li>
     * </ul>
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

        // throw an exception if the event already contains a machine ID.
        // At best, this is an indication that this processor has been executed repeatedly in the audit chain
        if (event.containsField(properties.getEventFieldName())) {

            final String error = "The processor " + this.getClass().getCanonicalName()
                    + " is present multiple times in the audit chain";
            LOG.warn(error);
            throw new AuditException(AuditErrorConditions.CONFIGURATION, error);
        }

        // we could further reduce unnecessary performance loss by introducing a double synchronized block, but it is
        // okay to go through the machine ID resolution multiple times (and throw n-1 of the results away) like this
        // because the operation is cheap and we can so avoid unnecessary locking through a double-check if/synchronized
        if (machineId.get() == null) {

            String resolvedMachineId;

            // if the properties have a machine ID configured, we can use that instead of making one up / creating one
            // from other means:
            if (properties.getMachineId() == null || properties.getMachineId().isEmpty()) {

                resolvedMachineId = Util.getMachineId(
                        properties.isMachineIdFromEnv(),
                        properties.getMachineIdEnvName(),
                        properties.isMachineIdFromHostname());
            } else {

                resolvedMachineId = properties.getMachineId();
            }

            machineId.compareAndSet(null, resolvedMachineId);
        }

        // add the machine ID to the event
        byte[] bytes;
        try {
            bytes = machineId.get().getBytes(commonProperties.getEncoding());
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
     * This method removes the machine ID field from an event. If the event does not contain the field, then it is
     * returned unchanged.
     *
     * @param event The event to revert changes on
     * @return An event with the machine ID field removed
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
                final String error = "The machine ID field could not be removed from the event";
                LOG.warn(error);
                throw new AuditException(AuditErrorConditions.PROCESSING, error);
            }
        } else {

            return event;
        }
    }

}
