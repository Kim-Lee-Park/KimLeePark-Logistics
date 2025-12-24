package com.klp.order.domain.entity.cancel;

import com.klp.global.exception.BusinessException;
import com.klp.global.exception.OrderCancellationErrorCode;
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

@Entity
@Table(name = "p_order_cancellation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderCancellation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID orderCancellationId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    private String cancelReason;

    @Column(name = "cancelled_by", nullable = false)
    private Long cancelledBy;

    @Column(name = "cancelled_at", nullable = false)
    private LocalDateTime cancelledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancel_type", nullable = false)
    private CancelType cancelType;

    public static OrderCancellation create(UUID orderId, String cancelReason, Long cancelledBy,
        CancelType cancelType) {
        validateOrder(orderId);
        validateCancelledBy(cancelledBy);
        validateCancelType(cancelType);

        OrderCancellation cancellation = new OrderCancellation();
        cancellation.orderId = orderId;
        cancellation.cancelReason = cancelReason;
        cancellation.cancelType = cancelType;
        cancellation.cancelledBy = cancelledBy;
        cancellation.cancelledAt = LocalDateTime.now();
        return cancellation;
    }

    private static void validateOrder(UUID orderId) {
        if (orderId == null) {
            throw new BusinessException(OrderCancellationErrorCode.ORDER_INFO_REQUIRED);

        }
    }

    private static void validateCancelledBy(Long cancelledBy) {
        if (cancelledBy == null) {
            throw new BusinessException(OrderCancellationErrorCode.CANCELLED_BY_REQUIRED);
        }
    }

    private static void validateCancelType(CancelType cancelType) {
        if (cancelType == null) {
            throw new BusinessException(OrderCancellationErrorCode.CANCEL_TYPE_REQUIRED);
        }
    }
}
