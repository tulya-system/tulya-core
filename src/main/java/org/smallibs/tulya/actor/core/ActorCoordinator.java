package org.smallibs.tulya.actor.core;

import org.smallibs.tulya.actor.core.impl.ActorCoordinatorImpl;
import org.smallibs.tulya.actor.core.impl.ActorRuntimeContextImpl;
import org.smallibs.tulya.actor.core.impl.ActorRuntimeImpl;
import org.smallibs.tulya.actor.core.impl.ActorUniverseImpl;
import org.smallibs.tulya.standard.Try;

import java.util.concurrent.Executors;

public interface ActorCoordinator {

    static ActorCoordinator create() {
        return new ActorCoordinatorImpl(
                new ActorUniverseImpl(),
                new ActorRuntimeImpl(Executors.newVirtualThreadPerTaskExecutor()),
                new ActorRuntimeContextImpl()
        );
    }

    <Protocol> Try<ActorReference<Protocol>> register(ActorAddress parent, String name, BehaviorBuilder<Protocol> actor);

    <Protocol> boolean unregister(ActorReference<Protocol> reference);
}
