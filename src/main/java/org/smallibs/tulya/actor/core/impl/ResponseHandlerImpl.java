package org.smallibs.tulya.actor.core.impl;

import org.smallibs.tulya.actor.core.ResponseHandler;
import org.smallibs.tulya.async.Promise;
import org.smallibs.tulya.async.Solvable;
import org.smallibs.tulya.standard.Try;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;

public record ResponseHandlerImpl<T>(Promise<T> promise, Solvable<T> solvable) implements ResponseHandler<T> {
    @Override
    public T await() throws Throwable {
        return promise.await();
    }

    @Override
    public T await(Duration duration) throws Throwable {
        return promise.await(duration);
    }

    @Override
    public boolean isDone() {
        return promise.isDone();
    }

    @Override
    public <R> Promise<R> map(Function<? super T, ? extends R> mapper) {
        return promise.map(mapper);
    }

    @Override
    public <R> Promise<R> flatMap(Function<? super T, ? extends Promise<R>> flatMapper) {
        return promise.flatMap(flatMapper);
    }

    @Override
    public Promise<T> onComplete(Consumer<? super Try<T>> fn) {
        return promise.onComplete(fn);
    }
}
