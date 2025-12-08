package com.klp.user.infrastructure.event;

import com.klp.user.application.event.UserProfileChangedEvent;
import com.klp.user.infrastructure.event.dto.UserProfileChangedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserProfileChangedEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserInfoChangedEvent(UserProfileChangedEvent event) {
        log.info("[UserProfileChangedEventPublisher] AFTER_COMMIT event received. userId={}",
            event.userId());

        UserProfileChangedMessage msg = new UserProfileChangedMessage(event.userId(),
            "PROFILE_UPDATED");

        log.info(
            "[UserProfileChangedEventPublisher] Sending Kafka message. topic={}, key={}, message={}",
            "user.profile.changed",
            msg.userId(),
            msg
        );

        kafkaTemplate.send("user.profile.changed", msg.userId().toString(), msg)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    // 성공
                    log.info(
                        "[UserProfileChangedEventPublisher] Kafka message sent. topic={}, partition={}, offset={}, key={}, message={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        msg.userId(),
                        msg
                    );
                } else {
                    // 실패
                    log.error(
                        "[UserProfileChangedEventPublisher] Kafka message FAILED. topic={}, key={}, message={}, error={}",
                        "user.profile.changed",
                        msg.userId(),
                        msg,
                        ex.getMessage(),
                        ex
                    );
                }
            });
    }
}
