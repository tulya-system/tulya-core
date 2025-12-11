package org.smallibs.tulya.async.impl;

import org.smallibs.tulya.async.Async;
import org.smallibs.tulya.async.Promise;
import org.smallibs.tulya.lang.RunnableWithError;
import org.smallibs.tulya.lang.SupplierWithError;
import org.smallibs.tulya.standard.Unit;

import java.util.concurrent.ExecutorService;

final public class AsyncImpl implements Async {
    private final ExecutorService executorService;

    public AsyncImpl(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public <R> Promise<R> run(SupplierWithError<R> callable) {
        var promise = new RunnablePromise<>(callable);
        this.executorService.execute(promise);
        return promise;
    }

    public Promise<Unit> run(RunnableWithError runnable) {
        var promise = new RunnablePromise<>(() -> {
            runnable.run();
            return Unit.unit;
        });
        this.executorService.execute(promise);
        return promise;
    }

    @Override
    public void close() {
        this.executorService.shutdownNow();
    }
}
