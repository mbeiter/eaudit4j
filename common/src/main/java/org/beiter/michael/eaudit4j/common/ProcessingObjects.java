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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class stores a set of processing objects, that is, objects that can be passed to a processor for use during
 * event processing.
 * <p>
 * Examples for such object include, for instance, a data source object that can be used by a processor to persist
 * data to a database.
 */
// suppress warnings about the constructor (required for producing java docs)
@SuppressWarnings("PMD.UnnecessaryConstructor")
public class ProcessingObjects {

    /**
     * The logger object for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProcessingObjects.class);

    /**
     * A map that stores objects, identifying objects with a name.
     */
    private final Map<String, Object> objects = new ConcurrentHashMap<>();

    /**
     * Constructs an empty set of processing objects.
     * <p>
     * Use the setter and getter to store objects and retrieve them.
     */
    public ProcessingObjects() {

        // no code here, constructor just for java docs
    }

    /**
     * Add an object to this structure.
     * <p>
     * Note that this method does <strong>not</strong> create a defensive copy of the provided {@code Object}).
     *
     * @param name  The name of the object to add
     * @param value The object to add
     * @return The name of the object that was added to the event
     * @throws NullPointerException     When {@code name} or {@code value} is {@code null}
     * @throws IllegalArgumentException When {@code name} is empty
     */
    public final String add(final String name, final Object value) {

        Validate.notBlank(name, "The validated character sequence 'name' is null or empty");
        Validate.notNull(value, "The validated object 'object' is null");

        objects.put(name, value);

        return name;
    }

    /**
     * Checks if this structure contains an object with the specified name.
     *
     * @param name The object name to check for
     * @return {@code true} if the this structure contains the object, {@code false} otherwise
     */
    public final boolean contains(final String name) {

        Validate.notBlank(name, "The validated character sequence 'name' is null or empty");

        return objects.containsKey(name);
    }

    /**
     * Remove an object from this struecture.
     *
     * @param name The name of the object to remove
     * @return {@code true} if the object could be removed, {@code false} otherwise
     * @throws NullPointerException     When {@code name} is {@code null}
     * @throws IllegalArgumentException When {@code name} is empty
     */
    public final boolean remove(final String name) {

        Validate.notBlank(name, "The validated character sequence 'name' is null or empty");

        // return "true" if the field was found in the map, "false" otherwise
        return objects.remove(name) != null;
    }

    /**
     * Retrieve an object from this structure.
     * <p>
     * Note that this method returns original objects, and does not make defensive copies.
     *
     * @param name The name of the object to retrieve.
     * @return The object
     * @throws NullPointerException     When {@code name} is {@code null}
     * @throws IllegalArgumentException When {@code name} is empty
     * @throws NoSuchElementException   When the object is not part of this structure
     */
    public final Object get(final String name) {

        Validate.notBlank(name, "The validated character sequence 'name' is null or empty");

        if (!objects.containsKey(name)) {
            final String error = "The object " + name + " is not part of this structure.";
            LOG.info(error);
            throw new NoSuchElementException(error);
        }

        // return the original (not a defensive copy!) of the requested object
        return objects.get(name);
    }

    /**
     * Retrieve a list of all objects (i.e. a list of objects names) currently included in this structure.
     * <p>
     * Note that the returned implementation of {@link List} may or may not be thread safe. If multiple threads access
     * the returned list concurrently, and at least one of the threads modifies the list structurally, it should be
     * synchronized externally.
     * <p>
     * Note that the returned implementation of {@link List} may be an unmodifiable List (e.g. wrapped into
     * {@code Collections.unmodifiableList()}.
     *
     * @return A list of field names
     */
    public final List<String> getObjectNames() {

        // Use an array list instead of a vector - much fast due to lack of synchronization,
        // and the caller must cope with the implications of the List interface anyway...
        final List<String> objectNames = new ArrayList<>(objects.keySet().size());

        // copy the object names into a new structure, we do not want to
        // return the original enumeration that backs the internal map!
        for (final String fieldName : objects.keySet()) {
            objectNames.add(fieldName);
        }

        // wrap the result to help the caller avoid accidental modifications...
        return Collections.unmodifiableList(objectNames);
    }
}
