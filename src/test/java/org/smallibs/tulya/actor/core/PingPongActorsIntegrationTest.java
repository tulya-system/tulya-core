package org.smallibs.tulya.actor.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.smallibs.tulya.async.Solvable;
import org.smallibs.tulya.standard.Try;

import java.util.Arrays;
import java.util.Random;

import static org.smallibs.tulya.actor.core.ActorAddress.Companion.address;

class PingPongActorsIntegrationTest {

    @Test
    void shouldPerformPingPong() throws Throwable {
        // Given
        try (var coordinator = ActorCoordinator.Companion.build()) {
            var random = new Random();
            var alice = coordinator.register(address("Alice"), PingPong.create(random)).orElseThrow();
            var bob = coordinator.register(address("Bob"), PingPong.create(random)).orElseThrow();

            // When
            var sent = alice.<String>ask(s -> new Ball(1, bob, s));

            // Then
            Assertions.assertTrue(Arrays.asList("Alice", "Bob").contains(sent.await()));
        }
    }
}

record Ball(int stage, ActorReference<Ball> reference, Solvable<String> winnerIs) {
}

record PingPong(@Override ActorReference<Ball> self, Random random) implements Behavior<Ball> {
    public void ask(Ball message) {
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