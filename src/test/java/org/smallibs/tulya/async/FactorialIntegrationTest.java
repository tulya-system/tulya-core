package org.smallibs.tulya.async;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.TWO;

public class FactorialIntegrationTest {

    private final BigInteger factorialLimit = BigInteger.valueOf(200);
    private final BigInteger expectedResult = new BigInteger("788657867364790503552363213932185062295135977687173263294742533244359449963403342920304284011984623904177212138919638830257642790242637105061926624952829931113462857270763317237396988943922445621451664240254033291864131227428294853277524242407573903240321257405579568660226031904170324062351700858796178922222789623703897374720000000000000000000000000000000000000000000000000");

    @Test
    void shouldComputeIndirectFibonacci() throws Throwable {
        // Given
        try (var async = Async.Companion.ofVirtual()) {
            // When
            var result = factorialAsync(async, factorialLimit);

            // Then
            Assertions.assertEquals(expectedResult, result.await());
        }
    }

    @Test
    void shouldComputeDirectFibonacci() throws Throwable {
        // Given
        try (var async = Async.Companion.ofVirtual()) {
            // When
            var result = async.run(() -> factorial(async, factorialLimit));

            // Then
            Assertions.assertEquals(expectedResult, result.await());
        }
    }

    Promise<BigInteger> factorialAsync(Async async, BigInteger value) {
        if (value.compareTo(TWO) < 0) {
            return Promise.success(value);
        } else {
            return async.run(() -> factorialAsync(async, value.subtract(ONE)))
                    .flatMap(r -> r)
                    .map(v -> v.multiply(value));
        }
    }

    BigInteger factorial(Async async, BigInteger value) throws Throwable {
        if (value.compareTo(TWO) < 0) {
            return value;
        } else {
            return async.run(() -> factorial(async, value.subtract(ONE)))
                    .await()
                    .multiply(value);
        }
    }
}
