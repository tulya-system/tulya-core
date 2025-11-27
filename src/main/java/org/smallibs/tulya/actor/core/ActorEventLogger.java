package org.smallibs.tulya.actor.core;

import java.time.Duration;
import java.util.Optional;

public interface ActorEventLogger {

    void log(Optional<ActorAddress> address, ActorAddress destination, Event event);

    sealed interface Event {
        record StartAwait(Duration duration) implements Event {
        }

        record EndAwait(Duration duration) implements Event {
        }

        record Submitted<Prototype>(Prototype message) implements Event {
        }

        record Start<Prototype>(Prototype message) implements Event {
        }

        record End<Prototype>(Prototype message) implements Event {
        }
    }

}
