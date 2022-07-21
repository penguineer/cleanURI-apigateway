package com.penguineering.cleanuri.apigateway.http;

import com.penguineering.cleanuri.apigateway.amqp.ReductionTaskEmitter;
import com.penguineering.cleanuri.apigateway.model.ReductionTask;
import com.penguineering.cleanuri.apigateway.results.ExpectedResult;
import com.penguineering.cleanuri.apigateway.results.ResultManager;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Controller("/reduce")
public class ReductionEndpoint {
    @Inject
    ResultManager<String> resultMgr;

    @Inject
    ReductionTaskEmitter emitter;

    @Value("${gateway.cache-timeout:60s}")
    Duration timeout;

    @Get
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponse(responseCode = "200", description = "Successful")
    @ApiResponse(responseCode = "400", description = "Invalid call arguments")
    @ApiResponse(responseCode = "504", description = "Timeout waiting for backend response")
    public CompletableFuture<String> reduce(
            final URI uri,
            final @Nullable String meta
    ) {
        final ReductionTask.Builder taskBuilder = ReductionTask.Builder.withURI(uri);

        if (meta != null) {
            if (meta.contains("T"))
                taskBuilder.addMeta(ReductionTask.Meta.TITLE);
            if (meta.contains("P"))
                taskBuilder.addMeta(ReductionTask.Meta.PRICE);
        }

        final ReductionTask task = taskBuilder.instance();

        ExpectedResult<String> res = resultMgr.registerExpectation(timeout.toMillis());

        emitter.send(res.getCorrelationId(), task);

        return res.getCompletableFuture();
    }
}
