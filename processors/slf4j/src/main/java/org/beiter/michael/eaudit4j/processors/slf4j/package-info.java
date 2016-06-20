/**
 * Provides a processor that writes audit events to a slf4j logger.
 * The default database appender implementation in slf4j does not provide very good database write performance.
 * Consider an alternative processor when planning to persist events to a database in production environments.
 */
package org.beiter.michael.eaudit4j.processors.slf4j;
