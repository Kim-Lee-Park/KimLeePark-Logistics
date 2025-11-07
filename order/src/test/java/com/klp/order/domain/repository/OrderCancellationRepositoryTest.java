package com.klp.order.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.klp.order.command.OrderItemCommand;
import com.klp.order.domain.entity.cancel.CancelType;
import com.klp.order.domain.entity.cancel.OrderCancellation;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.global.AuditConfig;
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
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AuditConfig.class)
@DisplayName("OrderCancellation 테스트")
public class OrderCancellationRepositoryTest {

    @Autowired
    private OrderCancellationRepository orderCancellationRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Order order1;
    private Order order2;
    private OrderCancellation cancellation1;
    private List<OrderItemCommand> itemCommands1;
    private List<OrderItemCommand> itemCommands2;


    @BeforeEach
    void setup() {
        itemCommands1 = new ArrayList<>();
        itemCommands2 = new ArrayList<>();
        itemCommands1.add(new OrderItemCommand(UUID.randomUUID(), 10));
        itemCommands1.add(new OrderItemCommand(UUID.randomUUID(), 20));
        itemCommands2.add(new OrderItemCommand(UUID.randomUUID(), 5));

        order1 = Order.create(1L, 2L, "주문1", itemCommands1);
        order1 = orderRepository.save(order1);
        order2 = Order.create(2L, 3L, "주문2", itemCommands2);
        order2 = orderRepository.save(order2);

        cancellation1 = order1.cancel("고객 요청", 100L, CancelType.USER_REQUEST);
        orderRepository.save(order1);
        cancellation1 = order1.getCancellation();
    }

    @Test
    @DisplayName("취소되지 않은 주문은 취소 정보가 null인지 체크")
    void 취소되지않은_주문_취소정보_null() {
        // given
        UUID orderId = order2.getOrderId();

        // when
        Order foundOrder = orderRepository.findById(orderId).orElseThrow();

        // then
        assertThat(foundOrder.getCancellation()).isNull();
    }

    @Test
    @DisplayName("취소 사유 생성-정상")
    void 취소_생성_정상() {
        //given
        String cancelReason = "단순 변심";
        Long cancelledBy = 100L;
        CancelType cancelType = CancelType.USER_REQUEST;

        //when
        order2.cancel(cancelReason, cancelledBy, cancelType);
        orderRepository.save(order2);
        OrderCancellation cancellation = order2.getCancellation();

        //then
        OrderCancellation savedCancellation = orderCancellationRepository
            .findById(cancellation.getOrderCancellationId())
            .orElseThrow();

        assertThat(savedCancellation.getOrderCancellationId()).isNotNull();
        assertThat(savedCancellation.getCancelReason()).isEqualTo(cancelReason);
        assertThat(savedCancellation.getCancelledBy()).isEqualTo(cancelledBy);
        assertThat(savedCancellation.getCancelType()).isEqualTo(cancelType);
        assertThat(savedCancellation.getCancelledAt()).isNotNull();

    }

    @Test
    @DisplayName("취소 정보 ID로 조회 - 정상")
    void 취소정보_ID로_조회_정상() {
        // given
        UUID cancellationId = cancellation1.getOrderCancellationId();

        // when
        Optional<OrderCancellation> foundCancellation = orderCancellationRepository
            .findById(cancellationId);

        // then
        assertThat(foundCancellation).isPresent();
        assertThat(foundCancellation.get().getOrderCancellationId()).isEqualTo(cancellationId);
        assertThat(foundCancellation.get().getCancelReason()).isEqualTo("고객 요청");
        assertThat(foundCancellation.get().getCancelType()).isEqualTo(CancelType.USER_REQUEST);
    }

    @Test
    @DisplayName("취소 정보 ID로 조회 - 존재하지 않는 취소 정보")
    void 없는_ID로_조회() {
        // given
        UUID nonExistentId = UUID.randomUUID();

        // when
        Optional<OrderCancellation> foundCancellation = orderCancellationRepository
            .findById(nonExistentId);

        // then
        assertThat(foundCancellation).isEmpty();
    }

    @Test
    @DisplayName("전체 취소 정보 조회")
    void 전체_취소정보_조회() {
        // when
        List<OrderCancellation> cancellations = orderCancellationRepository.findAll();

        // then
        assertThat(cancellations).hasSize(1);
    }

    @Test
    @DisplayName("주문 ID로 취소 정보 조회 - 정상")
    void 주문ID로_취소정보_조회_정상() {
        // given
        UUID orderId = order1.getOrderId();

        // when
        Optional<OrderCancellation> foundCancellation = orderCancellationRepository
            .findByOrder_OrderId(orderId);

        // then
        assertThat(foundCancellation).isPresent();
        assertThat(foundCancellation.get().getOrder().getOrderId()).isEqualTo(orderId);
        assertThat(foundCancellation.get().getCancelReason()).isEqualTo("고객 요청");
    }

    @Test
    @DisplayName("주문 삭제 시 취소 정보도 함께 삭제 (CASCADE)")
    void 주문삭제시_취소정보도_삭제() {
        // given
        UUID orderId = order1.getOrderId();
        UUID cancellationId = cancellation1.getOrderCancellationId();

        // when
        orderRepository.deleteById(orderId);

        // then
        Optional<Order> deletedOrder = orderRepository.findById(orderId);
        Optional<OrderCancellation> deletedCancellation = orderCancellationRepository
            .findById(cancellationId);

        assertThat(deletedOrder).isEmpty();
        assertThat(deletedCancellation).isEmpty();
    }

}
