/*
 * #%L
 * This file is part of eAudit4j, a library for creating pluggable
 * auditing solutions, providing an audit processor that persists
 * audit events to a Cassandra database.
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
package org.beiter.michael.eaudit4j.processors.cassandra;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import org.apache.commons.lang3.Validate;
import org.beiter.michael.eaudit4j.common.AuditErrorConditions;
import org.beiter.michael.eaudit4j.common.AuditException;
import org.beiter.michael.eaudit4j.common.CommonProperties;
import org.beiter.michael.eaudit4j.common.Event;
import org.beiter.michael.eaudit4j.common.FactoryException;
import org.beiter.michael.eaudit4j.common.Field;
import org.beiter.michael.eaudit4j.common.ProcessingObjects;
import org.beiter.michael.eaudit4j.common.Processor;
import org.beiter.michael.eaudit4j.common.Reversible;
import org.beiter.michael.eaudit4j.processors.cassandra.propsbuilder.MapBasedCassandraPropsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This processors persists events to a Cassandra database. The Cassandra session is not managed in this class and must
 * be provided by the users of this processors through the {@link ProcessingObjects} in the
 * {@link CassandraProcessor#process(Event, String, ProcessingObjects)} method.
 * <p>
 * Note that, as this processor relies on the Cassandra session being managed by the integrating application, the
 * methods that do not take a {@link ProcessingObjects} parameter will fail with an exception when called.
 * <p>
 */
