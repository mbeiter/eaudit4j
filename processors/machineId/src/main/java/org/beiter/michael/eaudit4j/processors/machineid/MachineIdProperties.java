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

import org.apache.commons.lang3.Validate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class specifies properties specific to the Machine ID Processor.
 */
// suppress warnings about the long variable names
@SuppressWarnings("PMD.LongVariable")
public class MachineIdProperties {

    /**
     * @see MachineIdProperties#setMachineId(String)
     */
    private String machineId;

    /**
     * @see MachineIdProperties#setEventFieldName(String)
     */
    private String eventFieldName;

    /**
     * @see MachineIdProperties#setMachineIdFromEnv(boolean)
     */
    private boolean machineIdFromEnv;

    /**
     * @see MachineIdProperties#setMachineIdEnvName(String)
     */
    private String machineIdEnvName;

    /**
     * @see MachineIdProperties#setMachineIdFromHostname(boolean)
     */
    private boolean machineIdFromHostname;

    /**
     * @see MachineIdProperties#setAdditionalProperties(Map)
     */
    private Map<String, String> additionalProperties = new ConcurrentHashMap<>();

    /**
     * Constructs an empty set of machine ID properties, with most values being set to <code>null</code>, 0, or empty
     * (depending on the type of the property). Usually this constructor is used if this configuration POJO is populated
     * in an automated fashion (e.g. injection). If you need to build them manually (possibly with defaults), use or
     * create a properties builder.
     * <p>
     * You can change the defaults with the setters.
     *
     * @see org.beiter.michael.eaudit4j.common.propsbuilder.MapBasedCommonPropsBuilder#buildDefault()
     * @see org.beiter.michael.eaudit4j.common.propsbuilder.MapBasedCommonPropsBuilder#build(Map)
     */
    public MachineIdProperties() {

        // no code here, constructor just for java docs
    }

    /**
     * Creates a set of machine ID properties from an existing set of machine ID properties, making a defensive copy.
     *
     * @param properties The set of properties to copy
     * @throws NullPointerException When {@code properties} is {@code null}
     * @see MachineIdProperties()
     */
    public MachineIdProperties(final MachineIdProperties properties) {

        this();

        Validate.notNull(properties, "The validated object 'properties' is null");

        setMachineId(properties.getMachineId());
        setEventFieldName(properties.getEventFieldName());
        setMachineIdFromEnv(properties.isMachineIdFromEnv());
        setMachineIdEnvName(properties.getMachineIdEnvName());
        setMachineIdFromHostname(properties.isMachineIdFromHostname());
        setAdditionalProperties(properties.getAdditionalProperties());
    }

    /**
     * @return The machine ID of this machine (may be {@code null} if no machine ID has been configured)
     * @see MachineIdProperties#setMachineId(String)
     */
    public final String getMachineId() {

        // no need for defensive copies of String

        return machineId;
    }

    /**
     * Set the machine ID of this machine.
     * <p>
     * If no machine ID is set, implementations may try alternative means to determine a unique ID for the machine.
     *
     * @param machineId The ID of this machine
     */
    public final void setMachineId(final String machineId) {

        // no need for validation, as we cannot possible validate all machine IDs and null is allowed

        // no need for defensive copies of String

        this.machineId = machineId;
    }

    /**
     * @return The field name used to store the machine ID in events
     * @see MachineIdProperties#setEventFieldName(String)
     */
    public final String getEventFieldName() {

        // no need for defensive copies of String

        return eventFieldName;
    }

    /**
     * Set the field name to be used when storing the machine ID in audit events
     *
     * @param eventFieldName The field name to store the machine ID in
     * @throws NullPointerException When the {@code eventFieldName} is {@code null}
     * @throws IllegalArgumentException When the {@code eventFieldName} is {@code empty}
     */
    public final void setEventFieldName(final String eventFieldName) {

        Validate.notBlank(eventFieldName, "The validated character sequence 'eventFieldName' is null or empty");

        // no need for defensive copies of String

        this.eventFieldName = eventFieldName;
    }

