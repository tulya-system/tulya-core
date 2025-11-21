package org.smallibs.tulya.standard;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TryTest {

    @Test
    void shouldMapSuccess() {
        // Given
        var data = Try.success("Hello");

        // When
        var mapped = data.map(String::length);

        // Then
        Assertions.assertEquals(Try.success("Hello".length()), mapped);
    }

    @Test
    void shouldMapFailure() {
        // Given
        var data = Try.<String>failure(new Exception());

        // When
        var mapped = data.map(String::length);

        // Then
        Assertions.assertTrue(mapped.isFailure());
    }

    @Test
    void shouldFlatMapSuccess() {
        // Given
        var data = Try.success("Hello");

        // When
        var mapped = data.flatMap(s -> Try.success(s.length()));

        // Then
        Assertions.assertEquals(Try.success("Hello".length()), mapped);
    }

    @Test
    void shouldFlatMapToError() {
        // Given
        var data = Try.success("Hello");

        // When
        var mapped = data.flatMap(s -> Try.failure(new Exception()));

        // Then
        Assertions.assertTrue(mapped.isFailure());
    }

    @Test
    void shouldFlatMapFailure() {
        // Given
        var data = Try.<String>failure(new Exception());

        // When
        var mapped = data.flatMap(s -> Try.success(s.length()));

        // Then
        Assertions.assertTrue(mapped.isFailure());
    }

    @Test
    void shouldFoldSuccess() {
        // Given
        var data = Try.success("Hello");

        // When
        var fold = data.fold(s -> s, __ -> null);

        // Then
        Assertions.assertEquals("Hello", fold);
    }

    @Test
    void shouldFoldFailure() {
        // Given
        var data = Try.<String>failure(new Exception());

        // When
        var fold = data.fold(__ -> false, __ -> true);

        // Then
        Assertions.assertTrue(fold);
    }
}