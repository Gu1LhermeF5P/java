package com.seuprojeto.rabbitmq.ecommerce_mensageria.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.seuprojeto.rabbitmq.ecommerce_mensageria.model.OrderCreatedMessage;
import com.seuprojeto.rabbitmq.ecommerce_mensageria.service.OrderPublisher;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderPublisher orderPublisher;

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderCreatedMessage message) {
        orderPublisher.publishOrderCreated(message);
        return ResponseEntity.ok("Pedido enviado para processamento: " + message.orderId());
    }
}