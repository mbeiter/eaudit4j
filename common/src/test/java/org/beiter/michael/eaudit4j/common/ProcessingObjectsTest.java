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

import org.junit.Before;
import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ProcessingObjectsTest {

    private java.lang.reflect.Field field_objects;

    /**
     * Make some of the private fields in the ProcessingObjects class accessible.
     * <p>
     * This is executed before every test to ensure consistency even if one of the tests mock with field accessibility.
     */
    @Before
    public void makeObjectsPrivateFieldsAccessible() {

        // make private fields accessible as needed
        try {
            field_objects = ProcessingObjects.class.getDeclaredField("objects");
        } catch (NoSuchFieldException e) {
            AssertionError ae = new AssertionError("An expected private field does not exist");
            ae.initCause(e);
            throw ae;
        }
        field_objects.setAccessible(true);
    }

    /**
     * Test that we can add an object, and that the object instance is stored as provided
     *
     * @throws IllegalAccessException when reflection does not work
     */
    @Test
    public void addObjectTest()
            throws IllegalAccessException {

        String key = "objectName";
        String value = "objectValue";

        ProcessingObjects processingObjects = new ProcessingObjects();

        // Use reflection to get access to the internal fields
        ConcurrentHashMap<String, Object> objectsInObject = (ConcurrentHashMap<String, Object>) field_objects.get(processingObjects);

        // add the object to the processingObjects
        processingObjects.add(key, value);

        // assert that the two objects are identical
        String error = "The method does not store the same object instance as provided";
        assertThat(error, (String) objectsInObject.get(key), is(sameInstance(value)));
    }

    /**
     * Test adding a null key
     */
    @Test(expected = NullPointerException.class)
    public void addNullKey() {

        String key = null;
        String value = "objectValue";

        ProcessingObjects processingObjects = new ProcessingObjects();

        processingObjects.add(key, value);
    }

    /**
     * Test adding a blank key
     */
    @Test(expected = IllegalArgumentException.class)
    public void addBlankKey() {

        String key = "";
        String value = "objectValue";

        ProcessingObjects processingObjects = new ProcessingObjects();

        processingObjects.add(key, value);
    }

    /**
     * Test adding a null value
     */
    @Test(expected = NullPointerException.class)
    public void addNullValue() {

        String key = "objectKey";
        String value = null;

        ProcessingObjects processingObjects = new ProcessingObjects();

        processingObjects.add(key, value);
    }

    /**
     * Test that we can retrieve an object, and that the object reference returned is the one that was originally
     * provided
     */
    @Test
    public void retrieveObjectTest() {

        String key = "objectName";
        String value = "objectValue";

        ProcessingObjects processingObjects = new ProcessingObjects();

        // add the object to the processingObjects
        processingObjects.add(key, value);

        // retrieve the object from the processingObjects
        String retrieved = (String) processingObjects.get(key);

        // assert that the two objects are identical
        String error = "The method does not store the same object instance as provided";
        assertThat(error, retrieved, is(sameInstance(value)));
    }

    /**
     * Test getting a null key
     */
    @Test(expected = NullPointerException.class)
    public void getNullKey() {

        String key = "objectKey";
        String value = "objectValue";

        ProcessingObjects processingObjects = new ProcessingObjects();

        processingObjects.add(key, value);

        processingObjects.get(null);
    }

    /**
     * Test getting a blank key
     */
    @Test(expected = IllegalArgumentException.class)
    public void getBlankKey() {

        String key = "objectKey";
        String value = "objectValue";

        ProcessingObjects processingObjects = new ProcessingObjects();

        processingObjects.add(key, value);

        processingObjects.get("");
    }

    /**
     * Test getting a non-existing key
     */
    @Test(expected = NoSuchElementException.class)
    public void getNonExistingValue() {

        String key = "objectKey";
        String value = "objectValue";

        ProcessingObjects processingObjects = new ProcessingObjects();

        processingObjects.add(key, value);

        processingObjects.get(key + "_invalid");
    }

    /**
     * Test that:
     * <ul>
     * <li>An object name that was not yet added to the structure is correctly reported as not present</li>
     * <li>An object name that was added to the structure is correctly reported as present</li>
     * </ul>
     */
    @Test
    public void containsObjectTest() {

        String key = "objectName";
        String value = "objectValue";

        ProcessingObjects processingObjects = new ProcessingObjects();

        // assert that the object is reported as not included
        String error = "The method reports a non-present object as present";
        assertThat(error, processingObjects.contains(key), is(false));

        // add the object to the processingObjects
        processingObjects.add(key, value);

        // assert that the object is reported as not included
        error = "The method reports a present object as non-present";
        assertThat(error, processingObjects.contains(key), is(true));
    }

    /**
     * Test getting state of a null key
     */
    @Test(expected = NullPointerException.class)
    public void containsNullKey() {

        String key = "objectKey";
        String value = "objectValue";

        ProcessingObjects processingObjects = new ProcessingObjects();

        processingObjects.add(key, value);

        processingObjects.contains(null);
    }

    /**
     * Test getting state of a blank key
     */
    @Test(expected = IllegalArgumentException.class)
    public void containsBlankKey() {

        String key = "objectKey";
        String value = "objectValue";

        ProcessingObjects processingObjects = new ProcessingObjects();

        processingObjects.add(key, value);

        processingObjects.contains("");
    }

    /**
     * Test that we can remove an object, and that the object is reported as "not present" once it has been removed
     *
     * @throws IllegalAccessException when reflection does not work
     */
    @Test
    public void removeObjectTest()
            throws IllegalAccessException {

        String key = "objectName";
        String value = "objectValue";

        ProcessingObjects processingObjects = new ProcessingObjects();

        // Use reflection to get access to the internal fields
        ConcurrentHashMap<String, Object> objectsInObject = (ConcurrentHashMap<String, Object>) field_objects.get(processingObjects);

        // assert that the object is reported as not included
        String error = "The method reports a non-present object as present";
        assertThat(error, processingObjects.contains(key), is(false));

        // add the object to the processingObjects
        processingObjects.add(key, value);

        // assert that the object is in the internal map
        error = "The method does not add the object";
        assertThat(error, objectsInObject.containsKey(key), is(true));

        // assert that the object is reported as not included
        error = "The method reports a present object as non-present";
        assertThat(error, processingObjects.contains(key), is(true));

        // remove the object to the processingObjects
        processingObjects.remove(key);

        // assert that the object is no longer in the internal map
        error = "The method does not remove the object";
        assertThat(error, objectsInObject.contains(key), is(false));

        // assert that the object is reported as not included
        error = "The method reports a non-present object as present";
        assertThat(error, processingObjects.contains(key), is(false));
    }

    /**
     * Test that removing an existing object returns "true"
     */
    @Test
    public void removeExistingKey() {

        String key = "objectKey";
        String value = "objectValue";

        ProcessingObjects processingObjects = new ProcessingObjects();

        processingObjects.add(key, value);

        String error = "The method does not return 'true' when removing an existing object";
        assertThat(error, processingObjects.remove(key), is(true));
    }

    /**
     * Test that removing a non-existing object returns "false"
     */
    @Test
    public void removeNonExistingKey() {

        String key = "objectKey";
        String value = "objectValue";

        ProcessingObjects processingObjects = new ProcessingObjects();

        processingObjects.add(key, value);

        String error = "The method does not return 'false' when removing a non-existing object";
        assertThat(error, processingObjects.remove(key + "_invalid"), is(false));
    }

    /**
     * Test removing a null key
     */
    @Test(expected = NullPointerException.class)
    public void removeNullKey() {

        String key = "objectKey";
        String value = "objectValue";

        ProcessingObjects processingObjects = new ProcessingObjects();

        processingObjects.add(key, value);

        processingObjects.remove(null);
    }

    /**
     * Test removing a blank key
     */
    @Test(expected = IllegalArgumentException.class)
    public void removeBlankKey() {

        String key = "objectKey";
        String value = "objectValue";

        ProcessingObjects processingObjects = new ProcessingObjects();

        processingObjects.add(key, value);

        processingObjects.remove("");
    }


    /**
     * Test that we can retrieve a list of object names, and that the list contains all objects and only all objects
     * that have previously been added to the structure
     */
    @Test
    public void removeFieldFromAuditEventTest() {

        String key1 = "objectKey1";
        String value1 = "objectValue1";

        String key2 = "objectKey2";
        String value2 = "objectValue2";

        ProcessingObjects processingObjects = new ProcessingObjects();

        processingObjects.add(key1, value1);
        processingObjects.add(key2, value2);

        String error = "The number of fields in the structure is not correct";
        assertThat(error, processingObjects.getObjectNames().size(), is(equalTo(2)));
        error = "An expected field is missing in the structure";
        assertThat(error, processingObjects.getObjectNames(), containsInAnyOrder(key1, key2));

        // remove an object
        processingObjects.remove(key1);

        error = "The number of fields in the structure is not correct";
        assertThat(error, processingObjects.getObjectNames().size(), is(equalTo(1)));
        error = "An expected field is missing in the structure";
        assertThat(error, processingObjects.getObjectNames(), containsInAnyOrder(key2));
    }
}
