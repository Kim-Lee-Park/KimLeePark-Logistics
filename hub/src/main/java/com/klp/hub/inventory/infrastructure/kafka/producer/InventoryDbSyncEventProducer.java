package com.klp.hub.inventory.infrastructure.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InventoryDbSyncEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InventoryDbSyncEventProducer(
        @Qualifier("inventoryKafkaTemplate") KafkaTemplate<String, Object> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

}
