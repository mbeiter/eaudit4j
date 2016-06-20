/*
 * #%L
 * This file is part of eAudit4j, a library for creating pluggable auditing solutions.
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
package org.beiter.michael.eaudit4j.common;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * A factory to create instances of objects that implement the {@link Processor} interface.
 */
public final class ProcessorFactory {

    /**
     * The logger object for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProcessorFactory.class);

    /**
     * The singleton instance of the audit processor
     */
    private static volatile Processor processorInstance;

    /**
     * A private constructor to prevent instantiation of this class
     */
    private ProcessorFactory() {
    }

    /**
     * Return a new, fully initialized instance of a {@link Processor} class to use for event processing.
     * <p>
     * Classes implementing the {@link Processor} interface <b>must</b> be thread safe.
     *
     * @param className  The name of a class that implements the {@link Processor} interface
     * @param properties The properties to initialize the instance with
     * @return An instance of a class implementing the {@link Processor} interface
     * @throws FactoryException When the class cannot be instantiated
     * @throws NullPointerException     When the {@code className} or {@code properties} are {@code null}
     * @throws IllegalArgumentException When {@code className} is empty
     */
    public static Processor getInstance(final String className, final CommonProperties properties)
            throws FactoryException {

        Validate.notBlank(className, "The validated character sequence 'className' is null or empty");
        Validate.notNull(properties, "The validated object 'properties' is null");

        final Class<? extends Processor> processorsClazz;
        try {
            processorsClazz = Class.forName(className).asSubclass(Processor.class);
        } catch (ClassNotFoundException e) {
            final String error = "Class not found: " + className;
            LOG.warn(error);
            throw new FactoryException(error, e);
        } catch (ClassCastException e) {
            final String error = "The provided registry factory class name ('" + className
                    + "') is not a subclass of '" + Processor.class.getCanonicalName() + "'";
            LOG.warn(error);
            throw new FactoryException(error, e);
        }

        final Processor processor;
        try {
            final Constructor<? extends Processor> constructor = processorsClazz.getDeclaredConstructor();
            if (!constructor.isAccessible()) {
                final String error = "Constructor of class '" + processorsClazz.getCanonicalName()
                        + "' is not accessible, changing the accessible flag to instantiate the class";
                LOG.info(error);
                constructor.setAccessible(true);
            }
            processor = constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException | IllegalArgumentException e) {
            final String error = "Cannot instantiate class '" + processorsClazz.getCanonicalName() + "'";
            LOG.warn(error, e);
            throw new FactoryException(error, e);
        }

        processor.init(properties);

        return processor;
    }

    /**
     * Return a singleton, fully initialized instance of a {@link Processor} class to use for event processing.
     * <p>
     * Retrieving a singleton by this method will cause the factory to keep state, and store a reference to the
     * singleton for later use. You may reset the factory state using the {@code reset()} method to retrieve a new
     * / different singleton the next time this method is called..
     * <p>
     * Note that any properties of the singleton (e.g. configuration) cannot necessarily be changed easily. You may call
     * the singleton's {@code init()} method, but depending on the implementation provided by the respective class, this
     * may or may not have the expected effect.
     * <p>
     * If you need tight control over the singleton, including its lifecycle and configuration, or you require more than
     * one singleton that are different in their internal state (e.g. with different configurations), then you should
     * create such objects with the {@code getInstance()} method and maintain their state as "singletons" in your
     * application's business logic.
     * <p>
     * Classes implementing the {@link Processor} interface <b>must</b> be thread safe.
     *
     * @param className  The name of a class that implements the {@link Processor} interface
     * @param properties The properties to initialize the instance with
     * @return An instance of a class implementing the {@link Processor} interface
     * @throws FactoryException When the class cannot be instantiated
     * @throws NullPointerException     When the {@code className} or {@code properties} are {@code null}
     * @throws IllegalArgumentException When {@code className} is empty
     */
    @SuppressWarnings("PMD.NonThreadSafeSingleton")
    public static Processor getSingleton(final String className, final CommonProperties properties)
            throws FactoryException {

        Validate.notBlank(className, "The validated character sequence 'className' is null or empty");
        Validate.notNull(properties, "The validated object 'properties' is null");

        // The double-check idiom is safe and acceptable here (Bloch, 2nd ed. p 284)
        if (processorInstance == null) {
            synchronized (ProcessorFactory.class) {
                if (processorInstance == null) {

                    processorInstance = getInstance(className, properties);
                }
            }
        }

        return processorInstance;
    }

    /**
     * Resets the internal state of the factory, which causes the
     * {@link ProcessorFactory#getSingleton(String, CommonProperties)} method to return a new {@link Processor}
     * instance the next time it is called.
     */
    // CHECKSTYLE:OFF
    // this is flagged in checkstyle with a missing whitespace before '}', which is a bug in checkstyle
    @SuppressWarnings({"PMD.NonThreadSafeSingleton", "PMD.NullAssignment"})
    // CHECKSTYLE:ON
    public static void reset() {

        // Unset the instance singleton that has been created earlier
        // The double-check idiom is safe and acceptable here (Bloch, 2nd ed. p 284)
        // null-assignments for de-referencing objects are okay
        if (processorInstance != null) {
            synchronized (ProcessorFactory.class) {
                if (processorInstance != null) {
                    processorInstance = null;
                }
            }
        }
    }
}
