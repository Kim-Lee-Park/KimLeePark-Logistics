package com.klp.payment.payment.presentation.dto.response;

import com.klp.payment.payment.domain.entity.Payment;
import java.util.List;
import org.springframework.data.domain.Page;

public record PaymentListResponse(
    List<PaymentResponse> payments,
    int currentPage,
    int totalPages,
    long totalElements,
    int pageSize
) {

    public static PaymentListResponse from(Page<Payment> paymentPage) {
        List<PaymentResponse> payments = paymentPage.getContent().stream()
            .map(PaymentResponse::from)
            .toList();

        return new PaymentListResponse(
            payments,
            paymentPage.getNumber(),
            paymentPage.getTotalPages(),
            paymentPage.getTotalElements(),
            paymentPage.getSize()
        );
    }
}
