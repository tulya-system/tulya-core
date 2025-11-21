package org.smallibs.tulya.async.impl;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.smallibs.tulya.standard.Try;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

class SolvableFutureTest {

    @Test
    void shouldNotBeCancelled() {
        // Given
        var future = new SolvableFuture<Integer>();

        // When
        var cancelled = future.isCancelled();

        // Then
        Assertions.assertFalse(cancelled);
    }

    @Test
    void shouldNotBeDone() {
        // Given
        var future = new SolvableFuture<Integer>();

        // When
        var done = future.isDone();

        // Then
        Assertions.assertFalse(done);
    }

    @Test
    void shouldCancel() {
        // Given
        var future = new SolvableFuture<Integer>();

        // When
        var cancelled = future.cancel(false);

        // Then
        Assertions.assertTrue(cancelled);
    }

    @Test
    void shouldBeCancelled() {
        // Given
        var future = new SolvableFuture<Integer>();

        future.cancel(false);

        // When
        var cancelled = future.isCancelled();

        // Then
        Assertions.assertTrue(cancelled);
    }

    @Test
    void shouldSolve() {
        // Given
        var future = new SolvableFuture<Integer>();

        // When
        var done = future.solve(Try.success(42));

        // Then
        Assertions.assertTrue(done);
    }

    @Test
    void shouldBeSolved() {
        // Given
        var future = new SolvableFuture<Integer>();

        future.solve(Try.success(42));

        // When
        var done = future.isDone();

        // Then
        Assertions.assertTrue(done);
    }

    @Test
    void shouldBeNotifiedOnCancel() {
        // Given
        var cancelled = new AtomicBoolean(false);
        var future = new SolvableFuture<Integer>(() -> cancelled.set(true));

        // When
        future.cancel(false);

        // Then
        Assertions.assertTrue(cancelled.get());
    }

    @Test
    void shouldBeNotifiedOnSolved() {
        // Given
        var solved = new AtomicBoolean(false);
        var future = new SolvableFuture<Integer>(() -> solved.set(true));

        // When
        future.solve(Try.success(42));

        // Then
        Assertions.assertTrue(solved.get());
    }

    @Test
    void shouldBeSyncNotifiedOnSolved() {
        // Given
        try (var executor = Executors.newSingleThreadExecutor()) {
            var solved = new AtomicBoolean(false);
            var future = new SolvableFuture<Integer>();

            // When
            executor.submit(() -> {
                try {
                    future.get();
                    solved.set(true);
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });

            future.solve(Try.success(42));

            // Then
            Awaitility.await().untilTrue(solved);
        }
    }

    @Test
    void shouldGetData() {
        // Given
        try (var executor = Executors.newSingleThreadExecutor()) {
            var solved = new AtomicInteger(0);
            var future = new SolvableFuture<Integer>();

            // When
            executor.submit(() -> {
                try {
                    solved.set(future.get());
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            });

            future.solve(Try.success(42));

            // Then
            Awaitility.await().untilAsserted(() -> Assertions.assertEquals(42, solved.get()));
        }
    }
}

