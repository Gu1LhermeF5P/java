package com.seuprojeto.rabbitmq.ecommerce_mensageria.model;

public record OrderItem(
    String productId,
    int quantity
) {}