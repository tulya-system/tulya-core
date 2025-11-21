package org.smallibs.tulya.adapters;

import org.smallibs.tulya.async.Promise;
import org.smallibs.tulya.async.impl.SolvablePromise;
import org.smallibs.tulya.standard.Try;

import java.util.concurrent.CompletableFuture;

public interface CompletableFutureAdapter {

    static <T> CompletableFuture<T> toCompletable(Promise<T> promise) {
        final CompletableFuture<T> future = new CompletableFuture<>();

        promise.onComplete(tTry ->
                tTry.onSuccess(future::complete).onFailure(future::completeExceptionally)
        );

        return future;
    }

    static <A> Promise<A> fromCompletable(CompletableFuture<A> future) {
        var promise = new SolvablePromise<A>();

        future.whenComplete((a, e) -> {
            if (e != null) {
                promise.solve(Try.failure(e));
            } else {
                promise.solve(Try.success(a));
            }
        });

        return promise;
    }

}
