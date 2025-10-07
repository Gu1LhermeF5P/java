package com.seuprojeto.rabbitmq.ecommerce_mensageria.model;

import java.util.List;

public record OrderCreatedMessage(
    String orderId,
    String clientId,
    List<OrderItem> items
) {}