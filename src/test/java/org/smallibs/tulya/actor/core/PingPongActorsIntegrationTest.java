package org.smallibs.tulya.actor.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.smallibs.tulya.async.impl.SolvablePromise;
import org.smallibs.tulya.standard.Try;

import java.util.Arrays;
import java.util.Random;

class PingPongActorsIntegrationTest {

    @Test
    void shouldPerformPingPong() throws Throwable {
        // Given
        var random = new Random();
        var coordinator = ActorCoordinator.Companion.build();
        var alice = coordinator.register(ActorAddress.Companion.address("Alice"), PingPong.create(random)).orElseThrow();
        var bob = coordinator.register(ActorAddress.Companion.address("Bob"), PingPong.create(random)).orElseThrow();

        // When
        var result = new SolvablePromise<String>();
        var sent = alice.tell(new Ball(1, bob, result));

        // Then
        Assertions.assertTrue(sent);
        Assertions.assertTrue(Arrays.asList("Alice", "Bob").contains(result.await()));
    }
}

record Ball(int stage, ActorReference<Ball> reference, SolvablePromise<String> winnerIs) {
}

record PingPong(@Override ActorReference<Ball> self, Random random) implements Behavior<Ball> {
    public void tell(Ball message) {
        if (message.stage() < random.nextInt(100) + 10) {
            message.reference().tell(new Ball(message.stage() + 1, self, message.winnerIs()));
        } else {
            message.winnerIs().solve(Try.success(self.address().name()));
        }
    }

    static BehaviorBuilder<Ball> create(Random random) {
        return reference -> new PingPong(reference, random);
    }
}