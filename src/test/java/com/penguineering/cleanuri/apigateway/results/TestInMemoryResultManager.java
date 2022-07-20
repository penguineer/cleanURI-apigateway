package com.penguineering.cleanuri.apigateway.results;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class TestInMemoryResultManager {
    @Inject
    ResultManager<String> resultManager;

    @Test
    public void testRegisterAndFulfill() {
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

}
