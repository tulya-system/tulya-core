package org.smallibs.tulya.actor.core;

public interface Behavior<Protocol> {

    ActorReference<Protocol> self();

    void tell(Protocol message);

    default void dispose() {
        // Do nothing by default
    }

}
