package com.seuprojeto.rabbitmq.ecommerce_mensageria.model;



import com.rabbitmq.client.Channel;
import com.seuprojeto.rabbitmq.ecommerce_mensageria.config.RabbitConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class OrderConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderConsumer.class);

    @RabbitListener(queues = RabbitConfig.QUEUE_NAME, containerFactory = "rabbitListenerContainerFactory")
    public void processOrderCreated(OrderCreatedMessage message,
                                    Channel channel,
                                    @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("Processando mensagem: {}", message);
        try {
         
            channel.basicAck(tag, false);
            log.info("Mensagem (tag={}) processada com SUCESSO!", tag);

        } catch (Exception ex) {
            log.error("ERRO ao processar mensagem (tag={}). Rejeitando...", tag, ex);
            
            
            channel.basicNack(tag, false, false);
        }
    }
}