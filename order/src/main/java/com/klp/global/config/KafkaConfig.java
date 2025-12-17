package com.klp.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.order.infrastructure.event.dto.ProductInfoChangedMessage;
import com.klp.order.infrastructure.event.dto.UserProfileChangedMessage;
import com.klp.order.infrastructure.event.dto.UserAddressChangedMessage;
import com.klp.order.infrastructure.event.dto.UserProfileChangedMessage;
import com.klp.order.infrastructure.event.event.CouponCancelledEvent;
import com.klp.order.infrastructure.event.event.CouponCancelledFailedEvent;
import com.klp.order.infrastructure.event.event.CouponUsedEvent;
import com.klp.order.infrastructure.event.event.CouponUsedFailedEvent;
import com.klp.order.infrastructure.event.event.DeliveryArrivedEvent;
import com.klp.order.infrastructure.event.event.DeliveryArrivedFailedEvent;
import com.klp.order.infrastructure.event.event.DeliveryCreatedEvent;
import com.klp.order.infrastructure.event.event.DeliveryCreatedFailedEvent;
import com.klp.order.infrastructure.event.event.DeliveryShippingEvent;
import com.klp.order.infrastructure.event.event.DeliveryShippingFailedEvent;
import com.klp.order.infrastructure.event.event.InventoryDeductedEvent;
import com.klp.order.infrastructure.event.event.InventoryDeductedFailedEvent;
import com.klp.order.infrastructure.event.event.InventoryReplenishedEvent;
import com.klp.order.infrastructure.event.event.InventoryReplenishedFailedEvent;
import com.klp.order.infrastructure.event.event.OrderCancelledEvent;
import com.klp.order.infrastructure.event.event.OrderCreatedEvent;
import com.klp.order.infrastructure.event.event.OrderPaidEvent;
import com.klp.order.infrastructure.event.event.PaymentApprovedEvent;
import com.klp.order.infrastructure.event.event.PaymentCancelledEvent;
import com.klp.order.infrastructure.event.event.PaymentCancelledFailedEvent;
import io.micrometer.observation.ObservationRegistry;
import com.klp.order.infrastructure.event.event.PaymentFailedEvent;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

