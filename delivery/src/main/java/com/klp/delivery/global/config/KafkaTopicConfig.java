package com.klp.delivery.global.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    public static final String DELIVERY_CREATED_EVENTS = "delivery.created";
    public static final String DELIVERY_SHIPPING_EVENTS = "delivery.shipping";
    public static final String DELIVERY_COMPLETED_EVENTS = "delivery.completed";
    public static final String DELIVERY_CANCELLED_EVENTS = "delivery.cancelled";

    @Bean
    public NewTopic deliveryCreateEventsTopic() {
        return TopicBuilder.name(DELIVERY_CREATED_EVENTS)
            .partitions(3)
            .replicas(1)
            .build();
    }


    @Bean
    public NewTopic deliveryShippingEventsTopic() {
        return TopicBuilder.name(DELIVERY_SHIPPING_EVENTS)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic deliveryCompletedEventsTopic() {
        return TopicBuilder.name(DELIVERY_COMPLETED_EVENTS)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic deliveryCancelledEventsTopic() {
        return TopicBuilder.name(DELIVERY_CANCELLED_EVENTS)
            .partitions(3)
            .replicas(1)
            .build();
    }



}
