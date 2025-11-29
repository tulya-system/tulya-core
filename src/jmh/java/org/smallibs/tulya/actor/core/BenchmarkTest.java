package org.smallibs.tulya.actor.core;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Warmup;
import org.smallibs.tulya.actor.core.impl.BehaviorImpl;
import org.smallibs.tulya.async.Promise;
import org.smallibs.tulya.async.Promises;
import org.smallibs.tulya.async.Solvable;
import org.smallibs.tulya.standard.Unit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.smallibs.tulya.actor.core.ActorAddress.Companion.address;

public class BenchmarkTest {

    @Benchmark
    @Warmup(iterations = 5, time = 2000, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 10, time = 2000, timeUnit = TimeUnit.MILLISECONDS)
    public void benchmarkVirtual() throws Throwable {
        benchmark(Executors.newVirtualThreadPerTaskExecutor());
    }

    @Benchmark
    @Warmup(iterations = 5, time = 2000, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 10, time = 2000, timeUnit = TimeUnit.MILLISECONDS)
    public void benchmarkPlatform() throws Throwable {
        benchmark(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
    }

    // Private section

    void benchmark(ExecutorService service) throws Throwable {
        try (var coordinator = ActorCoordinator.Companion.build(service)) {
            var nbActors = 100_000;
            var nbMessages = 1_000_000;

            var actors = IntStream.range(0, nbActors).mapToObj(i -> {
                try {
                    return coordinator.<Solvable<Unit>>register(
                            address("test" + i),
                            self -> new BehaviorImpl<>(self, r -> r.success(Unit.unit))
                    ).orElseThrow();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }).toList();

            var responses = IntStream.range(0, nbMessages).mapToObj(i ->
                    actors.get(i % nbActors).<Unit>ask(s -> s)
            ).toList();

            Promises.forall(responses.toArray(Promise[]::new)).await();
        }
    }


}
