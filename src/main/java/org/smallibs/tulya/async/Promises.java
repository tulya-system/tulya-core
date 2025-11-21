package org.smallibs.tulya.async;

import org.smallibs.tulya.async.impl.SolvablePromise;
import org.smallibs.tulya.standard.Try;
import org.smallibs.tulya.standard.Unit;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public interface Promises {

    // Return Unit from all successful promises or the first failure | Fail fast implementation
    static Promise<Unit> join(Promise<?>... promises) {
        var response = new SolvablePromise<Unit>();
        var collected = new AtomicInteger(promises.length);

        for (var promise : promises) {
            promise.onFailure(e -> response.solve(Try.failure(e)))
                    .onSuccess(e -> {
                        if (response.isDone()) {
                            return;
                        }

                        if (collected.decrementAndGet() == 0) {
                            response.solve(Try.success(Unit.unit));
                        }
                    });
        }

        return response;
    }

    // Return result from all successful promises or the first failure | Fail fast implementation
    @SafeVarargs
    static <R> Promise<List<R>> forall(Promise<R>... promises) {
        var response = new SolvablePromise<List<R>>();
        var collected = new AtomicInteger(promises.length);
        var results = new ConcurrentLinkedQueue<R>();

        for (var promise : promises) {
            promise.onFailure(e -> response.solve(Try.failure(e)))
                    .onSuccess(e -> {
                        if (response.isDone()) {
                            return;
                        }

                        results.add(e);
                        if (collected.decrementAndGet() == 0) {
                            response.solve(Try.success(results.stream().toList()));
                        }
                    });
        }

        return response;
    }

    // Return result from the first successful promise or failure if all fail | Succeed fast
    @SafeVarargs
    static <R> Promise<R> exists(Promise<R>... promises) {
        var response = new SolvablePromise<R>();
        var collected = new AtomicInteger(promises.length);

        for (var promise : promises) {
            promise.onSuccess(e -> response.solve(Try.success(e)))
                    .onFailure(e -> {
                        if (response.isDone()) {
                            return;
                        }

                        if (collected.decrementAndGet() == 0) {
                            response.solve(Try.failure(e));
                        }
                    });
        }

        return response;
    }

}
