package org.smallibs.tulya.async.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.smallibs.tulya.standard.Try;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

class SolvablePromiseTest {

    @Test
    void shouldWaitForTheResult() throws Throwable {
        // Given
        var promise = new SolvablePromise<Integer>();

        // When
        promise.solve(Try.success(42));

        // Then
        Assertions.assertEquals(42, promise.await());
    }

    @Test
    void shouldWaitForTheResultWithTimeout() {
        // Given
        var promise = new SolvablePromise<Integer>();

        // When
        // Then
        Assertions.assertThrows(TimeoutException.class, () -> promise.await(Duration.ofMillis(50)));
    }

    @Test
    void shouldPerformMap() throws Throwable {
        // Given
        var promise = new SolvablePromise<Integer>();

        // When
        promise.solve(Try.success(42));

        // Then
        Assertions.assertEquals("42", promise.map(Object::toString).await());
    }

    @Test
    void shouldPerformFlatMap() throws Throwable {
        // Given
        var promise1 = new SolvablePromise<String>();
        var promise2 = new SolvablePromise<Integer>();

        // When
        promise1.solve(Try.success("21"));
        promise2.solve(Try.success(21));

        // Then
        Assertions.assertEquals(42, promise1.flatMap(s -> promise2.map(i -> Integer.parseInt(s) + i)).await());
    }

    @Test
    void shouldPerformOnSuccessAfterSolved() {
        // Given
        var executed = new AtomicBoolean(false);
        var promise = new SolvablePromise<String>();

        // When
        promise.solve(Try.success("21"));
        promise.onSuccess(s -> executed.set(true));

        // Then
        Assertions.assertTrue(executed.get());
    }

    @Test
    void shouldPerformOnSuccessBeforeSolved() {
        // Given
        var executed = new AtomicBoolean(false);
        var promise = new SolvablePromise<String>();

        // When
        promise.onSuccess(s -> executed.set(true));
        promise.solve(Try.success("21"));

        // Then
        Assertions.assertTrue(executed.get());
    }

    @Test
    void shouldPerformOnFailureAfterSolved() {
        // Given
        var executed = new AtomicBoolean(false);
        var promise = new SolvablePromise<String>();

        // When
        promise.solve(Try.failure(new Throwable()));
        promise.onFailure(s -> executed.set(true));

        // Then
        Assertions.assertTrue(executed.get());
    }

    @Test
    void shouldPerformOnFailureBeforeSolved() {
        // Given
        var executed = new AtomicBoolean(false);
        var promise = new SolvablePromise<String>();

        // When
        promise.onFailure(s -> executed.set(true));
        promise.solve(Try.failure(new Throwable()));

        // Then
        Assertions.assertTrue(executed.get());
    }
}