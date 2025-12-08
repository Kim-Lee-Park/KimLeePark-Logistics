package com.klp.hub.product.infrastructure.event;

import com.klp.hub.product.application.event.ProductInfoChangedEvent;
import com.klp.hub.product.infrastructure.event.dto.ProductInfoChangedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductInfoChangedEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplateAcks1;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductInfoChangedEvent(ProductInfoChangedEvent event) {
        log.info("[ProductInfoChangedEventPublisher] AFTER_COMMIT event received. productId={}",
            event.productId());

        ProductInfoChangedMessage msg = new ProductInfoChangedMessage(
            event.productId(),
            "PRODUCT_UPDATED"
        );

        String topic = "product.info.changed";

        log.info(
            "[ProductInfoChangedEventPublisher] Sending Kafka message. topic={}, key={}, message={}",
            topic,
            msg.productId(),
            msg
        );

        kafkaTemplateAcks1.send(topic, msg.productId().toString(), msg)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info(
                        "[ProductInfoChangedEventPublisher] Kafka message sent. topic={}, partition={}, offset={}, key={}, message={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        msg.productId(),
                        msg
                    );
                } else {
                    log.error(
                        "[ProductInfoChangedEventPublisher] Kafka message FAILED. topic={}, key={}, message={}, error={}",
                        topic,
                        msg.productId(),
                        msg,
                        ex.getMessage(),
                        ex
                    );
                }
            });
    }
}
