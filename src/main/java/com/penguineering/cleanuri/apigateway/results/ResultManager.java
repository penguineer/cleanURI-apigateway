package com.penguineering.cleanuri.apigateway.results;

import io.micronaut.core.annotation.Nullable;

import javax.validation.constraints.NotNull;

/**
 * <p>Hold a list of result expectations and remove stale entries on timeout
 * or emit results resp. errors when available.</p>
 *
 * <p>This manager enables decoupling of request/response in HTTP and
 * asynchronous message patterns.</p>
 *
 * @param <T> The result type.
 */
public interface ResultManager<T> {
    /**
     * Register an expected result.
     *
     * <p>Create an ExpectedResult with a correlation ID and set a timeout.</p>
     * @param timeout The timeout in milliseconds starting from _now_.
     * @return An expected result instance specifically for this request.
     */
    ExpectedResult<T> registerExpectation(long timeout);

    /**
     * Emit a result from asynchronous processing.
     *
     * @param correlationId The correlation ID from a registered expectation.
     * @param result The actual result or null.
     * @return True if the correlation ID was (still) registered
     * @throws IllegalArgumentException if the correlationId is null.
     */
    boolean emitResult(String correlationId, @Nullable T result);

    /**
     * Emit an exception from processing.
     *
     * @param correlationId The correlation ID from a registered expectation.
     * @param ex The Throwable from processing.
     * @return True if the correlation ID was (still) registered
     * @throws IllegalArgumentException if the correlationId is null.
     */
    boolean emitException(String correlationId, @NotNull Throwable ex);
}
