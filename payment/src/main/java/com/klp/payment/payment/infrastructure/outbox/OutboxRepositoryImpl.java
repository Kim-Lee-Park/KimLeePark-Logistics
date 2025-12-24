package com.klp.payment.payment.infrastructure.outbox;

import com.klp.payment.payment.domain.outbox.PaymentOutbox;
import com.klp.payment.payment.domain.repository.PaymentOutboxRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class OutboxRepositoryImpl implements PaymentOutboxRepository {

    private final OutboxJpaRepository outboxJpaRepository;


    @Override
    public PaymentOutbox save(PaymentOutbox outbox) {
        return outboxJpaRepository.save(outbox);
    }

    @Override
    public List<PaymentOutbox> findPendingEvents(int limit) {
        return outboxJpaRepository.findPendingEvents(limit);
    }

    @Override
    @Transactional
    public void markAsPublished(UUID paymentOutboxId) {
        outboxJpaRepository.markAsPublished(paymentOutboxId);
    }

    @Override
    @Transactional
    public void markAsFailed(UUID paymentOutboxId) {
        outboxJpaRepository.markAsFailed(paymentOutboxId);
    }
}
