package com.klp.order.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.klp.order.domain.order.Order;
import com.klp.order.domain.order.OrderStatus;
import com.klp.order.domain.orderitem.OrderItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@DataJpaTest
@DisplayName("OrderRepository 테스트")
public class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    private Order order1;
    private Order order2;
    private List<OrderItem> orderItems1;
    private List<OrderItem> orderItems2;


    @BeforeEach
    void setup() {
        orderItems1 = List.of(new OrderItem(UUID.randomUUID(), 10));
        orderItems2 = List.of(new OrderItem(UUID.randomUUID(), 10));

    }

    @Test
    @DisplayName("주문 저장- 정상")
    void 주문정상() {
        //given
        order1 = Order.create(1L, 2L, "주문1요청사항", orderItems1);

        //when
        Order saveOrder = orderRepository.save(order1);

        //then
        //assertThat(saveOrder.getOrderId()).isNotNull();
        assertThat(saveOrder.getSupplierId()).isEqualTo(1L);
        assertThat(saveOrder.getCustomerId()).isEqualTo(2L);
        assertThat(saveOrder.getComment()).isEqualTo("주문1요청사항");
        assertThat(saveOrder.getOrderStatus()).isEqualTo(OrderStatus.ING);
        assertThat(saveOrder.getCancellation()).isNull();
        assertThat(saveOrder.getOutboundRequests()).isNull();
    }

    @Test
    @DisplayName("주문 ID로 조회 - 정상")
    void 주문ID로조회_정상() {

        //given
        UUID orderId = order1.getOrderId();

        //when
        Optional<Order> foundOrder = orderRepository.findById(orderId);

        //then
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getOrderId()).isEqualTo(orderId);
        assertThat(foundOrder.get().getSupplierId()).isEqualTo(1L);
        assertThat(foundOrder.get().getCustomerId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("주문 ID로 조회 - 존재하지 않는 주문")
    void 없는ID로_조회() {
        // given
        UUID noExistId = UUID.randomUUID();

        // when
        Optional<Order> foundOrder = orderRepository.findById(noExistId);

        // then
        assertThat(foundOrder).isEmpty();
    }

    @Test
    @DisplayName("전체 주문 조회-성공")
    void 전체_주문_조회() {
        // when
        List<Order> orders = orderRepository.findAll();

        // then
        assertThat(orders).hasSize(1);
    }

    @Test
    @DisplayName("페이징 처리 -정상")
    void 페이징처리_정상() {
        // given
        Pageable pageable = PageRequest.of(0, 1, Sort.by("createdAt").descending());

        // when
        Page<Order> orderPage = orderRepository.findAll(pageable);

        // then
        assertThat(orderPage.getContent()).hasSize(1);
        assertThat(orderPage.getTotalElements()).isEqualTo(1);
        assertThat(orderPage.getTotalPages()).isEqualTo(1);
        assertThat(orderPage.isFirst()).isTrue();
        assertThat(orderPage.hasNext()).isTrue();
    }

    @Test
    @DisplayName("주문 수정 - 정상")
    void 주문수정_정상() {
        // given
        UUID orderId = order1.getOrderId();
        String newComment = "수정된 주문";
        List<OrderItem> newOrderItems = List.of(
            new OrderItem(UUID.randomUUID(), 100)
        );

        // when
        Order foundOrder = orderRepository.findById(orderId).orElseThrow();
        foundOrder.updateOrder(newComment, newOrderItems);
        Order updatedOrder = orderRepository.save(foundOrder);

        // then
        assertThat(updatedOrder.getComment()).isEqualTo(newComment);
        assertThat(updatedOrder.getOrderItems()).hasSize(1);
        assertThat(updatedOrder.getOrderItems().get(0).getQuantity()).isEqualTo(100);
    }

    @Test
    @DisplayName("주문 삭제 - 정상 (Soft Delete)")
    void 주문삭제_정상() {
        // given
        UUID orderId = order1.getOrderId();
        Long deletedBy = 3L;

        // when
        Order foundOrder = orderRepository.findById(orderId).orElseThrow();
        foundOrder.delete(deletedBy);
        orderRepository.save(foundOrder);

        // then
        Order deletedOrder = orderRepository.findById(orderId).orElseThrow();
        assertThat(deletedOrder.isDeleted()).isTrue();
        assertThat(deletedOrder.getDeletedAt()).isNotNull();
        assertThat(deletedOrder.getDeletedBy()).isEqualTo(deletedBy);
    }


}