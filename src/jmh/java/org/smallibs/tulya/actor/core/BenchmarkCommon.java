package org.smallibs.tulya.actor.core;

import org.openjdk.jmh.infra.Blackhole;
import org.smallibs.tulya.actor.core.impl.BehaviorImpl;
import org.smallibs.tulya.async.Promise;
import org.smallibs.tulya.async.Promises;
import org.smallibs.tulya.async.Solvable;
import org.smallibs.tulya.standard.Unit;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static org.smallibs.tulya.actor.core.ActorAddress.Companion.address;

public abstract class BenchmarkCommon {

    public abstract static class BenchStateCommon {
        private final Supplier<ExecutorService> service;

        private ActorCoordinator coordinator;
        private List<ActorReference<Solvable<Unit>>> actors;

        public BenchStateCommon(Supplier<ExecutorService> service) {
            this.service = service;
        }

        public void doSetup() {
            this.coordinator = ActorCoordinator.Companion.build(service.get());
            this.actors = createActors(this.coordinator, this.nbActors());
        }

        abstract protected int nbActors();

        abstract protected int nbMessages();

        public void doTearDown() throws IOException {
            this.coordinator.close();
        }

    }

    static List<ActorReference<Solvable<Unit>>> createActors(ActorCoordinator coordinator, int nbActors) {
        return IntStream.range(0, nbActors).mapToObj(i -> {
            try {
                return coordinator.<Solvable<Unit>>register(
                        address("test" + i),
                        self -> new BehaviorImpl<>(self, r -> r.success(Unit.unit))
                ).orElseThrow();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }).toList();
    }

    @SuppressWarnings("unchecked")
    void benchmark(BenchStateCommon common, Blackhole blackhole) throws Throwable {
        var responses = IntStream.range(0, common.nbMessages()).mapToObj(i ->
                common.actors.get(i % common.actors.size()).<Unit>ask(s -> s)
        ).toArray(Promise[]::new);

        Promises.forall(responses).await();

        blackhole.consume(responses);
    }
}
