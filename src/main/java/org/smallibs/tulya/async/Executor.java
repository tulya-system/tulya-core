package org.smallibs.tulya.async;

import org.smallibs.tulya.async.impl.RunnablePromise;
import org.smallibs.tulya.lang.RunnableWithError;
import org.smallibs.tulya.lang.SupplierWithError;
import org.smallibs.tulya.standard.Unit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final public class Executor {
    private final ExecutorService executorService;

    private Executor(ExecutorService executorService) {
        this.executorService = executorService;
    }

    <R> Promise<R> async(SupplierWithError<R> callable) {
        var promise = new RunnablePromise<>(callable);
        this.executorService.execute(promise);
        return promise;
    }

    Promise<Unit> async(RunnableWithError runnable) {
        var promise = new RunnablePromise<>(() -> {
            runnable.run();
            return Unit.unit;
        });
        this.executorService.execute(promise);
        return promise;
    }

    public static Executor ofVirtual() {
        return new Executor(Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("async-virtual").factory()));
    }

    public static Executor ofPlatform() {
        return new Executor(Executors.newThreadPerTaskExecutor(Thread.ofPlatform().name("async-platform").factory()));
    }

    public static Executor of(ExecutorService executorService) {
        return new Executor(executorService);
    }
}
