package org.smallibs.tulya.actor.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ActorAddressTest {

    @Test
    void shouldCheckParent() {
        // Given
        var bob = ActorAddress.Companion.address("Bob");

        // When
        var alice = bob.child("Alice");

        // Then
        assertTrue(bob.isParentOf(alice));
    }

    @Test
    void shouldCheckChild() {
        // Given
        var bob = ActorAddress.Companion.address("Bob");

        // When
        var alice = bob.child("Alice");

        // Then
        assertTrue(alice.isChildOf(bob));
    }

    @Test
    void shouldCheckAncestor() {
        // Given
        var bob = ActorAddress.Companion.address("Bob");

        // When
        var alice = bob.child("Alice");
        var sally = alice.child("Sally");

        // Then
        assertTrue(bob.isAncestorOf(alice));
        assertTrue(bob.isAncestorOf(sally));
    }

    @Test
    void shouldCheckDescendant() {
        // Given
        var bob = ActorAddress.Companion.address("Bob");

        // When
        var alice = bob.child("Alice");
        var sally = alice.child("Sally");

        // Then
        assertTrue(alice.isDescendantOf(bob));
        assertTrue(sally.isDescendantOf(bob));
    }
}