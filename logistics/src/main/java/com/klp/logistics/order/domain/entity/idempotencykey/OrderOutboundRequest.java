package com.klp.logistics.order.domain.entity.idempotencykey;

import com.klp.logistics.order.domain.entity.order.Order;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_order_outbound_request", schema = "order_schema")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderOutboundRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "reqeust_id")
    private UUID reqeustId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "target", nullable = false)
    private Target target;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation", nullable = false)
    private OperationType operation;


    public static OrderOutboundRequest create(Order order, String idempotencyKey,
        Target target, OperationType operation) {
        OrderOutboundRequest request = new OrderOutboundRequest();

        request.validateOrder(order);
        request.validateIdempotencyKey(idempotencyKey);
        request.validateTarget(target);
        request.validateOperation(operation);

        request.order = order;
        request.idempotencyKey = idempotencyKey;
        request.target = target;
        request.operation = operation;
        return request;
    }


    private void validateOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("주문 정보는 필수입니다.");
        }
    }

    private void validateIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("멱등키는 필수입니다.");
        }
    }

    private void validateTarget(Target target) {
        if (target == null) {
            throw new IllegalArgumentException("요청 대상은 필수입니다.");
        }
    }

    private void validateOperation(OperationType operation) {
        if (operation == null) {
            throw new IllegalArgumentException("요청 작업은 필수입니다.");
        }
    }
}
