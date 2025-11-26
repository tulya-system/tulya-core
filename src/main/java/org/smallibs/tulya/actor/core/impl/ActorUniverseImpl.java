package org.smallibs.tulya.actor.core.impl;

import org.smallibs.tulya.actor.core.Actor;
import org.smallibs.tulya.actor.core.ActorAddress;
import org.smallibs.tulya.actor.core.ActorUniverse;
import org.smallibs.tulya.actor.core.exception.ActorAlreadyRegisteredException;
import org.smallibs.tulya.actor.core.exception.UnknownParentException;
import org.smallibs.tulya.standard.Try;
import org.smallibs.tulya.standard.Unit;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ActorUniverseImpl implements ActorUniverse {

    private final Map<ActorAddress, Actor<?>> actors;

    public ActorUniverseImpl() {
        this.actors = new ConcurrentHashMap<>();
    }

    @Override
    public Try<Unit> store(ActorAddress address, Actor<?> actor) {
        if (address.parent().map(p -> !actors.containsKey(p)).orElse(false)) {
            return Try.failure(new UnknownParentException(address.parent().get()));
        }

        if (actors.containsKey(address)) {
            return Try.failure(new ActorAlreadyRegisteredException(address));
        } else {
            actors.put(address, actor);
            return Try.success(Unit.unit);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Protocol> Optional<Actor<Protocol>> retrieve(ActorAddress address) {
        return Optional.ofNullable((Actor<Protocol>) actors.get(address));
    }

    @Override
    public List<ActorAddress> remove(ActorAddress address) {
        try {
            return actors.keySet().stream().filter(key -> key.isChildOf(address)).toList();
        } finally {
            actors.remove(address);
        }
    }
}
