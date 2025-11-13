package com.klp.order.payment.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "p_payment", schema = "order_schema")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Comment("주문 ID")
    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Comment("총 결제 금액")
    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Comment("결제 상태")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    public Payment(UUID orderId, BigDecimal totalAmount) {
        if (totalAmount == null) {
            throw new IllegalArgumentException("결제 금액은 필수값입니다.");
        }

        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("결제 금액은 0 이상이어야 합니다.");
        }

        if (orderId == null) {
            throw new IllegalArgumentException("주문 ID 는 필수값입니다");
        }

        this.totalAmount = totalAmount;
        this.orderId = orderId;
        this.status = PaymentStatus.PENDING;
    }

    public void completed() {
        this.status = PaymentStatus.COMPLETED;
    }
}
