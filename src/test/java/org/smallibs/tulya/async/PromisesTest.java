package org.smallibs.tulya.async;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.smallibs.tulya.async.impl.SolvablePromise;
import org.smallibs.tulya.async.impl.SolvedPromise;
import org.smallibs.tulya.standard.Try;
import org.smallibs.tulya.standard.Unit;

import java.util.List;
import java.util.function.Function;

class PromisesTest {

    @Test
    void shouldJoinPromisesSuccessfully() throws Throwable {
        // Given
        var promise = Promises.join(
                new SolvedPromise<>(Try.success(Unit.unit)),
                new SolvedPromise<>(Try.success(Unit.unit))
        );

        // When
        var result = promise.await();

        // Then
        Assertions.assertEquals(Unit.unit, result);
    }

    @Test
    void shouldJoinPromisesAndFailFast() {
        // Given
        var promise = Promises.join(
                new SolvablePromise<>(),
                new SolvedPromise<>(Try.failure(new Exception())),
                new SolvablePromise<>()
        );

        // When
        // Then
        Assertions.assertThrows(Exception.class, promise::await);
    }

    @Test
    void shouldWaitForAllPromisesSuccessfully() throws Throwable {
        // Given
        var promise = Promises.forall(
                new SolvedPromise<>(Try.success(1)),
                new SolvedPromise<>(Try.success(2))
        );

        // When
        var result = promise.await();

        // Then
        Assertions.assertEquals(List.of(1, 2), result);
    }

    @Test
    void shouldWaitExistingPromiseSuccessfully() throws Throwable {
        // Given
        var promise = Promises.exists(
                new SolvedPromise<>(Try.failure(new Exception())),
                new SolvedPromise<>(Try.success(1))
        );

        // When
        var result = promise.await();

        // Then
        Assertions.assertEquals(1, result);
    }

}