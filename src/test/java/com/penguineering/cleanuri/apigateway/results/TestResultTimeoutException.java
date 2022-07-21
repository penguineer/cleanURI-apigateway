package com.penguineering.cleanuri.apigateway.results;

import io.micronaut.http.hateoas.JsonError;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class TestResultTimeoutException {

    public static final String CORRELATION_ID = "Mbz8KGzZZ2T-Tk2-wVepo9B1uHhPsJSV";
    public static final String MESSAGE = "foo message";

    @Test
    public void testFullyPopulated() {
        final ResultTimeoutException rte = new ResultTimeoutException(CORRELATION_ID, MESSAGE);

        assertEquals(Optional.of(CORRELATION_ID), rte.getCorrelationId());
        assertEquals(MESSAGE, rte.getMessage());

        final JsonError error = rte.toJsonError();
        assertNotNull(error);
        assertEquals(Optional.of(CORRELATION_ID), error.getLogref());
        assertEquals(MESSAGE, error.getMessage());
    }

    @Test
    public void testMessageOnly() {
        final ResultTimeoutException rte = new ResultTimeoutException(null, MESSAGE);

        assertFalse(rte.getCorrelationId().isPresent());
        assertEquals(MESSAGE, rte.getMessage());

        final JsonError error = rte.toJsonError();
        assertNotNull(error);
        assertFalse(error.getLogref().isPresent());
        assertEquals(MESSAGE, error.getMessage());
    }


    @Test
    public void testEmpty() {
        final ResultTimeoutException rte = new ResultTimeoutException(null, null);

        assertFalse(rte.getCorrelationId().isPresent());
        assertNull(rte.getMessage());

        final JsonError error = rte.toJsonError();
        assertNotNull(error);
        assertFalse(error.getLogref().isPresent());
        assertNull(error.getMessage());
    }
}
