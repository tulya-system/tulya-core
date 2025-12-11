package org.smallibs.tulya.async;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FibonacciIntegrationTest {

    private final int fibonacciLimit = 25;
    private final int expectedResult = 75025;

    @Test
    void shouldComputeDirectFibonacci() throws Throwable {
        // Given
        try (var async = Async.Companion.ofVirtual()) {
            // When
            var result = async.run(() -> fibonacci(async, fibonacciLimit));

            // Then
            Assertions.assertEquals(expectedResult, result.await());
        }
    }

    int fibonacci(Async async, int value) throws Throwable {
        if (value < 2) {
            return value;
        } else {
            var i = async.run(() -> fibonacci(async, value - 1));
            var j = async.run(() -> fibonacci(async, value - 2));
            return i.await() + j.await();
        }
    }
}
