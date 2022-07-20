package com.penguineering.cleanuri.apigateway.results;

import jakarta.inject.Singleton;
import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Singleton
@ThreadSafe
public class InMemoryResultManager<T> implements ResultManager<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryResultManager.class);

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
}
