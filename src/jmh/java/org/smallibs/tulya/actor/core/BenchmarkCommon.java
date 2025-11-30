package org.smallibs.tulya.actor.core;

import org.openjdk.jmh.infra.Blackhole;
import org.smallibs.tulya.actor.core.impl.BehaviorImpl;
import org.smallibs.tulya.async.Promise;
import org.smallibs.tulya.async.Promises;
import org.smallibs.tulya.async.Solvable;
import org.smallibs.tulya.standard.Unit;

import java.util.List;
import java.util.stream.IntStream;

import static org.smallibs.tulya.actor.core.ActorAddress.Companion.address;

public class BenchmarkCommon {

    static final int nbActors = 1_000_000;
    static final int nbMessages = 1_000_000;

    static List<ActorReference<Solvable<Unit>>> createActors(ActorCoordinator coordinator) {
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

    void benchmark(List<ActorReference<Solvable<Unit>>> actors, Blackhole blackhole) throws Throwable {
        var responses = IntStream.range(0, nbMessages).mapToObj(i ->
                actors.get(i % actors.size()).<Unit>ask(s -> s)
        ).toArray(Promise[]::new);

        Promises.forall(responses).await();

        blackhole.consume(responses);
    }
}
