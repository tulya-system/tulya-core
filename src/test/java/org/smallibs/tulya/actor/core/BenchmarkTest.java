package org.smallibs.tulya.actor.core;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.smallibs.tulya.actor.core.impl.BehaviorImpl;
import org.smallibs.tulya.async.Promise;
import org.smallibs.tulya.async.Promises;
import org.smallibs.tulya.async.Solvable;
import org.smallibs.tulya.standard.Unit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.smallibs.tulya.actor.core.ActorAddress.Companion.address;

public class BenchmarkTest {

    @Disabled
    @ParameterizedTest
    @MethodSource("benches")
    void shouldBenchmark(Supplier<ExecutorService> supplier) throws Throwable {
        var totalRPMS = 0;
        var maxSize = 10;

        for (var size = 1; size <= maxSize; size++) {
            try (var coordinator = ActorCoordinator.Companion.build(supplier.get())) {
                var nbActors = 100_000 * size;

                var actors = IntStream.range(0, nbActors).mapToObj(i -> {
                    try {
                        return coordinator.<Request>register(
                                address("test" + i),
                                self -> new BehaviorImpl<>(self, r -> r.handler.success(Unit.unit))
                        ).orElseThrow();
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }).toList();

                for (int j = 1; j <= maxSize; j++) {
                    var nbMessages = 1_000_000 * j;

                    var t0 = System.currentTimeMillis();

                    var responses = IntStream.range(0, nbMessages).mapToObj(i ->
                            actors.get(i % nbActors).ask(Request::new)
                    ).toList();

                    Promises.forall(responses.toArray(Promise[]::new)).await();

                    var duration = (System.currentTimeMillis() - t0);

                    System.out.printf("%d %d %d%n", nbActors, nbMessages, nbMessages / duration);

                    totalRPMS += nbMessages / duration;
                }
            }
        }

        System.out.println("----------------------[ " + totalRPMS / (maxSize*maxSize) + "]");
    }

    public static Stream<Arguments> benches() {
        return Stream.of(
                Arguments.of((Supplier<ExecutorService>) Executors::newVirtualThreadPerTaskExecutor),
                Arguments.of((Supplier<ExecutorService>) () -> Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()))
        );
    }

    // Private section

    record Request(Solvable<Unit> handler) {
    }

}
