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

import org.beiter.michael.eaudit4j.common.ProcessingObjects;
import org.beiter.michael.db.ConnectionFactory;
import org.beiter.michael.db.ConnectionProperties;
import org.beiter.michael.db.FactoryException;
import org.beiter.michael.db.propsbuilder.MapBasedConnPropsBuilder;

import java.sql.Connection;

/**
 * This processors persists events to a JDBC database using a configurable connection pool.
 * <p>
 * The processor stores the event in a first table and, optionally if so configured, a configurable set of fields in a
 * second table as key / value pairs. This allows searching for specific event attributes in the second table. The
 * entries in the second table are "linked" to the first table by a shared identifier, allowing to make joins e.g. when
 * audit events are to be analyzed or exported.
 * <p>
 * This implementation supports storing specific {@link org.beiter.michael.eaudit4j.common.Event} fields in the second
 * table, either under their {@link org.beiter.michael.eaudit4j.common.Event} field name, or through an "alias", which
 * allows storing {@link org.beiter.michael.eaudit4j.common.Event} fields in the second table using an alternative name
 * ("key") to identify the field.
 */
public class JdbcPoolProcessor
        extends AbstractJdbcProcessor {

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

        // create the connection properties from the Additional Properties map
        final ConnectionProperties connProps = MapBasedConnPropsBuilder.build(pProperties.getAdditionalProperties());

        return ConnectionFactory.getConnection(connProps);
    }
}
