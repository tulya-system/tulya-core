package org.smallibs.tulya.actor.core;

public interface Behavior<Protocol> {

    ActorReference<Protocol> self();

    void ask(Protocol message);

    default void activate() {
        // Do nothing by default
    }

    default void dispose() {
        // Do nothing by default
    }
}
