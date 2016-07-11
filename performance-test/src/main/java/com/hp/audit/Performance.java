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

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Performance {
    public static final String NUM_THREADS = "numThreads";
    public static final String NUM_LOOPS = "numLoops";
    public static final String RAMP_UP_TIME = "rumpUpInMilliseconds";
    public static final String JTL_FILE = "jtlFile";
    public static final String MAX_TEST_TIME = "maxTestTime";
    public static final String MAX_TEST_TIME_UNIT = "maxTestTimeUnit";
    public static final String MAX_WARM_UP_TIME = "maxWarmUpTime";
    public static final String MAX_WARM_UP_TIME_UNIT = "maxWarmUpTimeUnit";
    public static final String MAX_TEST_TIME_DEFAULT_UNIT = "MINUTES";
    public static final String WARM_UP_TIME_DEFAULT_UNIT = "SECONDS";
    public static final String USER_HOME = "user.home";
    public static final String DEFAULT_FILE = "/test.jtl";

    private int numThreads;
    private int numLoops;
    private long rampUpTimeInMilliseconds;
    private ConcurrentLinkedQueue<JtlResult> results = new ConcurrentLinkedQueue<>();
    private static AtomicInteger numThreadsRunning = new AtomicInteger(0);
    private String threadGroupName = "Thread Group 1";
    private File outputFile;
    private int maxTestTime;
    private TimeUnit maxTestTimeUnit;
    private long gracefullShutDownTime = 10000;
    private boolean isShutingDown = false;

    private Thread saveFileThread;

    private ScheduledExecutorService scheduledExecutorService;

    public void setGracefullShutDownTime(long gracefullShutDownTime) {
        this.gracefullShutDownTime = gracefullShutDownTime;
    }

    public void setThreadGroupName(String threadGroupName) {
        this.threadGroupName = threadGroupName;
    }

    public Performance(int numThreads, int numLoops, long rampUpTimeInMilliseconds,
                       File outputFile, int maxTestTime, TimeUnit maxTestTimeUnit) {
        this.numThreads = numThreads;
        this.scheduledExecutorService =
                Executors.newScheduledThreadPool(numThreads);
        this.numLoops = numLoops;
        this.rampUpTimeInMilliseconds = rampUpTimeInMilliseconds;

        this.outputFile = outputFile;

        this.maxTestTime = maxTestTime;
        this.maxTestTimeUnit = maxTestTimeUnit;
    }

    public <P> void runTests(String label, Supplier<P> parametersSupplier, Consumer<P> testParametersConsumer) {
        if(numThreads > 0 && rampUpTimeInMilliseconds > 0) {
            numThreadsRunning.set(0);
            results.clear();
            long deltaUpThreads = rampUpTimeInMilliseconds / numThreads;
            startThreads(label, parametersSupplier, testParametersConsumer, deltaUpThreads);
            try {
                waitThreadsFinishOrTimeout();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("\nWritting file:" + outputFile.getAbsolutePath());

            System.out.println("done!");
        } else {
            System.out.println("Number of Threads and Ramp up Time can not be less than 0!");
        }
    }

    private <P> void startThreads(String label, Supplier<P> parametersSupplier, Consumer<P> testParametersConsumer, long deltaUpThreads) {
        for (int i = 0; i < numThreads; i++) {
            numThreadsRunning.incrementAndGet();
            scheduledExecutorService.execute(() -> runThreadLoop(
                    getThreadName(threadGroupName, numThreadsRunning.get()), label,
                    parametersSupplier, testParametersConsumer)
            );
            sleep(deltaUpThreads);
        }
        saveFileThread = new Thread(() -> writeResultToFile(), "saveFileThread");
        saveFileThread.start();
    }

    private void waitThreadsFinishOrTimeout() throws InterruptedException {
        scheduledExecutorService.shutdown();
        scheduledExecutorService.awaitTermination(maxTestTime, maxTestTimeUnit);
        isShutingDown = true;
        if (!waitCondition(() -> scheduledExecutorService.isTerminated(), gracefullShutDownTime)) {
            scheduledExecutorService.shutdownNow();
        }
        if (saveFileThread.isAlive()) {
            saveFileThread.join();
        }
    }

    public boolean waitCondition(Supplier<Boolean> condition, long timeMillis) {
        long startTs = System.currentTimeMillis();
        while (!condition.get() && (timeMillis > (System.currentTimeMillis() - startTs))) {
            sleep(150);
        }
        return condition.get();
    }

    public String getThreadName(String threadGroupName, int threadCount) {
        return new StringBuilder().append(threadGroupName).append("-")
                .append(threadCount).toString();
    }

    public <P> void runThreadLoop(String threadName, String label,
                               Supplier<P> parametersSupplier, Consumer<P> testParametersConsumer) {
        for (int i = 0; i < numLoops; i++) {
            if (isShutingDown) {
                return;
            }
            runWithMetrics(threadName, label, parametersSupplier, testParametersConsumer);
        }
    }

    public <P> void runWithMetrics(String threadName, String label,
            Supplier<P> parametersSupplier, Consumer<P> testParametersConsumer) {
        JtlResult result = new JtlResult();
        result.setThreadName(threadName);
        result.setLabel(label);
        int numThreads = numThreadsRunning.get();
        result.setGrpThrds(numThreads);
        result.setAllThrds(numThreads);
        result.setSampleCount(1);
        P params = parametersSupplier.get();

        result.setTimeStamp(System.currentTimeMillis());
        long startTime = 0;
        try {
            startTime = System.nanoTime();
            testParametersConsumer.accept(params);
            result.setTime((System.nanoTime() - startTime) / 1000000);
            result.setSuccess(true);
        } catch (Throwable e) {
            result.setTime((System.nanoTime() - startTime) / 1000000);
            result.setSuccess(false);
            result.setErrorCount(1);
            result.setResponseMessage(e.getMessage());
            result.setResponseCode(e.getClass().getSimpleName());
            result.setResponseData(ExceptionUtils.getStackTrace(e));
            result.setDataType("text");
            e.printStackTrace();
        }
        results.add(result);
    }

    private void writeResultToFile() {
        try {
            BufferedWriter w = Files.newBufferedWriter(outputFile.toPath());
            w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<testResults version=\"1.2\">\n");
            while (!scheduledExecutorService.isTerminated() || results.size() > 0) {
                JtlResult result = results.poll();
                if (result != null) {
                    w.write(result.toString());
                } else {
                    sleep(100);
                }
            }
            w.write("</testResults>");
            w.flush();
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
