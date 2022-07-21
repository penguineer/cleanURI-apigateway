package com.penguineering.cleanuri.apigateway.results;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.hateoas.JsonError;

import java.util.Optional;

/**
 * This exception is thrown when an ExpectedResult is not provided within the configured timeout.
 */
public class ResultTimeoutException extends Exception{
    private final String correlationId;
    public ResultTimeoutException(@Nullable String correlationId, String message) {
        super(message);
        this.correlationId = correlationId;
    }

    /**
     * Get the correlation ID for the backend process.
     *
     * @return A correlation ID for debugging purposes.
     */
    public Optional<String> getCorrelationId() {
        return correlationId == null ? Optional.empty() : Optional.of(correlationId);
    }

    /**
     * Convert to JsonError and store the correlation ID as log ref.
     * @return A JsonError for the exception.
     */
    public JsonError toJsonError() {
        return new JsonError(this.getMessage()).logref(correlationId);
    }
}
