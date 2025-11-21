package org.smallibs.tulya.async.impl;

import org.smallibs.tulya.async.Solvable;
import org.smallibs.tulya.standard.Try;
import org.smallibs.tulya.standard.Try.Failure;
import org.smallibs.tulya.standard.Try.Success;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SolvableFuture<R> implements Solvable<R>, Future<R> {

    private final Runnable onSolved;
    private final CountDownLatch barrier;

    private Status<R> status;

    public SolvableFuture() {
        this(() -> {
        });
    }

    public SolvableFuture(Runnable onSolved) {
        this.onSolved = onSolved;
        this.barrier = new CountDownLatch(1);
        this.status = new Status.Waiting<>();
    }

    //
    // Solvable implementation section
    //

    @Override
    public boolean solve(Try<R> value) {
        return this.solve(new Status.Done<>(value));
    }

    //
    // Runnable implementation section
    //

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return this.solve(new Status.Cancelled<>());
    }

    @Override
    public synchronized boolean isCancelled() {
        return status.isCancelled();
    }

    @Override
    public synchronized boolean isDone() {
        return status.isDone() || status.isCancelled();
    }

    @Override
    public R get() throws InterruptedException, ExecutionException {
        if (!this.isDone()) {
            barrier.await();
        }

        return this.unsafeGet(); // Interrupted, cancelled or done
    }

    @Override
    public R get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (!this.isDone()) {
            if (!barrier.await(timeout, unit)) {
                throw new TimeoutException();
            }
        }

        return this.unsafeGet(); // Interrupted, cancelled or done
    }

    //
    // Package protected section
    //

    Else<Runnable> ifWaiting(Runnable onWaiting) {
        synchronized (this) {
            if (this.status.isWaiting()) {
                onWaiting.run();
                return new Else.Nothing<>();
            }
        }

        return new Else.Pending<>();
    }

    //
    // Private section
    //

    private R unsafeGet() throws ExecutionException {
        return switch (status.unsafeGetResult()) {
            case Success<R>(var v) -> v;
            case Failure<R>(var e) -> {
                if (e instanceof ExecutionException exception) {
                    throw exception;
                } else {
                    throw new ExecutionException(e);
                }
            }
        };
    }

    private boolean solve(Status<R> newStatus) {
        synchronized (this) {
            if (this.isDone()) return false;

            this.status = newStatus;

            if (barrier != null) {
                barrier.countDown();
            }
        }

        onSolved.run();

        return true;
    }

    private sealed interface Status<R> {

        default boolean isWaiting() {
            return false;
        }

        default boolean isCancelled() {
            return false;
        }

        default boolean isDone() {
            return false;
        }

        default Try<R> unsafeGetResult() {
            return switch (this) {
                case Status.Waiting<R>() -> throw new IllegalStateException(); // Impossible case / Need D.T. here
                case Status.Cancelled<R>() -> Try.failure(new CancellationException());
                case Status.Done<R>(var v) -> v;
            };
        }

        record Waiting<R>() implements Status<R> {
            @Override
            public boolean isWaiting() {
                return true;
            }
        }

        record Cancelled<R>() implements Status<R> {
            @Override
            public boolean isCancelled() {
                return true;
            }
        }

        record Done<R>(Try<R> value) implements Status<R> {
            @Override
            public boolean isDone() {
                return true;
            }
        }
    }

    sealed interface Else<R> {

        default void orElse(Runnable runnable) {
            switch (this) {
                case Else.Pending<R>() -> runnable.run();
                case Else.Nothing<R>() -> {
                }
            }
        }

        record Pending<R>() implements Else<R> {
        }

        record Nothing<R>() implements Else<R> {
        }
    }

}
