package com.klp.hub.inventory.infrastructure.kafka.config;

import com.klp.hub.inventory.domain.event.CouponUsedEvent;
import com.klp.hub.inventory.domain.event.OrderCreatedEvent;
import com.klp.hub.inventory.domain.event.PaymentCancelledEvent;
import com.klp.hub.inventory.domain.event.PaymentFailedEvent;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_INTERVAL_MS = 1000L;

    /**
     * Inventory 전용 Consumer Factory 설정
     */
    @Bean
    public ConsumerFactory<String, Object> inventoryConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        // 기본 설정
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "inventory-service-group");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // Consumer 동작 설정
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);

        // JsonDeserializer 설정
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.klp.*");
        configProps.put(JsonDeserializer.TYPE_MAPPINGS, buildTypeMappings());

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    private String buildTypeMappings() {
        return String.join(",",
            "OrderCreatedEvent:" + OrderCreatedEvent.class.getName(),
            "CouponUsedEvent:" + CouponUsedEvent.class.getName(),
            "PaymentFailedEvent:" + PaymentFailedEvent.class.getName(),
            "PaymentCancelledEvent:" + PaymentCancelledEvent.class.getName()
        );
    }

    /**
     * DLT(Dead Letter Topic) 발행 설정. 단일 DLT 사용
     */
    @Bean
    public DeadLetterPublishingRecoverer inventoryDeadLetterPublishingRecoverer(
        @Qualifier("inventoryKafkaTemplate") KafkaTemplate<String, Object> kafkaTemplate
    ) {
        return new DeadLetterPublishingRecoverer(kafkaTemplate,
            (record, exception) -> {
                log.error("메시지 처리 실패, DLT로 이동: originalTopic={}, error={}",
                    record.topic(), exception.getMessage());
                return new TopicPartition(KafkaTopicConfig.INVENTORY_DLT, record.partition());
            });
    }

    /**
     * Inventory 전용 에러 핸들러 설정. 3회 재시도 후 DLT로 이동. 역직렬화 예외는 재시도 없이 바로 DLT로 이동
     */
    @Bean
    public DefaultErrorHandler inventoryErrorHandler(
        @Qualifier("inventoryDeadLetterPublishingRecoverer") DeadLetterPublishingRecoverer recoverer
    ) {
        FixedBackOff backOff = new FixedBackOff(RETRY_INTERVAL_MS, MAX_RETRY_ATTEMPTS);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        errorHandler.addNotRetryableExceptions(
            org.springframework.kafka.support.serializer.DeserializationException.class,
            org.springframework.messaging.converter.MessageConversionException.class
        );

        errorHandler.setRetryListeners((record, ex, deliveryAttempt) ->
            log.warn("메시지 처리 재시도: topic={}, attempt={}, error={}",
                record.topic(), deliveryAttempt, ex.getMessage())
        );

        return errorHandler;
    }

    /**
     * Inventory 전용 Kafka Listener Container Factory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> inventoryKafkaListenerContainerFactory(
        @Qualifier("inventoryErrorHandler") DefaultErrorHandler errorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
            new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(inventoryConsumerFactory());
        factory.setConcurrency(3);
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }
}
