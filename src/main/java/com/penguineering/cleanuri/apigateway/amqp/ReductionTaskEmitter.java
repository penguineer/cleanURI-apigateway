package com.penguineering.cleanuri.apigateway.amqp;

import com.penguineering.cleanuri.apigateway.model.ReductionTask;
import io.micronaut.rabbitmq.annotation.Binding;
import io.micronaut.rabbitmq.annotation.RabbitClient;
import io.micronaut.rabbitmq.annotation.RabbitProperty;

@RabbitClient
public interface ReductionTaskEmitter {
    @Binding("${gateway.amqp-task-queue}")
    @RabbitProperty(name = "contentType", value = "application/json")
    @RabbitProperty(name = "replyTo", value = "${gateway.amqp-result-queue}")
    void send(@RabbitProperty("correlationId") String correlationId, ReductionTask task);
}
