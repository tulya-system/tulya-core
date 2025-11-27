package org.smallibs.tulya.actor.core.impl;

import org.smallibs.tulya.actor.core.ActorAddress;
import org.smallibs.tulya.actor.core.ActorCoordinator;
import org.smallibs.tulya.actor.core.ActorEventLogger;
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

import java.io.IOException;
import java.util.Optional;

public class ActorCoordinatorImpl implements ActorCoordinator {

    private final ActorUniverse universe;
    private final ActorRuntime runtime;
    private final ActorRuntimeContext runtimeContext;
    private final ActorEventLogger logger;

    public ActorCoordinatorImpl(ActorUniverse universe, ActorRuntime runtime, ActorRuntimeContext runtimeContext) {
        this.universe = universe;
        this.runtime = runtime;
        this.runtimeContext = runtimeContext;

        this.logger = new StdoutActorEventLoggerImpl();
    }

    @Override
    public void close() throws IOException {
        this.runtime.close();
    }

    @Override
    public <Protocol> Try<ActorReference<Protocol>> register(ActorAddress address, BehaviorBuilder<Protocol> builder) {
        var reference = new ActorReferenceImpl<Protocol>(this, address);
        var behavior = builder.apply(reference);
        var actor = new ActorImpl<>(address, runtime, runtimeContext, logger, behavior);

        actor.tell(new Extended.Activate<>());

        return universe.store(address, actor).map(__ -> reference);
    }

    @Override
    public <Protocol> Optional<ActorReference<Protocol>> retrieve(ActorAddress address) {
        return universe.retrieve(address).map(__ -> new ActorReferenceImpl<>(this, address));
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
        return universe.<Protocol>retrieve(reference.address())
                .map(actor -> actor.tell(message))
                .orElse(false);
    }

    <Protocol> ResponseHandler<Protocol> responseHandler() {
        var promise = new SolvablePromise<Protocol>();
        var actorPromise = new ActorPromiseImpl<>(runtimeContext, promise);
        return new ResponseHandlerImpl<>(actorPromise, promise);
    }

}
