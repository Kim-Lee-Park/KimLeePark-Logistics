package com.klp.order.domain.entity.orderitem;

import com.klp.order.domain.entity.order.Order;
import com.klp.order.global.entity.BaseEntity;
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

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "delivery_id")
    private UUID deliveryId;

    private OrderItem(Order order, UUID productId, int quantity) {
        validateOrder(order);
        validateProductId(productId);
        validateQuantity(quantity);

        this.order = order;
        this.productId = productId;
        this.quantity = quantity;
    }


    public static OrderItem of(Order order, OrderItemCommand command) {
        return new OrderItem(order, command.productId(), command.quantity());
    }

    private void validateOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("주문은 필수입니다.");
        }
    }

    private void validateProductId(UUID productId) {
        if (productId == null) {
            throw new IllegalArgumentException("상품 ID는 필수입니다.");
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("주문 수량은 1개 이상이어야 합니다.");
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
