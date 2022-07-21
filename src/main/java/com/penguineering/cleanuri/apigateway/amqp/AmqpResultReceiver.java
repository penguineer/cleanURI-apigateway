package com.penguineering.cleanuri.apigateway.amqp;

import com.penguineering.cleanuri.apigateway.results.ResultManager;
import io.micronaut.rabbitmq.annotation.Queue;
import io.micronaut.rabbitmq.annotation.RabbitListener;
import io.micronaut.rabbitmq.annotation.RabbitProperty;
import io.micronaut.rabbitmq.bind.RabbitAcknowledgement;
import jakarta.inject.Inject;

@RabbitListener
public class AmqpResultReceiver {
    @Inject
    ResultManager<String> resultMgr;

    @Queue("${gateway.amqp-result-queue}")
    public void receive(@RabbitProperty("correlationId") String correlationId,
                        String body,
                        RabbitAcknowledgement acknowledgement) {
        resultMgr.emitResult(correlationId, body);
        acknowledgement.ack();
    }
}
