package org.smallibs.tulya.async;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.smallibs.tulya.async.impl.SolvablePromise;
import org.smallibs.tulya.standard.Try;
import org.smallibs.tulya.standard.Unit;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static java.time.Duration.ofMinutes;

public class PromiseAwaitTest {

    @Test
    public void shouldAwaitFor_1_000_000_Tasks() throws Exception {
        // Given
        try (var async = Async.Companion.ofVirtual()) {
            var numberOfTasks = 1_000_000;
            var runningTasks = new AtomicInteger(0);
            var barrier = new SolvablePromise<Unit>();

            // When
            var promises = IntStream.range(0, numberOfTasks).mapToObj(__ ->
                    async.run(() -> {
                        runningTasks.incrementAndGet();
                        barrier.await();
                        runningTasks.decrementAndGet();
                    })
            ).toArray(Promise[]::new);

            // Then - virtual thread saturation waiting on a barrier
            Awaitility.await().until(() -> runningTasks.get() == numberOfTasks);
            barrier.success(Unit.unit);
            Awaitility.await().atMost(Duration.ofMinutes(1)).until(() -> runningTasks.get() == 0);
        }
    }

    @Test
    public void shouldSleepAndAwaitFor_1_000_000_Tasks() throws Throwable {
        // Given
        try (var async = Async.Companion.ofVirtual()) {
            var numberOfTasks = 1_000_000;
            var runningTasks = new AtomicInteger(numberOfTasks);

            // When
            var promises = IntStream.range(0, numberOfTasks)
                    .mapToObj(__ ->
                            async.run(() -> {
                                Thread.sleep(1_000);
                                runningTasks.decrementAndGet();
                            })
                    ).toArray(Promise[]::new);

            Promises.join(promises).await();

            // Then
            Assertions.assertEquals(0, runningTasks.get());
        }
    }

    @Test
    public void shouldPerformAwait() throws Throwable {
        // Given
        try (var executor = Async.Companion.ofVirtual()) {
            // Saturation ...
            var numberOfTasks = 1_000_000;
            var neverResolved = new SolvablePromise<Integer>();
            var runningTasks = new AtomicInteger(numberOfTasks);
            var ignored = IntStream.range(0, numberOfTasks).mapToObj(__ ->
                    executor.run(() -> {
                        runningTasks.decrementAndGet();
                        neverResolved.await();
                    })
            ).toList();

            Awaitility.await().atMost(ofMinutes(1)).until(() -> runningTasks.get() == 0);

            // When
            var barrier = new SolvablePromise<Unit>();

            var promise = executor.run(() -> {
                barrier.await();
            });

            barrier.solve(Try.success(Unit.unit));

            // Then
            Assertions.assertEquals(Unit.unit, promise.await());
        }
    }

    @Test
    public void shouldAwaitForPromiseResponse() throws Exception {
        // Given
        try (var executor = Async.Companion.ofVirtual()) {
            //When
            var aLongAddition = Try.handle(() -> {
                var firstInteger = integer(executor, 1_000);
                var secondInteger = integer(executor, 500);
                var thirdInteger = integer(executor, 1_000);

                return firstInteger.await() + secondInteger.await() + thirdInteger.await();
            });

            // Then
            Assertions.assertEquals(aLongAddition, Try.success(2_500));
        }
    }

    //
    // Private section
    //

    private static Promise<Integer> integer(Async runtime, int value) {
        return runtime.run(() -> {
            Thread.sleep(value);
            return value;
        });
    }

}
