package com.klp.order.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.entity.orderitem.OrderItem;
import com.klp.order.global.AuditConfig;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AuditConfig.class)
@DisplayName("OrderItemRepository 테스트")
public class OrderItemRepositoryTest {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Order order1;
    private Order order2;
    private OrderItem orderItem1;
    private OrderItem orderItem2;
    private OrderItem orderItem3;

    @BeforeEach
    void setup() {
        List<OrderItem> orderItems1 = List.of(
            new OrderItem(UUID.randomUUID(), 10),
            new OrderItem(UUID.randomUUID(), 5)
        );
        List<OrderItem> orderItems2 = List.of(
            new OrderItem(UUID.randomUUID(), 20)
        );
        order1 = Order.create(1L, 2L, "주문1", orderItems1);
        order1 = orderRepository.save(order1);
        order2 = Order.create(2L, 3L, "주문2", orderItems2);
        order2 = orderRepository.save(order2);

        orderItem1 = order1.getOrderItems().get(0);
        orderItem2 = order1.getOrderItems().get(1);
        orderItem3 = order2.getOrderItems().get(0);
    }

    @Test
    @DisplayName("OrderItem ID로 조회 - 정상")
    void OrderItem_ID로_조회_정상() {
        // given
        UUID orderItemId = orderItem1.getOrderItemId();

        // when
        Optional<OrderItem> foundOrderItem = orderItemRepository.findById(orderItemId);

        // then
        assertThat(foundOrderItem).isPresent();
        assertThat(foundOrderItem.get().getOrderItemId()).isEqualTo(orderItemId);
        assertThat(foundOrderItem.get().getQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("OrderItem ID로 조회 - 존재하지 않는 OrderItem")
    void 없는_ID로_조회() {
        // given
        UUID nonExistentId = UUID.randomUUID();

        // when
        Optional<OrderItem> foundOrderItem = orderItemRepository.findById(nonExistentId);

        // then
        assertThat(foundOrderItem).isEmpty();
    }

    @Test
    @DisplayName("전체 OrderItem 조회")
    void 전체_OrderItem_조회() {
        // when
        List<OrderItem> orderItems = orderItemRepository.findAll();

        // then
        assertThat(orderItems).hasSize(3);
    }

    @Test
    @DisplayName("주문 ID로 OrderItem 조회 - 정상")
    void 주문ID로_OrderItem_조회_정상() {
        // given
        UUID orderId = order1.getOrderId();

        // when
        List<OrderItem> orderItems = orderItemRepository.findByOrder_OrderId(orderId);

        // then
        assertThat(orderItems).hasSize(2);
        assertThat(orderItems).extracting(OrderItem::getQuantity)
            .containsExactlyInAnyOrder(10, 5);
    }


    @Test
    @DisplayName("배송 ID가 null인 OrderItem 조회 - 정상")
    void 배송ID가null인_OrderItem_조회_정상() {
        // when
        List<OrderItem> orderItems = orderItemRepository.findByDeliveryIdIsNull();

        // then
        assertThat(orderItems).hasSize(3);
        assertThat(orderItems).extracting(OrderItem::getDeliveryId)
            .containsOnly((UUID) null);
    }

    @Test
    @DisplayName("OrderItem 수정 - 수량 변경")
    void OrderItem_수정_수량변경() {
        // given
        UUID orderItemId = orderItem1.getOrderItemId();
        int newQuantity = 100;

        // when
        OrderItem foundOrderItem = orderItemRepository.findById(orderItemId).orElseThrow();
        foundOrderItem.updateQuantity(newQuantity);
        OrderItem updatedOrderItem = orderItemRepository.save(foundOrderItem);

        // then
        assertThat(updatedOrderItem.getQuantity()).isEqualTo(newQuantity);
    }

    @Test
    @DisplayName("OrderItem 배송 ID 할당 - 정상")
    void OrderItem_배송ID할당_정상() {
        // given
        UUID orderItemId = orderItem1.getOrderItemId();
        UUID deliveryId = UUID.randomUUID();

        // when
        OrderItem foundOrderItem = orderItemRepository.findById(orderItemId).orElseThrow();
        foundOrderItem.assignDeliveryId(deliveryId);
        OrderItem updatedOrderItem = orderItemRepository.save(foundOrderItem);

        // then
        assertThat(updatedOrderItem.getDeliveryId()).isEqualTo(deliveryId);
    }

    //
    @Test
    @DisplayName("OrderItem 삭제 - 정상 (Soft Delete)")
    void OrderItem_삭제_정상() {
        // given
        UUID orderItemId = orderItem1.getOrderItemId();
        Long deletedBy = 100L;

        // when
        OrderItem foundOrderItem = orderItemRepository.findById(orderItemId).orElseThrow();
        foundOrderItem.delete(deletedBy);
        orderItemRepository.save(foundOrderItem);

        // then
        OrderItem deletedOrderItem = orderItemRepository.findById(orderItemId).orElseThrow();
        assertThat(deletedOrderItem.isDeleted()).isTrue();
        assertThat(deletedOrderItem.getDeletedAt()).isNotNull();
        assertThat(deletedOrderItem.getDeletedBy()).isEqualTo(deletedBy);
    }

    //
    @Test
    @DisplayName("삭제되지 않은 OrderItem만 조회 - 정상")
    void 삭제되지않은_OrderItem만_조회_정상() {
        // given
        orderItem1.delete(100L);
        orderItemRepository.save(orderItem1);

        // when
        List<OrderItem> activeOrderItems = orderItemRepository.findByDeletedAtIsNull();

        // then
        assertThat(activeOrderItems).hasSize(2);
        assertThat(activeOrderItems).extracting(OrderItem::getOrderItemId)
            .doesNotContain(orderItem1.getOrderItemId());
    }

    @Test
    @DisplayName("주문 ID로 삭제되지 않은 OrderItem 조회 - 정상")
    void 주문ID로_삭제되지않은_OrderItem_조회_정상() {
        // given
        UUID orderId = order1.getOrderId();
        orderItem1.delete(100L);
        orderItemRepository.save(orderItem1);

        // when
        List<OrderItem> activeOrderItems = orderItemRepository
            .findByOrder_OrderIdAndDeletedAtIsNull(orderId);

        // then
        assertThat(activeOrderItems).hasSize(1);
        assertThat(activeOrderItems.get(0).getOrderItemId())
            .isEqualTo(orderItem2.getOrderItemId());
    }
}