package com.example.carsharing1.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;

@Slf4j
@Service
public class CounterDemoService {

    private int unsafeCounter = 0;
    private final AtomicInteger atomicCounter = new AtomicInteger(0);
    private int synchronizedCounter = 0;

    public int runRaceConditionDemo(int threads, int iterationsPerThread) {
        unsafeCounter = 0;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < iterationsPerThread; j++) {
                        int current = unsafeCounter;
                        if ((j & 127) == 0) {
                            Thread.yield();
                        }
                        unsafeCounter = current + 1;
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        await(latch, executor);
        int expected = threads * iterationsPerThread;
        log.info("Race condition demo: expected={}, actual={}", expected, unsafeCounter);
        return unsafeCounter;
    }

    public int runAtomicCounterDemo(int threads, int iterationsPerThread) {
        atomicCounter.set(0);
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < iterationsPerThread; j++) {
                        atomicCounter.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        await(latch, executor);
        int expected = threads * iterationsPerThread;
        int actual = atomicCounter.get();
        log.info("Atomic counter demo: expected={}, actual={}", expected, actual);
        return actual;
    }

    public int runSynchronizedCounterDemo(int threads, int iterationsPerThread) {
        synchronizedCounter = 0;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < iterationsPerThread; j++) {
                        incrementSynchronized();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        await(latch, executor);
        int expected = threads * iterationsPerThread;
        log.info("Synchronized counter demo: expected={}, actual={}", expected, synchronizedCounter);
        return synchronizedCounter;
    }

    private synchronized void incrementSynchronized() {
        synchronizedCounter++;
    }

    private void await(CountDownLatch latch, ExecutorService executor) {
        boolean completed = false;
        try {
            completed = latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Await interrupted: {}", e.getMessage());
        } finally {
            executor.shutdown();
        }

        if (!completed) {
            log.warn("CountDownLatch await completed false, some tasks may not have finished");
        }
    }
}