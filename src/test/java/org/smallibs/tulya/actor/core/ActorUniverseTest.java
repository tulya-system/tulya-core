package org.smallibs.tulya.actor.core;

import org.junit.jupiter.api.Test;
import org.smallibs.tulya.actor.core.exception.ActorAlreadyRegisteredException;
import org.smallibs.tulya.actor.core.exception.UnknownParentException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActorUniverseTest {

    @Test
    void shouldRetrieveNothing() {
        // Given
        var universe = ActorUniverse.Companion.build();

        // When
        var actor = universe.retrieve(new ActorAddress(Optional.empty(), "unknown"));

        // Then
        assertFalse(actor.isPresent());
    }

    @Test
    void shouldStoreAnActor() {
        // Given
        var universe = ActorUniverse.Companion.build();

        // When
        var stored = universe.store(new ActorAddress(Optional.empty(), "unknown"), (__) -> true);

        // Then
        assertTrue(stored.isSuccess());
    }

    @Test
    void shouldNotStoreAnActorTwice() {
        // Given
        var universe = ActorUniverse.Companion.build();
        universe.store(new ActorAddress(Optional.empty(), "unknown"), (__) -> true);

        // When
        var stored = universe.store(new ActorAddress(Optional.empty(), "unknown"), (__) -> true);

        // Then
        assertThrows(ActorAlreadyRegisteredException.class, stored::orElseThrow);
    }

    @Test
    void shouldRetrieveAnActor() {
        // Given
        var universe = ActorUniverse.Companion.build();
        universe.store(new ActorAddress(Optional.empty(), "alice"), (__) -> true);

        // When
        var stored = universe.retrieve(new ActorAddress(Optional.empty(), "alice"));

        // Then
        assertTrue(stored.isPresent());
    }

    @Test
    void shouldNotAbleToStoreActorWithUnknownParent() {
        // Given
        var universe = ActorUniverse.Companion.build();

        // When
        var stored = universe.store(new ActorAddress(Optional.of(new ActorAddress(Optional.empty(), "bob")), "alice"), (__) -> true);

        // Then
        assertThrows(UnknownParentException.class, stored::orElseThrow);
    }

    @Test
    void shouldAbleToStoreActorWithKnownParent() throws Throwable {
        // Given
        var universe = ActorUniverse.Companion.build();
        var bob = new ActorAddress(Optional.empty(), "Bob");
        var alice = new ActorAddress(Optional.of(bob), "Alice");

        universe.store(bob, (__) -> true).orElseThrow();

        // When
        var stored = universe.store(alice, (__) -> true);

        // Then
        assertTrue(stored.isSuccess());
    }

    @Test
    void shouldRemoveAndReturnActorsToRemove() throws Throwable {
        // Given
        var universe = ActorUniverse.Companion.build();
        var bob = new ActorAddress(Optional.empty(), "Bob");
        var alice = new ActorAddress(Optional.of(bob), "Alice");

        universe.store(bob, (__) -> true).orElseThrow();
        universe.store(alice, (__) -> true).orElseThrow();

        // When
        var toRemove = universe.remove(bob);

        // Then
        assertArrayEquals(List.of(alice).toArray(), toRemove.toArray());
    }
}