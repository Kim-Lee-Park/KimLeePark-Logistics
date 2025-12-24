package com.klp.delivery.global.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    // 발행용 토픽 (단일 토픽으로 통합)
    public static final String DELIVERY_CREATED_TOPIC = "delivery.created";
    public static final String DELIVERY_CREATED_FAILED_TOPIC = "delivery.created.failed";
    public static final String DELIVERY_SHIPPING_TOPIC = "delivery.shipping";
    public static final String DELIVERY_SHIPPING_FAILED_TOPIC = "delivery.shipping.failed";
    public static final String DELIVERY_ARRIVED_TOPIC = "delivery.arrived";
    public static final String DELIVERY_ARRIVED_FAILED_TOPIC = "delivery.arrived.failed";
    public static final String DELIVERY_NOTIFICATION_TOPIC = "delivery.notification";
    public static final String DELIVERY_NOTIFICATION_FAILED_TOPIC = "delivery.notification.failed";
    public static final String DELIVERY_DLT = "delivery.dlt";

    // 구독용 토픽
    public static final String INVENTORY_DEDUCTED_TOPIC = "inventory.deducted";
    public static final String INVENTORY_DEDUCTED_DLT = "inventory.deducted.delivery.dlt";
    public static final String INVENTORY_REPLENISHED_TOPIC = "inventory.replenished";
    public static final String INVENTORY_REPLENISHED_DLT = "inventory.replenished.delivery.dlt";

    @Bean
    public NewTopic deliveryCreatedTopic() {
        return TopicBuilder.name(DELIVERY_CREATED_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic deliveryCreatedFailedTopic() {
        return TopicBuilder.name(DELIVERY_CREATED_FAILED_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic deliveryShippingTopic() {
        return TopicBuilder.name(DELIVERY_SHIPPING_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic deliveryShippingFailedTopic() {
        return TopicBuilder.name(DELIVERY_SHIPPING_FAILED_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic deliveryArrivedTopic() {
        return TopicBuilder.name(DELIVERY_ARRIVED_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic deliveryDLTTopic() {
        return TopicBuilder.name(DELIVERY_DLT)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic inventoryDeductedTopic() {
        return TopicBuilder.name(INVENTORY_DEDUCTED_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic inventoryDeductedDltTopic() {
        return TopicBuilder.name(INVENTORY_DEDUCTED_DLT)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic inventoryReplenishedTopic() {
        return TopicBuilder.name(INVENTORY_REPLENISHED_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic inventoryReplenishedDltTopic() {
        return TopicBuilder.name(INVENTORY_REPLENISHED_DLT)
            .partitions(3)
            .replicas(1)
            .build();
    }

}
