package com.klp.payment.payment.application;

import com.klp.payment.global.exception.BusinessException;
import com.klp.payment.global.exception.PaymentErrorCode;
import com.klp.payment.infrastructure.client.UserClient;
import com.klp.payment.infrastructure.client.dto.UserDetailResponse;
import com.klp.payment.payment.application.command.ApprovePaymentCommand;
import com.klp.payment.payment.application.command.CancelPaymentCommand;
import com.klp.payment.payment.application.command.FailPaymentCommand;
import com.klp.payment.payment.application.command.PreparePaymentCommand;
import com.klp.payment.payment.domain.entity.Payment;
import com.klp.payment.payment.domain.enums.PaymentMethodType;
import com.klp.payment.payment.domain.repository.PaymentRepository;
import com.klp.payment.payment.infrastructure.PaymentMockService;
import com.klp.payment.payment.infrastructure.PaymentMockService.PaymentMockResult;
import com.klp.payment.payment.presentation.dto.response.PaymentResponse;
import com.klp.payment.payment.presentation.dto.response.PreparePaymentResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMockService paymentMockService;
    private final UserClient userClient;

    /**
     * 결제 준비 메소드. 현재는 카드 결제만 지원
     */
    @Transactional
    public PreparePaymentResponse preparePayment(PreparePaymentCommand command) {
        Payment payment = Payment.create(
            command.orderId(),
            command.userId(),
            command.hubId(),
            PaymentMethodType.CARD,
            command.amount()
        );

        Payment savedPayment = paymentRepository.save(payment);
        return PreparePaymentResponse.from(savedPayment);
    }

    /**
     * paymentId를 바탕으로 결제 관련 응답 생성
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        return PaymentResponse.from(payment);
    }

    /**
     * 모든 결제 리스트 페이징 방식으로 조회 (MASTER 전용)
     */
    @Transactional(readOnly = true)
    public Page<Payment> getAllPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable);
    }

    /**
     * 사용자 ID로 결제 리스트 페이징 방식으로 조회 (CUSTOMER 전용)
     */
    @Transactional(readOnly = true)
    public Page<Payment> getPaymentsByUserId(Long userId, Pageable pageable) {
        return paymentRepository.findByUserId(userId, pageable);
    }

    /**
     * 허브 ID로 결제 리스트 페이징 방식으로 조회 (HUB 전용)
     */
    @Transactional(readOnly = true)
    public Page<Payment> getPaymentsByHubId(Long userId, Pageable pageable) {
        UserDetailResponse response = userClient.getUserDetails(userId);
        UUID hubId = response.affiliationId();
        return paymentRepository.findByHubId(hubId, pageable);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByOrderId(UUID orderId) {
        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        return payments.stream()
            .map(PaymentResponse::from)
            .toList();
    }

    @Transactional
    public PaymentResponse approvePayment(UUID paymentId, ApprovePaymentCommand command) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        // 모킹 서비스를 통해 결제 승인 시뮬레이션 (80% 성공, 20% 실패)
        // 실제 PG사 연동 시 이 부분을 PG사 API 호출로 교체
        PaymentMockResult mockResult = paymentMockService.mockApprovePayment();

        if (mockResult.success()) {
            payment.approve(
                mockResult.pgTransactionId(),
                mockResult.billingKey(),
                command.cardType(),
                command.installmentMonths()
            );
        } else {
            payment.fail(mockResult.failReason());
        }

        return PaymentResponse.from(payment);
    }

    @Transactional
    public PaymentResponse cancelPayment(UUID paymentId, CancelPaymentCommand command) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        // 실제로는 PG사 API를 호출해서 결제 취소 요청을 해야 함
        payment.cancel(command.reason());

        return PaymentResponse.from(payment);
    }

    @Transactional
    public PaymentResponse failPayment(UUID paymentId, FailPaymentCommand command) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        payment.fail(command.reason());

        return PaymentResponse.from(payment);
    }
}
