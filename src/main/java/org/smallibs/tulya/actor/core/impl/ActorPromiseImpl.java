package org.smallibs.tulya.actor.core.impl;

import org.smallibs.tulya.actor.core.ActorRuntime;
import org.smallibs.tulya.actor.core.Extended.Deferred;
import org.smallibs.tulya.async.Promise;
import org.smallibs.tulya.async.impl.SolvablePromise;
import org.smallibs.tulya.standard.Try;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;

public class ActorPromiseImpl<T> implements Promise<T> {

    private final ActorRuntime runtime;
    private final SolvablePromise<T> promise;

    public ActorPromiseImpl(ActorRuntime runtime) {
        this(runtime, new SolvablePromise<>());
    }

    ActorPromiseImpl(ActorRuntime runtime, SolvablePromise<T> promise) {
        this.runtime = runtime;
        this.promise = promise;
    }

    @Override
    public T await() throws Throwable {
        var mayBeActor = runtime.context().getCurrentActor();

        if (mayBeActor.isPresent()) {
            var actor = mayBeActor.get();

            try {
                actor.release();
                return promise.await();
            } finally {
                actor.acquire();
            }
        } else {
            return promise.await();
        }
    }

    @Override
    public T await(Duration duration) throws Throwable {
        var mayBeActor = runtime.context().getCurrentActor();

        if (mayBeActor.isPresent()) {
            var actor = mayBeActor.get();

            try {
                actor.release(duration);
                return promise.await(duration);
            } finally {
                actor.acquire(duration);
            }
        } else {
            return promise.await(duration);
        }

    }

    @Override
    public boolean isDone() {
        return promise.isDone();
    }

    @Override
    public <R> Promise<R> map(Function<? super T, ? extends R> fn) {
        var response = new ActorPromiseImpl<R>(runtime);

        runtime.context().getCurrentActor().ifPresentOrElse(
                actor -> this.promise
                        .onSuccess(e -> actor.tell(new Deferred<>(() -> response.promise.solve(Try.success(fn.apply(e))))))
                        .onFailure(e -> response.promise.solve(Try.failure(e))),
                () -> this.promise
                        .onSuccess(e -> response.promise.solve(Try.success(fn.apply(e))))
                        .onFailure(e -> response.promise.solve(Try.failure(e)))
        );

        return response;
    }

    @Override
    public <R> Promise<R> flatMap(Function<? super T, ? extends Promise<R>> fn) {
        var response = new ActorPromiseImpl<R>(runtime);

        runtime.context().getCurrentActor().ifPresentOrElse(
                actor -> this.promise
                        .onSuccess(e -> actor.tell(new Deferred<>(() -> fn.apply(e).onComplete(response.promise::solve))))
                        .onFailure(e -> response.promise.solve(Try.failure(e))),
                () -> this.promise
                        .onSuccess(e -> fn.apply(e).onComplete(response.promise::solve))
                        .onFailure(e -> response.promise.solve(Try.failure(e)))
        );

        return response;
    }

    @Override
    public Promise<T> onComplete(Consumer<? super Try<T>> fn) {
        runtime.context().getCurrentActor().ifPresentOrElse(
                actor -> this.promise.onComplete(e -> actor.tell(new Deferred<>(() -> fn.accept(e)))),
                () -> this.promise.onComplete(fn)
        );

        return this;
    }
}
