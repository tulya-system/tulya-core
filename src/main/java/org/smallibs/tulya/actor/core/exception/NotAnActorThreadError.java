package org.smallibs.tulya.actor.core.exception;

import org.smallibs.tulya.actor.core.ActorAddress;

public class NotAnActorThreadError extends RuntimeException {
    public NotAnActorThreadError(ActorAddress address) {
        super("Not an actor thread for [" + address + "]");
    }
}
