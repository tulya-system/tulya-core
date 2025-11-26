package org.smallibs.tulya.actor.core;

import org.smallibs.tulya.actor.core.impl.ActorCoordinatorImpl;
import org.smallibs.tulya.standard.Try;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface ActorCoordinator {

    <Protocol> Try<ActorReference<Protocol>> register(ActorAddress address, BehaviorBuilder<Protocol> actor);

    void unregister(ActorAddress address);

    final class Companion {
        static ActorCoordinator build() {
            return build(
                    ActorUniverse.Companion.build(),
                    ActorRuntime.Companion.build(Executors.newVirtualThreadPerTaskExecutor()),
                    ActorRuntimeContext.Companion.build()
            );
        }

        static ActorCoordinator build(ExecutorService executor) {
            return build(
                    ActorUniverse.Companion.build(),
                    ActorRuntime.Companion.build(executor),
                    ActorRuntimeContext.Companion.build()
            );
        }

        static ActorCoordinator build(
                ActorUniverse universe,
                ActorRuntime runtime,
                ActorRuntimeContext runtimeContext
        ) {
            return new ActorCoordinatorImpl(universe, runtime, runtimeContext);
        }
    }
}
