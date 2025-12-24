package com.klp.order.domain.entity.idempotencykey;

import com.klp.global.exception.BusinessException;
import com.klp.global.exception.OrderOutboundRequestErrorCode;
import com.klp.order.domain.entity.order.Order;
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
@Table(name = "p_order_outbound_request")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderOutboundRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "request_id")
    private UUID requestId;

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
            throw new BusinessException(OrderOutboundRequestErrorCode.OREDER_REQUIRED);
        }
    }

    private void validateIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new BusinessException(OrderOutboundRequestErrorCode.IDEMPOTENCY_KEY_REQUIRED);
        }
    }

    private void validateTarget(Target target) {
        if (target == null) {
            throw new BusinessException(OrderOutboundRequestErrorCode.TARGET_REQUIRED);
        }
    }

    private void validateOperation(OperationType operation) {
        if (operation == null) {
            throw new BusinessException(OrderOutboundRequestErrorCode.OPERATION_REQUIRED);
        }
    }
}
