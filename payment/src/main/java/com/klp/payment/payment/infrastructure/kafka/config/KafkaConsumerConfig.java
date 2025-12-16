package com.klp.payment.payment.infrastructure.kafka.config;

import com.klp.payment.payment.domain.event.CouponUsedFailedEvent;
import com.klp.payment.payment.domain.event.InventoryDeductedFailedEvent;
import com.klp.payment.payment.domain.event.OrderCancelledEvent;
import com.klp.payment.payment.domain.event.OrderCreatedEvent;
import com.klp.payment.payment.domain.event.PaymentApprovedEvent;
import com.klp.payment.payment.domain.event.PaymentCancelledEvent;
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
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_INTERVAL_MS = 1000L;

    @Bean
    public ConsumerFactory<String, Object> paymentConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "payment-service-group");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.klp.*");
        configProps.put(JsonDeserializer.TYPE_MAPPINGS, buildTypeMappings());

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    private String buildTypeMappings() {
        return String.join(",",
            "OrderCreatedEvent:" + OrderCreatedEvent.class.getName(),
            "OrderCancelledEvent:" + OrderCancelledEvent.class.getName(),
            "PaymentApprovedEvent:" + PaymentApprovedEvent.class.getName(),
            "PaymentCancelledEvent:" + PaymentCancelledEvent.class.getName(),
            "CouponUseFailedEvent:" + CouponUsedFailedEvent.class.getName(),
            "InventoryDeductedFailedEvent:" + InventoryDeductedFailedEvent.class.getName()
        );
    }

    @Bean
    public DeadLetterPublishingRecoverer paymentDeadLetterPublishingRecoverer(
        @Qualifier("paymentKafkaTemplate") KafkaTemplate<String, Object> kafkaTemplate
    ) {
        return new DeadLetterPublishingRecoverer(kafkaTemplate,
            (record, exception) -> {
                log.error("메시지 처리 실패, DLT로 이동: topic={}, error={}", record.topic(),
                    exception.getMessage());
                return new TopicPartition(KafkaTopicConfig.PAYMENT_DLT, record.partition());
            });
    }

    @Bean
    public DefaultErrorHandler paymentErrorHandler(
        @Qualifier("paymentDeadLetterPublishingRecoverer") DeadLetterPublishingRecoverer recoverer
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
    public ConcurrentKafkaListenerContainerFactory<String, Object> paymentKafkaListenerContainerFactory(
        @Qualifier("paymentErrorHandler") DefaultErrorHandler errorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
            new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(paymentConsumerFactory());
        factory.setConcurrency(3);
        factory.setCommonErrorHandler(errorHandler);

        factory.getContainerProperties().setObservationEnabled(true);

        return factory;
    }
}
