package org.smallibs.tulya.async;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class CompletableFutureAwaitTest {

    @Test
    public void shouldAwaitWithFor_1_000_000_Tasks() {
        // Given
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var numberOfTasks = 1_000_000;
            var runningTasks = new AtomicInteger(0);
            var barrier = new CompletableFuture<Integer>();

            // When
            var futures = IntStream.range(0, numberOfTasks).mapToObj(__ ->
                    CompletableFuture.runAsync(() -> {
                        runningTasks.incrementAndGet();
                        barrier.join();
                        runningTasks.decrementAndGet();
                    }, executor)
            ).toArray(Future[]::new);

            // Then
            Awaitility.await().until(() -> runningTasks.get() == numberOfTasks);
            barrier.complete(1);
            Awaitility.await().atMost(Duration.ofMinutes(1)).until(() -> runningTasks.get() == 0);
        }
    }

}
