package com.klp.promotion.coupon.infrastructure.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    // 구독용 토픽
    public static final String PAYMENT_TOPIC = "payment.topic";

    public static final String INVENTORY_TOPIC = "inventory.topic";

    // 발행용 토픽
    public static final String COUPON_TOPIC = "coupon.topic";

    // 주문 토픽
    public static final String ORDER_TOPIC = "order.topic";

    /**
     * 결제 이벤트 토픽 (구독용)
     */
    @Bean
    public NewTopic paymentTopic() {
        return TopicBuilder.name(PAYMENT_TOPIC)
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

