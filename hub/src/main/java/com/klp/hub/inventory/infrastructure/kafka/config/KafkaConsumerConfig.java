package com.klp.hub.inventory.infrastructure.kafka.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.hub.inventory.domain.event.CouponCancelledEvent;
import com.klp.hub.inventory.domain.event.CouponUsedEvent;
import com.klp.hub.inventory.domain.event.CouponUsedFailedEvent;
import com.klp.hub.inventory.domain.event.OrderCancelledEvent;
import com.klp.hub.inventory.domain.event.OrderCreatedEvent;
import com.klp.hub.inventory.domain.event.OrderFailedEvent;
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
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.util.backoff.ExponentialBackOff;

@Slf4j
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private final ObjectMapper objectMapper;

    public KafkaConsumerConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public ConsumerFactory<String, Object> inventoryConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "inventory-service-group");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
            ErrorHandlingDeserializer.class);
        configProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        configProps.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024);
        configProps.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);

        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, true);
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, Object.class);
        configProps.put(JsonDeserializer.TYPE_MAPPINGS, buildTypeMappings());

        return new DefaultKafkaConsumerFactory<>(
            configProps,
            new StringDeserializer(),
            new ErrorHandlingDeserializer<>(new JsonDeserializer<>(objectMapper))
        );
    }

    private String buildTypeMappings() {
        return String.join(",",
            "OrderCreatedEvent:" + OrderCreatedEvent.class.getName(),
            "OrderFailedEvent:" + OrderFailedEvent.class.getName(),
            "OrderCancelledEvent:" + OrderCancelledEvent.class.getName(),
            "CouponUsedEvent:" + CouponUsedEvent.class.getName(),
            "CouponCancelledEvent:" + CouponCancelledEvent.class.getName(),
            "PaymentFailedEvent:" + PaymentFailedEvent.class.getName(),
            "PaymentCancelledEvent:" + PaymentCancelledEvent.class.getName(),
            "CouponUsedFailedEvent:" + CouponUsedFailedEvent.class.getName()
        );
    }

    @Bean
    public DeadLetterPublishingRecoverer inventoryDeadLetterPublishingRecoverer(
        @Qualifier("inventoryKafkaTemplate") KafkaTemplate<String, Object> kafkaTemplate
    ) {
        return new DeadLetterPublishingRecoverer(kafkaTemplate,
            (record, exception) -> {
                String dltTopic = record.topic() + ".inventory.dlt";
                log.error("메시지 처리 실패, DLT로 이동: topic={} -> {}, error={}",
                    record.topic(), dltTopic, exception.getMessage());
                return new TopicPartition(dltTopic, record.partition());
            });
    }

    @Bean
    public DefaultErrorHandler inventoryErrorHandler(
        @Qualifier("inventoryDeadLetterPublishingRecoverer") DeadLetterPublishingRecoverer recoverer
    ) {
        ExponentialBackOff backOff = new ExponentialBackOff(
            1000L,
            2.0
        );
        backOff.setMaxInterval(4000L);
        backOff.setMaxElapsedTime(10000L);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        errorHandler.addNotRetryableExceptions(
            DeserializationException.class,
            MessageConversionException.class
        );

        errorHandler.setRetryListeners((record, ex, deliveryAttempt) -> {
            log.warn("메시지 처리 재시도: topic={}, attempt={}, error={}",
                record.topic(), deliveryAttempt, ex.getMessage());
        });

        return errorHandler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> inventoryKafkaListenerContainerFactory(
        @Qualifier("inventoryErrorHandler") DefaultErrorHandler errorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
            new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(inventoryConsumerFactory());
        factory.setConcurrency(3);
        factory.setBatchListener(true);

        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        factory.setCommonErrorHandler(errorHandler);

        factory.getContainerProperties().setObservationEnabled(true);

        return factory;
    }
}