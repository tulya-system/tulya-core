package org.smallibs.tulya.async.impl;

import org.smallibs.tulya.async.Promise;
import org.smallibs.tulya.lang.SupplierWithError;
import org.smallibs.tulya.standard.Try;

import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public final class RunnablePromise<T> implements Runnable, Promise<T> {

    private final SupplierWithError<T> callable;
    private final WeakReference<Thread> currentThread;
    private final SolvablePromise<T> solvablePromise;

    public RunnablePromise(SupplierWithError<T> callable) {
        this.callable = callable;
        this.currentThread = new WeakReference<>(null);
        this.solvablePromise = new SolvablePromise<>(future(this.currentThread));
    }

    //
    // Runnable implementation section
    //

    @Override
    public void run() {
        try {
            this.currentThread.refersTo(Thread.currentThread());
            this.solvablePromise.solve(Try.success(this.callable.get()));
        } catch (final Throwable exception) {
            this.solvablePromise.solve(Try.failure(exception));
        } finally {
            this.currentThread.clear();
        }
    }

    //
    // Promise implementation section
    //


    @Override
    public boolean isDone() {
        return solvablePromise.isDone();
    }

    @Override
    public T await() throws Throwable {
        return solvablePromise.await();
    }

    @Override
    public T await(Duration duration) throws Throwable {
        return solvablePromise.await(duration);
    }

    @Override
    public <R> Promise<R> map(Function<? super T, ? extends R> mapper) {
        return this.solvablePromise.map(mapper);
    }

    @Override
    public <R> Promise<R> flatMap(Function<? super T, ? extends Promise<R>> flatMapper) {
        return this.solvablePromise.flatMap(flatMapper);
    }

    @Override
    public Promise<T> onComplete(Consumer<? super Try<T>> fn) {
        return this.solvablePromise.onComplete(fn);
    }

    // Private section

    private static <T> Function<Runnable, SolvableFuture<T>> future(WeakReference<Thread> currentThread) {
        return run -> new SolvableFuture<>(run) {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                if (mayInterruptIfRunning) {
                    Optional.ofNullable(currentThread.get()).ifPresent(Thread::interrupt);
                }

                return super.cancel(mayInterruptIfRunning);
            }
        };
    }
}
