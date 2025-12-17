package com.klp.payment.payment.infrastructure.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    // 구독용
    public static final String ORDER_CREATED_TOPIC = "order.created";
    public static final String ORDER_CREATED_DLT = "order.created.dlt";

    public static final String ORDER_CANCELLED_TOPIC = "order.cancelled";
    public static final String ORDER_CANCELLED_DLT = "order.cancelled.dlt";

    // 구독용
    public static final String COUPON_TOPIC = "coupon.topic";

    // 구독용
    public static final String INVENTORY_TOPIC = "inventory.topic";

    // 발행용
    public static final String PAYMENT_APPROVED_TOPIC = "payment.approved";
    public static final String PAYMENT_CANCELLED_TOPIC = "payment.cancelled";
    public static final String PAYMENT_FAILED_TOPIC = "payment.failed";

    // DLT
    public static final String PAYMENT_DLT = "payment.dlt";

    /**
     * 결제 이벤트 토픽
     */
    @Bean
    public NewTopic paymentApprovedTopic() {
        return TopicBuilder.name(PAYMENT_APPROVED_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic paymentCancelledTopic() {
        return TopicBuilder.name(PAYMENT_CANCELLED_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic paymentFailedTopic() {
        return TopicBuilder.name(PAYMENT_FAILED_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    /**
     * 결제 DLT 토픽
     */
    @Bean
    public NewTopic paymentDltTopic() {
        return TopicBuilder.name(PAYMENT_DLT)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic orderCreatedTopic() {
        return TopicBuilder.name(ORDER_CREATED_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic orderCancelledTopic() {
        return TopicBuilder.name(ORDER_CANCELLED_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    /**
     * 쿠폰 이벤트 토픽
     */
    @Bean
    public NewTopic couponDltTopic() {
        return TopicBuilder.name(COUPON_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic inventoryTopic() {
        return TopicBuilder.name(INVENTORY_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic orderCreatedDltTopic() {
        return TopicBuilder.name(ORDER_CREATED_DLT)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic orderCancelledDltTopic() {
        return TopicBuilder.name(ORDER_CANCELLED_DLT)
            .partitions(3)
            .replicas(1)
            .build();
    }
}
