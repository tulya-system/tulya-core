package org.smallibs.tulya.actor.core;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.smallibs.tulya.async.Solvable;
import org.smallibs.tulya.standard.Unit;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BenchmarkVirtualTest extends BenchmarkCommon {

    @State(Scope.Thread)
    public static class BenchState {
        private final ActorCoordinator coordinator;
        private final List<ActorReference<Solvable<Unit>>> actors;

        public BenchState() {
            this.coordinator = ActorCoordinator.Companion.build(Executors.newVirtualThreadPerTaskExecutor());
            this.actors = createActors(coordinator);
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Warmup(iterations = 5, time = 2000, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 10, time = 2000, timeUnit = TimeUnit.MILLISECONDS)
    public void benchmark(BenchState state, Blackhole blackhole) throws Throwable {
        benchmark(state.actors, blackhole);
    }

}
