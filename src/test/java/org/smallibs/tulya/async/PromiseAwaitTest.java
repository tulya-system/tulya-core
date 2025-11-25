package org.smallibs.tulya.async;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.smallibs.tulya.async.impl.SolvablePromise;
import org.smallibs.tulya.standard.Try;
import org.smallibs.tulya.standard.Unit;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static java.time.Duration.ofMinutes;
import static java.time.Duration.ofSeconds;

public class PromiseAwaitTest {

    @Test
    public void shouldAwaitFor_1_000_000_Tasks() throws Throwable {
        // Given
        var numberOfTasks = 1_000_000;
        var executor = Execution.ofVirtual();
        var runningTasks = new AtomicInteger(numberOfTasks);
        var barrier = new SolvablePromise<Integer>();

        // When
        var promises = IntStream.range(0, numberOfTasks)
                .mapToObj(__ ->
                        executor.async(() -> {
                            barrier.await();
                            runningTasks.decrementAndGet();
                        })
                ).toArray(Promise[]::new);

        barrier.solve(Try.success(1));

        Promises.join(promises).await();

        // Then
        Assertions.assertEquals(0, runningTasks.get());
    }

    @Test
    public void shouldSleepAndAwaitFor_1_000_000_Tasks() throws Throwable {
        // Given
        var numberOfTasks = 1_000_000;
        var executor = Execution.ofVirtual();
        var runningTasks = new AtomicInteger(numberOfTasks);

        // When
        var promises = IntStream.range(0, numberOfTasks)
                .mapToObj(__ ->
                        executor.async(() -> {
                            Thread.sleep(1_000);
                            runningTasks.decrementAndGet();
                        })
                ).toArray(Promise[]::new);

        Promises.join(promises).await();

        // Then
        Assertions.assertEquals(0, runningTasks.get());
    }

    @Test
    public void shouldPerformAwait() throws Throwable {
        // Given
        var executor = Execution.ofVirtual();
        // Saturation ...
        var numberOfTasks = 1_000_000;
        var neverResolved = new SolvablePromise<Integer>();
        var runningTasks = new AtomicInteger(numberOfTasks);
        var ignored = IntStream.range(0, numberOfTasks).mapToObj(__ ->
                executor.async(() -> {
                    runningTasks.decrementAndGet();
                    neverResolved.await();
                })
        ).toList();

        Awaitility.await().atMost(ofMinutes(1)).until(() -> runningTasks.get() == 0);

        // When
        var barrier = new SolvablePromise<Integer>();

        var promise = executor.async(() -> {
            barrier.await();
        });

        barrier.solve(Try.success(1));

        // Then
        Assertions.assertEquals(Unit.unit, promise.await());
    }

    @Test
    public void shouldAwaitForPromiseResponse() {
        // Given
        var executor = Execution.ofVirtual();

        //When
        var aLongAddition = Try.handle(() -> {
            var firstInteger = integer(executor, 1_000);
            var secondInteger = integer(executor, 1_000);

            return firstInteger.await() + secondInteger.await();
        });

        // Then
        Assertions.assertEquals(aLongAddition, Try.success(2_000));
    }

    //
    // Private section
    //

    private static Promise<Integer> integer(Execution runtime, int value) {
        return runtime.async(() -> {
            Thread.sleep(value);
            return value;
        });
    }

}
