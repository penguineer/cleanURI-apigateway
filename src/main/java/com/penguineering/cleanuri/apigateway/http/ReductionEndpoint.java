package com.penguineering.cleanuri.apigateway.http;

import com.penguineering.cleanuri.apigateway.results.ExpectedResult;
import com.penguineering.cleanuri.apigateway.results.ResultManager;
import com.penguineering.cleanuri.common.amqp.ExtractionTaskEmitter;
import com.penguineering.cleanuri.common.message.ExtractionRequest;
import com.penguineering.cleanuri.common.message.ExtractionTask;
import com.penguineering.cleanuri.common.message.MetaData;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Controller("/reduce")
public class ReductionEndpoint {
    @Value("${gateway.cache-timeout:60s}")
    Duration timeout;

    @Inject
    ResultManager<ExtractionTask> resultMgr;

    @Property(name = "gateway.amqp-task-rk")
    String taskRK;

    @Property(name = "gateway.amqp-result-queue")
    String resultQueue;

    @Inject
    ExtractionTaskEmitter emitter;

    @Get
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponse(responseCode = "200", description = "Successful")
    @ApiResponse(responseCode = "400", description = "Invalid call arguments")
    @ApiResponse(responseCode = "504", description = "Timeout waiting for backend response")
    public CompletableFuture<ExtractionTask> reduce(
            final URI uri,
            final @Nullable String meta
    ) {
        final ExtractionRequest.Builder requestBuilder = ExtractionRequest.Builder.withURI(uri);

        if (meta != null) {
            if (meta.contains("I")) {
                requestBuilder.addField(MetaData.Fields.ID);
            }
            if (meta.contains("T")) {
                requestBuilder.addField(MetaData.Fields.TITLE);
            }
        }

        final ExtractionTask task = ExtractionTask.Builder.withRequest(requestBuilder.instance()).instance();
        final ExpectedResult<ExtractionTask> res = resultMgr.registerExpectation(timeout.toMillis());

        Mono.fromRunnable(
                        () -> emitter.send(taskRK, res.getCorrelationId(), resultQueue, task))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();

        return res.getCompletableFuture();

    }
}
