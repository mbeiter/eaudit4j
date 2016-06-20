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
 * A class implementing the {@code process()} methods in the {@link Processor} interface may make modifications to an
 * event, such as modifying existing fields, or adding new fields.
 * <p>
 * Possible modifications of an event that may be provided in a class implementing the {@link Processor} interface in
 * particular include data compression and data encryption. While not all possible modification operations are
 * reversible, some of them are.
 * <p>
 * Classes implementing this interface indicate that their modifications to an event are reversible, either by undoing
 * modifications on event fields (e.g. decrypting a previously encrypted field), or by removing additional fields
 * that were added during processing (e.g. removing a field that contains a one-way hash of the event).
 */
public interface Reversible {

    /**
     * Revert changes that have been made to an event by the {@link Processor#process(Event)} and
     * {@link Processor#process(Event, String)} methods.
     *
     * @param event The event to revert changes on
     * @return The provided event with previously made changes reverted
     * @throws AuditException When the changes cannot be reverted
     */
    Event revert(Event event)
            throws AuditException;

}
