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

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import org.apache.commons.lang3.Validate;
import org.beiter.michael.eaudit4j.common.AuditErrorConditions;
import org.beiter.michael.eaudit4j.common.AuditException;
import org.beiter.michael.eaudit4j.common.CommonProperties;
import org.beiter.michael.eaudit4j.common.Event;
import org.beiter.michael.eaudit4j.common.Field;
import org.beiter.michael.eaudit4j.common.ProcessingObjects;
import org.beiter.michael.eaudit4j.common.Processor;
import org.beiter.michael.eaudit4j.common.Reversible;
import org.beiter.michael.eaudit4j.processors.cassandra.propsbuilder.MapBasedCassandraPropsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CassandraProcessor implements Processor, Reversible {
    /**
     * The logger object for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(CassandraProcessor.class);

    /**
     * A copy of the common properties
     */
    protected CommonProperties commonProperties;

    /**
     * A copy of the processor specific properties
     */
    protected CassandraProperties properties;

    @Override
    public void init(CommonProperties properties) {
        Validate.notNull(properties, "The validated object 'properties' is null");

        this.commonProperties = new CommonProperties(properties);
        this.properties = MapBasedCassandraPropsBuilder.build(properties.getAdditionalProperties());
    }

    @Override
    public Event process(Event event) throws AuditException {
        return process(event, commonProperties.getDefaultAuditStream());
    }

    @Override
    public Event process(Event event, String auditStreamName) throws AuditException {
        return process(event, auditStreamName, new ProcessingObjects());
    }

    @Override
    public Event process(Event event, String auditStreamName, ProcessingObjects processingObjects) throws AuditException {
        if (event == null) {
            final String error = "The validated object 'event' is null";
            LOG.warn(error);
            throw new AuditException(AuditErrorConditions.INVALID_EVENT, error);
        }

        Validate.notBlank(auditStreamName, "The validated character sequence 'auditStreamName' is null or empty");
        Validate.notNull(processingObjects, "The validated object 'processingObjects' is null");

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

        final String eventJson = String.valueOf(event.toJson(properties.getStringEncoding()));

        persistEvent(auditStreamName, eventId, eventJson, processingObjects);

        return event;
    }

    @Override
    public void cleanUp() {
        //do nothing
    }

    @Override
    public Event revert(Event event) throws AuditException {
        Validate.notNull(event, "The validated object 'event' is null");
        // do nothing
        return event;
    }

    private Session getSession(ProcessingObjects processingObjects) throws AuditException {
        if (processingObjects.contains(MapBasedCassandraPropsBuilder.KEY_CASSANDRA_CONNECTION_SESSION)) {
            return (Session) processingObjects.get(MapBasedCassandraPropsBuilder.KEY_CASSANDRA_CONNECTION_SESSION);
        } else {
            final String error = "Cannot retrieve a connection from Cassandra cluster";
            LOG.warn(error);
            throw new AuditException(AuditErrorConditions.PROCESSING, error);
        }
    }

    private void persistEvent(final String auditStreamName, final String eventId, final String eventJson,
                              final ProcessingObjects processingObjects)
            throws AuditException {
        try {
            Session session = getSession(processingObjects);

            Statement query = new SimpleStatement(properties.getInsertEventSqlStmt(),
                    eventId, auditStreamName, eventJson);
            ResultSet rsEvent = session.execute(query);

            if (!rsEvent.wasApplied()) {
                final String error = "Error when persisting the audit event. The operation should have affected '1' "
                        + "index row per field, but a total of 0 rows was affected";
                LOG.warn(error);
                throw new AuditException(AuditErrorConditions.PROCESSING, error);
            }
        }catch (InvalidQueryException e) {
            final String error = "Unrecoverable error when executing the SQL transaction";
            LOG.warn(error, e);
            throw new AuditException(AuditErrorConditions.PROCESSING, error, e);
        }
    }
}
