package com.klp.order.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("OrderItems 엔티티 테스트")
public class OrderItemTest {

    @Test
    @DisplayName("OrderItem 생성 - 정상")
    void createOrderItem_Success() {

        //given
        UUID productId = UUID.randomUUID();
        int quantity = 1;

        //when
        OrderItem orderItem = new OrderItem(productId, quantity);

        //then
        assertThat(orderItem.getProductId()).isEqualTo(productId);
        assertThat(orderItem.getQuantity()).isEqualTo(quantity);
        assertThat(orderItem.getOrderItemId()).isNotNull();
        assertThat(orderItem.getDeliveryId()).isNull();
    }

    @Test
    @DisplayName("OrderItem 생성 - productId null이면 예외")
    void createOrderItem_Fail_ProductId_is_Null() {
        // given
        UUID productId = null;
        int quantity = 10;

        // when & then
        assertThatThrownBy(() -> new OrderItem(productId, quantity))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("상품 ID는 필수입니다.");
    }

    @Test
    @DisplayName("OrderItem 생성 - quantity가 0 이하면 예외 - 0 포함")
    void createOrderItem_Fail_Quantity_is_Zero_or_Negative() {

        //given
        UUID productId = UUID.randomUUID();

        // when & then
        assertThatThrownBy(() -> new OrderItem(productId, 0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("주문 수량은 1개 이상이어야 합니다.");

        assertThatThrownBy(() -> new OrderItem(productId, -5))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("주문 수량은 1개 이상이어야 합니다.");
    }

    @Test
    @DisplayName("배송 ID 할당")
    void assignDeliveryId() {
        // given
        OrderItem orderItem = new OrderItem(UUID.randomUUID(), 10);
        UUID deliveryId = UUID.randomUUID();

        // when
        orderItem.assignDelivery(deliveryId);

        // then
        assertThat(orderItem.getDeliveryId()).isEqualTo(deliveryId);
    }


}
