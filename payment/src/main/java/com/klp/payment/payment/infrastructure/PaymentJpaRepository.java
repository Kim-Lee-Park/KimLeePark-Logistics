package com.klp.payment.payment.infrastructure;

import com.klp.payment.payment.domain.entity.Payment;
import com.klp.payment.payment.domain.enums.PaymentStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByOrderId(UUID orderId);

    Page<Payment> findByUserId(Long userId, Pageable pageable);

    Page<Payment> findByHubId(UUID hubId, Pageable pageable);

    boolean existsByOrderId(UUID orderId);

    Optional<Payment> findFirstByOrderIdAndStatus(UUID orderId, PaymentStatus status);
}
