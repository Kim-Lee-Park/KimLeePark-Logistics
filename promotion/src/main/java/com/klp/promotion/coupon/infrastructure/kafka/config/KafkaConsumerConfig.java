package com.klp.promotion.coupon.infrastructure.kafka.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.promotion.coupon.domain.event.InventoryDeductedFailedEvent;
import com.klp.promotion.coupon.domain.event.PaymentApprovedEvent;
import com.klp.promotion.coupon.domain.event.PaymentCancelledEvent;
import com.klp.promotion.coupon.domain.event.PaymentFailedEvent;
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
import org.springframework.util.backoff.FixedBackOff;

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

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_INTERVAL_MS = 1000L;

    @Bean
    public ConsumerFactory<String, Object> couponConsumerFactory() {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "coupon-service-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, true);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, Object.class);
        props.put(JsonDeserializer.TYPE_MAPPINGS,
            "PaymentApprovedEvent:" + PaymentApprovedEvent.class.getName() + "," +
                "PaymentCancelledEvent:" + PaymentCancelledEvent.class.getName() + "," +
                "PaymentFailedEvent:" + PaymentFailedEvent.class.getName() + "," +
                "InventoryDeductedFailedEvent:" + InventoryDeductedFailedEvent.class.getName()
        );

        return new DefaultKafkaConsumerFactory<>(props,
            new StringDeserializer(),
            new ErrorHandlingDeserializer<>(new JsonDeserializer<>(objectMapper)));
    }

    @Bean
    public DeadLetterPublishingRecoverer couponDeadLetterPublishingRecoverer(
        @Qualifier("couponKafkaTemplate") KafkaTemplate<String, Object> kafkaTemplate
    ) {
        return new DeadLetterPublishingRecoverer(kafkaTemplate,
            (record, exception) -> {
                log.error("메시지 처리 실패, DLT로 이동: topic={}, error={}", record.topic(),
                    exception.getMessage());
                return new TopicPartition(KafkaTopicConfig.COUPON_TOPIC + ".dlt",
                    record.partition());
            });
    }

    @Bean
    public DefaultErrorHandler couponErrorHandler(
        @Qualifier("couponDeadLetterPublishingRecoverer") DeadLetterPublishingRecoverer recoverer
    ) {
        FixedBackOff backOff = new FixedBackOff(RETRY_INTERVAL_MS, MAX_RETRY_ATTEMPTS);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
        errorHandler.addNotRetryableExceptions(
            DeserializationException.class,
            MessageConversionException.class
        );

        errorHandler.setRetryListeners((record, ex, deliveryAttempt) -> {
            log.warn("메시지 처리 재시도: topic={}, attempt={}, error={}", record.topic(), deliveryAttempt,
                ex.getMessage());
        });

        return errorHandler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> couponKafkaListenerContainerFactory(
        @Qualifier("couponErrorHandler") DefaultErrorHandler errorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
            new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(couponConsumerFactory());
        factory.setConcurrency(3);
        factory.getContainerProperties()
            .setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }
}