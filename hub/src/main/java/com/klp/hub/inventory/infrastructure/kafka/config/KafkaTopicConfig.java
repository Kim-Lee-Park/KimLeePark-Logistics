package com.klp.hub.inventory.infrastructure.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String ORDER_CREATED_EVENTS = "order.created";
    public static final String ORDER_CANCELLED_EVENTS = "order.cancelled";
    public static final String INVENTORY_EVENTS = "inventory.events";

    /**
     * 주문 성공 이벤트 토픽 (구독용)
     */
    @Bean
    public NewTopic orderCreatedEventsTopic() {
        return TopicBuilder.name(ORDER_CREATED_EVENTS)
            .partitions(3)
            .replicas(1)
            .build();
    }

    /**
     * 주문 취소 이벤트 토픽 (구독용)
     */
    @Bean
    public NewTopic orderCancelledEventsTopic() {
        return TopicBuilder.name(ORDER_CANCELLED_EVENTS)
            .partitions(3)
            .replicas(1)
            .build();
    }


    /**
     * Inventory 이벤트 토픽 (발행용)
     */
    @Bean
    public NewTopic inventoryEventsTopic() {
        return TopicBuilder.name(INVENTORY_EVENTS)
            .partitions(3)
            .replicas(1)
            .build();
    }
}
