package com.klp.payment.payment.infrastructure;

import com.klp.payment.payment.domain.entity.Payment;
import com.klp.payment.payment.domain.repository.PaymentRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public Payment save(Payment payment) {
        return paymentJpaRepository.save(payment);
    }

    @Override
    public Optional<Payment> findById(UUID paymentId) {
        return paymentJpaRepository.findById(paymentId);
    }

    @Override
    public List<Payment> findByOrderId(UUID orderId) {
        return paymentJpaRepository.findByOrderId(orderId);
    }

    @Override
    public Page<Payment> findAll(Pageable pageable) {
        return paymentJpaRepository.findAll(pageable);
    }

    @Override
    public Page<Payment> findByUserId(Long userId, Pageable pageable) {
        return paymentJpaRepository.findByUserId(userId, pageable);
    }

    @Override
    public Page<Payment> findByHubId(UUID hubId, Pageable pageable) {
        return paymentJpaRepository.findByHubId(hubId, pageable);
    }
}
