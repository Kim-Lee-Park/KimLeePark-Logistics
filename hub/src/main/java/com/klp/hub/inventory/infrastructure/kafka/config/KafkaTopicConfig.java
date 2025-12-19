package com.klp.hub.inventory.infrastructure.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    // 구독용 토픽
    public static final String ORDER_FAILED_TOPIC = "order.failed";
    public static final String ORDER_FAILED_DLT = "order.failed.inventory.dlt";

    public static final String COUPON_USED_TOPIC = "coupon.used";
    public static final String COUPON_USED_DLT = "coupon.used.inventory.dlt";

    public static final String COUPON_CANCELLED_TOPIC = "coupon.cancelled";
    public static final String COUPON_CANCELLED_DLT = "coupon.cancelled.inventory.dlt";

    public static final String COUPON_USED_FAILED_TOPIC = "coupon.used.failed";
    public static final String COUPON_USED_FAILED_DLT = "coupon.used.failed.inventory.dlt";

    public static final String COUPON_RESTORED_TOPIC = "coupon.restored";
    public static final String COUPON_RESTORED_DLT = "coupon.restored.inventory.dlt";

    public static final String COUPON_RESTORED_FAILED_TOPIC = "coupon.restored.failed";
    public static final String COUPON_RESTORED_FAILED_DLT = "coupon.restored.inventory.dlt";

    public static final String PAYMENT_FAILED_TOPIC = "payment.failed";
    public static final String PAYMENT_FAILED_DLT = "payment.failed.inventory.dlt";

    // 발행용 토픽
    public static final String INVENTORY_DEDUCTED_TOPIC = "inventory.deducted";
    public static final String INVENTORY_DEDUCTED_FAILED_TOPIC = "inventory.deducted.failed";

    public static final String INVENTORY_REPLENISHED_TOPIC = "inventory.replenished";
    public static final String INVENTORY_REPLENISHED_FAILED_TOPIC = "inventory.replenished.failed";

    // DLT 토픽
    public static final String INVENTORY_DLT = "inventory.dlt";

    @Bean
    public NewTopic orderFailedTopic() {
        return TopicBuilder.name(ORDER_FAILED_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic orderFailedDltTopic() {
        return TopicBuilder.name(ORDER_FAILED_DLT)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic couponUsedTopic() {
        return TopicBuilder.name(COUPON_USED_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic couponCancelledTopic() {
        return TopicBuilder.name(COUPON_CANCELLED_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic couponUsedDltTopic() {
        return TopicBuilder.name(COUPON_USED_DLT)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic couponCancelledDltTopic() {
        return TopicBuilder.name(COUPON_CANCELLED_DLT)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic couponUsedFailTopic() {
        return TopicBuilder.name(COUPON_USED_FAILED_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic couponUsedFailedDltTopic() {
        return TopicBuilder.name(COUPON_USED_FAILED_DLT)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic couponRestoredTopic() {
        return TopicBuilder.name(COUPON_RESTORED_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic couponRestoredDltTopic() {
        return TopicBuilder.name(COUPON_RESTORED_DLT)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic couponRestoredFailedTopic() {
        return TopicBuilder.name(COUPON_RESTORED_FAILED_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic couponRestoredFailedDltTopic() {
        return TopicBuilder.name(COUPON_RESTORED_FAILED_DLT)
            .partitions(3)
            .replicas(1)
            .build();
    }


    /**
     * Inventory 이벤트 토픽 (발행용)
     */
    @Bean
    public NewTopic paymentFailedTopic() {
        return TopicBuilder.name(PAYMENT_FAILED_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    /**
     * 쿠폰 이벤트 토픽 (구독용)
     */
    @Bean
    public NewTopic paymentFailedDltTopic() {
        return TopicBuilder.name(PAYMENT_FAILED_DLT)
            .partitions(3)
            .replicas(1)
            .build();
    }

    /**
     * 결제 이벤트 토픽 (구독용)
     */
    @Bean
    public NewTopic inventoryDeductedTopic() {
        return TopicBuilder.name(INVENTORY_DEDUCTED_TOPIC)
            .partitions(9)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic inventoryDeductedFailedTopic() {
        return TopicBuilder.name(INVENTORY_DEDUCTED_FAILED_TOPIC)
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
    public NewTopic inventoryReplenishedFailedTopic() {
        return TopicBuilder.name(INVENTORY_REPLENISHED_FAILED_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic inventoryDltTopic() {
        return TopicBuilder.name(INVENTORY_DLT)
            .partitions(3)
            .replicas(1)
            .build();
    }
}
