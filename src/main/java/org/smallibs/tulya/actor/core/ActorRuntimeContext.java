package org.smallibs.tulya.actor.core;

import org.smallibs.tulya.actor.core.impl.ActorImpl;
import org.smallibs.tulya.actor.core.impl.ActorRuntimeContextImpl;

import java.util.Optional;

public interface ActorRuntimeContext {

    void registerCurrent(ActorImpl<?> actor);

    Optional<ActorImpl<?>> getCurrentActor();

    void unregisterCurrent();

    final class Companion {
        static ActorRuntimeContext build() {
            return new ActorRuntimeContextImpl();
        }
    }

}
