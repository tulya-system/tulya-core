package org.smallibs.tulya.async;

import org.smallibs.tulya.async.impl.AsyncImpl;
import org.smallibs.tulya.lang.RunnableWithError;
import org.smallibs.tulya.lang.SupplierWithError;
import org.smallibs.tulya.standard.Unit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface Async extends AutoCloseable {
    <R> Promise<R> run(SupplierWithError<R> callable);

    Promise<Unit> run(RunnableWithError runnable);

    class Companion {
        static Async ofVirtual() {
            return new AsyncImpl(Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("async:virtual").factory()));
        }

        static Async ofPlatform(int size) {
            return new AsyncImpl(Executors.newFixedThreadPool(size, Thread.ofPlatform().name("async:platform").factory()));
        }

        static Async of(ExecutorService executorService) {
            return new AsyncImpl(executorService);
        }
    }
}
