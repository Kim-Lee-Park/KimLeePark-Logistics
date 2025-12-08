package com.klp.payment.payment.domain.repository;

import com.klp.payment.payment.domain.outbox.PaymentOutbox;
import java.util.List;
import java.util.UUID;

public interface PaymentOutboxRepository {

    PaymentOutbox save(PaymentOutbox outbox);

    List<PaymentOutbox> findPendingEvents(int limit);

    void markAsPublished(UUID paymentOutboxId);

    void markAsFailed(UUID paymentOutboxId);
}
