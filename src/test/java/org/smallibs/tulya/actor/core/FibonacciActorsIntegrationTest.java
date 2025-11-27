package org.smallibs.tulya.actor.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.smallibs.tulya.async.Promise;
import org.smallibs.tulya.async.Solvable;
import org.smallibs.tulya.standard.Try;

import java.time.Duration;

import static org.smallibs.tulya.actor.core.ActorAddress.Companion.address;
import static org.smallibs.tulya.actor.core.FibonacciActorsIntegrationTest.Fibonacci.Companion.fibonacci;

public class FibonacciActorsIntegrationTest {

    private final int fibonacciLimit = 19;

    @Test
    void shouldComputeDirectFibonacci() throws Throwable {
        // Given
        try (var coordinator = ActorCoordinator.Companion.build()) {
            var fibonacci = coordinator.register(address("fibonacci"), DirectComputation::new).orElseThrow();

            // When
            var result = fibonacci.ask(fibonacci(fibonacciLimit));

            // Then
            Assertions.assertEquals(4181, result.await());
        }
    }

    @Test
    void shouldComputeMixedFibonacci() throws Throwable {
        // Given
        try (var coordinator = ActorCoordinator.Companion.build()) {
            var fibonacci = coordinator.register(address("fibonacci"), MixedComputation::new).orElseThrow();

            // When
            var result = fibonacci.ask(fibonacci(fibonacciLimit));

            // Then
            Assertions.assertEquals(4181, result.await());
        }
    }

    @Test
    void shouldComputeIndirectFibonacci() throws Throwable {
        // Given
        try (var coordinator = ActorCoordinator.Companion.build()) {
            var fibonacci = coordinator.register(address("fibonacci"), IndirectComputation::new).orElseThrow();

            // When
            var result = fibonacci.ask(fibonacci(fibonacciLimit));

            // Then
            Assertions.assertEquals(4181, result.await());
        }
    }

    // Private section

    record IndirectComputation(ActorReference<Fibonacci> self) implements Behavior<Fibonacci> {
        @Override
        public void ask(Fibonacci message) {
            if (message.value < 2) {
                message.response().success(message.value());
            } else {
                self().ask(fibonacci(message.value() - 1))
                        .flatMap(i1 ->
                                self().ask(fibonacci(message.value() - 2)).map(i2 -> i1 + i2)
                        )
                        .onComplete(message.response()::solve);
            }
        }
    }

    record MixedComputation(ActorReference<Fibonacci> self) implements Behavior<Fibonacci> {
        @Override
        public void ask(Fibonacci message) {
            if (message.value < 2) {
                message.response().success(message.value());
            } else {
                self().ask(fibonacci(message.value() - 1))
                        .flatMap(i1 -> Promise.handle(() ->
                                i1 + self().ask(fibonacci(message.value() - 2)).await()
                        ))
                        .onComplete(e -> message.response().solve(e));
            }
        }
    }

    record DirectComputation(ActorReference<Fibonacci> self) implements Behavior<Fibonacci> {
        @Override
        public void ask(Fibonacci message) {

            if (message.value() < 2) {
                message.response().success(message.value());
            } else {
                var result = Try.handle(() -> {
                    var minus1 = self().ask(fibonacci(message.value() - 1));
                    var minus2 = self().ask(fibonacci(message.value() - 2));

                    self().delay(Duration.ofMillis(5));

                    return minus1.await() + minus2.await();
                });

                message.response().solve(result);
            }
        }
    }

    record Fibonacci(int value, Solvable<Integer> response) {
        final public static class Companion {
            static BehaviorCall<Fibonacci, Integer> fibonacci(int value) {
                return solvable -> new Fibonacci(value, solvable);
            }
        }
    }
}
