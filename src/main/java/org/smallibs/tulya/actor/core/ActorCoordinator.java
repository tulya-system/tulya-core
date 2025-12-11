package org.smallibs.tulya.actor.core;

import org.smallibs.tulya.actor.core.impl.ActorCoordinatorImpl;
import org.smallibs.tulya.standard.Try;

import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface ActorCoordinator extends Closeable {

    <Protocol> Try<ActorReference<Protocol>> register(ActorAddress address, BehaviorBuilder<Protocol> actor);

    <Protocol> Optional<ActorReference<Protocol>> retrieve(ActorAddress address);

    void unregister(ActorAddress address);

    @Override
    void close() throws IOException;

    final class Companion {
        public static ActorCoordinator build() {
            return build(
                    ActorUniverse.Companion.build(),
                    ActorRuntime.Companion.build(ActorRuntimeContext.Companion.build(), Executors.newVirtualThreadPerTaskExecutor())
            );
        }

        public static ActorCoordinator build(ExecutorService executor) {
            return build(
                    ActorUniverse.Companion.build(),
                    ActorRuntime.Companion.build(ActorRuntimeContext.Companion.build(), executor)
            );
        }

        private static ActorCoordinator build(
                ActorUniverse universe,
                ActorRuntime runtime
        ) {
            return new ActorCoordinatorImpl(universe, runtime);
        }
    }
}
