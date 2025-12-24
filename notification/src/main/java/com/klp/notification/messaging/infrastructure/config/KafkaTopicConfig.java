package com.klp.notification.messaging.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String DELIVERY_CREATED_TOPIC = "delivery.notification";
    public static final String DELIVERY_CREATED_DLT = "delivery.notification.notification.dlt";
    public static final String NOTIFICATION_DLT = "notification.dlt";

    @Bean
    public NewTopic deliveryCreatedTopic() {
        return TopicBuilder.name(DELIVERY_CREATED_TOPIC)
            .partitions(1)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic notificationDltTopic() {
        return TopicBuilder.name(NOTIFICATION_DLT)
            .partitions(3)
            .replicas(1)
            .build();
    }
}
