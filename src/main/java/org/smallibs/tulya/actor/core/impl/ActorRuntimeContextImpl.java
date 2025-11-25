package org.smallibs.tulya.actor.core.impl;

import org.smallibs.tulya.actor.core.ActorRuntimeContext;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ActorRuntimeContextImpl implements ActorRuntimeContext {

    private final Map<Long, ActorImpl<?>> activeActors = new ConcurrentHashMap<>();

    @Override
    public void registerCurrent(ActorImpl<?> actor) {
        activeActors.put(Thread.currentThread().threadId(), actor);
    }

    @Override
    public Optional<ActorImpl<?>> getCurrentActor() {
        return Optional.ofNullable(activeActors.get(Thread.currentThread().threadId()));
    }

    @Override
    public void unregisterCurrent() {
        activeActors.remove(Thread.currentThread().threadId());
    }

}
