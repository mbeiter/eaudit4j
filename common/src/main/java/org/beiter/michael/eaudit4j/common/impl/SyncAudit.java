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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.beiter.michael.eaudit4j.common.Audit;
import org.beiter.michael.eaudit4j.common.AuditErrorConditions;
import org.beiter.michael.eaudit4j.common.AuditException;
import org.beiter.michael.eaudit4j.common.CommonProperties;
import org.beiter.michael.eaudit4j.common.Event;
import org.beiter.michael.eaudit4j.common.FactoryException;
import org.beiter.michael.eaudit4j.common.ProcessingObjects;
import org.beiter.michael.eaudit4j.common.Processor;
import org.beiter.michael.eaudit4j.common.ProcessorFactory;
import org.beiter.michael.eaudit4j.common.propsbuilder.MapBasedCommonPropsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class provides a synchronous implementation of {@link Audit}.
 */
public class SyncAudit
        implements Audit {

    /**
     * The logger object for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(SyncAudit.class);

    /**
     * A copy of the common properties, initialize with a default config set:
     */
    private CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();

    /**
     * The List itself is not thread safe, but it also does not need to be modified in this class after initialization
     * (i.e. build once in {@code init()), and then read only from there on). Repeated calls to {@code init()} will
     * create a new list, and then replace this list reference.
     */
    private List<Processor> processors = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public final void init(final CommonProperties properties)
            throws AuditException {

        // store a local copy of the properties
        this.commonProps = new CommonProperties(properties);

        // get the config string will the processors from the configuration
        final String processorClasses = this.commonProps.getProcessors();
        if (processorClasses != null && processorClasses.length() > 0) {

            // split the processor config string to give us a list of classes to instantiate
            final String[] procClassesList = processorClasses.split(",");

            // we want to be thread safe with the configuration, hence we create a local instance of the audit processor
            // chain, and assign it at the end of the init() operation in a single call
            final List<Processor> tmpProcessors = new ArrayList<>(procClassesList.length);

            // the processors are stored from left to right in the config file, so we can simply create them and add
            // them to the list of registered processors (which we will invoke later on in the audit() methods) in the
            // provided order
            for (final String procClassName : procClassesList) {

                if (procClassName != null && !procClassName.isEmpty()) {

                    Processor processor;
                    try {
                        processor = ProcessorFactory.getInstance(procClassName, this.commonProps);
                    } catch (FactoryException e) {
                        final String error = "Failed to retrieve instance of processor class: " + procClassName;
                        LOG.warn(error, e);
                        throw new AuditException(AuditErrorConditions.INITIALIZATION, error, e);
                    }

                    // add the newly created processor to the local tmpProcessors list
                    tmpProcessors.add(processor);
                }
            }

            // swap the processors reference, and make sure that the list cannot be accidentally modified
            processors = Collections.unmodifiableList(tmpProcessors);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Event audit(final Event event)
            throws AuditException {

        if (event == null) {
            final String error = "The validated object 'event' is null";
            LOG.warn(error);
            throw new AuditException(AuditErrorConditions.INVALID_EVENT, error);
        }

        final String auditStream = commonProps.getDefaultAuditStream();
        if (auditStream == null || StringUtils.isBlank(auditStream)) {
            final String error = "The validated character sequence 'auditStream' is null or empty";
            LOG.warn(error);
            throw new AuditException(AuditErrorConditions.CONFIGURATION, error);
        }

        return audit(event, auditStream);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Event audit(final Event event, final String auditStreamName)
            throws AuditException {

        if (event == null) {
            final String error = "The validated object 'event' is null";
            LOG.warn(error);
            throw new AuditException(AuditErrorConditions.INVALID_EVENT, error);
        }

        Validate.notBlank(auditStreamName, "The validated character sequence 'auditStreamName' is null or empty");

        // We create the set of default (empty) ProcessingObjects every time this method is invoked to prevent
        // interference, because there is zero protection on that object by design (processors can modify the objects
        // list and the objects themselves at will, there are no defensive copies or other isolation).
        final ProcessingObjects processingObjects = new ProcessingObjects();

        return audit(event, auditStreamName, processingObjects);
    }

    /**
     * {@inheritDoc}
     */
    // suppress warnings about using a StringBuffer for the error concatenation (only used for exception handling)
    @SuppressWarnings("PMD.UseStringBufferForStringAppends")
    @Override
    public final Event audit(final Event event, final String auditStreamName, final ProcessingObjects processingObjects)
            throws AuditException {

        if (event == null) {
            final String error = "The validated object 'event' is null";
            LOG.warn(error);
            throw new AuditException(AuditErrorConditions.INVALID_EVENT, error);
        }

        Validate.notBlank(auditStreamName, "The validated character sequence 'auditStreamName' is null or empty");
        Validate.notNull(processingObjects, "The validated object 'processingObjects' is null");

        // if the array with processors is empty, no processors have been configured.
        // Log an error (and fail) or just log a warning, depending on the configuration:
        if (processors == null || processors.isEmpty()) {

            String error = "No processors configured for auditing subsystem";
            if (commonProps.isFailOnMissingProcessors()) {
                LOG.warn(error);
                throw new AuditException(AuditErrorConditions.CONFIGURATION, error);
            } else {
                error += ". Audit events are neither being processed, nor persisted.";
                LOG.warn(error);
                return event;
            }
        } else {

            // Go through the processor chain, have each processor work on the event,
            // and feed the modified event into the next processor down the chain:
            Event tmpEvent = event; // start the chain with the original event
            for (final Processor processor : processors) {

                tmpEvent = processor.process(tmpEvent, auditStreamName, processingObjects);
            }

            // once all processing is complete, return the (potentially modified) Event
            return tmpEvent;
        }
    }
}
