package org.smallibs.tulya.async;

import org.smallibs.tulya.async.impl.AsyncImpl;
import org.smallibs.tulya.lang.RunnableWithError;
import org.smallibs.tulya.lang.SupplierWithError;
import org.smallibs.tulya.standard.Unit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.Executors.newThreadPerTaskExecutor;

public interface Async extends AutoCloseable {
    <R> Promise<R> run(SupplierWithError<R> callable);

    Promise<Unit> run(RunnableWithError runnable);

    class Companion {
        static Async ofVirtual() {
            return Companion.of(newThreadPerTaskExecutor(Thread.ofVirtual().name("async:virtual").factory()));
        }

        static Async ofPlatform(int size) {
            return Companion.of(newFixedThreadPool(size, Thread.ofPlatform().name("async:platform").factory()));
        }

        static Async of(ExecutorService executorService) {
            return new AsyncImpl(executorService);
        }
    }
}
