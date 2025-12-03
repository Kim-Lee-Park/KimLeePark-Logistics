package com.klp.payment.payment.domain.repository;

import com.klp.payment.payment.domain.entity.Payment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(UUID paymentId);

    List<Payment> findByOrderId(UUID orderId);

    Page<Payment> findAll(Pageable pageable);
}
