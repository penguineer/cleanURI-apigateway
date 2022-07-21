package com.penguineering.cleanuri.apigateway.results;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class TestInMemoryResultManager {
    @Inject
    ResultManager<String> resultManager;

    private void setFixedClock() {
        final long clockOffset = 0;
        InMemoryResultManager.clock = Clock.fixed(
                Instant.ofEpochMilli(clockOffset), ZoneOffset.UTC
        );
        ExpectedResult.clock = InMemoryResultManager.clock;
    }

    private void offsetClock(Duration duration) {
        InMemoryResultManager.clock = Clock.offset(InMemoryResultManager.clock, duration);
    }

    @Test
    public void testRegisterAndFulfill() {
        setFixedClock();
        final String[] inputs = new String[] {null, "1"};

        // workaround for missing runtime classes in JUnit parametrized tests
        for (final String input : inputs) {

            // register an expected result
            ExpectedResult<String> res = resultManager.registerExpectation(1000);

            // must return a result with a completable future
            assertNotNull(res);
            assertNotNull(res.getCompletableFuture());
            assertFalse(res.getCompletableFuture().isDone());

            // emitting a result must lead to a change
            assertTrue(
                    resultManager.emitResult(res.getCorrelationId(), input)
            );

            // check that future is done and has no exception
            assertTrue(res.getCompletableFuture().isDone());
            assertFalse(res.getCompletableFuture().isCompletedExceptionally());

            // get future and compare with provided result
            final String result = assertDoesNotThrow(
                    () -> res.getCompletableFuture().get()
            );
            assertEquals(input, result);

            // emitting another result should not lead to a change,
            // but also not to an exception
            assertFalse(
                    assertDoesNotThrow(
                            () -> resultManager.emitResult(res.getCorrelationId(), "foo")
                    )
            );
        }
    }

    @Test
    public void testRegisterAndFail() {
        setFixedClock();

        // register an expected result
        ExpectedResult<String> res = resultManager.registerExpectation(1000);

        // must return a result with a completable future
        assertNotNull(res);
        assertNotNull(res.getCompletableFuture());
        assertFalse(res.getCompletableFuture().isDone());

        // emitting an exception must lead to a change
        assertTrue(
                resultManager.emitException(res.getCorrelationId(), new Exception("Test"))
        );

        // check that future is done and has an exception
        assertTrue(res.getCompletableFuture().isDone());
        assertTrue(res.getCompletableFuture().isCompletedExceptionally());

        // getting the future should throw an exception
        ExecutionException e = assertThrows(
                ExecutionException.class,
                () -> res.getCompletableFuture().get()
        );
        assertNotNull(e.getCause());
        assertEquals("Test", e.getCause().getMessage());

        // emitting another result should not lead to a change,
        // but also not to an exception
        assertFalse(
                assertDoesNotThrow(
                        () -> resultManager.emitException(res.getCorrelationId(), new Exception("Test"))
                )
        );
    }

    @Test
    void testTimeout() {
        setFixedClock();

        final long timeout = 1000;

        // register a expected results
        ExpectedResult<String> res1 = resultManager.registerExpectation(timeout + 2);
        ExpectedResult<String> res2 = resultManager.registerExpectation(timeout);

        // completable futures are not yet done
        assertFalse(res1.getCompletableFuture().isDone());
        assertFalse(res2.getCompletableFuture().isDone());

        // forward clock by timeout
        offsetClock(Duration.ofMillis(timeout + 1));
        // call cache cleanup
        ((InMemoryResultManager<String>) resultManager).cleanupCache();

        // res1 is still there
        assertTrue(
                assertDoesNotThrow(
                        () -> resultManager.emitResult(res1.getCorrelationId(), "1")
                )
        );
        // res1 was successful
        assertTrue(res1.getCompletableFuture().isDone());
        assertFalse(res1.getCompletableFuture().isCompletedExceptionally());

        // completable future has failed
        assertTrue(res2.getCompletableFuture().isCompletedExceptionally());

        // getting the future should throw a ResultTimeoutException
        ExecutionException e = assertThrows(
                ExecutionException.class,
                () -> res2.getCompletableFuture().get()
        );
        assertNotNull(e.getCause());
        assertTrue(e.getCause() instanceof ResultTimeoutException);
        assertEquals("Timeout on cache cleanup!", e.getCause().getMessage());
        assertEquals(Optional.of(res2.getCorrelationId()),
                ((ResultTimeoutException)e.getCause()).getCorrelationId());

        // emitting another result should not lead to a change,
        // i.e. the expected result has been deleted
        assertFalse(
                assertDoesNotThrow(
                        () -> resultManager.emitResult(res2.getCorrelationId(), "1")
                )
        );

        // res1 still successful
        assertTrue(res1.getCompletableFuture().isDone());
        assertFalse(res1.getCompletableFuture().isCompletedExceptionally());
    }
}
