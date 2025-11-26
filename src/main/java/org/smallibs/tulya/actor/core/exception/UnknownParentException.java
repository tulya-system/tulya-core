package org.smallibs.tulya.actor.core.exception;

import org.smallibs.tulya.actor.core.ActorAddress;

public class UnknownParentException extends Exception {
    public UnknownParentException(ActorAddress address) {
        super(String.format("Unknown parent [%s]", address));
    }
}
