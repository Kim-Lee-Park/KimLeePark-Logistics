package com.klp.payment.payment.domain.entity;

import com.klp.payment.common.model.BaseEntity;
import com.klp.payment.payment.domain.enums.CardType;
import com.klp.payment.payment.domain.enums.PaymentMethodType;
import com.klp.payment.payment.domain.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Getter
@Table(name = "p_payments", schema = "payment_schema")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id")
    @Comment("결제 ID")
    private UUID paymentId;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    @Comment("결제 요청 사용자 ID")
    private Long userId;

    @Comment("허브 ID")
    private UUID hubId;

    @Comment("PG사 거래 ID")
    private String pgTransactionId;

    @Column(nullable = false)
    @Comment("결제 수단")
    @Enumerated(EnumType.STRING)
    private PaymentMethodType method;

    @Comment("카드 종류")
    @Enumerated(EnumType.STRING)
    private CardType cardType;

    // 실제 카드번호 대신 토큰 사용
    @Comment("PG사 발급 빌링키(카드 토큰)")
    private String billingKey;

    @Comment("할부 개월수")
    private Integer installmentMonths;

    @Column(nullable = false)
    @Comment("결제 금액")
    private Long amount;

    @Column(nullable = false)
    @Comment("결제 상태")
    private PaymentStatus status;

    @Comment("실패/취소 사유")
    private String reason;

    @Comment("결제 완료 시각")
    private LocalDateTime paidAt;

    public static Payment create(UUID orderId, Long userId, UUID hubId, PaymentMethodType methodType, Long amount) {
        Payment payment = new Payment();

        payment.orderId = orderId;
        payment.userId = userId;
        payment.hubId = hubId;
        payment.method = methodType;
        payment.amount = amount;
        payment.status = PaymentStatus.READY;

        return payment;
    }

    /**
     * 결제 승인
     */
    public void approve(String pgTransactionId, String billingKey, CardType cardType, Integer installmentMonths) {
        validateApprovalPossible();
        this.pgTransactionId = pgTransactionId;
        this.billingKey = billingKey;
        this.cardType = cardType;
        this.installmentMonths = installmentMonths;
        this.status = PaymentStatus.APPROVED;
        this.paidAt = LocalDateTime.now();
    }

    /**
     * 결제 취소
     */
    public void cancel(String reason) {
        validateCancelPossible();
        this.status = PaymentStatus.CANCELLED;
        this.reason = reason;
    }

    /**
     * 결제 실패
     */
    public void fail(String reason) {
        this.status = PaymentStatus.FAILED;
        this.reason = reason;
    }

    /**
     * 결제 대기 상태로 변경
     */
    public void pending() {
        if (this.status != PaymentStatus.READY) {
            throw new IllegalStateException("READY 상태에서만 PENDING으로 변경 가능합니다.");
        }
        this.status = PaymentStatus.PENDING;
    }

    private void validateApprovalPossible() {
        if (this.status != PaymentStatus.READY && this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("READY 또는 PENDING 상태에서만 승인 가능합니다.");
        }
    }

    private void validateCancelPossible() {
        if (this.status != PaymentStatus.APPROVED) {
            throw new IllegalStateException("승인된 결제만 취소 가능합니다.");
        }
    }
}
