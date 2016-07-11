/*
 * #%L
 * This file is part of eAudit4j, a library for creating pluggable
 * auditing solutions, providing an audit processor that persists
 * audit events to a JDBC database.
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
package org.beiter.michael.eaudit4j.processors.jdbc;

import org.apache.commons.lang3.Validate;
import org.beiter.michael.eaudit4j.common.AuditErrorConditions;
import org.beiter.michael.eaudit4j.common.AuditException;
import org.beiter.michael.eaudit4j.common.CommonProperties;
import org.beiter.michael.eaudit4j.common.Event;
import org.beiter.michael.eaudit4j.common.Field;
import org.beiter.michael.eaudit4j.common.ProcessingObjects;
import org.beiter.michael.eaudit4j.common.Processor;
import org.beiter.michael.eaudit4j.common.Reversible;
import org.beiter.michael.eaudit4j.processors.jdbc.propsbuilder.MapBasedJdbcPropsBuilder;
import org.beiter.michael.db.FactoryException;
import org.beiter.michael.db.propsbuilder.MapBasedConnPropsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This processors persists events to a JDBC database. The connection method (pool, JNDI, ...) is not managed in this
 * class and depends on the implementations extending this class.
 * <p>
 * The processor stores the event in a first table and, optionally if so configured, a configurable set of fields in a
 * second table as key / value pairs. This allows searching for specific event attributes in the second table. The
 * entries in the second table are "linked" to the first table by a shared identifier, allowing to make joins e.g. when
 * audit events are to be analyzed or exported.
 * <p>
 * This implementation supports storing specific {@link Event} fields in the second table, either under their
 * {@link Event} field name, or through an "alias", which allows storing {@link Event} fields in the second table using
 * an alternative name ("key") to identify the field.
 */