@EnableKafka
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private final ObjectMapper objectMapper;
    private final ObservationRegistry observationRegistry;

    public KafkaConfig(ObjectMapper objectMapper, ObservationRegistry observationRegistry) {
        this.objectMapper = objectMapper;
        this.observationRegistry = observationRegistry;
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        // 기본 설정
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // 신뢰성 설정
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);
        configProps.put(JsonSerializer.TYPE_MAPPINGS,
            "OrderCreatedEvent:" + OrderCreatedEvent.class.getName() + "," +
                "OrderCancelledEvent:" + OrderCancelledEvent.class.getName() + "," +
                "OrderPaidEvent:" + OrderPaidEvent.class.getName());

        return new DefaultKafkaProducerFactory<>(configProps,
            new StringSerializer(),
            new JsonSerializer<>(objectMapper));
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        KafkaTemplate<String, Object> kafkaTemplate = new KafkaTemplate<>(producerFactory());
        kafkaTemplate.setObservationEnabled(true);
        kafkaTemplate.setObservationRegistry(observationRegistry);
        return new KafkaTemplate<>(producerFactory());
    }

    // consumer 설정
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();

        // 기본 설정
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "order-service-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, true);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, Object.class);
        props.put(JsonDeserializer.TYPE_MAPPINGS,
            "PaymentApprovedEvent:" + PaymentApprovedEvent.class.getName() + "," +
                "PaymentCancelledEvent:" + PaymentCancelledEvent.class.getName() + "," +
                "PaymentFailedEvent:" + PaymentFailedEvent.class.getName() + "," +
                "PaymentCancelledFailedEvent:" + PaymentCancelledFailedEvent.class.getName() + "," +
                "InventoryDeductedEvent:" + InventoryDeductedEvent.class.getName() + "," +
                "InventoryDeductedFailedEvent:" + InventoryDeductedFailedEvent.class.getName() + ","
                +
                "InventoryReplenishedEvent:" + InventoryReplenishedEvent.class.getName() + "," +
                "InventoryReplenishedFailedEvent:" + InventoryReplenishedFailedEvent.class.getName()
                + "," +
                "DeliveryCreatedEvent:" + DeliveryCreatedEvent.class.getName() + "," +
                "DeliveryCreatedFailedEvent:" + DeliveryCreatedFailedEvent.class.getName() + "," +
                "DeliveryShippingEvent:" + DeliveryShippingEvent.class.getName() + "," +
                "DeliveryShippingFailedEvent:" + DeliveryShippingFailedEvent.class.getName() + "," +
                "DeliveryArrivedEvent:" + DeliveryArrivedEvent.class.getName() + "," +
                "DeliveryArrivedFailedEvent:" + DeliveryArrivedFailedEvent.class.getName() + "," +
                "CouponUsedEvent:" + CouponUsedEvent.class.getName() + "," +
                "CouponUsedFailedEvent:" + CouponUsedFailedEvent.class.getName() + "," +
                "CouponCancelledEvent:" + CouponCancelledEvent.class.getName() + "," +
                "CouponCancelledFailedEvent:" + CouponCancelledFailedEvent.class.getName() + "," +
                "UserProfileChangedMessage:" + UserProfileChangedMessage.class.getName() + "," +
                "ProductInfoChangedMessage:" + ProductInfoChangedMessage.class.getName());

        // 수동 커밋 설정
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(props,
            new StringDeserializer(),
            new ErrorHandlingDeserializer<>(new JsonDeserializer<>(objectMapper)));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
            new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(errorHandler());

        factory.getContainerProperties().setObservationEnabled(true);

        return factory;
    }

    @Bean
    public DefaultErrorHandler errorHandler() {
        // 최대 3번 재시도, 초기 1초 간격
        FixedBackOff fixedBackOff = new FixedBackOff(1000L, 3L);
        return new DefaultErrorHandler(fixedBackOff);
    }

    @Bean
    public Map<String, Object> commonConsumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "order-service-group");

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
            org.apache.kafka.common.serialization.StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
            org.springframework.kafka.support.serializer.JsonDeserializer.class);

        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.klp.order.infrastructure.event.dto.*");
//        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        return props;
    }

    @Bean
    public ConsumerFactory<String, UserProfileChangedMessage> userProfileChangedConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
            commonConsumerConfigs(),
            new org.apache.kafka.common.serialization.StringDeserializer(),
            new ErrorHandlingDeserializer<>(new JsonDeserializer<>(UserProfileChangedMessage.class))
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserProfileChangedMessage>
    userProfileChangedKafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, UserProfileChangedMessage> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(userProfileChangedConsumerFactory());

        factory.getContainerProperties().setObservationEnabled(true);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, ProductInfoChangedMessage> productInfoChangedConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
            commonConsumerConfigs(),
            new org.apache.kafka.common.serialization.StringDeserializer(),
            new ErrorHandlingDeserializer<>(new JsonDeserializer<>(ProductInfoChangedMessage.class))
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProductInfoChangedMessage> productInfoChangedKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ProductInfoChangedMessage> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(productInfoChangedConsumerFactory());

        factory.getContainerProperties().setObservationEnabled(true);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, UserAddressChangedMessage> userAddressChangedConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
            commonConsumerConfigs(),
            new org.apache.kafka.common.serialization.StringDeserializer(),
            new ErrorHandlingDeserializer<>(new JsonDeserializer<>(UserAddressChangedMessage.class))
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserAddressChangedMessage> userAddressChangedKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserAddressChangedMessage> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(userAddressChangedConsumerFactory());

        factory.getContainerProperties().setObservationEnabled(true);
        return factory;
    }
}