    /**
     * @return the indication of whether the machine ID should be read from an environment variable
     * @see MachineIdProperties#setMachineIdFromEnv(boolean)
     */
    public final boolean isMachineIdFromEnv() {

        // no need for defensive copies of boolean

        return machineIdFromEnv;
    }

    /**
     * The indication of whether the machine ID should be read from an environment variable.
     * <p>
     * If no machine ID can be obtained, implementations may try alternative means to determine a unique ID for the
     * machine.
     *
     * @param machineIdFromEnv the indication of whether objects will be read from the environment
     */
    public final void setMachineIdFromEnv(final boolean machineIdFromEnv) {

        // no need for validation, as boolean cannot be null and all possible values are allowed
        // no need for defensive copies of boolean

        this.machineIdFromEnv = machineIdFromEnv;
    }

    /**
     * @return The name of the env variable from which the machine of this machine should be read (may be {@code null})
     * @see MachineIdProperties#setMachineIdEnvName(String)
     */
    public final String getMachineIdEnvName() {

        // no need for defensive copies of String

        return machineIdEnvName;
    }

    /**
     * Set the name of the environment variable from which the machine ID of this machine should be read
     * <p>
     * If no machine ID can be obtained, implementations may try alternative means to determine a unique ID for the
     * machine.
     *
     * @param machineIdEnvName The name of the env var to get the ID of this machine from
     */
    public final void setMachineIdEnvName(final String machineIdEnvName) {

        // no need for validation, as we cannot possible validate all environment variable names and null is allowed

        // no need for defensive copies of String

        this.machineIdEnvName = machineIdEnvName;
    }

    /**
     * @return the indication of whether the machine ID should be created from the hostname
     * @see MachineIdProperties#setMachineIdFromHostname(boolean)
     */
    public final boolean isMachineIdFromHostname() {

        // no need for defensive copies of boolean

        return machineIdFromHostname;
    }

    /**
     * The indication of whether the machine ID should be created from the hostname
     * <p>
     * If no machine ID can be obtained, implementations may try alternative means to determine a unique ID for the
     * machine.
     *
     * @param machineIdFromHostname the indication of whether the machine ID will be created based on the hostname
     */
    public final void setMachineIdFromHostname(final boolean machineIdFromHostname) {

        // no need for validation, as boolean cannot be null and all possible values are allowed
        // no need for defensive copies of boolean

        this.machineIdFromHostname = machineIdFromHostname;
    }

    /**
     * @return Any additional properties stored in this object that have not explicitly been parsed
     * @see MachineIdProperties#setAdditionalProperties(Map)
     */
    public final Map<String, String> getAdditionalProperties() {

        // create a defensive copy of the map and all its properties
        if (this.additionalProperties == null) {
            // this should never happen!
            return new ConcurrentHashMap<>();
        } else {
            final Map<String, String> tempMap = new ConcurrentHashMap<>();
            tempMap.putAll(additionalProperties);

            return tempMap;
        }
    }

    /**
     * Any additional properties which have not been parsed, and for which no getter/setter exists, but are to be
     * stored in this object nevertheless.
     * <p>
     * This property is commonly used to preserve original properties from upstream components that are to be passed
     * on to downstream components unchanged. This properties set may or may not include properties that have been
     * extracted from the map, and been made available through this POJO.
     * <p>
     * Note that these additional properties may be <code>null</code> or empty, even in a fully populated POJO where
     * other properties commonly have values assigned to.
     *
     * @param additionalProperties The additional properties to store
     */
    public final void setAdditionalProperties(final Map<String, String> additionalProperties) {

        // create a defensive copy of the map and all its properties
        if (additionalProperties == null) {
            this.additionalProperties = new ConcurrentHashMap<>();
        } else {
            this.additionalProperties = new ConcurrentHashMap<>();
            this.additionalProperties.putAll(additionalProperties);
        }
    }
}