public abstract class AbstractJdbcProcessor
        implements Processor, Reversible {

    /**
     * The logger object for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractJdbcProcessor.class);

    /**
     * A copy of the common properties
     */
    private CommonProperties commonProperties;

    /**
     * A copy of the processor specific properties
     */
    private JdbcProperties properties;


    /**
     * {@inheritDoc}
     */
    @Override
    public final void init(final CommonProperties pCommonProperties) {

        Validate.notNull(pCommonProperties, "The validated object 'pCommonProperties' is null");

        this.commonProperties = new CommonProperties(pCommonProperties);
        this.properties = MapBasedJdbcPropsBuilder.build(pCommonProperties.getAdditionalProperties());
    }

    /**
     * This method processes an event in the default audit stream.
     * <p>
     * See {@link org.beiter.michael.eaudit4j.common.Audit#audit(Event)}.
     * See {@link AbstractJdbcProcessor#process(Event, String, ProcessingObjects)}.
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
     * See {@link AbstractJdbcProcessor#process(Event, String, ProcessingObjects)}.
     *
     * @param event           The event to audit
     * @param auditStreamName The audit stream to send events to
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

        // this implementation does not use processing objects, but validate anyway to ensure library API compliance
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

        // create a key / value map of the fields that go to the index table
        final Map<String, Field> indexedFields = getIndexedFields(event, properties);

        // serializing the event to JSON will take bit, which is why we do it outside of the transaction
        final String eventJson = String.valueOf(event.toJson(properties.getStringEncoding()));

        // persist the event
        persistEvent(auditStreamName, processingObjects, eventId, indexedFields, eventJson);

        // return the event unchanged
        return event;
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
     * Retrieve a database connection, for instance (and depending on the implementation of this method), from:
     * <ul>
     * <li>a connection pool spec (as provided in this class' additional properties)</li>
     * <li>a JNDI name (as provided in this class' properties)</li>
     * <li>a data source (as provided in this class' processing objects)</li>
     * </ul>
     *
     * @param pProperties       The processor configuration
     * @param processingObjects The processing objects providing to the class, which may contain a
     *                          {@link javax.sql.DataSource} object
     * @return A database connection
     * @throws FactoryException When no database connection can be retrieved
     */
    protected abstract Connection getConnection(final JdbcProperties pProperties,
                                                final ProcessingObjects processingObjects)
            throws FactoryException;


    /**
     * Create a key / value map of the indexed event fields to be added to the search table as configured
     *
     * @param event       The event to take the fields from
     * @param pProperties The processor configuration
     * @return A key / value map with the fields to add to the search table
     * @throws AuditException When there is an invalid indexed field list configuration
     */
    // CHECKSTYLE:OFF
    // this is flagged in checkstyle with a missing whitespace before '}', which is a bug in checkstyle
    // Need to validate sizes of the indexedField split to decide whether we use a custom name, or the field name. We
    // could do that with a null check, but that seems to be awkward.
    // Suppress warnings about this method being too complex (can't extract a generic subroutine to reduce exec paths).
    @SuppressWarnings({"PMD.AvoidLiteralsInIfCondition", "PMD.CyclomaticComplexity"})
    // CHECKSTYLE:ON
    private Map<String, Field> getIndexedFields(final Event event, final JdbcProperties pProperties)
            throws AuditException {

        final Map<String, Field> map = new ConcurrentHashMap<>();

        // split the configured event name list, and see what we need to add to the map
        if (pProperties.getIndexedFields() != null && !pProperties.getIndexedFields().isEmpty()) {

            // split the list of fields that we need to add to the map
            final String[] indexedFieldNames =
                    pProperties.getIndexedFields().split(pProperties.getIndexedFieldSeparator());

            // Go through the list of configured fields, and check if there is an alias configured.
            // If so, use that alias - otherwise, use the field name directly.
            // Only add the field if it exists in the event (not all events necessarily contain all fields)
            for (final String indexedFieldName : indexedFieldNames) {

                final String[] indexedField = indexedFieldName.split(pProperties.getIndexedFieldNameSeparator());
                final Field field; // the actual field
                final String fieldName; // the name (i.e. the key) we use for the field in the map

                if (indexedField.length == 1) {

                    // we do not have a dedicated key configured, hence use the field name.
                    // first, we check if the field has been set:
                    if (event.containsField(indexedField[0])) {

                        // the field exists for this event, which means we can pull it
                        field = event.getField(indexedField[0]);
                        fieldName = field.getName();
                    } else {

                        // the field does not exist in this event, hence proceed to the next field
                        continue;
                    }
                } else if (indexedField.length == 2) {

                    // we do have a dedicated key configured
                    // first, we check if the field has been set:
                    if (event.containsField(indexedField[0])) {

                        // the field exists for this event, which means we can pull it
                        field = event.getField(indexedField[0]);
                        fieldName = indexedField[1];
                    } else {

                        // the field does not exist in this event, hence proceed to the next field
                        continue;
                    }
                } else {
                    // We have less than 1 and more than 2 field name components.
                    // This should never happen.
                    final String error = "The Event field name / index mapping is invalid. ";
                    LOG.warn(error);
                    throw new AuditException(AuditErrorConditions.CONFIGURATION, error);
                }

                // add the field to the map
                map.put(fieldName, field);
            }
        }

        // return the map
        return map;
    }

    /**
     * Persist an event to the database
     *
     * @param auditStreamName   The name of the audit stream
     * @param processingObjects The provided processing objects
     * @param eventId           The unique event ID of the event
     * @param indexedFields     The fields that should go in the indexed fields table
     * @param eventJson         The serialized event
     * @throws AuditException When The database operation fails
     */
    // CHECKSTYLE:OFF
    // this is flagged in checkstyle with a missing whitespace before '}', which is a bug in checkstyle
    // suppress warnings about the DB connection not being closed - seems as if that checker does not get it
    // suppress warnings about comparing to "1" - changing this seems to be over-engineering...
    // suppress warnings about nested catches and re-throwing of exceptions, with the cause being lost - can't avoid.
    // suppress warnings about this method being too long (not much point in splitting up this one!)
    // suppress warnings about this method being too complex (can't extract a generic subroutine to reduce exec paths)
    @SuppressWarnings({"PMD.CloseResource", "PMD.AvoidLiteralsInIfCondition", "PMD.PreserveStackTrace", "PMD.ExcessiveMethodLength", "PMD.NPathComplexity", "PMD.CyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity"})
    // The SQL statement is retrieved from the configuration, and the admin is trusted
    // Same open data source comment as above - may be a problem with the checker and the code complexity
    // The OBL_UNSATISFIED_OBLIGATION checker (which is marked experimental) does not seem to work properly
    @edu.umd.cs.findbugs.annotations.SuppressWarnings({"SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING", "ODR_OPEN_DATABASE_RESOURCE", "OBL_UNSATISFIED_OBLIGATION"})
    // CHECKSTYLE:ON
    private void persistEvent(final String auditStreamName, final ProcessingObjects processingObjects,
                              final String eventId, final Map<String, Field> indexedFields,
                              final String eventJson)
            throws AuditException {

        // get a database connection
        final Connection con;
        try {
            con = getConnection(properties, processingObjects);
        } catch (FactoryException e) {
            final String error = "Cannot retrieve database connection";
            LOG.warn(error, e);
            throw new AuditException(AuditErrorConditions.PROCESSING, error, e);
        }

        // get a reasonable fallback for the auto-commit
        boolean autoCommit = MapBasedConnPropsBuilder.build(properties.getAdditionalProperties()).isDefaultAutoCommit();

        // execute the DB transaction
        PreparedStatement psEvent = null;
        PreparedStatement psIndex = null;
        try {

            // we will commit manually
            autoCommit = con.getAutoCommit();
            con.setAutoCommit(false);

            // create prepared statement for the event, and populate it
            psEvent = con.prepareStatement(properties.getInsertEventSqlStmt());
            psEvent.setString(1, eventId);
            psEvent.setString(2, auditStreamName);
            psEvent.setString(3, eventJson);
            final int eventRows = psEvent.executeUpdate();

            // check if the operation has been executed properly:
            if (eventRows != 1) {
                final String error = "Error when persisting the audit event. The operation should have affected '1' "
                        + "index row per field, but a total of '" + eventRows + "' rows was affected";
                LOG.warn(error);
                throw new AuditException(AuditErrorConditions.PROCESSING, error);
            }

            // only add indexed fields if there are indexed fields configured... obviously.
            if (properties.getIndexedFields() != null && !properties.getIndexedFields().isEmpty()) {

                // create a prepared statement for the index, and batch the operations
                psIndex = con.prepareStatement(properties.getInsertIndexedFieldSqlStmt());
                for (final Map.Entry<String, Field> entry : indexedFields.entrySet()) {

                    final String fieldName = entry.getKey();
                    String fieldValue = String.valueOf(entry.getValue().getCharValue(properties.getStringEncoding()));

                    // truncate the field if needed
                    if (fieldValue.length() > properties.getIndexedFieldsMaxLength()) {
                        fieldValue = fieldValue.substring(0, properties.getIndexedFieldsMaxLength());
                    }

                    // convert the field value to lower if needed
                    if (properties.isIndexedFieldsToLower()) {
                        // We use the platform's default locale here. This could be made configurable, but we wait
                        // until someone asks for it. Using an existing object here will help a lot with performance,
                        // and the alternative of using a constant (e.g. Locale.US) is not very appealing either.
                        // This is a pattern used throughout the library. Search for Locale.getDefault() to find all
                        // locations.
                        fieldValue = fieldValue.toLowerCase(Locale.getDefault());
                    }

                    // normalize the searchable String value to NFC form for deterministic search
                    fieldValue = Normalizer.normalize(fieldValue, Normalizer.Form.NFC);

                    psIndex.setString(1, eventId);
                    psIndex.setString(2, auditStreamName);
                    psIndex.setString(3, fieldName);
                    psIndex.setString(4, fieldValue);

                    psIndex.addBatch();
                }
                final int[] indexRows = psIndex.executeBatch();

                // check if the operation has been executed properly:
                // We do not know the number of field operations for sure (this varies with the number of
                // fields-to-be-persisted present in the actual event), but we can check the execution status of each
                // field. Not much use without knowing which field, but it is better than nothing and we probably do
                // not want to over-engineer the error processing here.
                for (final int indexRow : indexRows) {
                    if (indexRow != 1) {
                        final String error = "Error when persisting the audit event. The operation should have "
                                + "affected '1' index row per field, but a total of '" + indexRow
                                + "' rows was affected";
                        LOG.warn(error);
                        throw new AuditException(AuditErrorConditions.PROCESSING, error);
                    }
                }
            }

            // commit
            con.commit();

            // Reset the auto-commit and close the resources.
            // We add this code twice, here and in the finalizer. This is because we never know when the finalizer
            // will actually be executed, and closing the connection here immediately in the non-error case prevents
            // resource leaks. Repeating the same code below in the finalizer is just a safety net.
            con.setAutoCommit(autoCommit);
            psEvent.close();
            if (psIndex != null) {
                psIndex.close();
            }
            if (!con.isClosed()) {
                con.close();
            }
        } catch (SQLException e) {

            final String error = "Unrecoverable error when executing the SQL transaction";
            LOG.warn(error, e);

            try {

                // try to roll back
                con.rollback();

            } catch (SQLException e1) {

                final String error1 = "Unrecoverable error when executing the SQL transaction. "
                        + "Also encountered an unrecoverable error when trying to roll back the SQL transaction. "
                        + "The transaction has not been executed, has not been closed, and has not been rolled back!";
                LOG.warn(error, e1);
                throw new AuditException(AuditErrorConditions.PROCESSING, error1, e1);
            }

            throw new AuditException(AuditErrorConditions.PROCESSING, error, e);

        } finally {

            // reset the auto-commit and close the resources only if they have not already been closed in the happy path
            try {
                if (!con.isClosed() && con.getAutoCommit() != autoCommit) {
                    con.setAutoCommit(autoCommit);
                }
                if (psEvent != null) {
                    psEvent.close();
                }
                if (psIndex != null) {
                    psIndex.close();
                }
                if (!con.isClosed()) {
                    con.close();
                }
            } catch (SQLException e) {
                final String error = "Cannot close the database connection";
                LOG.warn(error, e);
            }
        }
    }
}
