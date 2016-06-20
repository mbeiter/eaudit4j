/*
 * #%L
 * This file is part of eAudit4j, a library for creating pluggable
 * auditing solutions, providing an audit processor that creates
 * a unique machine ID for the machine executing the library and
 * appends it as a field to audit events.
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
package org.beiter.michael.eaudit4j.processors.machineid.propsbuilder;

import org.beiter.michael.eaudit4j.processors.machineid.MachineIdProperties;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class MapPropsMachineIdPropsBuilderTest {

    ///////////////////////////////////////////////////////////////////////////
    // Additional Properties Tests
    //   (test the additional properties that are not explicitly named)
    ///////////////////////////////////////////////////////////////////////////

    /**
     * additionalProperties test: make sure that the additional properties are being set to a new object (i.e. a
     * defensive copy is being made)
     */
    @Test
    public void additionalPropertiesNoSingletonTest() {

        String key = "some property";
        String value = "some value";

        Map<String, String> map = new HashMap<>();

        map.put(key, value);
        MachineIdProperties properties = MapBasedMachineIdPropsBuilder.build(map);

        String error = "The properties builder returns a singleton";
        assertThat(error, map, is(not(sameInstance(properties.getAdditionalProperties()))));
    }


    ///////////////////////////////////////////////////////////////////////////
    // Named Properties Tests
    //   (test the explicitly named properties)
    ///////////////////////////////////////////////////////////////////////////

    /**
     * default machine ID test
     */
    @Test
    public void defaultMachineIdTest() {

        MachineIdProperties properties = MapBasedMachineIdPropsBuilder.buildDefault();

        String error = "machine ID does not match expected default value";
        assertThat(error, properties.getMachineId(),
                is(equalTo(MapBasedMachineIdPropsBuilder.DEFAULT_MACHINE_ID)));
        error = "machine ID does not match expected value";
        properties.setMachineId("42");
        assertThat(error, properties.getMachineId(), is(equalTo("42")));
    }

    /**
     * machine ID test
     */
    @Test
    public void machineIdTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedMachineIdPropsBuilder.KEY_MACHINE_ID, null);
        MachineIdProperties properties = MapBasedMachineIdPropsBuilder.build(map);
        String error = "machine ID does not match expected default value";
        assertThat(error, properties.getMachineId(),
                is(equalTo(MapBasedMachineIdPropsBuilder.DEFAULT_MACHINE_ID)));

        map.put(MapBasedMachineIdPropsBuilder.KEY_MACHINE_ID, "42");
        properties = MapBasedMachineIdPropsBuilder.build(map);
        error = "machine ID does not match expected value";
        assertThat(error, properties.getMachineId(), is(equalTo("42")));

        // copy constructor test
        MachineIdProperties properties2 = new MachineIdProperties(properties);
        error = "copy constructor does not copy field";
        assertThat(error, properties2.getMachineId(), is(equalTo("42")));
    }

    /**
     * default event field name test
     */
    @Test
    public void defaultEventFieldNameTest() {

        MachineIdProperties properties = MapBasedMachineIdPropsBuilder.buildDefault();

        String error = "event field name does not match expected default value";
        assertThat(error, properties.getEventFieldName(),
                is(equalTo(MapBasedMachineIdPropsBuilder.DEFAULT_EVENT_FIELD_NAME)));
        error = "event field name does not match expected value";
        properties.setEventFieldName("42");
        assertThat(error, properties.getEventFieldName(), is(equalTo("42")));
    }

    /**
     * event field name test
     */
    @Test
    public void eventFieldNameTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedMachineIdPropsBuilder.KEY_EVENT_FIELD_NAME, null);
        MachineIdProperties properties = MapBasedMachineIdPropsBuilder.build(map);
        String error = "event field name does not match expected default value";
        assertThat(error, properties.getEventFieldName(),
                is(equalTo(MapBasedMachineIdPropsBuilder.DEFAULT_EVENT_FIELD_NAME)));

        map.put(MapBasedMachineIdPropsBuilder.KEY_EVENT_FIELD_NAME, "42");
        properties = MapBasedMachineIdPropsBuilder.build(map);
        error = "event field name does not match expected value";
        assertThat(error, properties.getEventFieldName(), is(equalTo("42")));

        // copy constructor test
        MachineIdProperties properties2 = new MachineIdProperties(properties);
        error = "copy constructor does not copy field";
        assertThat(error, properties2.getEventFieldName(), is(equalTo("42")));
    }

    /**
     * default is_machineIdfromEnv test
     */
    @Test
    public void defaultIsMachineIdFromEnvTest() {

        MachineIdProperties properties = MapBasedMachineIdPropsBuilder.buildDefault();

        String error = "is_machineIdfromEnv does not match expected default value";
        assertThat(error, properties.isMachineIdFromEnv(),
                is(equalTo(MapBasedMachineIdPropsBuilder.DEFAULT_MACHINE_ID_FROM_ENV)));
        error = "is_machineIdfromEnv does not match expected value";
        properties.setMachineIdFromEnv(true);
        assertThat(error, properties.isMachineIdFromEnv(), is(equalTo(true)));
        error = "is_machineIdfromEnv does not match expected value";
        properties.setMachineIdFromEnv(false);
        assertThat(error, properties.isMachineIdFromEnv(), is(equalTo(false)));
    }

    /**
     * is_machineIdfromEnv test
     */
    @Test
    public void isMachineIdFromEnvTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedMachineIdPropsBuilder.KEY_MACHINE_ID_FROM_ENV, null);
        MachineIdProperties properties = MapBasedMachineIdPropsBuilder.build(map);
        String error = "is_machineIdfromEnv does not match expected default value";
        assertThat(error, properties.isMachineIdFromEnv(),
                is(equalTo(MapBasedMachineIdPropsBuilder.DEFAULT_MACHINE_ID_FROM_ENV)));

        map.put(MapBasedMachineIdPropsBuilder.KEY_MACHINE_ID_FROM_ENV, "asdf");
        properties = MapBasedMachineIdPropsBuilder.build(map);
        error = "is_machineIdfromEnv does not match expected value";
        assertThat(error, properties.isMachineIdFromEnv(), is(equalTo(false)));

        map.put(MapBasedMachineIdPropsBuilder.KEY_MACHINE_ID_FROM_ENV, "tRuE");
        properties = MapBasedMachineIdPropsBuilder.build(map);
        error = "is_machineIdfromEnv does not match expected value";
        assertThat(error, properties.isMachineIdFromEnv(), is(equalTo(true)));

        // copy constructor test
        MachineIdProperties properties2 = new MachineIdProperties(properties);
        error = "copy constructor does not copy field";
        assertThat(error, properties2.isMachineIdFromEnv(), is(equalTo(true)));
    }

    /**
     * default machine ID environment variable name test
     */
    @Test
    public void defaultMachineIdEnvNameTest() {

        MachineIdProperties properties = MapBasedMachineIdPropsBuilder.buildDefault();

        String error = "machine ID env name does not match expected default value";
        assertThat(error, properties.getMachineIdEnvName(),
                is(equalTo(MapBasedMachineIdPropsBuilder.DEFAULT_MACHINE_ID_ENV_NAME)));
        error = "machine ID env name does not match expected value";
        properties.setMachineIdEnvName("42");
        assertThat(error, properties.getMachineIdEnvName(), is(equalTo("42")));
    }

    /**
     * machine ID environment variable name test
     */
    @Test
    public void machineIdEnvNameTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedMachineIdPropsBuilder.KEY_MACHINE_ID_ENV_NAME, null);
        MachineIdProperties properties = MapBasedMachineIdPropsBuilder.build(map);
        String error = "machine ID env name does not match expected default value";
        assertThat(error, properties.getMachineIdEnvName(),
                is(equalTo(MapBasedMachineIdPropsBuilder.DEFAULT_MACHINE_ID_ENV_NAME)));

        map.put(MapBasedMachineIdPropsBuilder.KEY_MACHINE_ID_ENV_NAME, "42");
        properties = MapBasedMachineIdPropsBuilder.build(map);
        error = "machine ID env name does not match expected value";
        assertThat(error, properties.getMachineIdEnvName(), is(equalTo("42")));

        // copy constructor test
        MachineIdProperties properties2 = new MachineIdProperties(properties);
        error = "copy constructor does not copy field";
        assertThat(error, properties2.getMachineIdEnvName(), is(equalTo("42")));
    }

    /**
     * default is_machineIdfromHostname test
     */
    @Test
    public void defaultIsMachineIdFromHostnameTest() {

        MachineIdProperties properties = MapBasedMachineIdPropsBuilder.buildDefault();

        String error = "is_machineIdfromHostname does not match expected default value";
        assertThat(error, properties.isMachineIdFromEnv(),
                is(equalTo(MapBasedMachineIdPropsBuilder.DEFAULT_MACHINE_ID_FROM_HOSTNAME)));
        error = "is_machineIdfromHostname does not match expected value";
        properties.setMachineIdFromHostname(true);
        assertThat(error, properties.isMachineIdFromHostname(), is(equalTo(true)));
        error = "is_machineIdfromHostname does not match expected value";
        properties.setMachineIdFromHostname(false);
        assertThat(error, properties.isMachineIdFromHostname(), is(equalTo(false)));
    }

    /**
     * is_machineIdfromHostname test
     */
    @Test
    public void isMachineIdFromHostnameTest() {

        Map<String, String> map = new HashMap<>();

        map.put(MapBasedMachineIdPropsBuilder.KEY_MACHINE_ID_FROM_HOSTNAME, null);
        MachineIdProperties properties = MapBasedMachineIdPropsBuilder.build(map);
        String error = "is_machineIdfromHostname does not match expected default value";
        assertThat(error, properties.isMachineIdFromHostname(),
                is(equalTo(MapBasedMachineIdPropsBuilder.DEFAULT_MACHINE_ID_FROM_HOSTNAME)));

        map.put(MapBasedMachineIdPropsBuilder.KEY_MACHINE_ID_FROM_HOSTNAME, "asdf");
        properties = MapBasedMachineIdPropsBuilder.build(map);
        error = "is_machineIdfromHostname does not match expected value";
        assertThat(error, properties.isMachineIdFromHostname(), is(equalTo(false)));

        map.put(MapBasedMachineIdPropsBuilder.KEY_MACHINE_ID_FROM_HOSTNAME, "tRuE");
        properties = MapBasedMachineIdPropsBuilder.build(map);
        error = "is_machineIdfromHostname does not match expected value";
        assertThat(error, properties.isMachineIdFromHostname(), is(equalTo(true)));

        // copy constructor test
        MachineIdProperties properties2 = new MachineIdProperties(properties);
        error = "copy constructor does not copy field";
        assertThat(error, properties2.isMachineIdFromHostname(), is(equalTo(true)));
    }
}
