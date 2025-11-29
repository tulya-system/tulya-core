package org.smallibs.tulya.actor.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.smallibs.tulya.async.Solvable;
import org.smallibs.tulya.async.impl.SolvablePromise;
import org.smallibs.tulya.standard.Try;
import org.smallibs.tulya.standard.Unit;

import java.time.Duration;

import static org.smallibs.tulya.actor.core.ActorAddress.Companion.address;

class AwaitingActorTests {

    @Test
    void shouldHandleAwaitingActor() throws Throwable {
        // Given
        try(var coordinator = ActorCoordinator.Companion.build()) {
            var sender = coordinator.register(address("sender"), sender()).orElseThrow();
            var receiver = coordinator.register(address("receiver"), receiver(Try.success(Unit.unit))).orElseThrow();

            // When
            var promise = new SolvablePromise<Unit>();
            sender.tell(new Request(promise, Duration.ofMillis(1000), receiver));

            // Then
            Assertions.assertEquals(Unit.unit, promise.await(Duration.ofMillis(1200)));
        }
    }

    @Test
    void shouldHandleConcurrentAwaitingActor() throws Throwable {
        // Given
        try(var coordinator = ActorCoordinator.Companion.build()) {
            var sender = coordinator.register(address("sender"), sender()).orElseThrow();
            var receiver = coordinator.register(address("receiver"), receiver(Try.success(Unit.unit))).orElseThrow();

            sender.tell(new Request(new SolvablePromise<>(), Duration.ofMillis(1000), receiver));

            // When
            var promise = new SolvablePromise<Unit>();
            sender.tell(new Request(promise, Duration.ofMillis(2000), receiver));

            // Then
            Assertions.assertEquals(Unit.unit, promise.await(Duration.ofMillis(2200)));
        }
    }

    // Private section

    private static BehaviorBuilder<Request> sender() {
        return self ->
                new Behavior<>() {
                    @Override
                    public ActorReference<Request> self() {
                        return self;
                    }

                    @Override
                    public void ask(Request message) {
                        var promise = message.receiver.<Unit>ask(solvable -> new Ask(message.duration, solvable));
                        message.response.solve(Try.handle(() -> promise.await()));
                    }
                };
    }

    private static BehaviorBuilder<Ask> receiver(Try<Unit> response) {
        return self ->
                new Behavior<>() {
                    @Override
                    public ActorReference<Ask> self() {
                        return self;
                    }

                    @Override
                    public void ask(Ask message) {
                        self().delay(message.duration);
                        System.out.println("Receiver sleep " + message.duration.toMillis() + " ms");
                        message.responseHandler().solve(response);
                    }
                };
    }

    record Request(SolvablePromise<Unit> response, Duration duration, ActorReference<Ask> receiver) {
    }

    record Ask(Duration duration, Solvable<Unit> responseHandler) {
    }
}