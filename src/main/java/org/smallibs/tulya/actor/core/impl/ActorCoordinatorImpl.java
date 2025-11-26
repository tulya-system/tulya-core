package org.smallibs.tulya.actor.core.impl;

import org.smallibs.tulya.actor.core.ActorAddress;
import org.smallibs.tulya.actor.core.ActorCoordinator;
import org.smallibs.tulya.actor.core.ActorReference;
import org.smallibs.tulya.actor.core.ActorRuntime;
import org.smallibs.tulya.actor.core.ActorRuntimeContext;
import org.smallibs.tulya.actor.core.ActorUniverse;
import org.smallibs.tulya.actor.core.BehaviorBuilder;
import org.smallibs.tulya.actor.core.Extended;
import org.smallibs.tulya.actor.core.ResponseHandler;
import org.smallibs.tulya.actor.core.exception.NotAnActorThreadError;
import org.smallibs.tulya.async.impl.SolvablePromise;
import org.smallibs.tulya.standard.Try;
import org.smallibs.tulya.standard.Unit;

public class ActorCoordinatorImpl implements ActorCoordinator {

    private final ActorUniverse universe;
    private final ActorRuntime runtime;
    private final ActorRuntimeContext runtimeContext;

    public ActorCoordinatorImpl(ActorUniverse universe, ActorRuntime runtime, ActorRuntimeContext runtimeContext) {
        this.universe = universe;
        this.runtime = runtime;
        this.runtimeContext = runtimeContext;
    }

    @Override
    public <Protocol> Try<ActorReference<Protocol>> register(ActorAddress address, BehaviorBuilder<Protocol> builder) {
        var reference = new ActorReferenceImpl<Protocol>(this, address);
        var behavior = builder.apply(reference);
        var actor = new ActorImpl<>(address, runtime, runtimeContext, behavior);

        return universe.store(address, actor).map(__ -> reference);
    }

    @Override
    public void unregister(ActorAddress address) {
        // Unregister children actors | like processes
        universe.remove(address).forEach(this::unregister);

        try {
            var barrier = new SolvablePromise<Unit>();
            universe.retrieve(address).map(actor -> actor.tell(new Extended.Dispose<>(barrier)));
            barrier.await();
        } catch (Throwable ignored) {
            // consumed
        }
    }

    // Package protected section

    <Protocol> boolean tell(ActorReference<Protocol> reference, Extended<Protocol> message) {
        return universe.<Protocol>retrieve(reference.address()).map(actor -> actor.tell(message)).orElse(false);
    }

    <Protocol> ResponseHandler<Protocol> responseHandler(ActorAddress address) {
        // A response handler is meant to be built in an actor context only
        if (runtimeContext.getCurrentActor().filter(actor -> actor.getAddress().equals(address)).isEmpty()) {
            throw new NotAnActorThreadError(address);
        }

        var promise = new SolvablePromise<Protocol>();
        var actorPromise = new ActorPromiseImpl<>(runtimeContext, promise);

        return new ResponseHandlerImpl<>(actorPromise, promise);
    }

}
