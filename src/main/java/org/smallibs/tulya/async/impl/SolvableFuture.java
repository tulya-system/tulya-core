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

    // Solvable section

    @Override
    public boolean solve(Try<R> value) {
        return solve(new Status.Done<>(value));
    }

    // Runnable section

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return solve(new Status.Cancelled<>());
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
        if (!isDone()) {
            barrier.await();
        }

        return unsafeGet(); // Interrupted, cancelled or done
    }

    @Override
    public R get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (!isDone()) {
            if (!barrier.await(timeout, unit)) {
                throw new TimeoutException();
            }
        }

        return unsafeGet(); // Interrupted, Canceled or Done
    }

    // Package protected section

    boolean onWaiting(Runnable onWaiting) {
        synchronized (this) {
            if (status.isWaiting()) {
                onWaiting.run();
                return true;
            }
        }

        return false;
    }

    // Private section

    private R unsafeGet() throws ExecutionException {
        return switch (status.unsafeGet()) {
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
            if (isDone()) return false;

            status = newStatus;
            barrier.countDown();
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

        default Try<R> unsafeGet() {
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
}
