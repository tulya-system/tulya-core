package org.smallibs.tulya.actor.core;

import org.smallibs.tulya.actor.core.impl.ActorUniverseImpl;
import org.smallibs.tulya.standard.Try;
import org.smallibs.tulya.standard.Unit;

import java.util.List;
import java.util.Optional;

public interface ActorUniverse {

    Try<Unit> store(ActorAddress address, Actor<?> actor);

    <Protocol> Optional<Actor<Protocol>> retrieve(ActorAddress address);

    List<ActorAddress> remove(ActorAddress address);

    final class Companion {
        static ActorUniverse build() {
            return new ActorUniverseImpl();
        }
    }

}
