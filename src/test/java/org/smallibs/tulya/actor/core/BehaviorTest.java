package org.smallibs.tulya.actor.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.smallibs.tulya.async.impl.SolvablePromise;
import org.smallibs.tulya.standard.Try;

import java.util.Arrays;
import java.util.Random;

class BehaviorTest {

    @Test
    void shouldPerformPingPong() throws Throwable {
        // Given
        var coordinator = ActorCoordinator.create();
        var alice = coordinator.register(ActorAddress.SYSTEM, "alice", PingPong.create(new Random(), "Alice")).orElseThrow();
        var bob = coordinator.register(ActorAddress.SYSTEM, "bob", PingPong.create(new Random(), "Bob")).orElseThrow();

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

record PingPong(@Override ActorReference<Ball> self, Random random, String who) implements Behavior<Ball> {
    public void tell(Ball message) {
        System.out.println(message.stage() + " " + who);

        if (message.stage() < random.nextInt(100) + 10) {
            message.reference().tell(new Ball(message.stage() + 1, self, message.winnerIs()));
        } else {
            message.winnerIs().solve(Try.success(who));
        }
    }

    static BehaviorBuilder<Ball> create(Random random, String who) {
        return reference -> new PingPong(reference, random, who);
    }
}