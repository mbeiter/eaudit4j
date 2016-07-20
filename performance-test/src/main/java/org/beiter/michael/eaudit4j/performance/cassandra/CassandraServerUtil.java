/*
 * #%L
 * This file is part of eAudit4j, a library for creating pluggable
 * auditing solutions, providing an application to execute performance tests agains supported databases.
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
package org.beiter.michael.eaudit4j.performance.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.Session;

import static org.beiter.michael.eaudit4j.performance.PropertiesUtil.getIntProperty;

public class CassandraServerUtil {
    private static final String CONTACT_POINTS = "contactPoints";
    private static final String PORT = "port";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private static String contactPoints = System.getProperty(CONTACT_POINTS, "192.168.99.100");
    private static int port = getIntProperty(PORT, 9042);
    private static String username = System.getProperty(USERNAME, "cassandra");
    private static String password = System.getProperty(PASSWORD, "cassandra");

    private static Cluster cluster;

    public static String[] CREATE_DB = new String[]{
            "CREATE KEYSPACE IF NOT EXISTS audit WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor' : 1};\n",
            "CREATE TABLE IF NOT EXISTS audit.events (\n" +
                "  eventId     VARCHAR PRIMARY KEY,\n" +
                "  auditStream VARCHAR,\n" +
                "  eventJson   VARCHAR\n" +
                ") WITH COMPACTION = {'class':  'LeveledCompactionStrategy'} " +
                    "AND COMPRESSION = {'sstable_compression':  'LZ4Compressor'};\n"
    };

    public static void startConnection() throws Exception {
        PoolingOptions options = new PoolingOptions();
        options.setCoreConnectionsPerHost(HostDistance.REMOTE, 2)
                .setMaxConnectionsPerHost(HostDistance.REMOTE, 4)
                .setCoreConnectionsPerHost(HostDistance.LOCAL, 4)
                .setMaxConnectionsPerHost(HostDistance.LOCAL, 10)
                .setMaxRequestsPerConnection(HostDistance.LOCAL, 32768)
                .setMaxRequestsPerConnection(HostDistance.REMOTE, 2000);

        Cluster.Builder builder = cluster.builder();

        String[] points = contactPoints.split(",");
        for(String contactPoint : points) {
            builder.addContactPoint(contactPoint);
        }

        cluster  = builder
                .withPort(port)
                .withCredentials(username, password)
                .withPoolingOptions(options)
                .withProtocolVersion(ProtocolVersion.V3)
                .build();
    }

    public static void recreateTables() throws Exception {
        Session session = getSession();
        try {
            for (String sql : CREATE_DB) {
                session.execute(sql);
            }
        } finally {
            if (!session.isClosed()) {
                session.close();
            }
        }
    }

    private static Session getSession() {
        return cluster.connect();
    }

    public static Cluster getCluster() {
        return cluster;
    }

    public static void close() {
        cluster.close();
    }

}
