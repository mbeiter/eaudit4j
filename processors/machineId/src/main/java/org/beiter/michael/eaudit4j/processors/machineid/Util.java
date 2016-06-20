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
import org.beiter.michael.eaudit4j.processors.machineid.propsbuilder.MapBasedMachineIdPropsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.UUID;

/**
 * A utility class providing methods to resolve the machine ID from:
 * <ul>
 * <li>an environment variable or</li>
 * <li>a hostname or</li>
 * <li>as a random ID</li>
 * </ul>
 */
@SuppressWarnings("PMD.ShortClassName")
public final class Util {

    /**
     * The logger object for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(Util.class);

    /**
     * A private constructor to prevent instantiation of this class
     */
    private Util() {
    }

    /**
     * This method retrieves the machine ID as configured in a specific environment variable.
     * <p>
     * If the env variable is not configured or empty, the host ID defaults to "$HOSTNAME:$TIMESTAMP", where:
     * <ul>
     * <li>$HOSTNAME is the fully qualified hostname of localhost (see {@link InetAddress#getLocalHost()} and
     * {@link InetAddress#getCanonicalHostName()})</li>
     * <li>$TIMESTAMP is the epoch timestamp</li>
     * </ul>
     * <p>
     * If the hostname cannot be resolved from the environment or the hostname, then a ID is generated as the
     * machine ID.
     *
     * @param getfromEnv      True if the method should try reading the machine ID from the environment, false to skip
     * @param envVariableName The name of the environment variable to read the machine ID from
     * @param getFromHostname True if the method should try crafting the machine ID from the hostname, false to skip
     * @return The machine ID as resolved by the algorithm described above
     * @throws AuditException When the machineID cannot be determined
     */
    // Allow catching a general Exception because of possible RuntimeException thrown by getCanonicalHostName(), which
    // we will ignore and are handling in business logic as part of the flow (i.e. we will do an alternative resolve)
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public static String getMachineId(final boolean getfromEnv,
                                      final String envVariableName,
                                      final boolean getFromHostname)
            throws AuditException {

        // this is the machine ID that we will eventually use as a return value of this method
        String resolvedMachineId = null;

        // check whether we should read this Machine ID from an environment variable, and do so if necessary
        if (getfromEnv) {

            LOG.debug("Trying to obtain the machine ID from the environment...");

            resolvedMachineId = getMachineIdFromEnv(envVariableName);
        }

        // check whether we should try resolve the hostname and craft a machine ID from that
        if (getFromHostname) {

            LOG.debug("Trying to obtain the machine ID from the hostname...");

            try {
                resolvedMachineId = getCanonicalHostName() + ":" + new Date().getTime();
            } catch (Exception e) {
                // Catching general Exception because of possible RuntimeException thrown by getCanonicalHostName()
                final String error = "Canonical hostname can not be resolved: " + e.getLocalizedMessage()
                        + " Attempting alternative machine ID resolution method.";

                LOG.info(error, e);
            }
        }

        // Fallback: if we failed to resolve the machine ID up to this point, and we also did not bail out yet with an
        //           exception, then we create a random string and use that as the machine ID, logging this in the app
        //           log so that the operator can find it and properly map the audit logs to a specific machine

        if (resolvedMachineId == null || resolvedMachineId.isEmpty()) {

            LOG.warn("Generating a random machine ID");

            resolvedMachineId = UUID.randomUUID().toString();

            // create a log trace so that the operator can see what machine ID is going to be used:
            LOG.warn(MapBasedMachineIdPropsBuilder.KEY_MACHINE_ID + " has been set to: " + resolvedMachineId);
        }

        return resolvedMachineId;
    }

    /**
     * Retrieve the machine ID a provided environment variable
     *
     * @param envVariableName The environment variable that holds the machine ID
     * @return The machine ID (as stored in the env variable)
     * @throws AuditException When the provided environment variable name is null or empty
     */
    private static String getMachineIdFromEnv(final String envVariableName)
            throws AuditException {

        if (envVariableName == null || envVariableName.isEmpty()) {
            final String error = "The configured Machine ID environment variable name is null or empty";
            LOG.warn(error);
            throw new AuditException(AuditErrorConditions.CONFIGURATION, error);
        }

        final String resolvedMachineId = System.getenv(envVariableName);

        if (resolvedMachineId == null || resolvedMachineId.isEmpty()) {
            final String error = "The value of the configured Machine ID environment variable is null or empty. "
                    + "Attempting alternative machine ID resolution method.";
            LOG.warn(error);
        }
        return resolvedMachineId;
    }


    /**
     * This method tries to retrieve the canonical machine name.
     *
     * @return the canonical machine name
     * @throws AuditException When the hostname cannot be determined
     */
    private static String getCanonicalHostName()
            throws AuditException {

        String canonicalHostName;
        try {
            canonicalHostName = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            final String error = "Error when retrieving the canonical host name: " + e.getLocalizedMessage();
            LOG.warn(error, e);
            throw new AuditException(AuditErrorConditions.INITIALIZATION, error, e);
        }

        return canonicalHostName;
    }

}
