package com.klp.order.domain.entity.orderitem;

import com.klp.common.exception.BusinessException;
import com.klp.order.application.command.OrderItemCommand;
import com.klp.order.common.BaseEntity;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.global.exception.OrderItemErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "p_order_items", schema = "order_schema")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_item_id", nullable = false)
    private UUID orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "hub_id", nullable = false)
    private UUID hubId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "delivery_id")
    private UUID deliveryId;

    private OrderItem(Order order, UUID productId, String productName, UUID hubId, int quantity) {
        validateOrder(order);
        validateProductId(productId);
        validateProductName(productName);
        validateHubId(hubId);
        validateQuantity(quantity);

        this.order = order;
        this.productId = productId;
        this.productName = productName;
        this.hubId = hubId;
        this.quantity = quantity;
    }


    public static OrderItem of(Order order, OrderItemCommand command) {
        return new OrderItem(order, command.productId(), command.productName(), command.hubId(),
            command.quantity());
    }

    private void validateOrder(Order order) {
        if (order == null) {
            throw new BusinessException(OrderItemErrorCode.ORDER_REQUIRED);
        }
    }

    private void validateProductId(UUID productId) {
        if (productId == null) {
            throw new BusinessException(OrderItemErrorCode.PRODUCT_ID_REQUIRED);
        }
    }

    private void validateProductName(String productName) {
        if (productName == null) {
            throw new BusinessException(OrderItemErrorCode.PRODUCT_NAME_REQUIRED);
        }
    }

    private void validateHubId(UUID hubId) {
        if (hubId == null) {
            throw new BusinessException(OrderItemErrorCode.HUB_ID_REQUIRED);
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException(OrderItemErrorCode.QUANTITY_MIN_REQUIRED);
        }
    }

    public void assignDeliveryId(UUID deliveryId) {
        this.deliveryId = deliveryId;
    }

    public void updateQuantity(Integer quantity) {
        validateQuantity(quantity);
        this.quantity = quantity;
    }

}