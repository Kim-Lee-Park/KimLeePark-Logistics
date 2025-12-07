package com.klp.hub.inventory.infrastructure.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    // 구독용 토픽
    public static final String ORDER_CREATED_EVENTS = "order.created";
    public static final String ORDER_CANCELLED_EVENTS = "order.cancelled";
    public static final String COUPON_EVENTS = "coupon.topic";
    public static final String PAYMENT_EVENTS = "payment.topic";

    // 발행용 토픽
    public static final String INVENTORY_EVENTS = "inventory.topic";

    // DLT 토픽
    public static final String INVENTORY_DLT = "inventory.dlt";

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

    /**
     * 쿠폰 이벤트 토픽 (구독용)
     */
    @Bean
    public NewTopic couponEventsTopic() {
        return TopicBuilder.name(COUPON_EVENTS)
            .partitions(3)
            .replicas(1)
            .build();
    }

    /**
     * 결제 이벤트 토픽 (구독용)
     */
    @Bean
    public NewTopic paymentEventsTopic() {
        return TopicBuilder.name(PAYMENT_EVENTS)
            .partitions(3)
            .replicas(1)
            .build();
    }

    /**
     * Inventory DLT 토픽 (단일 DLT - 모든 실패 메시지 통합)
     */
    @Bean
    public NewTopic inventoryDltTopic() {
        return TopicBuilder.name(INVENTORY_DLT)
            .partitions(3)
            .replicas(1)
            .build();
    }
}
