package org.smallibs.tulya.async.impl;

import org.smallibs.tulya.async.Promise;
import org.smallibs.tulya.async.Solvable;
import org.smallibs.tulya.standard.Try;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

public class SolvablePromise<T> implements Solvable<T>, Promise<T> {

    private final SolvableFuture<T> future;
    private final List<Consumer<? super Try<T>>> consumers;

    public SolvablePromise() {
        this(SolvableFuture::new);
    }

    protected SolvablePromise(Function<Runnable, SolvableFuture<T>> futureBuilder) {
        this.future = futureBuilder.apply(this::onSolved);
        this.consumers = new ArrayList<>();
    }

    //
    // Solvable implementation section
    //

    @Override
    public boolean solve(Try<T> result) {
        return this.future.solve(result);
    }

    //
    // Promise implementation section
    //


    @Override
    public boolean isDone() {
        return this.future.isDone();
    }

    @Override
    public T await() throws Throwable {
        try {
            return future.get();
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Override
    public T await(Duration duration) throws Throwable {
        try {
            return future.get(duration.toMillis(), TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Override
    public <R> Promise<R> map(Function<? super T, ? extends R> mapper) {
        var promise = new SolvablePromise<R>();
        this.onComplete(t -> promise.solve(t.map(mapper)));
        return promise;
    }

    @Override
    public <R> Promise<R> flatMap(Function<? super T, ? extends Promise<R>> flatMapper) {
        var promise = new SolvablePromise<R>();
        this.onComplete(t -> t
                .onSuccess(s -> flatMapper.apply(s).onComplete(promise::solve))
                .onFailure(e -> promise.solve(Try.failure(e)))
        );
        return promise;
    }

    @Override
    public Promise<T> onComplete(Consumer<? super Try<T>> consumer) {
        if (!this.future.onWaiting(() -> this.consumers.add(consumer))) {
            consumer.accept(getResult());
        }

        return this;
    }

    // Private section

    private void onSolved() {
        var result = this.getResult();
        consumers.forEach(consumer -> consumer.accept(result));
        consumers.clear();
    }

    private Try<T> getResult() {
        try {
            return Try.success(future.get());
        } catch (ExecutionException e) {
            return Try.failure(e.getCause());
        } catch (InterruptedException e) {
            return Try.failure(e);
        }
    }

}
