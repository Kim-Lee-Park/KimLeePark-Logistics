package com.klp.order.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.klp.common.exception.BusinessException;
import com.klp.order.application.command.OrderItemCommand;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.entity.orderitem.OrderItem;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("OrderItem 엔티티 테스트")
public class OrderItemTest {

    private UUID productId;
    private int quantity;
    private Order order;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        quantity = 10;
        List<OrderItemCommand> initialItems = List.of(
            new OrderItemCommand(UUID.randomUUID(), 1)
        );
        order = Order.create(1L, 2L, "테스트 주문", initialItems);
    }

    @Test
    @DisplayName("OrderItem 생성 - 정상")
    void createOrderItem_Success() {
        // given
        OrderItemCommand command = new OrderItemCommand(productId, quantity);

        // when
        OrderItem orderItem = OrderItem.of(order, command);

        // then
        assertThat(orderItem.getProductId()).isEqualTo(productId);
        assertThat(orderItem.getQuantity()).isEqualTo(quantity);
        assertThat(orderItem.getOrder()).isEqualTo(order);
        assertThat(orderItem.getDeliveryId()).isNull();
    }

    @Test
    @DisplayName("OrderItem 생성 - productId null이면 예외")
    void createOrderItem_Fail_ProductId_is_Null() {
        // given
        OrderItemCommand command = new OrderItemCommand(null, quantity);

        // when & then
        assertThatThrownBy(() -> OrderItem.of(order, command))
            .isInstanceOf(BusinessException.class)
            .hasMessage("상품 ID는 필수입니다.");
    }

    @Test
    @DisplayName("OrderItem 생성 - quantity가 0 이하면 예외")
    void createOrderItem_Fail_Quantity_is_Zero_or_Negative() {
        // when & then
        assertThatThrownBy(() -> OrderItem.of(order, new OrderItemCommand(productId, 0)))
            .isInstanceOf(BusinessException.class)
            .hasMessage("주문 수량은 1개 이상이어야 합니다.");

        assertThatThrownBy(() -> OrderItem.of(order, new OrderItemCommand(productId, -5)))
            .isInstanceOf(BusinessException.class)
            .hasMessage("주문 수량은 1개 이상이어야 합니다.");
    }

    @Test
    @DisplayName("OrderItem 생성 - Order가 null이면 예외")
    void createOrderItem_Fail_Order_is_Null() {
        // given
        OrderItemCommand command = new OrderItemCommand(productId, quantity);

        // when & then
        assertThatThrownBy(() -> OrderItem.of(null, command))
            .isInstanceOf(BusinessException.class)
            .hasMessage("주문은 필수입니다.");
    }

    @Test
    @DisplayName("배송 ID 할당 - 정상")
    void assignDeliveryId_Success() {
        // given
        OrderItemCommand command = new OrderItemCommand(productId, quantity);
        OrderItem orderItem = OrderItem.of(order, command);
        UUID deliveryId = UUID.randomUUID();

        // when
        orderItem.assignDeliveryId(deliveryId);

        // then
        assertThat(orderItem.getDeliveryId()).isEqualTo(deliveryId);
    }

    @Test
    @DisplayName("수량 수정 - 정상")
    void updateQuantity_Success() {
        // given
        OrderItemCommand command = new OrderItemCommand(productId, quantity);
        OrderItem orderItem = OrderItem.of(order, command);
        int newQuantity = 100;

        // when
        orderItem.updateQuantity(newQuantity);

        // then
        assertThat(orderItem.getQuantity()).isEqualTo(newQuantity);
    }

    @Test
    @DisplayName("수량 수정 - 0 이하면 예외")
    void updateQuantity_Fail_When_Zero_or_Negative() {
        // given
        OrderItemCommand command = new OrderItemCommand(productId, quantity);
        OrderItem orderItem = OrderItem.of(order, command);

        // when & then
        assertThatThrownBy(() -> orderItem.updateQuantity(0))
            .isInstanceOf(BusinessException.class)
            .hasMessage("주문 수량은 1개 이상이어야 합니다.");

        assertThatThrownBy(() -> orderItem.updateQuantity(-5))
            .isInstanceOf(BusinessException.class)
            .hasMessage("주문 수량은 1개 이상이어야 합니다.");
    }
}