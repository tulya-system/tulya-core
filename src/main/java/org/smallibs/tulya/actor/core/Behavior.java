package org.smallibs.tulya.actor.core;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

public interface Behavior<Protocol> {

    ActorReference<Protocol> self();

    void tell(Protocol message);

    default void dispose() {
        // Do nothing by default
    }
}
