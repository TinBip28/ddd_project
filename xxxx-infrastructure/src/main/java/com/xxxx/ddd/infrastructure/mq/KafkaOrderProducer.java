package com.xxxx.ddd.infrastructure.mq;

import com.xxxx.ddd.infrastructure.config.kafka.KafkaTopicConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class KafkaOrderProducer {

    @Autowired
    private KafkaTemplate<String, PlaceOrderMQMessage> kafkaTemplate;

    public void sendOrderMessage(PlaceOrderMQMessage message) {
        CompletableFuture<SendResult<String, PlaceOrderMQMessage>> future =
                kafkaTemplate.send(KafkaTopicConfig.ORDER_PLACE_TOPIC, message.getToken(), message);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("KafkaOrderProducer: failed to send token={}", message.getToken(), ex);
            } else {
                log.debug("KafkaOrderProducer: sent token={} partition={} offset={}",
                        message.getToken(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}