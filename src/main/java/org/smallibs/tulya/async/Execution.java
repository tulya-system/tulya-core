package org.smallibs.tulya.async;

import org.smallibs.tulya.async.impl.RunnablePromise;
import org.smallibs.tulya.lang.RunnableWithError;
import org.smallibs.tulya.lang.SupplierWithError;
import org.smallibs.tulya.standard.Unit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final public class Execution {
    private final ExecutorService executorService;

    private Execution(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public <R> Promise<R> async(SupplierWithError<R> callable) {
        var promise = new RunnablePromise<>(callable);
        this.executorService.execute(promise);
        return promise;
    }

    public Promise<Unit> async(RunnableWithError runnable) {
        var promise = new RunnablePromise<>(() -> {
            runnable.run();
            return Unit.unit;
        });
        this.executorService.execute(promise);
        return promise;
    }

    public static Execution ofVirtual() {
        return new Execution(Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("async:virtual").factory()));
    }

    public static Execution ofPlatform(int size) {
        return new Execution(Executors.newFixedThreadPool(size, Thread.ofPlatform().name("async:platform").factory()));
    }

    public static Execution of(ExecutorService executorService) {
        return new Execution(executorService);
    }
}
