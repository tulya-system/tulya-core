package org.smallibs.tulya.actor.core;

import java.util.Optional;

public interface ActorUniverse {

    boolean register(ActorAddress address, Actor<?> actor);

    <Protocol> Optional<Actor<Protocol>> retrieve(ActorAddress address);

    boolean unregister(ActorAddress address);

}
