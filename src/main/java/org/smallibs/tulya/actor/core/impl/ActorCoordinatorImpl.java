package org.smallibs.tulya.actor.core.impl;

import org.smallibs.tulya.actor.core.Actor;
import org.smallibs.tulya.actor.core.ActorAddress;
import org.smallibs.tulya.actor.core.ActorCoordinator;
import org.smallibs.tulya.actor.core.ActorReference;
import org.smallibs.tulya.actor.core.ActorRuntime;
import org.smallibs.tulya.actor.core.ActorRuntimeContext;
import org.smallibs.tulya.actor.core.ActorUniverse;
import org.smallibs.tulya.actor.core.BehaviorBuilder;
import org.smallibs.tulya.actor.core.Extended;
import org.smallibs.tulya.actor.core.ResponseHandler;
import org.smallibs.tulya.async.impl.SolvablePromise;
import org.smallibs.tulya.standard.Try;
import org.smallibs.tulya.standard.Unit;

import java.util.Optional;

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
    public <Protocol> Try<ActorReference<Protocol>> register(ActorAddress parent, String name, BehaviorBuilder<Protocol> builder) {
        var address = new ActorAddress(Optional.of(parent), name);
        var reference = new ActorReferenceImpl<Protocol>(this, address);
        var behavior = builder.apply(reference);
        var actor = new ActorImpl<>(runtime, runtimeContext, behavior);

        if (universe.register(address, actor)) {
            return Try.success(reference);
        } else {
            return Try.failure(new RuntimeException("Actor already registered"));
        }
    }

    @Override
    public <Protocol> boolean unregister(ActorReference<Protocol> reference) {
        try {
            var barrier = new SolvablePromise<Unit>();
            retrieveActor(reference).map(actor -> actor.tell(new Extended.Dispose<>(barrier)));
            barrier.await();
        } catch (Throwable ignored) {
            // consumed
        }

        return universe.unregister(reference.address());
    }

    // Package protected section

    <Protocol> boolean tell(ActorReference<Protocol> reference, Extended<Protocol> message) {
        return retrieveActor(reference).map(actor -> actor.tell(message)).orElse(false);
    }

    <Protocol> ResponseHandler<Protocol> responseHandler() {
        var promise = new SolvablePromise<Protocol>();
        var actorPromise = new ActorPromiseImpl<>(runtimeContext, promise);

        return new ResponseHandlerImpl<>(actorPromise, promise);
    }

    // Private section

    private <Protocol> Optional<Actor<Protocol>> retrieveActor(ActorReference<Protocol> reference) {
        return universe.retrieve(reference.address());
    }

}
