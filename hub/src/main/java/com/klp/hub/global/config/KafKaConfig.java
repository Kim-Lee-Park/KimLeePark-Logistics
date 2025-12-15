package com.klp.hub.global.config;

import com.fasterxml.jackson.databind.ser.std.StringSerializer;
import io.micrometer.observation.ObservationRegistry;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
@EnableKafka
@RequiredArgsConstructor
public class KafKaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private final ObservationRegistry observationRegistry;

    @Bean
    public ProducerFactory<String, Object> kafkaProducerFactoryAcks1() {
        Map<String, Object> props = baseProducerConfig();
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public ProducerFactory<String, Object> kafkaProducerFactoryAcksAll() {
        Map<String, Object> props = baseProducerConfig();
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplateAcks1() {
        KafkaTemplate<String, Object> kafkaTemplate = new KafkaTemplate<>(
            kafkaProducerFactoryAcks1());
        kafkaTemplate.setObservationEnabled(true);
        kafkaTemplate.setObservationRegistry(observationRegistry);
        return kafkaTemplate;
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplateAcksAll() {
        KafkaTemplate<String, Object> kafkaTemplate = new KafkaTemplate<>(
            kafkaProducerFactoryAcksAll());
        kafkaTemplate.setObservationEnabled(true);
        kafkaTemplate.setObservationRegistry(observationRegistry);
        return kafkaTemplate;
    }

    @Bean
    public NewTopic productInfoChangedTopic() {
        return TopicBuilder.name("product.info.changed")
            .partitions(3)
            .replicas(1)
            .build();
    }

    private Map<String, Object> baseProducerConfig() {
        Map<String, Object> props = new HashMap<>();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        props.put("spring.json.trusted.packages", "*");

        // acks 설정 제외
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);

        return props;
    }
}
