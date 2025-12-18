package com.klp.user.infrastructure.event;

import com.klp.user.application.event.UserAddressChangedEvent;
import com.klp.user.infrastructure.event.dto.UserAddressChangedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserAddressChangedEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserAddressInfoChangedEvent(UserAddressChangedEvent event) {
        log.info("[UserAddressChangedEventPublisher] AFTER_COMMIT event received. userId={}",
            event.addressId());

        UserAddressChangedMessage msg = new UserAddressChangedMessage(event.addressId(),
            "ADDRESS_UPDATED");

        log.info(
            "[UserAddressChangedEventPublisher] Sending Kafka message. topic={}, key={}, message={}",
            "user.address.changed",
            msg.addressId(),
            msg
        );

        kafkaTemplate.send("user.address.changed", msg.addressId().toString(), msg)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    // 성공
                    log.info(
                        "[UserAddressChangedEventPublisher] Kafka message sent. topic={}, partition={}, offset={}, key={}, message={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        msg.addressId(),
                        msg
                    );
                } else {
                    // 실패
                    log.error(
                        "[UserAddressChangedEventPublisher] Kafka message FAILED. topic={}, key={}, message={}, error={}",
                        "user.address.changed",
                        msg.addressId(),
                        msg,
                        ex.getMessage(),
                        ex
                    );
                }
            });
    }
}
