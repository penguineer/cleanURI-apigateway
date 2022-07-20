package com.penguineering.cleanuri.apigateway.results;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class TestExpectedResult {
    @Test
    public void testTimeout() {
        // set test clock
        final long clockOffset = 1000;
        ExpectedResult.clock =  Clock.fixed(
                Instant.ofEpochMilli(clockOffset), ZoneOffset.UTC
        );

        final long timeout = 60 * 1000;
        ExpectedResult<Integer> res = ExpectedResult.withTimeout(timeout);

        assertNotNull(res);
        assertEquals(clockOffset + timeout, res.getDeadline(), "Deadline did not match calculated timeout!");
    }

    @Test
    public void testCorrelationId() {
        ExpectedResult<Integer> res = ExpectedResult.withTimeout(0);

        assertNotNull(res);
        assertNotNull(res.getCorrelationId());
        assertEquals(32, res.getCorrelationId().length());
        assertDoesNotThrow(
                () ->Base64.getUrlDecoder().decode(res.getCorrelationId())
        );


        final Set<String> ids = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            final String correlationId = ExpectedResult.withTimeout(0).getCorrelationId();
            assertFalse(ids.contains(correlationId));
            ids.add(correlationId);
        }
    }
    @Test
    public void testFuture() {
        final ExpectedResult<Integer> res = ExpectedResult.withTimeout(0);
        assertNotNull(res);

        final CompletableFuture<Integer> cf = res.getCompletableFuture();
        assertFalse(cf.isDone());
    }

}
