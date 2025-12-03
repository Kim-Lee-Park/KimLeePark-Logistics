package com.klp.payment.payment.presentation;

import com.klp.payment.payment.application.PaymentService;
import com.klp.payment.payment.presentation.dto.request.ApprovePaymentRequest;
import com.klp.payment.payment.presentation.dto.request.CancelPaymentRequest;
import com.klp.payment.payment.presentation.dto.request.FailPaymentRequest;
import com.klp.payment.payment.presentation.dto.request.PreparePaymentRequest;
import com.klp.payment.payment.presentation.dto.response.PaymentListResponse;
import com.klp.payment.payment.presentation.dto.response.PaymentResponse;
import com.klp.payment.payment.presentation.dto.response.PreparePaymentResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 결제 준비
     */
    @PostMapping("/prepare")
    public ResponseEntity<PreparePaymentResponse> preparePayment(
        @Valid @RequestBody PreparePaymentRequest request
    ) {
        PreparePaymentResponse response = paymentService.preparePayment(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 결제 승인
     */
    @PostMapping("/{paymentId}/approve")
    public ResponseEntity<PaymentResponse> approvePayment(
        @PathVariable UUID paymentId,
        @Valid @RequestBody ApprovePaymentRequest request
    ) {
        PaymentResponse response = paymentService.approvePayment(paymentId, request.toCommand());
        return ResponseEntity.ok(response);
    }

    /**
     * 단일 결제 상세 조회
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable UUID paymentId) {
        PaymentResponse response = paymentService.getPayment(paymentId);
        return ResponseEntity.ok(response);
    }

    /**
     * 모든 결제내역 조회
     */
    @GetMapping
    public ResponseEntity<PaymentListResponse> getAllPayments(
        @PageableDefault Pageable pageable
    ) {
        Page<com.klp.payment.payment.domain.entity.Payment> payments = paymentService.getAllPayments(pageable);
        PaymentListResponse response = PaymentListResponse.from(payments);
        return ResponseEntity.ok(response);
    }

    /**
     * 주문ID와 관련된 결제 내역 상세 조회
     */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentByOrderId(@PathVariable UUID orderId) {
        List<PaymentResponse> responses = paymentService.getPaymentsByOrderId(orderId);
        return ResponseEntity.ok(responses);
    }

    /**
     * 결제 취소
     */
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(
        @PathVariable UUID paymentId,
        @Valid @RequestBody CancelPaymentRequest request
    ) {
        PaymentResponse response = paymentService.cancelPayment(paymentId, request.toCommand());
        return ResponseEntity.ok(response);
    }

    /**
     * 결제 실패
     */
    @PostMapping("/{paymentId}/fail")
    public ResponseEntity<PaymentResponse> failPayment(
        @PathVariable UUID paymentId,
        @Valid @RequestBody FailPaymentRequest request
    ) {
        PaymentResponse response = paymentService.failPayment(paymentId, request.toCommand());
        return ResponseEntity.ok(response);
    }
}