public class CassandraProcessor
        implements Processor, Reversible {

    /**
     * The logger object for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(CassandraProcessor.class);

    /**
     * A copy of the common properties
     */
    private CommonProperties commonProperties;

    /**
     * A copy of the processor specific properties
     */
    private CassandraProperties properties;

    /**
     * {@inheritDoc}
     */
    @Override
    public final void init(final CommonProperties pCommonProperties) {

        Validate.notNull(pCommonProperties, "The validated object 'pCommonProperties' is null");

        this.commonProperties = new CommonProperties(pCommonProperties);
        this.properties = MapBasedCassandraPropsBuilder.build(pCommonProperties.getAdditionalProperties());
    }

    /**
     * This method processes an event in the default audit stream.
     * <p>
     * See {@link org.beiter.michael.eaudit4j.common.Audit#audit(Event)}.
     * See {@link CassandraProcessor#process(Event, String, ProcessingObjects)}.
     *
     * @param event The event to audit
     * @return The event after processing (for synchronous audit implementations), undefined otherwise
     * @throws AuditException when the audit operation fails (e.g. the event could not be persisted)
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
     * See {@link CassandraProcessor#process(Event, String, ProcessingObjects)}.
     *
     * @param event           The event to audit
     * @param auditStreamName The audit stream to send events to\
     * @return The event after processing (for synchronous audit implementations), undefined otherwise
     * @throws AuditException           when the audit operation fails (e.g. the event could not be persisted)
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
     * @throws AuditException           when the audit operation fails (e.g. the event could not be persisted)
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
        Validate.notNull(processingObjects, "The validated object 'processingObjects' is null");

        // Make sure that the properties we need are available
        if (properties == null) {

            final String error = "eAudit4j processor specific properties for " + this.getClass().getCanonicalName()
                    + " have not been initialized.";
            LOG.warn(error);
            throw new AuditException(AuditErrorConditions.INITIALIZATION, error);
        }

        // extract the event ID from the event, and throw an exception if the event ID is not present
        final String eventId;
        if (event.containsField(properties.getEventIdFieldName())) {
            final Field eventIdField = event.getField(properties.getEventIdFieldName()); // compiler will optimize this
            eventId = String.valueOf(eventIdField.getCharValue(properties.getStringEncoding()));
        } else {
            final String error = "The required field `event ID` is not present in the event. Have you configured "
                    + "(1) a processor that adds (random) event IDs, and "
                    + "(2) configured that processor to run before this processor in the chain, and "
                    + "(3) configured this processor to use the correct name for the event ID field?";
            LOG.warn(error);
            throw new AuditException(AuditErrorConditions.CONFIGURATION, error);
        }

        // get a JSON representation of the event
        final String eventJson = String.valueOf(event.toJson(properties.getStringEncoding()));

        // persist the event
        persistEvent(auditStreamName, processingObjects, eventId, eventJson);

        // return the event unchanged
        return event;
    }

    /**
     * This processor does not store any confidential information.
     * <p>
     * The implementation of this method does nothing.
     */
    @Override
    public final void cleanUp() {

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
     * Retrieve a Cassandra session from the processing objects, using the configured object name to identify the
     * session object.
     *
     * @param pProperties       The processor configuration
     * @param processingObjects The processing objects providing to the class, which may contain a {@link Session}
     *                          object
     * @return A database connection
     * @throws FactoryException When no database connection can be retrieved
     */
    private Session getSession(final CassandraProperties pProperties, final ProcessingObjects processingObjects)
            throws FactoryException {

        final String snName = pProperties.getSessionName();

        // get the session from the processing objects, and throw an exception if the session object is not present or
        // is of the wrong class type
        final Session session;
        if (processingObjects.contains(snName)) {

            final Object snObject = processingObjects.get(snName); // compiler will optimize this
            if (snObject instanceof Session) {

                session = (Session) snObject;
            } else {

                final String error = "The object provided in the 'ProcessingObjects' referenced by the configured "
                        + " session name ('" + snName + "') is not an instance of '"
                        + Session.class.getCanonicalName() + "'";
                LOG.warn(error);
                throw new FactoryException(error);
            }
        } else {

            final String error = "The configured session name ('" + snName
                    + "') does not exist in the 'ProcessingObjects'";
            LOG.warn(error);
            throw new FactoryException(error);
        }

        return session;
    }

    /**
     * Persist an event to Cassandra
     *
     * @param auditStreamName   The name of the audit stream
     * @param processingObjects The provided processing objects
     * @param eventId           The unique event ID of the event
     * @param eventJson         The serialized event
     * @throws AuditException When The database operation fails
     */
    // CHECKSTYLE:OFF
    // this is flagged in checkstyle with a missing whitespace before '}', which is a bug in checkstyle
    // no need to close the ResultSet or Session for Cassandra
    // see http://www.datastax.com/dev/blog/4-simple-rules-when-using-the-datastax-drivers-for-cassandra
    // The datastax driver throws many runtime exceptions that are not very well documented, hence catching them all
    @SuppressWarnings({"PMD.CloseResource", "PMD.AvoidCatchingGenericException"})
    // CHECKSTYLE:ON
    private void persistEvent(final String auditStreamName, final ProcessingObjects processingObjects,
                              final String eventId, final String eventJson)
            throws AuditException {

        // get a Cassandra session
        final Session session;
        try {
            session = getSession(properties, processingObjects);
        } catch (FactoryException e) {
            final String error = "Cannot retrieve Cassandra session";
            LOG.warn(error, e);
            throw new AuditException(AuditErrorConditions.PROCESSING, error, e);
        }

        try {
            // create a prepared statement
            final PreparedStatement prepared = session.prepare(properties.getInsertEventCqlStmt());

            // bind to the prepared statement, populate, and execute it
            final BoundStatement bound = prepared.bind()
                    .setString(properties.getEventIdCqlParam(), eventId)
                    .setString(properties.getAuditStreamNameCqlParam(), auditStreamName)
                    .setString(properties.getEventJsonCqlParam(), eventJson);

            // execute the operation
            final ResultSet rsEvent = session.execute(bound);

            if (!rsEvent.wasApplied()) {
                final String error = "Error when persisting the audit event. The operation was not executed.";
                LOG.warn(error);
                throw new AuditException(AuditErrorConditions.PROCESSING, error);
            }
        } catch (InvalidQueryException e) {

            final String error = "Invalid CQL query";
            LOG.warn(error, e);
            throw new AuditException(AuditErrorConditions.PROCESSING, error, e);
        } catch (Exception e) {

            final String error = "Unrecoverable error when executing the CQL transaction";
            LOG.warn(error, e);
            throw new AuditException(AuditErrorConditions.PROCESSING, error, e);
        }
    }
}
