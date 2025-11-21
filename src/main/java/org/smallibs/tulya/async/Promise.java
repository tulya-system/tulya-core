package org.smallibs.tulya.async;

import org.smallibs.tulya.lang.SupplierWithError;
import org.smallibs.tulya.standard.Try;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;

public interface Promise<T> {

    static <R> Promise<R> success(R result) {
        return Try.success(result).toPromise();
    }

    static <R> Promise<R> failure(Throwable result) {
        return Try.<R>failure(result).toPromise();
    }

    static <R> Promise<R> handle(SupplierWithError<R> supplier) {
        return Try.handle(supplier).toPromise();
    }

    T await() throws Throwable;

    T await(Duration duration) throws Throwable;

    boolean isDone();

    <R> Promise<R> map(Function<? super T, ? extends R> mapper);

    <R> Promise<R> flatMap(Function<? super T, ? extends Promise<R>> flatMapper);

    Promise<T> onComplete(Consumer<? super Try<T>> fn);

    default Promise<T> onSuccess(Consumer<? super T> fn) {
        return onComplete(t -> {
            switch (t) {
                case Try.Success<T> v -> fn.accept(v.value());
                default -> { /* Nothing */ }
            }
        });
    }

    default Promise<T> onFailure(Consumer<Throwable> fn) {
        return onComplete(t -> {
            switch (t) {
                case Try.Failure<T> v -> fn.accept(v.throwable());
                default -> { /* Nothing */ }
            }
        });
    }
}