package com.klp.order.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.klp.common.exception.BusinessException;
import com.klp.order.application.command.OrderItemCommand;
import com.klp.order.domain.entity.cancel.CancelType;
import com.klp.order.domain.entity.cancel.OrderCancellation;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.entity.order.OrderStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Order 엔티티 테스트")
class OrderTest {

    private Long supplierId;
    private Long customerId;
    private String comment;
    private UUID productId1;
    private UUID productId2;
    private UUID hubId1;
    private UUID hubId2;

    private List<OrderItemCommand> itemCommands;

    @BeforeEach
    void setUp() {
        supplierId = 1L;
        customerId = 2L;
        comment = "TDD 프로젝트 진행될 때까지 납품 요청";

        productId1 = UUID.randomUUID();
        productId2 = UUID.randomUUID();
        hubId1 = UUID.randomUUID();
        hubId2 = UUID.randomUUID();

        itemCommands = new ArrayList<>();
        itemCommands.add(new OrderItemCommand(productId1, "상품명", hubId1, 10));
        itemCommands.add(new OrderItemCommand(productId2, "상품명", hubId2, 5));
    }


    @Test
    @DisplayName("주문 생성 - 정상 (주문 아이템 포함)")
    void createOrder_Success() {
        // when
        Order order = Order.create(supplierId, customerId, comment, itemCommands);

        // then
        assertThat(order.getSupplierId()).isEqualTo(supplierId);
        assertThat(order.getCustomerId()).isEqualTo(customerId);
        assertThat(order.getComment()).isEqualTo(comment);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING);

        // 주문 아이템 검증
        assertThat(order.getOrderItems()).hasSize(2);
        assertThat(order.getOrderItems().get(0).getProductId()).isEqualTo(productId1);
        assertThat(order.getOrderItems().get(0).getQuantity()).isEqualTo(10);
        assertThat(order.getOrderItems().get(1).getProductId()).isEqualTo(productId2);
        assertThat(order.getOrderItems().get(1).getQuantity()).isEqualTo(5);
        assertThat(order.getOrderItems().get(1).getDeliveryId()).isNull();

