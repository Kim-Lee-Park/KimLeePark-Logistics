package com.klp.hub.inventory.infrastructure.kafka.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.ObservationRegistry;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Slf4j
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private final ObjectMapper objectMapper;
    private final ObservationRegistry observationRegistry;

    public KafkaProducerConfig(ObjectMapper objectMapper, ObservationRegistry observationRegistry) {
        this.objectMapper = objectMapper;
        this.observationRegistry = observationRegistry;
    }

    @Bean
    public ProducerFactory<String, Object> inventoryProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();

        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 10);

        // ⭐ 추가된 부분 - 타입 정보 헤더 활성화
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);
        configProps.put(JsonSerializer.TYPE_MAPPINGS, buildTypeMappings());

        return new DefaultKafkaProducerFactory<>(
            configProps,
            new StringSerializer(),
            new JsonSerializer<>(objectMapper)
        );
    }

    // ⭐ 추가된 메서드 - 타입 매핑 설정
    private String buildTypeMappings() {
        return String.join(",",
            "InventoryDeductedEvent:" +
                com.klp.hub.inventory.domain.event.InventoryDeductedEvent.class.getName()

        );
    }

    @Bean
    public KafkaTemplate<String, Object> inventoryKafkaTemplate() {
        KafkaTemplate<String, Object> kafkaTemplate = new KafkaTemplate<>(
            inventoryProducerFactory());
        kafkaTemplate.setObservationEnabled(true);
        kafkaTemplate.setObservationRegistry(observationRegistry);
        return kafkaTemplate;
    }
}