package org.smallibs.tulya.actor.core.impl;

import org.smallibs.tulya.actor.core.Actor;
import org.smallibs.tulya.actor.core.ActorAddress;
import org.smallibs.tulya.actor.core.ActorRuntime;
import org.smallibs.tulya.actor.core.ActorRuntimeContext;
import org.smallibs.tulya.actor.core.Behavior;
import org.smallibs.tulya.actor.core.Extended;
import org.smallibs.tulya.async.impl.SolvablePromise;
import org.smallibs.tulya.standard.Try;
import org.smallibs.tulya.standard.Unit;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

public class ActorImpl<Protocol> implements Actor<Protocol> {

    private final ActorRuntime runtime;
    private final ActorRuntimeContext runtimeContext;
    private final Behavior<Protocol> behavior;

    private final AtomicReference<Status> status;
    private final Queue<Exclusive<Extended<Protocol>>> messages;

    public ActorImpl(ActorRuntime runtime, ActorRuntimeContext runtimeContext, Behavior<Protocol> behavior) {
        this.runtime = runtime;
        this.runtimeContext = runtimeContext;
        this.behavior = behavior;

        this.status = new AtomicReference<>(Status.WAITING);
        this.messages = new ConcurrentLinkedQueue<>();
    }

    // Section: Actor

    public boolean tell(Extended<Protocol> message) {
        return this.tell(new Exclusive.Carried<>(message));
    }

    // Section: ActorExclusive

    void release() {
        var currentActor = runtimeContext.getCurrentActor();

        if (currentActor.map(a -> a != this).orElse(false)) {
            var selfAddress = addressOf(this);
            var currentAddress = currentActor.map(a -> addressOf(a).toString()).orElse("N/A");

            throw new IllegalStateException(MessageFormat.format(
                    "Waiting for {0} while current is {1}",
                    selfAddress,
                    currentAddress)
            );
        }

        if (this.status.compareAndSet(Status.RUNNING, Status.WAITING)) {
            this.runtimeContext.unregisterCurrent();
            this.pump();
        } else {
            throw new IllegalStateException("Actor is not processing a message");
        }
    }

    void acquire() throws Throwable {
        var barrier = new SolvablePromise<Unit>();
        this.tell(new Exclusive.Acquire<>(barrier));
        barrier.await();
    }

    // Private section

    private static ActorAddress addressOf(ActorImpl<?> a) {
        return a.behavior.self().address();
    }

    private boolean tell(Exclusive<Extended<Protocol>> message) {
        if (this.messages.offer(message)) {
            this.pump();
            return true;
        } else {
            return false;
        }
    }

    private void pump() {
        if (this.status.compareAndSet(Status.WAITING, Status.RUNNING)) {
            oldestMessage().ifPresentOrElse(this::perform, () -> status.set(Status.WAITING));
        }
    }

    private void perform(Exclusive<Extended<Protocol>> m) {
        runtimeContext.registerCurrent(this);
        switch (m) {
            case Exclusive.Acquire(var p) -> p.solve(Try.success(Unit.unit));
            case Exclusive.Carried(var xc) -> runtime.perform(() -> {
                try {
                    switch (xc) {
                        case Extended.Deferred(var v) -> runtime.perform(v);
                        case Extended.Dispose(var p) -> {
                            try {
                                behavior.dispose();
                            } finally {
                                p.solve(Try.success(Unit.unit));
                            }
                        }
                        case Extended.Carried(var ec) -> behavior.tell(ec);
                    }
                } finally {
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

        record Acquire<Protocol>(SolvablePromise<Unit> barrier) implements Exclusive<Protocol> {
        }
    }

}
