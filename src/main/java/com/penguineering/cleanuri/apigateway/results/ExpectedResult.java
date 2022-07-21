package com.penguineering.cleanuri.apigateway.results;

import net.jcip.annotations.Immutable;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Immutable
public class ExpectedResult<T> {
    private static final SecureRandom secureRandom = new SecureRandom(); //threadsafe
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); //threadsafe

    static Clock clock = Clock.systemUTC();

    static String generateCorrelationId() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    public static <T> ExpectedResult<T> withTimeout(final long timeout_ms) {
        return  new ExpectedResult<>(
                generateCorrelationId(),
                clock.millis() + timeout_ms
        );
    }

    private final String correlationId;
    private final long deadline;
    private final CompletableFuture<T> future;

    private ExpectedResult(String correlationId, long deadline) {
        this.correlationId = correlationId;
        this.deadline = deadline;
        this.future = new CompletableFuture<>();
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public long getDeadline() {
        return deadline;
    }

    public CompletableFuture<T> getCompletableFuture() {
        return future;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpectedResult<?> that)) return false;
        return Objects.equals(correlationId, that.correlationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(correlationId);
    }
}
