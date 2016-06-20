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

import org.beiter.michael.eaudit4j.common.propsbuilder.MapBasedCommonPropsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

public class AuditFactoryTest {

    /**
     * The logger object for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(AuditFactoryTest.class);

    /**
     * The test class to instantiate
     */
    private static final String CLASS_NAME = "org.beiter.michael.eaudit4j.common.AuditDummy";

    /**
     * Reset the factory to allow creating several instances of the underlying implementations.
     */
    @Before
    public void unsetSingletonInFactory() {

        AuditFactory.reset();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Basic Tests
    ///////////////////////////////////////////////////////////////////////////

    /**
     * A non-existing class name (i.e. a class not in the class path) should throw an exception
     */
    @Test(expected = FactoryException.class)
    public void getNonExistingImplementationTest()
            throws FactoryException {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();

        AuditFactory.getInstance("someGarbageName", commonProps);
    }

    /**
     * An invalid class name (i.e. a class of the wrong type) should throw an exception
     */
    @Test(expected = FactoryException.class)
    public void getInvalidImplementationTest()
            throws FactoryException {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();

        AuditFactory.getInstance(String.class.getCanonicalName(), commonProps);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Advanced Tests
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Retrieve a specific implementation of the Audit interface, and assert that the returned implementation equals
     * the requested implementation.
     */
    @Test
    public void getSpecificImplementationTest() {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();

        Audit audit;
        try {
            audit = AuditFactory.getInstance(CLASS_NAME, commonProps);
        } catch (FactoryException e) {
            AssertionError ae = new AssertionError("Instantiation error");
            ae.initCause(e);
            throw ae;
        }

        String error = "The class instantiated by the factory does not match the expected class";
        assertThat(error, AuditDummy.class.getCanonicalName(), is(equalTo(CLASS_NAME)));
        assertThat(error, audit.getClass().getCanonicalName(), is(equalTo(CLASS_NAME)));
    }

    /**
     * Retrieve two instances of a specific implementation of the Audit interface, and asserts that the returned
     * objects are two separate instances.
     */
    @Test
    public void twoInstancesAreDifferentTest() {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();

        Audit audit1, audit2;
        try {
            audit1 = AuditFactory.getInstance(CLASS_NAME, commonProps);
            audit2 = AuditFactory.getInstance(CLASS_NAME, commonProps);
        } catch (FactoryException e) {
            AssertionError ae = new AssertionError("Instantiation error");
            ae.initCause(e);
            throw ae;
        }

        String error = "The factory returns a singleton instead of a new object";
        assertThat(error, audit1, is(not(sameInstance(audit2))));
    }

    /**
     * Retrieve two singleton instances of a specific implementation of the Audit interface, and asserts that the two
     * returned objects are identical (i.e. the factory returns a singleton).
     * <p>
     * Then, a regular (non-singleton) instance is retrieved, which are asserted to be different than the previously
     * retrieved objects.
     * <p>
     * Finally, the factory is reset, and another instance is retrieved. If the factory resets properly, the third
     * instance must be unequal to the first three instances.
     */
    @Test
    public void factoryReturnsSingletonTest() {

        CommonProperties commonProps = MapBasedCommonPropsBuilder.buildDefault();

        // test that two singletons retrieved from the factory are identical
        Audit audit1, audit2;
        try {
            audit1 = AuditFactory.getSingleton(CLASS_NAME, commonProps);
            audit2 = AuditFactory.getSingleton(CLASS_NAME, commonProps);
        } catch (FactoryException e) {
            AssertionError ae = new AssertionError("Instantiation error");
            ae.initCause(e);
            throw ae;
        }

        String error = "The factory does not return a singleton";
        assertThat(error, audit1, is(sameInstance(audit2)));

        // then test that a regular (non-singleton) instance is different
        Audit audit3;
        try {
            audit3 = AuditFactory.getInstance(CLASS_NAME, commonProps);
        } catch (FactoryException e) {
            AssertionError ae = new AssertionError("Instantiation error");
            ae.initCause(e);
            throw ae;
        }
        error = "The factory returns a singleton instead of a new object";
        assertThat(error, audit1, is(not(sameInstance(audit3))));
        assertThat(error, audit2, is(not(sameInstance(audit3))));

        // reset the factory
        AuditFactory.reset();

        // now test that the factory return a new object (i.e. a new singleton)
        Audit audit4;
        try {
            audit4 = AuditFactory.getSingleton(CLASS_NAME, commonProps);
        } catch (FactoryException e) {
            AssertionError ae = new AssertionError("Instantiation error");
            ae.initCause(e);
            throw ae;
        }

        error = "The factory does not return a singleton, or does not reset properly";
        assertThat(error, audit1, is(not(sameInstance(audit4))));
        assertThat(error, audit2, is(not(sameInstance(audit4))));
        assertThat(error, audit3, is(not(sameInstance(audit4))));
    }
}
