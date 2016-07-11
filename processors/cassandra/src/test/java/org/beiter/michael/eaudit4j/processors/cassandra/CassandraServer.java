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

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;

import java.io.IOException;

public class CassandraServer {

    private static Cluster cluster;

    public static void startCassandra() throws IOException, TTransportException, InterruptedException {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra(20000L);
        cluster = new Cluster.Builder()
                .addContactPoint("localhost")
                .withPort(9142)
                .build();
        Session session = cluster.connect();

        String[] createDb = new String[]{
                "DROP KEYSPACE IF EXISTS audit;\n",
                "CREATE KEYSPACE audit_test WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor' : 1};\n",
                "CREATE TABLE audit_test.events (\n" +
                        "  eventId     VARCHAR PRIMARY KEY,\n" +
                        "  auditStream VARCHAR,\n" +
                        "  eventJson   VARCHAR\n" +
                        ");"
        };

        try {
            for (String sql : createDb) {
                session.execute(sql);
            }
        } finally {
            if (!session.isClosed()) {
                session.close();
            }
        }
    }

    public static Cluster getCluster() {
        return cluster;
    }

    public static void cleanUp() throws InterruptedException {
        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
        Thread.sleep(2000L);
    }
}