        assertThat(order.getOrderItems().get(0).getOrder()).isEqualTo(order);
        assertThat(order.getOrderItems().get(1).getOrder()).isEqualTo(order);
    }

    @Test
    @DisplayName("주문 생성 - supplierId null이면 예외")
    void createOrder_Fail_Because_SupplierId_is_Null() {
        // given
        supplierId = null;

        // when & then
        assertThatThrownBy(() -> Order.create(supplierId, customerId, comment, itemCommands))
            .isInstanceOf(BusinessException.class)
            .hasMessage("공급 업체 ID는 필수입니다.");
    }

    @Test
    @DisplayName("주문 생성 - customerId null이면 예외")
    void createOrder_Fail_because_CustomerId_is_Null() {
        // given
        customerId = null;

        // when & then
        assertThatThrownBy(() -> Order.create(supplierId, customerId, comment, itemCommands))
            .isInstanceOf(BusinessException.class)
            .hasMessage("수령 업체 ID는 필수입니다.");
    }

    @Test
    @DisplayName("주문 생성 - itemCommands가 null이면 예외")
    void createOrder_Fail_Because_ItemCommands_is_Null() {
        // given
        itemCommands = null;

        // when & then
        assertThatThrownBy(() -> Order.create(supplierId, customerId, comment, itemCommands))
            .isInstanceOf(BusinessException.class)
            .hasMessage("주문 상품은 필수입니다.");
    }

    @Test
    @DisplayName("주문 생성 - itemCommands가 비어있으면 예외")
    void createOrder_Fail_Because_ItemCommands_is_EmptyArray() {
        // given
        itemCommands = new ArrayList<>();

        // when & then
        assertThatThrownBy(() -> Order.create(supplierId, customerId, comment, itemCommands))
            .isInstanceOf(BusinessException.class)
            .hasMessage("주문 상품은 최소 1개 이상이어야 합니다.");
    }

    @Test
    @DisplayName("주문 수정 - 정상 수정")
    void updateOrder_Success() {
        // given
        Order order = Order.create(supplierId, customerId, comment, itemCommands);
        String newComment = "수정된 요청사항";

        List<OrderItemCommand> newItemCommands = new ArrayList<>();
        newItemCommands.add(new OrderItemCommand(UUID.randomUUID(), "상품명", hubId1, 15));
        newItemCommands.add(new OrderItemCommand(UUID.randomUUID(), "상품명", hubId2, 20));

        // when
        order.updateOrder(newComment, newItemCommands);

        // then
        assertThat(order.getComment()).isEqualTo(newComment);
        assertThat(order.getOrderItems()).hasSize(2);
        assertThat(order.getOrderItems().get(0).getQuantity()).isEqualTo(15);
        assertThat(order.getOrderItems().get(1).getQuantity()).isEqualTo(20);

        assertThat(order.getOrderItems().get(0).getOrder()).isEqualTo(order);
        assertThat(order.getOrderItems().get(1).getOrder()).isEqualTo(order);
    }

    @Test
    @DisplayName("주문 수정 - 배송이 할당된 주문 수정 시도 - 실패")
    void updateOrder_Fail_Because_Already_assign_Delivery() {
        // given
        Order order = Order.create(supplierId, customerId, comment, itemCommands);
        order.changeStatus(OrderStatus.DELIVERY_ASSIGNED);
        String newComment = "수정 시도";

        List<OrderItemCommand> newItemCommands = List.of(
            new OrderItemCommand(UUID.randomUUID(), "상품명", hubId1, 10)
        );

        // when & then
        assertThatThrownBy(() -> order.updateOrder(newComment, newItemCommands))
            .isInstanceOf(BusinessException.class)
            .hasMessage("배송이 할당된 주문은 수정할 수 없습니다.");
    }

    @Test
    @DisplayName("주문 수정 - 취소된 주문 수정 시도 - 실패")
    void updateOrder_Fail_Because_Already_cancel() {
        // given
        Order order = Order.create(supplierId, customerId, comment, itemCommands);
        order.cancel("취소", 100L, CancelType.USER_REQUEST);
        String newComment = "수정 시도";

        List<OrderItemCommand> newItemCommands = List.of(
            new OrderItemCommand(UUID.randomUUID(), "상품명", hubId1, 10)
        );

        // when & then
        assertThatThrownBy(() -> order.updateOrder(newComment, newItemCommands))
            .isInstanceOf(BusinessException.class)
            .hasMessage("취소된 주문은 수정할 수 없습니다.");
    }

    @Test
    @DisplayName("주문 수정 - 완료된 주문 수정 시도 - 실패")
    void updateOrder_Fail_Because_Already_completed() {
        // given
        Order order = Order.create(supplierId, customerId, comment, itemCommands);
        order.changeStatus(OrderStatus.COMPLETE);
        String newComment = "수정 시도";

        List<OrderItemCommand> newItemCommands = List.of(
            new OrderItemCommand(UUID.randomUUID(), "상품명", hubId1, 10)
        );

        // when & then
        assertThatThrownBy(() -> order.updateOrder(newComment, newItemCommands))
            .isInstanceOf(BusinessException.class)
            .hasMessage("완료된 주문은 수정할 수 없습니다.");
    }

    @Test
    @DisplayName("주문 상태 변경 - 정상 변경")
    void changeOrderStatus_Success() {
        // given
        Order order = Order.create(supplierId, customerId, comment, itemCommands);

        // when
        order.changeStatus(OrderStatus.DELIVERY_ASSIGNED);

        // then
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.DELIVERY_ASSIGNED);
    }

    @Test
    @DisplayName("주문 상태 변경 - 실패 - 취소된 주문")
    void changeOrderStatus_Fail_Because_Order_Status_is_Cancelled() {
        // given
        Order order = Order.create(supplierId, customerId, comment, itemCommands);
        order.cancel("취소", 100L, CancelType.USER_REQUEST);

        // when & then
        assertThatThrownBy(() -> order.changeStatus(OrderStatus.DELIVERY_ASSIGNED))
            .isInstanceOf(BusinessException.class)
            .hasMessage("취소된 주문은 상태를 변경할 수 없습니다.");
    }

    @Test
    @DisplayName("주문 취소 - 정상")
    void cancelOrder_Success() {
        // given
        Order order = Order.create(supplierId, customerId, comment, itemCommands);
        String cancelReason = "고객 요청으로 인한 취소";
        Long cancelledBy = 3L;
        CancelType cancelType = CancelType.USER_REQUEST;

        // when
        OrderCancellation cancellation = order.cancel(cancelReason, cancelledBy, cancelType);

        // then
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getCancellation()).isNotNull();
        assertThat(cancellation.getCancelReason()).isEqualTo(cancelReason);
        assertThat(cancellation.getCancelledBy()).isEqualTo(cancelledBy);
        assertThat(cancellation.getCancelType()).isEqualTo(cancelType);
        assertThat(cancellation.getCancelledAt()).isNotNull();
    }

    @Test
    @DisplayName("주문 취소 - 실패 - 이미 취소된 주문")
    void cancelOrder_Fail_AlreadyCancelled() {
        // given
        Order order = Order.create(supplierId, customerId, comment, itemCommands);
        order.cancel("첫 번째 취소", 100L, CancelType.USER_REQUEST);

        // when & then
        assertThatThrownBy(() -> order.cancel("두 번째 취소", 100L, CancelType.ADMIN_CANCEL))
            .isInstanceOf(BusinessException.class)
            .hasMessage("이미 취소된 주문입니다.");
    }

    @Test
    @DisplayName("주문 취소 - 실패 - 완료된 주문")
    void cancelOrder_Fail_CompletedOrder() {
        // given
        Order order = Order.create(supplierId, customerId, comment, itemCommands);
        order.changeStatus(OrderStatus.COMPLETE);

        // when & then
        assertThatThrownBy(() -> order.cancel("취소 시도", 100L, CancelType.USER_REQUEST))
            .isInstanceOf(BusinessException.class)
            .hasMessage("완료된 주문은 취소할 수 없습니다.");
    }

    @Test
    @DisplayName("주문 취소 - 실패 - 배송 할당된 주문")
    void cancelOrder_Fail_DeliveryAssignedOrder() {
        // given
        Order order = Order.create(supplierId, customerId, comment, itemCommands);
        order.changeStatus(OrderStatus.DELIVERY_ASSIGNED);

        // when & then
        assertThatThrownBy(() -> order.cancel("취소 시도", 100L, CancelType.USER_REQUEST))
            .isInstanceOf(BusinessException.class)
            .hasMessage("배송이 할당된 주문은 취소할 수 없습니다.");
    }
}