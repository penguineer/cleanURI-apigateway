package com.penguineering.cleanuri.apigateway.results;

import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.time.Clock;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
@ThreadSafe
public class InMemoryResultManager<T> implements ResultManager<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryResultManager.class);

    static Clock clock = Clock.systemUTC();

    private final Map<String, ExpectedResult<T>> results = new HashMap<>();

    @Override
    public ExpectedResult<T> registerExpectation(long timeout) {
        final ExpectedResult<T> res = ExpectedResult.withTimeout(timeout);

        synchronized (results) {
            results.put(res.getCorrelationId(), res);
        }

        LOGGER.debug("Created expected result with correlation id {}", res.getCorrelationId());

        return res;
    }

    private boolean resolveAndRemove(String correlationId,
                                  Function<CompletableFuture<T>, ?> resolution) {
        if (correlationId == null)
            throw new IllegalArgumentException("CorrelationID must not be null!");

        synchronized (results) {
            final ExpectedResult<T> res = results.getOrDefault(correlationId, null);

            if (res == null)
                return false;

            resolution.apply(res.getCompletableFuture());
            results.remove(correlationId);

            return true;
        }
    }

    @Override
    public boolean emitResult(String correlationId, T result) {
        LOGGER.debug("Emitting result for correlation id {}", correlationId);

        return resolveAndRemove(correlationId,
                future -> future.complete(result));
    }

    public boolean emitException(String correlationId, Throwable ex) {
        LOGGER.debug("Emitting exception for correlation id {}", correlationId);

        return resolveAndRemove(correlationId,
                future -> future.completeExceptionally(ex));
    }

    @Scheduled(fixedDelay = "${gateway.cache-evict:300s}")
    void cleanupCache() {
        final long now = clock.millis();

        final Map<String, CompletableFuture<T>> expired = getAndRemoveExpired(now);
        timeoutUncompleted(expired);
        logExpired(expired.keySet());
    }

    Map<String, CompletableFuture<T>> getAndRemoveExpired(final long now) {
        synchronized (results) {
            // Get all expired entries
            final Map<String, CompletableFuture<T>> expired = results.entrySet().stream()
                    .filter(e -> e.getValue().getDeadline() < now)
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getCompletableFuture()));

            // Remove them from the results
            results.keySet().removeAll(expired.keySet());

            return expired;
        }
    }

    void timeoutUncompleted(@NotNull final Map<String, CompletableFuture<T>> expired) {
        expired.entrySet().stream()
                .filter(e -> !e.getValue().isDone())
                .forEach(e -> e.getValue().completeExceptionally(
                        new ResultTimeoutException(e.getKey(), "Timeout while waiting for backend response!")));
    }

    void logExpired(@NotNull final Collection<String> correlationIds) {
        correlationIds.forEach(id -> LOGGER.debug("Removed expired correlation id {}", id));
    }
}
