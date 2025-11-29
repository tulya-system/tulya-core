package org.smallibs.tulya.actor.core.impl;

import org.smallibs.tulya.actor.core.ActorAddress;
import org.smallibs.tulya.actor.core.ActorReference;
import org.smallibs.tulya.actor.core.BehaviorBuilder;
import org.smallibs.tulya.actor.core.Extended;
import org.smallibs.tulya.actor.core.ResponseHandler;
import org.smallibs.tulya.standard.Try;

public class ActorReferenceImpl<Protocol> implements ActorReference<Protocol> {

    private final ActorCoordinatorImpl coordinator;
    private final ActorAddress address;

    public ActorReferenceImpl(ActorCoordinatorImpl coordinator, ActorAddress address) {
        this.coordinator = coordinator;
        this.address = address;
    }

    @Override
    public ActorAddress address() {
        return this.address;
    }

    @Override

    public <AnotherProtocol> Try<ActorReference<AnotherProtocol>> tryCreate(String name, BehaviorBuilder<AnotherProtocol> actor) {
        return coordinator.register(address.child(name), actor);
    }

    @Override
    public boolean tell(Protocol message) {
        return coordinator.tell(this, new Extended.Carried<>(message));
    }

    @Override
    public void dispose() {
        coordinator.unregister(this.address);
    }

    @Override
    public <T> ResponseHandler<T> reponseHandler() {
        return coordinator.responseHandler();
    }

    @Override
    public String toString() {
        return "ActorReference@" + address;
    }
}
