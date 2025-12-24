/// /package com.klp.order.domain.service; / /import static
/// org.assertj.core.api.Assertions.assertThat; /import static
/// org.assertj.core.api.Assertions.assertThatThrownBy; /import static
/// org.mockito.ArgumentMatchers.any; /import static org.mockito.BDDMockito.given; / /import
/// com.klp.common.exception.BusinessException; /import
/// com.klp.order.application.command.OrderItemCommand; /import
/// com.klp.order.application.service.OrderCancellationService; /import
/// com.klp.order.domain.entity.cancel.CancelType; /import
/// com.klp.order.domain.entity.cancel.OrderCancellation; /import
/// com.klp.order.domain.entity.order.Order; /import
/// com.klp.order.domain.repository.OrderCancellationRepository; /import java.util.List; /import
/// java.util.Optional; /import java.util.UUID; /import org.junit.jupiter.api.BeforeEach; /import
/// org.junit.jupiter.api.DisplayName; /import org.junit.jupiter.api.Test; /import
/// org.junit.jupiter.api.extension.ExtendWith; /import org.mockito.InjectMocks; /import
/// org.mockito.Mock; /import org.mockito.junit.jupiter.MockitoExtension; /
/// /@ExtendWith(MockitoExtension.class) /@DisplayName("OrderCancellationService 테스트") /class
/// OrderCancellationServiceTest { / /    @Mock /    private OrderCancellationRepository
/// orderCancellationRepository; / /    @InjectMocks /    private OrderCancellationService
/// orderCancellationService; / /    private Order order; /    private OrderCancellation
/// orderCancellation; /    private UUID cancellationId; /    private UUID orderId; / /
/// @BeforeEach /    void setUp() { /        orderId = UUID.randomUUID(); /
/// List<OrderItemCommand> itemCommands = List.of( /            new
/// OrderItemCommand(UUID.randomUUID(), "상품명", UUID.randomUUID(), 10) /        ); /        order =
/// Order.create(1L, 2L, "테스트 주문", itemCommands); /        cancellationId = UUID.randomUUID(); /
/// } / /    private OrderCancellation create(Order order, String reason, Long deletedBy, /
/// CancelType canceltype) { /        return OrderCancellation.create(order, reason, deletedBy,
/// canceltype); /    } / /    @Test /    @DisplayName("주문 취소 정보 삭제 - ID로 삭제") /    void
/// deleteById_Success() { /        // given /        orderCancellation = create(order, "고객 요청",
/// 100L, CancelType.USER_REQUEST); /
/// given(orderCancellationRepository.findById(cancellationId)) /
/// .willReturn(Optional.of(orderCancellation)); / /        // when /
/// orderCancellationService.deleteById(cancellationId); / /        // then /
/// assertThat(orderCancellation) /    }
//
//    @Test
//    @DisplayName("주문이 취소되었는지 확인 - 취소됨")
//    void isOrderCancelled_True() {
//        // given
//        orderCancellation = create(order, "고객 요청", 100L, CancelType.USER_REQUEST);
//        given(orderCancellationRepository.findByOrder_OrderId(orderId))
//            .willReturn(Optional.of(orderCancellation));
//
//        // when
//        boolean result = orderCancellationService.isOrderCancelled(orderId);
//
//        // then
//        assertThat(result).isTrue();
//    }
//
//}