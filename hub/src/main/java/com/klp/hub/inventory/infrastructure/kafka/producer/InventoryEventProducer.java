package com.klp.hub.inventory.infrastructure.kafka.producer;

import com.klp.hub.inventory.domain.event.InventoryDeductedEvent;
import com.klp.hub.inventory.infrastructure.kafka.config.KafkaTopicConfig;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplateAcksAll;

    public void publishInventoryDeductedEvent(InventoryDeductedEvent event) {
        String key = event.orderId().toString();

        CompletableFuture<SendResult<String, Object>> future =
            kafkaTemplateAcksAll.send(KafkaTopicConfig.INVENTORY_EVENTS, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("재고 차감 이벤트 발행 성공: orderId={}", event.orderId());
            } else {
                log.error("재고 차감 이벤트 발행 실패: orderId={}", event.orderId());
            }
        });
    }
}
