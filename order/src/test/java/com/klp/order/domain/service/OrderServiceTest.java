package com.klp.order.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.klp.order.command.CreateOrderCommand;
import com.klp.order.command.OrderItemCommand;
import com.klp.order.command.UpdateOrderCommand;
import com.klp.order.domain.entity.cancel.CancelType;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.entity.order.OrderStatus;
import com.klp.order.domain.repository.OrderRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService 테스트")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private CreateOrderCommand createCommand;
    private Order savedOrder;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        List<OrderItemCommand> items = List.of(
            new OrderItemCommand(UUID.randomUUID(), 10),
            new OrderItemCommand(UUID.randomUUID(), 5)
        );

        createCommand = new CreateOrderCommand(
            1L,
            2L,
            "테스트 주문 요청사항",
            items
        );

        savedOrder = Order.create(
            createCommand.supplierId(),
            createCommand.customerId(),
            createCommand.comment(),
            createCommand.items()
        );

        orderId = UUID.randomUUID();
    }

    @Test
    @DisplayName("주문 생성 - 정상")
    void createOrder_Success() {
        // given
        given(orderRepository.save(any(Order.class)))
            .willReturn(savedOrder);

        // when
        Order result = orderService.createOrder(createCommand);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getSupplierId()).isEqualTo(1L);
        assertThat(result.getCustomerId()).isEqualTo(2L);
        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.ING);
        assertThat(result.getOrderItems()).hasSize(2);
    }

    @Test
    @DisplayName("주문 생성 - 실패 - supplierId가 null")
    void createOrder_Fail_SupplierIdNull() {
        // given
        CreateOrderCommand invalidCommand = new CreateOrderCommand(
            null,
            2L,
            "comment",
            createCommand.items()
        );

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(invalidCommand))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("공급 업체 ID는 필수입니다.");

    }

    @Test
    @DisplayName("주문 생성 - 실패 - customerId가 null")
    void createOrder_Fail_CustomerIdNull() {
        // given
        CreateOrderCommand invalidCommand = new CreateOrderCommand(
            1L,
            null,
            "comment",
            createCommand.items()
        );

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(invalidCommand))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("수령 업체 ID는 필수입니다.");

    }

    @Test
    @DisplayName("주문 생성 - 실패 - orderItems가 null")
    void createOrder_Fail_orderItem_is_Null() {
        // given
        CreateOrderCommand invalidCommand = new CreateOrderCommand(
            1L,
            2L,
            "comment",
            null
        );

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(invalidCommand))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("주문 상품은 필수입니다.");

    }

    @Test
    @DisplayName("주문 조회 - ID로 조회 성공")
    void findById_Success() {
        // given
        given(orderRepository.findById(orderId))
            .willReturn(Optional.of(savedOrder));

        // when
        Order result = orderService.findById(orderId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(savedOrder);
    }

    @Test
    @DisplayName("주문 조회 - 존재하지 않는 ID")
    void findById_NotFound() {
        // given
        UUID nonExistentId = UUID.randomUUID();
        given(orderRepository.findById(nonExistentId))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.findById(nonExistentId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("주문을 찾을 수 없습니다.");

    }

    @Test
    @DisplayName("주문 수정 - 정상")
    void updateOrder_Success() {
        // given
        UpdateOrderCommand updateCommand = new UpdateOrderCommand(
            "수정된 요청사항",
            List.of(new OrderItemCommand(UUID.randomUUID(), 15))
        );

        given(orderRepository.findById(orderId))
            .willReturn(Optional.of(savedOrder));
        given(orderRepository.save(any(Order.class)))
            .willReturn(savedOrder);

        // when
        Order result = orderService.updateOrder(orderId, updateCommand);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getComment()).isEqualTo("수정된 요청사항");
        assertThat(result.getOrderItems().get(0).getQuantity()).isEqualTo(15);
    }

    @Test
    @DisplayName("주문 취소 - 정상")
    void cancelOrder_Success() {
        // given
        String cancelReason = "고객 요청";
        Long cancelledBy = 100L;
        CancelType cancelType = CancelType.USER_REQUEST;

        given(orderRepository.findById(orderId))
            .willReturn(Optional.of(savedOrder));
        given(orderRepository.save(any(Order.class)))
            .willReturn(savedOrder);

        // when
        Order result = orderService.cancelOrder(
            orderId,
            cancelReason,
            cancelledBy,
            cancelType
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(result.getCancellation()).isNotNull();
    }

    @Test
    @DisplayName("공급업체별 주문 조회")
    void findBySupplierId_Success() {
        // given
        Long supplierId = 1L;
        List<Order> orders = List.of(savedOrder);

        given(orderRepository.findBySupplierId(supplierId))
            .willReturn(orders);

        // when
        List<Order> result = orderService.findBySupplierId(supplierId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSupplierId()).isEqualTo(supplierId);
    }

    @Test
    @DisplayName("고객별 주문 조회")
    void findByCustomerId_Success() {
        // given
        Long customerId = 2L;
        List<Order> orders = List.of(savedOrder);

        given(orderRepository.findByCustomerId(customerId))
            .willReturn(orders);

        // when
        List<Order> result = orderService.findByCustomerId(customerId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerId()).isEqualTo(customerId);
    }

    @Test
    @DisplayName("삭제되지 않은 주문만 조회")
    void findActiveOrders_Success() {
        // given
        List<Order> activeOrders = List.of(savedOrder);

        given(orderRepository.findByDeletedAtIsNull())
            .willReturn(activeOrders);

        // when
        List<Order> result = orderService.findNotDeletedOrders();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).isDeleted()).isFalse();
    }
}