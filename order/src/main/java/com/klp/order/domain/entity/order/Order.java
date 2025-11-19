package com.klp.order.domain.entity.order;

import com.klp.common.exception.BusinessException;
import com.klp.order.application.command.OrderItemCommand;
import com.klp.order.common.BaseEntity;
import com.klp.order.domain.entity.cancel.CancelType;
import com.klp.order.domain.entity.cancel.OrderCancellation;
import com.klp.order.domain.entity.idempotencykey.OrderOutboundRequest;
import com.klp.order.domain.entity.orderitem.OrderItem;
import com.klp.order.global.exception.OrderErrorCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "p_orders", schema = "order_schema")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private OrderCancellation cancellation;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderOutboundRequest> outboundRequests = new ArrayList<>();


    public static Order create(Long supplierId, Long customerId,
        String comment,
        List<OrderItemCommand> itemCommands) {
        Order order = new Order();
        order.validateSupplierId(supplierId);
        order.validateCustomerId(customerId);
        order.validateItemCommands(itemCommands);

        order.supplierId = supplierId;
        order.customerId = customerId;
        order.comment = comment;
        order.orderStatus = OrderStatus.ING;

        for (OrderItemCommand command : itemCommands) {
            OrderItem orderItem = OrderItem.of(
                order, command);
            order.orderItems.add(orderItem);
        }

        return order;
    }

    public void updateOrder(String comment, List<OrderItemCommand> itemCommands) {
        checkCanUpdate();
        this.comment = comment;

        if (itemCommands != null && !itemCommands.isEmpty()) {
            this.orderItems.clear();
            for (OrderItemCommand command : itemCommands) {
                OrderItem orderItem = OrderItem.of(this, command);
                this.orderItems.add(orderItem);
            }
        }
    }

    public void changeStatus(OrderStatus newStatus) {
        if (this.orderStatus == OrderStatus.CANCELLED) {
            throw new BusinessException(OrderErrorCode.CANNOT_CHANGE_CANCELLED_ORDER_STATUS);
        }
        if (newStatus == null) {
            throw new BusinessException(OrderErrorCode.ORDER_STATUS_REQUIRED);
        }
        this.orderStatus = newStatus;
    }

    public OrderCancellation cancel(String cancelReason,
        Long cancelledBy, CancelType cancelType) {
        checkCanCancel();
        this.orderStatus = OrderStatus.CANCELLED;
        this.cancellation = OrderCancellation.create(this, cancelReason, cancelledBy, cancelType);
        return this.cancellation;
    }

    private void validateSupplierId(Long supplierId) {
        if (supplierId == null) {
            throw new BusinessException(OrderErrorCode.SUPPLIER_ID_REQUIRED);
        }
    }

    private void validateCustomerId(Long customerId) {
        if (customerId == null) {
            throw new BusinessException(OrderErrorCode.CUSTOMER_ID_REQUIRED);
        }
    }

    private void validateItemCommands(List<OrderItemCommand> itemCommands) {
        if (itemCommands == null) {
            throw new BusinessException(OrderErrorCode.ORDER_ITEMS_REQUIRED);
        }
        if (itemCommands.isEmpty()) {
            throw new BusinessException(OrderErrorCode.ORDER_ITEMS_MIN_REQUIRED);
        }
    }


    private void checkCanCancel() {
        if (this.orderStatus == OrderStatus.DELIVERY_ASSIGNED) {
            throw new BusinessException(OrderErrorCode.CANNOT_CANCEL_DELIVERY_ASSIGNED);
        }
        if (this.orderStatus == OrderStatus.COMPLETE) {
            throw new BusinessException(OrderErrorCode.CANNOT_CANCEL_COMPLETED_ORDER);
        }
        if (this.orderStatus == OrderStatus.CANCELLED) {
            throw new BusinessException(OrderErrorCode.ALREADY_CANCELLED_ORDER);
        }
    }

    private void checkCanUpdate() {
        if (this.orderStatus == OrderStatus.DELIVERY_ASSIGNED) {
            throw new BusinessException(OrderErrorCode.CANNOT_UPDATE_DELIVERY_ASSIGNED);
        }
        if (this.orderStatus == OrderStatus.CANCELLED) {
            throw new BusinessException(OrderErrorCode.CANNOT_UPDATE_CANCELLED_ORDER);
        }
        if (this.orderStatus == OrderStatus.COMPLETE) {
            throw new BusinessException(OrderErrorCode.CANNOT_UPDATE_COMPLETED_ORDER);
        }
    }
}
