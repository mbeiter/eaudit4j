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
 * This exception is thrown by Factories when they encounter an error that needs to be handled by the caller.
 */
public class AuditException extends Exception {

    /**
     * Serialization
     */
    private static final long serialVersionUID = 20150915L;

    /**
     * The error condition represented by this instance
     */
    private AuditErrorConditions errorCondition = AuditErrorConditions.UNKNOWN;

    /**
     * @see Exception#Exception()
     */
    public AuditException() {
        super();
    }

    /**
     * @param errorCondition The error condition that was encountered when throwing this exception
     * @see Exception#Exception()
     */
    public AuditException(final AuditErrorConditions errorCondition) {

        this();
        this.errorCondition = errorCondition;
    }

    /**
     * @param message @see Exception#Exception(String, Throwable)
     * @param cause   @see Exception#Exception(String, Throwable)
     * @see Exception#Exception(String, Throwable)
     */
    public AuditException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * @param errorCondition The error condition that was encountered when throwing this exception
     * @param message        @see Exception#Exception(String, Throwable)
     * @param cause          @see Exception#Exception(String, Throwable)
     * @see Exception#Exception(String, Throwable)
     */
    public AuditException(final AuditErrorConditions errorCondition, final String message, final Throwable cause) {

        this(message, cause);
        this.errorCondition = errorCondition;
    }

    /**
     * @param message @see Exception#Exception(String)
     * @see Exception#Exception(String)
     */
    public AuditException(final String message) {
        super(message);
    }

    /**
     * @param errorCondition The error condition that was encountered when throwing this exception
     * @param message        @see Exception#Exception(String)
     * @see Exception#Exception(String)
     */
    public AuditException(final AuditErrorConditions errorCondition, final String message) {

        this(message);
        this.errorCondition = errorCondition;
    }

    /**
     * @param cause @see Exception#Exception(Throwable)
     * @see Exception#Exception(Throwable)
     */
    public AuditException(final Throwable cause) {
        super(cause);
    }

    /**
     * @param errorCondition The error condition that was encountered when throwing this exception
     * @param cause          @see Exception#Exception(Throwable)
     * @see Exception#Exception(Throwable)
     */
    public AuditException(final AuditErrorConditions errorCondition, final Throwable cause) {

        this(cause);
        this.errorCondition = errorCondition;
    }

    /**
     * Return the error condition that was encountered when throwing this exception.
     * <p>
     * This method returns {@link AuditErrorConditions#UNKNOWN} in case the error was not known when throwing the
     * exception.
     *
     * @return An element of {@link AuditErrorConditions} .
     */
    public final AuditErrorConditions getErrorCondition() {

        return errorCondition;
    }
}
