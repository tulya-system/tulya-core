package org.smallibs.tulya.actor.core;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BenchmarkPlatformTest extends BenchmarkCommon {

    @State(Scope.Thread)
    public static class BenchState extends BenchStateCommon {

        @Param({"10000", "20000", "30000", "40000", "50000", "60000", "70000", "80000", "90000", "100000"})
        protected int nbActors;
        @Param({"100000", "200000", "300000", "400000", "500000", "600000", "700000", "800000", "900000", "1000000"})
        protected int nbMessages;

        public BenchState() {
            super(() -> Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
        }

        @Override
        protected int nbActors() {
            return nbActors;
        }

        @Override
        protected int nbMessages() {
            return nbMessages;
        }

        @Setup(Level.Iteration)
        public void doSetup() {
            super.doSetup();
        }

        @TearDown(Level.Iteration)
        public void doTearDown() throws IOException {
            super.doTearDown();
        }
    }

    @Benchmark
    @Fork(value = 1, warmups = 0)
    @BenchmarkMode(Mode.Throughput)
    @Warmup(iterations = 2, time = 2000, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 4, time = 2000, timeUnit = TimeUnit.MILLISECONDS)
    public void benchmark(BenchState state, Blackhole blackhole) throws Throwable {
        super.benchmark(state, blackhole);
    }

}
