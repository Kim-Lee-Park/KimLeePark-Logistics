package com.klp.promotion.coupon.infrastructure.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    // 구독용 토픽
    public static final String PAYMENT_APPROVED_TOPIC = "payment.approved";
    public static final String PAYMENT_CANCELLED_TOPIC = "payment.cancelled";
    public static final String PAYMENT_FAILED_TOPIC = "payment.failed";

    public static final String INVENTORY_DEDUCTED_FAILED_TOPIC = "inventory.deducted.failed";
    public static final String ORDER_FAILED_TOPIC = "order.failed";

    // 발행용 토픽
    public static final String COUPON_USED_TOPIC = "coupon.used";
    public static final String COUPON_USED_FAILED_TOPIC = "coupon.used.failed";
    public static final String COUPON_RESTORED_TOPIC = "coupon.restored";
    public static final String COUPON_RESTORED_FAILED_TOPIC = "coupon.restored.failed";


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
     * 쿠폰 이벤트 토픽 (발행용)
     */
    @Bean
    public NewTopic couponTopic() {
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
    public NewTopic orderTopic() {
        return TopicBuilder.name(ORDER_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }
}

