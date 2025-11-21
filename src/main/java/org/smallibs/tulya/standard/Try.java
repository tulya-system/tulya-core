package org.smallibs.tulya.standard;

import org.smallibs.tulya.async.Promise;
import org.smallibs.tulya.async.impl.SolvedPromise;
import org.smallibs.tulya.lang.RunnableWithError;
import org.smallibs.tulya.lang.SupplierWithError;

import java.util.function.Consumer;
import java.util.function.Function;

public sealed interface Try<T> {

    static <R> Try<R> success(R value) {
        return new Success<>(value);
    }

    static <R> Try<R> failure(Throwable throwable) {
        return new Failure<>(throwable);
    }

    static <R> Try<R> handle(SupplierWithError<R> supplier) {
        try {
            return Try.success(supplier.get());
        } catch (Throwable e) {
            return Try.failure(e);
        }
    }

    default Promise<T> toPromise() {
        return new SolvedPromise<>(this);
    }

    static Try<Unit> handle(RunnableWithError callable) {
        try {
            callable.run();
            return Try.success(Unit.unit);
        } catch (Throwable e) {
            return Try.failure(e);
        }
    }

    default <R> Try<R> map(Function<? super T, ? extends R> map) {
        return switch (this) {
            case Success<T>(var v) -> success(map.apply(v));
            case Failure<T>(var e) -> failure(e);
        };
    }

    default <R> Try<R> flatMap(Function<? super T, ? extends Try<R>> flatMap) {
        return switch (this) {
            case Success<T>(var v) -> flatMap.apply(v);
            case Failure<T>(var e) -> failure(e);
        };
    }

    default <R> R fold(Function<? super T, ? extends R> onSuccess, Function<Throwable, ? extends R> onFailure) {
        return switch (this) {
            case Success<T>(var v) -> onSuccess.apply(v);
            case Failure<T>(var e) -> onFailure.apply(e);
        };
    }

    default Try<T> onSuccess(Consumer<T> value) {
        switch (this) {
            case Success<T>(var v) -> value.accept(v);
            case Failure<T>(var ignored) -> {
            }
        }
        return this;
    }

    default Try<T> onFailure(Consumer<Throwable> value) {
        switch (this) {
            case Failure<T>(var e) -> value.accept(e);
            case Success<T>(var ignored) -> {
            }
        }
        return this;
    }

    default boolean isSuccess() {
        return this.fold(__ -> true, __ -> false);
    }

    default boolean isFailure() {
        return this.fold(__ -> false, __ -> true);
    }

    default T orElseThrow() throws Throwable {
        return switch (this) {
            case Try.Failure<T>(var e) -> throw e;
            case Try.Success<T>(var v) -> v;
        };
    }

    record Success<T>(T value) implements Try<T> {
    }

    record Failure<T>(Throwable throwable) implements Try<T> {
    }

}
