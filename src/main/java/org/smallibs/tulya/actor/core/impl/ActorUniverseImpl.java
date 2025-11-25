package org.smallibs.tulya.actor.core.impl;

import org.smallibs.tulya.actor.core.Actor;
import org.smallibs.tulya.actor.core.ActorAddress;
import org.smallibs.tulya.actor.core.ActorUniverse;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ActorUniverseImpl implements ActorUniverse {

    private final Map<ActorAddress, Actor<?>> actors;

    public ActorUniverseImpl() {
        this.actors = new ConcurrentHashMap<>();
    }

    @Override
    public boolean register(ActorAddress address, Actor<?> actor) {
        if (actors.containsKey(address)) {
            return false;
        } else {
            actors.put(address, actor);
            return true;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Protocol> Optional<Actor<Protocol>> retrieve(ActorAddress address) {
        return Optional.ofNullable((Actor<Protocol>) actors.get(address));
    }

    @Override
    public boolean unregister(ActorAddress address) {
        return actors.remove(address) != null;
    }
}
