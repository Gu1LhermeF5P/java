package com.seuprojeto.rabbitmq.ecommerce_mensageria.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.seuprojeto.rabbitmq.ecommerce_mensageria.config.RabbitConfig;
import com.seuprojeto.rabbitmq.ecommerce_mensageria.model.OrderCreatedMessage;

@Service
public class OrderPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderPublisher.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishOrderCreated(OrderCreatedMessage message) {
        rabbitTemplate.convertAndSend(
            RabbitConfig.EXCHANGE_NAME,
            RabbitConfig.ROUTING_KEY,
            message
        );
        log.info("Mensagem publicada: {}", message);
    }
}