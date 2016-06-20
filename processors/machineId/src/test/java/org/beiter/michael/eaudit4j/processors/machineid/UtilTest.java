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
package org.beiter.michael.eaudit4j.processors.machineid;

import org.beiter.michael.eaudit4j.common.AuditErrorConditions;
import org.beiter.michael.eaudit4j.common.AuditException;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class UtilTest {

    private static final String UUID_REGEX = "[0-9a-fA-F]{8}(?:-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}";

    /**
     * Obtain a machine ID from the environment.
     * <p>
     * This test uses the "PATH" environment variable, because it is usually set in Windows, Linux, Macs, etc.
     * <p>
     * This test may fail if the PATH variable is not set. If so, either set it, or disable the test temporarily.
     */
    //@Ignore
    @Test
    public void getMachineIdFromEnvTest()
            throws AuditException {

        boolean getfromEnv = true;
        String envVariableName = "PATH";
        boolean getFromHostname = false;

        String machineId = Util.getMachineId(getfromEnv, envVariableName, getFromHostname);

        String error = "machineid is null or empty, this should never happen (fallback should kick in)";
        assertThat(error, machineId, is(not(isEmptyOrNullString())));

        error = "machineid seems to be a UUID, which is the fallback. This means retrieval from the environment most likely failed";
        assertThat(error, machineId.matches(UUID_REGEX), is(false));
    }

    /**
     * Obtain a machine ID from the environment, with a null environment variable name
     */
    @Test(expected = AuditException.class)
    public void getMachineIdFromNullEnvTest()
            throws AuditException {

        boolean getfromEnv = true;
        String envVariableName = null;
        boolean getFromHostname = false;

        try {
            Util.getMachineId(getfromEnv, envVariableName, getFromHostname);
        } catch (AuditException e) {
            AuditErrorConditions expected = AuditErrorConditions.CONFIGURATION;
            String error = "The type of exception thrown is not correct. Expected " + expected + ", got " + e.getErrorCondition();
            Assert.assertThat(error, e.getErrorCondition(), is(equalTo(expected)));
            throw e;
        }

        throw new AssertionError("Expected an exception, but that exception was not thrown");
    }

    /**
     * Obtain a machine ID from the environment, with a blank environment variable name
     */
    @Test(expected = AuditException.class)
    public void getMachineIdFromEmptyEnvTest()
            throws AuditException {

        boolean getfromEnv = true;
        String envVariableName = "";
        boolean getFromHostname = false;

        try {
            Util.getMachineId(getfromEnv, envVariableName, getFromHostname);
        } catch (AuditException e) {
            AuditErrorConditions expected = AuditErrorConditions.CONFIGURATION;
            String error = "The type of exception thrown is not correct. Expected " + expected + ", got " + e.getErrorCondition();
            Assert.assertThat(error, e.getErrorCondition(), is(equalTo(expected)));
            throw e;
        }

        throw new AssertionError("Expected an exception, but that exception was not thrown");
    }

    /**
     * Obtain a machine ID from the environment, with an invalid environment variable name
     */
    @Test
    public void getMachineIdFromInvalidEnvTest()
            throws AuditException {

        boolean getfromEnv = true;
        String envVariableName = "DOES_NOT_EXIST_9L2GDNMF4VHM";
        boolean getFromHostname = false;

        String machineId = Util.getMachineId(getfromEnv, envVariableName, getFromHostname);

        // the fallback in case of an empty after trying to resolve the machineID without success is a random UUID
        String error = "machineid is not a UUID";
        assertThat(error, machineId.matches(UUID_REGEX), is(true));
    }

    /**
     * Obtain a machine ID from the hostname
     * <p>
     * This test does not really test for the correct hostname or anything. It only makes sure that the machine ID that
     * is being created has the correct format.
     */
    @Test
    public void getMachineIdFromHostnameTest()
            throws AuditException {

        boolean getfromEnv = false;
        String envVariableName = null;
        boolean getFromHostname = true;

        String machineId = Util.getMachineId(getfromEnv, envVariableName, getFromHostname);

        String error = "machineid is null or empty, this should never happen (fallback should kick in)";
        assertThat(error, machineId, is(not(isEmptyOrNullString())));

        error = "machineid does not seem to match the expected format";
        assertThat(error, machineId.contains(":"), is(true));

        // this should never happen, as a regex does not contain a ':'
        error = "machineid seems to be a UUID, which is the fallback. This means building from the hostname most likely failed";
        assertThat(error, machineId.matches(UUID_REGEX), is(false));
    }

    /**
     * The the fallback (no machine ID retrieved from either env or hostname)
     * <p>
     * Make sure that the returned machine ID is a UUID.
     */
    @Test
    public void getMachineIdFallbackTest()
            throws AuditException {

        boolean getfromEnv = false;
        String envVariableName = null;
        boolean getFromHostname = false;

        String machineId = Util.getMachineId(getfromEnv, envVariableName, getFromHostname);

        String error = "machineid is not a UUID";
        assertThat(error, machineId.matches(UUID_REGEX), is(true));
    }
}
