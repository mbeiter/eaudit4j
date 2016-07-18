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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A utility class that starts an Cassandra server and populates it with test tables for auditing.
 */
public class CassandraServer {

    private static final Logger LOG = LoggerFactory.getLogger(CassandraServer.class);

    public static final String HOST = "localhost";
    public static final int PORT = 9142;
    public static final String KEY_SPACE = "audit";

    private static AtomicBoolean serverRunning = new AtomicBoolean(false);
    private static AtomicBoolean keyspaceCreated = new AtomicBoolean(false);
    private static volatile Session session;
    private static volatile Session sessionWithKeyspace;

    // prevent instantiation of this class
    private CassandraServer() {
    }

    /**
     * Initializes the default schema and sets default values (if feasible). This deletes any data that are already in
     * the database!
     */
    public static synchronized void init() {

        if (serverRunning.get()) {

            LOG.info("Initializing the Cassandra server with a default schema and default values");

            String[] createKeySpace = new String[]{
                    // "USE " + KEY_SPACE + ";\n", -> rather specify the key space explicitly for the generic session object we use here
                    "DROP TABLE IF EXISTS " + KEY_SPACE + ".events;\n",
                    "CREATE TABLE " + KEY_SPACE + ".events (\n" +
                            "  eventId     ASCII PRIMARY KEY,\n" +
                            "  auditStream ASCII,\n" +
                            "  eventJson   VARCHAR\n" +
                            ");"
            };

            try {
                for (String cql : createKeySpace) {
                    session.execute(cql);
                }
            } catch (Exception e) {
                LOG.error("The Cassandra server cannot be initialized", e.getCause(), e.getStackTrace());
            }
        } else {
            LOG.info("The Cassandra server cannot be initialized because it is not running");
        }

    }

    /**
     * Starts the server, but does not perform any database schema initialization
     *
     * @throws Exception When the server cannot be started
     */
    public static void start()
            throws Exception {

        LOG.info("Checking if the Cassandra server can be started");

        // start the server only if there is not already a server running
        if (serverRunning.compareAndSet(false, true)) {

            try {
                LOG.info("Starting the Cassandra server");
                EmbeddedCassandraServerHelper.startEmbeddedCassandra(20000L);
                Cluster cluster = new Cluster.Builder()
                        .addContactPoint(HOST)
                        .withPort(PORT)
                        .build();

                session = cluster.connect();

                // create the key space
                LOG.info("Creating the Cassandra key space");
                String[] createDb = new String[]{
                        "DROP KEYSPACE IF EXISTS " + KEY_SPACE + ";\n",
                        "CREATE KEYSPACE " + KEY_SPACE + " WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor' : 1};\n",
                };

                try {
                    for (String cql : createDb) {
                        session.execute(cql);
                    }
                } catch (Exception e) {
                    LOG.error("The keyspace cannot be created on the Cassandra server", e.getCause(), e.getStackTrace());
                }

                sessionWithKeyspace = cluster.connect(KEY_SPACE);
                keyspaceCreated.set(true);

                LOG.info("Cassandra server started");
            } catch (IOException | TTransportException | InterruptedException e) {
                serverRunning.set(false);
                LOG.warn("Cassandra server not started due to error");
                throw new Exception(e);
            }
        } else {
            LOG.info("Cassandra server already running");
        }
    }

    /**
     * Stops the server
     */
    public static void stop() {

        LOG.info("Checking if the Cassandra server can be stopped");

        // stop the server only if there is a server running
        if (serverRunning.compareAndSet(true, false)) {

            LOG.info("Stopping the Cassandra server");

            // clear all data from the cluster
            EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();

            // close the cluster and all sessions, and shutdown the embedded server
            session.getCluster().close();

            LOG.info("Cassandra server stopped");
        } else
            LOG.info("Cassandra server not running");
    }


    /**
     * Restarts the server, but does not perform any database schema initialization
     *
     * @throws Exception When the server cannot be started
     */
    public static void restart()
            throws Exception {

        stop();
        start();
    }

    /**
     * Returns a session on the Cassandra cluster if the cluster is up and running (not closed).
     * <p>
     * The session is not bound to a specific keyspace. Use the "getSessionWithKeyspace()" method to get a keyspaced
     * session that is specific to the keyspace used in these unit tests.
     * <p>
     * See http://www.datastax.com/dev/blog/4-simple-rules-when-using-the-datastax-drivers-for-cassandra
     *
     * @return A session on the cluster
     * @throws IllegalStateException when the cluster is not running
     */
    public static Session getSession() {

        if (serverRunning.get() && !session.isClosed()) {
            return session;
        } else {
            if (session.isClosed()) {
                throw new IllegalStateException("Session is closed");
            } else {
                throw new IllegalStateException("Cluster not running");
            }
        }
    }

    /**
     * Returns a keyspaced session on the Cassandra cluster if the cluster is up and running (not closed) AND the
     * keyspace has been created / initialized.
     * <p>
     * The session is bound to the keyspace used in these unit tests.
     * <p>
     * See http://www.datastax.com/dev/blog/4-simple-rules-when-using-the-datastax-drivers-for-cassandra
     *
     * @return A session on the cluster
     * @throws IllegalStateException when the cluster is not running
     */
    public static Session getSessionWithKeyspace() {

        if (serverRunning.get() && keyspaceCreated.get() && !sessionWithKeyspace.isClosed()) {
            return sessionWithKeyspace;
        } else {
            if (sessionWithKeyspace.isClosed()) {
                throw new IllegalStateException("Session is closed");
            } else {
                throw new IllegalStateException("Cluster not running or keyspace has not been created");
            }
        }
    }
}
