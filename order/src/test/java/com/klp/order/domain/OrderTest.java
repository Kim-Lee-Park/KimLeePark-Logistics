package com.klp.order.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@DisplayName("Order 엔티티 테스트")
class OrderTest {

    @Test
    @DisplayName("주문 생성 - 정상 (주문 아이템 포함)")
    void createOrder_Success() {
        // given
        Long supplierId = 1L;
        Long customerId = 2L;
        String comment = "TDD 프로젝트 진행될 때까지 납품 요청";

        UUID productId1 = UUID.randomUUID();
        UUID productId2 = UUID.randomUUID();

        //우선은 deliveryId 같은 경우 후에 채워지기 때문에 초반에는 Null 존재
        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(new OrderItem(productId1, 10));
        orderItems.add(new OrderItem(productId2, 5));

        // when
        Order order = Order.create(supplierId, customerId, comment, orderItems);

        // then
        assertThat(order.getSupplierId()).isEqualTo(supplierId);
        assertThat(order.getCustomerId()).isEqualTo(customerId);
        assertThat(order.getComment()).isEqualTo(comment);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.ING);
        assertThat(order.getOrderId()).isNotNull();

        // 주문 아이템 검증
        assertThat(order.getOrderItems()).hasSize(2);
        assertThat(order.getOrderItems().get(0).getProductId()).isEqualTo(productId1);
        assertThat(order.getOrderItems().get(0).getQuantity()).isEqualTo(10);
        assertThat(order.getOrderItems().get(1).getQuantity()).isEqualTo(5);
        assertThat(order.getOrderItems().get(1).getDeliveryId()).isNull();

        // 양방향 관계 확인
        assertThat(order.getOrderItems().get(0).getOrder()).isEqualTo(order);
        assertThat(order.getOrderItems().get(1).getOrder()).isEqualTo(order);
    }

    @Test
    @DisplayName("주문 생성 - supplierId null이면 예외")
    void createOrder_Fail_Because_SupplierId_is_Null() {
        // given
        Long supplierId = null;
        Long customerId = 2L;
        List<OrderItem> orderItems = List.of(new OrderItem(UUID.randomUUID(), 10));

        // when & then
        assertThatThrownBy(() -> Order.create(supplierId, customerId, null, orderItems))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("공급 업체 ID는 필수입니다.");
    }

    @Test
    @DisplayName("주문 생성 - customerId null이면 예외")
    void createOrder_Fail_because_CustomerId_is_Null() {
        // given
        Long supplierId = 1L;
        Long customerId = null;
        List<OrderItem> orderItems = List.of(new OrderItem(UUID.randomUUID(), 10));

        // when & then
        assertThatThrownBy(() -> Order.create(supplierId, customerId, null, orderItems))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("수령 업체 ID는 필수입니다.");
    }

    @Test
    @DisplayName("주문 생성 - orderItems가 null이면 예외")
    void createOrder_Fail_Because_OrderItems_is_Null() {
        // given
        Long supplierId = 1L;
        Long customerId = 2L;
        List<OrderItem> orderItems = null;

        // when & then
        assertThatThrownBy(() -> Order.create(supplierId, customerId, null, orderItems))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("주문 상품은 필수입니다.");
    }

    @Test
    @DisplayName("주문 생성 - orderItems가 비어있으면 예외")
    void createOrder_Fail_Because_OrderItems_is_EmptyArray() {
        // given
        Long supplierId = 1L;
        Long customerId = 2L;
        List<OrderItem> orderItems = new ArrayList<>();

        // when & then
        assertThatThrownBy(() -> Order.create(supplierId, customerId, null, orderItems))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("주문 상품은 최소 1개 이상이어야 합니다.");
    }
}
