package com.klp.promotion.coupon.infrastructure.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    // 구독용 토픽
    public static final String PAYMENT_APPROVED_TOPIC = "payment.approved";
    public static final String PAYMENT_APPROVED_DLT = "payment.approved.coupon.dlt";
    public static final String PAYMENT_CANCELLED_TOPIC = "payment.cancelled";
    public static final String PAYMENT_CANCELLED_DLT = "payment.cancelled.coupon.dlt";
    public static final String PAYMENT_FAILED_TOPIC = "payment.failed";
    public static final String PAYMENT_FAILED_DLT = "payment.failed.coupon.dlt";

    public static final String INVENTORY_DEDUCTED_FAILED_TOPIC = "inventory.deducted.failed";
    public static final String INVENTORY_DEDUCTED_FAILED_DLT = "inventory.deducted.failed.coupon.dlt";
    public static final String ORDER_FAILED_TOPIC = "order.failed";
    public static final String ORDER_FAILED_DLT = "order.failed.coupon.dlt";

    // 발행용 토픽
    public static final String COUPON_USED_TOPIC = "coupon.used";
    public static final String COUPON_USED_DLT = "coupon.used.coupon.dlt";
    public static final String COUPON_USED_FAILED_TOPIC = "coupon.used.failed";
    public static final String COUPON_USED_FAILED_DLT = "coupon.used.failed.coupon.dlt";
    public static final String COUPON_RESTORED_TOPIC = "coupon.restored";
    public static final String COUPON_RESTORED_DLT = "coupon.restored.coupon.dlt";
    public static final String COUPON_RESTORED_FAILED_TOPIC = "coupon.restored.failed";
    public static final String COUPON_RESTORED_FAILED_DLT = "coupon.restored.failed.coupon.dlt";


    @Bean
    public NewTopic paymentApprovedTopic() {
        return TopicBuilder.name(PAYMENT_APPROVED_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic paymentApprovedDltTopic() {
        return TopicBuilder.name(PAYMENT_APPROVED_DLT)
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
    public NewTopic paymentCancelledDltTopic() {
        return TopicBuilder.name(PAYMENT_CANCELLED_DLT)
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

    @Bean
    public NewTopic paymentFailedDltTopic() {
        return TopicBuilder.name(PAYMENT_FAILED_DLT)
            .partitions(3)
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
    public NewTopic inventoryDeductedFailedDltTopic() {
        return TopicBuilder.name(INVENTORY_DEDUCTED_FAILED_DLT)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic OrderFailedTopic() {
        return TopicBuilder.name(ORDER_FAILED_TOPIC)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic OrderFailedDltTopic() {
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
    public NewTopic couponUsedDltTopic() {
        return TopicBuilder.name(COUPON_USED_DLT)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic couponUsedFailedTopic() {
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


}

