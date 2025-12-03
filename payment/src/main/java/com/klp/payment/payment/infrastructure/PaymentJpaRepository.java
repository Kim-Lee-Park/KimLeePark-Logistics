package com.klp.payment.payment.infrastructure;

import com.klp.payment.payment.domain.entity.Payment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByOrderId(UUID orderId);
}
