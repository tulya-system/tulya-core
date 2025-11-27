package org.smallibs.tulya.actor.core.impl;

import org.smallibs.tulya.actor.core.Actor;
import org.smallibs.tulya.actor.core.ActorAddress;
import org.smallibs.tulya.actor.core.ActorEventLogger;
import org.smallibs.tulya.actor.core.ActorRuntime;
import org.smallibs.tulya.actor.core.ActorRuntimeContext;
import org.smallibs.tulya.actor.core.Behavior;
import org.smallibs.tulya.actor.core.Extended;
import org.smallibs.tulya.async.Solvable;
import org.smallibs.tulya.async.impl.SolvablePromise;
import org.smallibs.tulya.standard.Try;
import org.smallibs.tulya.standard.Unit;

import java.time.Duration;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

public class ActorImpl<Protocol> implements Actor<Protocol> {

    private final ActorAddress address;
    private final ActorRuntime runtime;
    private final ActorRuntimeContext runtimeContext;
    private final ActorEventLogger logger;
    private final Behavior<Protocol> behavior;

    private final AtomicReference<Status> status;
    private final Queue<Exclusive<Extended<Protocol>>> messages;

    public ActorImpl(ActorAddress address, ActorRuntime runtime, ActorRuntimeContext runtimeContext, ActorEventLogger logger, Behavior<Protocol> behavior) {
        this.address = address;
        this.runtime = runtime;
        this.runtimeContext = runtimeContext;
        this.logger = logger;
        this.behavior = behavior;

        this.status = new AtomicReference<>(Status.WAITING);
        this.messages = new ConcurrentLinkedQueue<>();
    }

    @Override
    public String toString() {
        return "Actor@" + addressOf(this);
    }

    // Section: Actor

    public boolean tell(Extended<Protocol> message) {
        return tell(new Exclusive.Carried<>(message));
    }

    // Section: ActorExclusive

    ActorAddress address() {
        return address;
    }

    void release(Duration duration) {
        var activeActorAddress = getActiveActorAddress();

        if (activeActorAddress.map(a -> !a.equals(this.address())).orElse(false)) {
            var self = addressOf(this);
            var current = activeActorAddress.map(ActorAddress::toString).orElse("N/A");

            throw new IllegalStateException("Waiting for %s and not %s".formatted(self, current));
        }

        if (status.compareAndSet(Status.RUNNING, Status.WAITING)) {
            logger.log(activeActorAddress, address, new ActorEventLogger.Event.StartAwait(duration));
            runtimeContext.unregisterCurrent();
            pump();
        } else {
            throw new IllegalStateException("Actor %s is not processing a message".formatted(addressOf(this)));
        }
    }

    void acquire(Duration duration) throws Throwable {
        try {
            var barrier = new SolvablePromise<Unit>();
            tell(new Exclusive.Acquire<>(duration.toString(), barrier));
            barrier.await();
        } finally {
            runtimeContext.registerCurrent(this);
            logger.log(getActiveActorAddress(), address, new ActorEventLogger.Event.EndAwait(duration));
        }

    }

    // Private section

    private Optional<ActorAddress> getActiveActorAddress() {
        return runtimeContext.getCurrentActor().map(ActorImpl::addressOf);
    }

    private static ActorAddress addressOf(ActorImpl<?> a) {
        return a.behavior.self().address();
    }

    private boolean tell(Exclusive<Extended<Protocol>> message) {
        if (messages.offer(message)) {
            logger.log(getActiveActorAddress(), address, new ActorEventLogger.Event.Submitted<>(message));
            pump();
            return true;
        } else {
            return false;
        }
    }

    private void pump() {
        if (!messages.isEmpty() && status.compareAndSet(Status.WAITING, Status.RUNNING)) {
            oldestMessage().ifPresentOrElse(this::perform, () -> status.set(Status.WAITING));
        }
    }

    private void perform(Exclusive<Extended<Protocol>> message) {
        switch (message) {
            case Exclusive.Acquire(var ignored, var barrier) -> {
                logger.log(getActiveActorAddress(), address, new ActorEventLogger.Event.Start<>(message));
                barrier.solve(Try.success(Unit.unit));
                logger.log(getActiveActorAddress(), address, new ActorEventLogger.Event.End<>(message));
            }
            case Exclusive.Carried(var extendedCarried) -> runtime.perform(() -> {
                try {
                    runtimeContext.registerCurrent(this);
                    logger.log(getActiveActorAddress(), address, new ActorEventLogger.Event.Start<>(message));
                    switch (extendedCarried) {
                        case Extended.Activate() -> behavior.activate();
                        case Extended.Deferred(var deferred) -> runtime.perform(deferred);
                        case Extended.Dispose(var barrier) -> {
                            try {
                                behavior.dispose();
                            } finally {
                                barrier.solve(Try.success(Unit.unit));
                            }
                        }
                        case Extended.Carried(var carried) -> behavior.ask(carried);
                    }
                } finally {
                    logger.log(getActiveActorAddress(), address, new ActorEventLogger.Event.End<>(message));
                    runtimeContext.unregisterCurrent();
                    status.set(Status.WAITING);
                    pump();
                }
            });
        }
    }

    private Optional<Exclusive<Extended<Protocol>>> oldestMessage() {
        return Optional.ofNullable(messages.poll());
    }

    private enum Status {WAITING, RUNNING}

    private sealed interface Exclusive<Protocol> {
        record Carried<Protocol>(Protocol message) implements Exclusive<Protocol> {
        }

        record Acquire<Protocol>(String description, Solvable<Unit> barrier) implements Exclusive<Protocol> {
        }
    }

}
