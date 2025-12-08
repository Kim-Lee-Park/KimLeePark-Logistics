package com.klp.delivery.global.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    // 발행용 토픽 (단일 토픽으로 통합)
    public static final String DELIVERY_EVENTS = "delivery.topic";

    @Bean
    public NewTopic deliveryEventsTopic() {
        return TopicBuilder.name(DELIVERY_EVENTS)
            .partitions(3)
            .replicas(1)
            .build();
    }



}
