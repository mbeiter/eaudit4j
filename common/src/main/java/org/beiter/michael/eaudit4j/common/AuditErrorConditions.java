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

/**
 * This enum contains the standard field names used by the helper methods e.g. in
 * {@link org.beiter.michael.eaudit4j.common.impl.EventBuilder} to populate common / frequently used fields.
 * <p>
 * If these field names collide with other fields you are intending to set, you may either use the fully qualified
 * default field names, or define your own fields / create a custom event build that uses custom field names.
 */
public enum AuditErrorConditions {

    //CHECKSTYLE:OFF
    INITIALIZATION("Initialization failure"),
    CONFIGURATION("Configuration error"),
    INVALID_EVENT("Invalid event ('null' reference, or invalid content)"),
    PROCESSING("Event processing error"),
    UNKNOWN("Unknown audit error");
    //CHECKSTYLE:ON

    /**
     * The error condition
     */
    private String errorCondition;

    /**
     * Populate a field name representation
     *
     * @param errorCondition The error condition
     */
    AuditErrorConditions(final String errorCondition) {

        this.errorCondition = errorCondition;
    }

    /**
     * Return the error condition
     *
     * @return The error condition
     */
    public String getErrorCondition() {
        return errorCondition;
    }

    /**
     * See {@link AuditErrorConditions#getErrorCondition()}
     *
     * @return A String representation of the field name
     */
    @Override
    public String toString() {
        return getErrorCondition();
    }
}
