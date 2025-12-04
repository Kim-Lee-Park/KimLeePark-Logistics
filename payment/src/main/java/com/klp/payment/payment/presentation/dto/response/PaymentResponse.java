package com.klp.payment.payment.presentation.dto.response;

import com.klp.payment.payment.domain.entity.Payment;
import com.klp.payment.payment.domain.enums.CardType;
import com.klp.payment.payment.domain.enums.PaymentMethodType;
import com.klp.payment.payment.domain.enums.PaymentStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
    UUID paymentId,
    UUID orderId,
    String pgTransactionId,
    PaymentMethodType method,
    CardType cardType,
    String billingKey,
    Integer installmentMonths,
    Long amount,
    PaymentStatus status,
    String reason,
    LocalDateTime paidAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
            payment.getPaymentId(),
            payment.getOrderId(),
            payment.getPgTransactionId(),
            payment.getMethod(),
            payment.getCardType(),
            maskBillingKey(payment.getBillingKey()),
            payment.getInstallmentMonths(),
            payment.getAmount(),
            payment.getStatus(),
            payment.getReason(),
            payment.getPaidAt(),
            payment.getCreatedAt(),
            payment.getUpdatedAt()
        );
    }

    /**
     * 빌링키 마스킹 처리 (개인정보 보호)
     * 예: "billing_key_1234567890" -> "billing_***********890"
     */
    private static String maskBillingKey(String billingKey) {
        if (billingKey == null || billingKey.length() <= 10) {
            return billingKey;
        }
        int visibleLength = 3;
        String prefix = billingKey.substring(0, Math.min(8, billingKey.length()));
        String suffix = billingKey.substring(billingKey.length() - visibleLength);
        int maskLength = billingKey.length() - prefix.length() - suffix.length();
        return prefix + "*".repeat(maskLength) + suffix;
    }
}
