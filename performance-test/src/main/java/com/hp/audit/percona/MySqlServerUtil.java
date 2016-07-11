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
package com.hp.audit.percona;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static com.hp.audit.PropertiesUtil.getBooleanProperty;
import static com.hp.audit.PropertiesUtil.getIntProperty;

public class MySqlServerUtil {
    public static final String DRIVER_CLASS = "DriverClass";
    public static final String JDBC_URL = "jdbcUrl";
    public static final String USER = "user";
    public static final String PASSWORD = "password";
    public static final String MIN_POOL_SIZE = "minPoolSize";
    public static final String ACQUIRE_INCREMENT = "acquireIncrement";
    public static final String MAX_POOL_SIZE = "maxPoolSize";
    public static final String MAX_STATEMENTS = "maxStatements";
    private static final String RECREATE_TABLES = "recreateTables";
    private static boolean recreateTables = getBooleanProperty(RECREATE_TABLES, true);
    private static ComboPooledDataSource connPool;

    public static final String[] CREATE_DB = new String[]{
            "CREATE TABLE IF NOT EXISTS fields (\n" +
                "  id              INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,\n" +
                "  eventId         VARCHAR(36)  NOT NULL,\n" +
                "  auditStreamName VARCHAR(32)  NOT NULL,\n" +
                "  fieldName       VARCHAR(128) NOT NULL,\n" +
                "  fieldValue      VARCHAR(255) NOT NULL\n" +
                ");\n",
            "CREATE TABLE IF NOT EXISTS events ("
                    + " id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,"
                    + " eventId VARCHAR(36) NOT NULL UNIQUE,"
                    + " auditStreamName VARCHAR(32) NOT NULL,"
                    + " eventJson TEXT NOT NULL"
                    + ")",
            "CREATE INDEX idx_fields_eventId ON fields (eventId);\n",
            "CREATE INDEX idx_fields_auditStreamName ON fields (auditStreamName);\n",
            "CREATE INDEX idx_fields_fieldName ON fields (fieldName);\n",
            "CREATE INDEX idx_fields_fieldValue ON fields (fieldValue);\n",
            "CREATE UNIQUE INDEX uk_fields_eventId_fieldName ON fields (eventId, fieldName);"
    };
    public static void startConnectionPool() throws Exception {
        connPool = new ComboPooledDataSource();
        connPool.setDriverClass(System.getProperty(DRIVER_CLASS, "com.mysql.jdbc.Driver"));
        connPool.setJdbcUrl(System.getProperty(JDBC_URL, "jdbc:mysql://192.168.99.100:3306/test"));
        connPool.setUser(System.getProperty(USER, "root"));
        connPool.setPassword(System.getProperty(PASSWORD, "rootPsw"));

        // the settings below are optional -- c3p0 can work with defaults
        connPool.setMinPoolSize(getIntProperty(MIN_POOL_SIZE, 5));
        connPool.setAcquireIncrement(getIntProperty(ACQUIRE_INCREMENT, 10));
        connPool.setMaxPoolSize(getIntProperty(MAX_POOL_SIZE, 140));
        connPool.setMaxStatements(getIntProperty(MAX_STATEMENTS, 500));
    }

    public static void closePool() {
        connPool.close();
    }

    public static void recreateTables() throws Exception {
        if(recreateTables) {
            Connection connection = null;
            try {
                connection = getConnection();
                Statement statement = connection.createStatement();
                for (String sql : CREATE_DB) {
                    statement.execute(sql);
                }
            } finally {
                if (connection != null) {
                    connection.close();
                }
            }
        }
    }

    public static java.sql.Connection getConnection() throws SQLException {
        return connPool.getConnection();
    }

    public static ComboPooledDataSource getDataSource() throws SQLException {
        return connPool;
    }
}
