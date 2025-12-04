package com.klp.payment.payment.presentation;

import com.klp.payment.global.security.model.UserDetailsImpl;
import com.klp.payment.payment.application.PaymentService;
import com.klp.payment.payment.domain.entity.Payment;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
     * 결제 준비 - CUSTOMER만 가능
     */
    @PostMapping("/prepare")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PreparePaymentResponse> preparePayment(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @Valid @RequestBody PreparePaymentRequest request
    ) {
        PreparePaymentResponse response = paymentService.preparePayment(
            request.toCommand(userDetails.getUserId())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 결제 승인 - CUSTOMER만 가능 (본인 결제만)
     */
    @PostMapping("/{paymentId}/approve")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentResponse> approvePayment(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable UUID paymentId,
        @Valid @RequestBody ApprovePaymentRequest request
    ) {
        PaymentResponse response = paymentService.approvePayment(paymentId, request.toCommand());
        return ResponseEntity.ok().body(response);
    }

    /**
     * 단일 결제 상세 조회 - MASTER, HUB(본인 허브), CUSTOMER(본인)
     */
    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB', 'CUSTOMER')")
    public ResponseEntity<PaymentResponse> getPaymentById(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable UUID paymentId
    ) {
        PaymentResponse response = paymentService.getPayment(paymentId);
        return ResponseEntity.ok().body(response);
    }

    /**
     * 모든 결제내역 조회 (페이징) - MASTER: 전체 조회 - HUB: 본인 허브의 결제내역만 조회 - CUSTOMER: 본인의 결제내역만 조회
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('MASTER', 'HUB', 'CUSTOMER')")
    public ResponseEntity<PaymentListResponse> getAllPayments(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PageableDefault Pageable pageable
    ) {
        Page<Payment> payments;
        String role = userDetails.getRole();

        if ("MASTER".equals(role)) {
            payments = paymentService.getAllPayments(pageable);
        } else if ("HUB".equals(role)) {
            payments = paymentService.getPaymentsByHubId(userDetails.getUserId(), pageable);
        } else {
            payments = paymentService.getPaymentsByUserId(userDetails.getUserId(), pageable);
        }

        PaymentListResponse response = PaymentListResponse.from(payments);
        return ResponseEntity.ok().body(response);
    }

    /**
     * 주문ID와 관련된 결제 내역 상세 조회 - MASTER, HUB, CUSTOMER
     */
    @GetMapping("/orders/{orderId}")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB', 'CUSTOMER')")
    public ResponseEntity<List<PaymentResponse>> getPaymentByOrderId(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable UUID orderId
    ) {
        List<PaymentResponse> responses = paymentService.getPaymentsByOrderId(orderId);
        return ResponseEntity.ok().body(responses);
    }

    /**
     * 결제 취소 - CUSTOMER만 가능 (본인 결제만)
     */
    @PostMapping("/{paymentId}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentResponse> cancelPayment(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable UUID paymentId,
        @Valid @RequestBody CancelPaymentRequest request
    ) {
        PaymentResponse response = paymentService.cancelPayment(paymentId, request.toCommand());
        return ResponseEntity.ok().body(response);
    }

    /**
     * 결제 실패 - CUSTOMER만 가능 (본인 결제만)
     */
    @PostMapping("/{paymentId}/fail")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentResponse> failPayment(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable UUID paymentId,
        @Valid @RequestBody FailPaymentRequest request
    ) {
        PaymentResponse response = paymentService.failPayment(paymentId, request.toCommand());
        return ResponseEntity.ok().body(response);
    }
}
