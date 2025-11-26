package org.smallibs.tulya.actor.core.exception;

import org.smallibs.tulya.actor.core.ActorAddress;

public class ActorAlreadyRegisteredException extends Exception {
    public ActorAlreadyRegisteredException(ActorAddress address) {
        super(String.format("Actor [%s] already registered", address));
    }
}
