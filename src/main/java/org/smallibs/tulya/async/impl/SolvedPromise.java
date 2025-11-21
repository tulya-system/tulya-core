package org.smallibs.tulya.async.impl;

import org.smallibs.tulya.async.Promise;
import org.smallibs.tulya.standard.Try;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;

public class SolvedPromise<T> implements Promise<T> {

    private final Try<T> result;

    public SolvedPromise(Try<T> result) {
        this.result = result;
    }

    //
    // Promise implementation section
    //

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public T await() throws Throwable {
        return result.orElseThrow();
    }

    @Override
    public T await(Duration duration) throws Throwable {
        return result.orElseThrow();
    }

    @Override
    public <R> Promise<R> map(Function<? super T, ? extends R> mapper) {
        return new SolvedPromise<>(this.result.map(mapper));
    }

    @Override
    public <R> Promise<R> flatMap(Function<? super T, ? extends Promise<R>> flatMapper) {
        return switch (result) {
            case Try.Success<T>(var v) -> flatMapper.apply(v);
            case Try.Failure<T>(var e) -> new SolvedPromise<>(Try.failure(e));
        };
    }

    @Override
    public Promise<T> onComplete(Consumer<? super Try<T>> consumer) {
        consumer.accept(this.result);
        return this;
    }
}
