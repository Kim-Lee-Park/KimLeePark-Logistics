package com.klp.payment.payment.presentation.dto.response;

import com.klp.payment.payment.domain.entity.Payment;
import com.klp.payment.payment.domain.enums.PaymentMethodType;
import com.klp.payment.payment.domain.enums.PaymentStatus;
import java.util.UUID;

public record PreparePaymentResponse(
    UUID paymentId,
    UUID orderId,
    Long amount,
    PaymentMethodType method,
    PaymentStatus status
) {

    public static PreparePaymentResponse from(Payment payment) {
        return new PreparePaymentResponse(
            payment.getPaymentId(),
            payment.getOrderId(),
            payment.getAmount(),
            payment.getMethod(),
            payment.getStatus()
        );
    }
}
