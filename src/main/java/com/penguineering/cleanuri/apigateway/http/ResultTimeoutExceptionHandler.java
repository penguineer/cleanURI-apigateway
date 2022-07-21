package com.penguineering.cleanuri.apigateway.http;

import com.penguineering.cleanuri.apigateway.results.ResultTimeoutException;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;

@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class ResultTimeoutExceptionHandler
        implements ExceptionHandler<ResultTimeoutException, HttpResponse<JsonError>> {
    @Override
    public HttpResponse<JsonError> handle(HttpRequest request, ResultTimeoutException rte) {
        return HttpResponse.status(HttpStatus.GATEWAY_TIMEOUT).body(rte.toJsonError());
    }
}
