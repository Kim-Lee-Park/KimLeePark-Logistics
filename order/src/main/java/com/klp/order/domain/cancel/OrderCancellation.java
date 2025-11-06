package com.klp.order.domain.cancel;

import com.klp.order.domain.order.Order;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_order_cancellation", schema = "order_schema")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderCancellation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID orderCancellationId;

    @OneToOne(mappedBy = "cancellation", fetch = FetchType.LAZY)
    private Order order;

    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    private String cancelReason;

    @Column(name = "cancelled_by")
    private Long cancelledBy;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancel_type", nullable = false)
    private CancelType cancelType;

    public static OrderCancellation create(Order order, String cancelReason, Long cancelledBy,
        CancelType cancelType) {
        OrderCancellation cancellation = new OrderCancellation();
        cancellation.order = order;
        cancellation.cancelReason = cancelReason;
        cancellation.cancelType = cancelType;
        cancellation.cancelledBy = cancelledBy;
        cancellation.cancelledAt = LocalDateTime.now();
        return cancellation;
    }

}
