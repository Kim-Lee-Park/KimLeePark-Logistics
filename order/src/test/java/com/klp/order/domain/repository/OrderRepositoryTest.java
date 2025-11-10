package com.klp.order.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.klp.order.application.command.OrderItemCommand;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.entity.order.OrderStatus;
import com.klp.order.global.AuditConfig;
import com.klp.order.infrastructure.repository.OrderJpaRepository;
import java.util.ArrayList;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@DisplayName("OrderRepository 테스트")
@ActiveProfiles("test")
@Import(AuditConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class OrderRepositoryTest {

    @Autowired
    private OrderJpaRepository orderRepository;

    private Order order1;
    private List<OrderItemCommand> itemCommands1;
    private List<OrderItemCommand> itemCommands2;


    @BeforeEach
    void setup() throws Exception {
        itemCommands1 = new ArrayList<>();
        itemCommands2 = new ArrayList<>();
        itemCommands1.add(new OrderItemCommand(UUID.randomUUID(), 10));
        itemCommands2.add(new OrderItemCommand(UUID.randomUUID(), 20));
        order1 = Order.create(1L, 2L, "주문1요청사항", itemCommands1);

    }

    @Test
    @DisplayName("주문 저장- 정상")
    void 주문정상() {
        //given
        //setup
        //order1 = Order.create(1L, 2L, "주문1요청사항", itemCommands1);

        //when
        Order saveOrder = orderRepository.save(order1);

        //then
        //assertThat(saveOrder.getOrderId()).isNotNull();
        assertThat(saveOrder.getSupplierId()).isEqualTo(1L);
        assertThat(saveOrder.getCustomerId()).isEqualTo(2L);
        assertThat(saveOrder.getComment()).isEqualTo("주문1요청사항");
        assertThat(saveOrder.getOrderStatus()).isEqualTo(OrderStatus.ING);
        assertThat(saveOrder.getCancellation()).isNull();
        assertThat(saveOrder.getOutboundRequests()).hasSize(0);
    }

    @Test
    @DisplayName("주문 ID로 조회 - 정상")
    void 주문ID로조회_정상() {

        //given
        Order saveOrder = orderRepository.save(order1);
        UUID orderId = saveOrder.getOrderId();

        //when
        Optional<Order> foundOrder = orderRepository.findById(orderId);

        //then
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getOrderId()).isEqualTo(orderId);
        assertThat(foundOrder.get().getSupplierId()).isEqualTo(1L);
        assertThat(foundOrder.get().getCustomerId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("삭제되지 않은 주문 조회")
    void 삭제되지_않은_주문_조회() {
        //when
        orderRepository.save(order1);
        List<Order> notDeletedOrders = orderRepository.findByDeletedAtIsNull();
        //then
        assertThat(notDeletedOrders).hasSize(1);
        assertThat(notDeletedOrders.get(0).getDeletedAt()).isNull();
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
        // given
        orderRepository.save(order1);
        // when
        List<Order> orders = orderRepository.findAll();

        // then
        assertThat(orders).hasSize(1);
    }

    @Test
    @DisplayName("페이징 처리 -정상")
    void 페이징처리_정상() {
        // given
        orderRepository.save(order1);
        Pageable pageable = PageRequest.of(0, 1, Sort.by("createdAt").descending());

        // when
        Page<Order> orderPage = orderRepository.findAll(pageable);

        // then
        assertThat(orderPage.getContent()).hasSize(1);
        assertThat(orderPage.getTotalElements()).isEqualTo(1);
        assertThat(orderPage.getTotalPages()).isEqualTo(1);
        assertThat(orderPage.isFirst()).isTrue();
        assertThat(orderPage.hasNext()).isFalse();
    }

    @Test
    @DisplayName("주문 수정 - 정상")
    void 주문수정_정상() {
        // given
        Order saveOrder = orderRepository.save(order1);
        UUID orderId = saveOrder.getOrderId();
        String newComment = "수정된 주문";
        List<OrderItemCommand> newOrderItemCommands = List.of(
            new OrderItemCommand(UUID.randomUUID(), 100));

        // when
        Order foundOrder = orderRepository.findById(orderId).orElseThrow();
        foundOrder.updateOrder(newComment, newOrderItemCommands);
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
        orderRepository.save(order1);
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

    @Test
    @DisplayName("공급업체Id로 조회")
    void 공급업체_Id로조회() {
        //given
        //setup
        //order1 = Order.create(1L, 2L, "주문1요청사항", itemCommands1);

        //when
        orderRepository.save(order1);
        List<Order> supplierOrders = orderRepository.findBySupplierId(1L);

        //then
        assertThat(supplierOrders).hasSize(1);
        assertThat(supplierOrders.get(0).getSupplierId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("수령업체 Id로 조회")
    void 수령업체Id로_조회() {
        //given
        //setup
        //order1 = Order.create(1L, 2L, "주문1요청사항", itemCommands1);

        //when
        orderRepository.save(order1);
        List<Order> customerOrders = orderRepository.findByCustomerId(2L);

        //then
        assertThat(customerOrders).hasSize(1);
        assertThat(customerOrders.get(0).getCustomerId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("주문Id로 검색 - 삭제되어있지 않은걸로 검색")
    void 삭제_되지않은_주문_검색() {
        //given
        //setup
        //order1 = Order.create(1L, 2L, "주문1요청사항", itemCommands1);

        //when
        orderRepository.save(order1);
        Optional notDeletedOrderSearch = orderRepository.findByOrderIdAndDeletedAtIsNull(
            order1.getOrderId());

        //then
        assertThat(notDeletedOrderSearch).isPresent();
    }

    @Test
    @DisplayName("주문Id로 검색 - 삭제되어있지 있는걸로 검색")
    void 삭제_되어있는_주문_검색() {
        //given
        //setup
        //order1 = Order.create(1L, 2L, "주문1요청사항", itemCommands1);

        //when
        orderRepository.save(order1);
        orderRepository.delete(order1);
        Optional deletedOrder = orderRepository.findByOrderIdAndDeletedAtIsNull(
            order1.getOrderId());

        //then
        assertThat(deletedOrder).isEmpty();

    }


}