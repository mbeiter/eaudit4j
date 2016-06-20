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

import org.beiter.michael.db.FactoryException;
import org.beiter.michael.eaudit4j.common.ProcessingObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This processors persists events to a JDBC database using a connection obtained from a database connection pool that
 * is managed outside of this library. An integrating application needs to provide a reference to the connection pool
 * through the {@link ProcessingObjects} in the
 * {@link AbstractJdbcProcessor#process(org.beiter.michael.eaudit4j.common.Event, String, ProcessingObjects)} method.
 * <p>
 * See {@link JdbcPoolProcessor} for more information.
 */
public class JdbcDsProcessor
        extends AbstractJdbcProcessor {

    /**
     * The logger object for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(JdbcDsProcessor.class);

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
     * Retrieve a database connection from a connection pool spec (as provided in this class' additional properties)
     * <p>
     * See {@link AbstractJdbcProcessor#getConnection(JdbcProperties, ProcessingObjects)}.
     * <p>
     * {@inheritDoc}
     */
    protected final Connection getConnection(final JdbcProperties pProperties,
                                             final ProcessingObjects processingObjects)
            throws FactoryException {

        final String dsName = pProperties.getDataSourceName();

        // get the data source from the processing objects, and throw an exception if the data source is not present
        // or is of the wrong class type
        final DataSource dataSource;
        if (processingObjects.contains(dsName)) {

            final Object dsObject = processingObjects.get(dsName); // compiler will optimize this
            if (dsObject instanceof DataSource) {

                dataSource = (DataSource) dsObject;
            } else {

                final String error = "The object provided in the 'ProcessingObjects' referenced by the configured "
                        + " data source name ('" + dsName + "') is not an instance of '"
                        + DataSource.class.getCanonicalName() + "'";
                LOG.warn(error);
                throw new FactoryException(error);
            }
        } else {

            final String error = "The configured data source name ('" + dsName
                    + "') does not exist in the 'ProcessingObjects'";
            LOG.warn(error);
            throw new FactoryException(error);
        }

        // try to get a connection from the data source
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            final String error = "Cannot retrieve a connection from the data source provided in 'ProcessingObjects'";
            LOG.warn(error);
            throw new FactoryException(error, e);
        }
    }
}
