/*
 * #%L
 * This file is part of eAudit4j, a library for creating pluggable
 * auditing solutions, providing an application to execute performance tests agains supported databases.
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
package com.hp.audit;


import org.apache.commons.lang3.StringEscapeUtils;

public class JtlResult {
    private long time;
    /** idleTime */
    private long idleTime;
    /** ATT_LATENCY */
    private long latency;
    /** ATT_TIME_STAMP */
    private long timeStamp;
    /** ATT_SUCCESS */
    private boolean success;
    /** ATT_LABEL */
    private String label;
    /** ATT_RESPONSE_CODE */
    private String responseCode;
    /** ATT_RESPONSE_MESSAGE */
    private String responseMessage;
    /** ATT_THREADNAME */
    private String threadName;
    /** ATT_DATA_TYPE */
    private String dataType = "";
    /** ATT_DATA_ENCODING */
    private String dataEncoding = "";
    /** ATT_BYTES */
    private long numBytes;
    /** ATT_SAMPLE_COUNT */
    private long sampleCount;
    /** ATT_ERROR_COUNT */
    private long errorCount;
    /** ATT_GRP_THRDS */
    private long grpThrds;
    /** ATT_ALL_THRDS */
    private long allThrds;

    private String responseData = "";

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getIdleTime() {
        return idleTime;
    }

    public void setIdleTime(long idleTime) {
        this.idleTime = idleTime;
    }

    public long getLatency() {
        return latency;
    }

    public void setLatency(long latency) {
        this.latency = latency;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDataEncoding() {
        return dataEncoding;
    }

    public void setDataEncoding(String dataEncoding) {
        this.dataEncoding = dataEncoding;
    }

    public long getNumBytes() {
        return numBytes;
    }

    public void setNumBytes(long numBytes) {
        this.numBytes = numBytes;
    }

    public long getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(long sampleCount) {
        this.sampleCount = sampleCount;
    }

    public long getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(long errorCount) {
        this.errorCount = errorCount;
    }

    public long getGrpThrds() {
        return grpThrds;
    }

    public void setGrpThrds(long grpThrds) {
        this.grpThrds = grpThrds;
    }

    public long getAllThrds() {
        return allThrds;
    }

    public void setAllThrds(long allThrds) {
        this.allThrds = allThrds;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }


    @Override
    public String toString() {
        return new StringBuilder().append("<httpSample t=\"").append(time).append("\" it=\"").append(idleTime)
                .append("\" lt=\"").append(latency).append("\" ts=\"").append(timeStamp).append("\" s=\"").append(success)
                .append("\" ").append("lb=\"").append(label).append("\" rc=\"").append(responseCode)
                .append("\" rm=\"").append(responseMessage).append("\" tn=\"").append(threadName)
                .append("\" dt=\"").append(dataType).append("\" de=\"").append(dataEncoding)
                .append("\" by=\"").append(numBytes).append("\" sc=\"").append(sampleCount)
                .append("\" ec=\"").append(errorCount).append("\" ng=\"").append(grpThrds)
                .append("\" na=\"").append(allThrds).append("\">\n")
                .append("  <responseHeader class=\"java.lang.String\"></responseHeader>\n")
                .append("  <requestHeader class=\"java.lang.String\"></requestHeader>\n")
                .append("  <responseData class=\"java.lang.String\">")
                .append(StringEscapeUtils.escapeXml11(responseData))
                .append("</responseData>\n")
                .append("  <responseFile class=\"java.lang.String\"></responseFile>\n")
                .append("</httpSample>\n").toString();
    }
}
