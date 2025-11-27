package com.klp.order.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.klp.common.exception.BusinessException;
import com.klp.order.application.command.OrderItemCommand;
import com.klp.order.application.service.OrderCancellationService;
import com.klp.order.domain.entity.cancel.CancelType;
import com.klp.order.domain.entity.cancel.OrderCancellation;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.repository.OrderCancellationRepository;
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
@DisplayName("OrderCancellationService 테스트")
class OrderCancellationServiceTest {

    @Mock
    private OrderCancellationRepository orderCancellationRepository;

    @InjectMocks
    private OrderCancellationService orderCancellationService;

    private Order order;
    private OrderCancellation orderCancellation;
    private UUID cancellationId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        List<OrderItemCommand> itemCommands = List.of(
            new OrderItemCommand(UUID.randomUUID(), "상품명", UUID.randomUUID(), 10)
        );
        order = Order.create(1L, 2L, "테스트 주문", itemCommands);
        cancellationId = UUID.randomUUID();
    }

    private OrderCancellation create(Order order, String reason, Long deletedBy,
        CancelType canceltype) {
        return OrderCancellation.create(order, reason, deletedBy, canceltype);
    }

    @Test
    @DisplayName("주문 취소 정보 조회 - ID로 조회 성공")
    void findById_Success() {
        // given
        orderCancellation = create(order, "고객 요청", 100L, CancelType.USER_REQUEST);

        given(orderCancellationRepository.findById(cancellationId))
            .willReturn(Optional.of(orderCancellation));

        // when
        OrderCancellation result = orderCancellationService.findById(cancellationId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(orderCancellation);
        assertThat(result.getCancelReason()).isEqualTo("고객 요청");
        assertThat(result.getCancelType()).isEqualTo(CancelType.USER_REQUEST);
        assertThat(result.getCancelledBy()).isEqualTo(100L);

    }

    @Test
    @DisplayName("주문 취소 정보 조회 - 존재하지 않는 ID")
    void findById_NotFound() {
        // given
        UUID nonExistentId = UUID.randomUUID();
        given(orderCancellationRepository.findById(nonExistentId))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderCancellationService.findById(nonExistentId))
            .isInstanceOf(BusinessException.class)
            .hasMessage("주문 취소 정보를 찾을 수 없습니다.");

    }

    @Test
    @DisplayName("주문 ID로 취소 정보 조회 - 정상")
    void findByOrderId_Success() {
        // given
        orderCancellation = create(order, "고객 요청", 100L, CancelType.USER_REQUEST);
        given(orderCancellationRepository.findByOrder_OrderId(orderId))
            .willReturn(Optional.of(orderCancellation));

        // when
        OrderCancellation result = orderCancellationService.findByOrderId(orderId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(orderCancellation);
        assertThat(result.getCancelReason()).isEqualTo("고객 요청");
        assertThat(result.getCancelledBy()).isEqualTo(100L);
        assertThat(result.getCancelType()).isEqualTo(CancelType.USER_REQUEST);

    }

    @Test
    @DisplayName("주문 ID로 취소 정보 조회 - 취소되지 않은 주문")
    void findByOrderId_NotCancelled() {
        // given
        UUID nonCancelledOrderId = UUID.randomUUID();
        given(orderCancellationRepository.findByOrder_OrderId(nonCancelledOrderId))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderCancellationService.findByOrderId(nonCancelledOrderId))
            .isInstanceOf(BusinessException.class)
            .hasMessage("해당 주문의 취소 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("전체 취소 정보 조회")
    void findAll_Success() {
        // given
        orderCancellation = create(order, "고객 요청", 100L, CancelType.USER_REQUEST);
        OrderCancellation cancellation2 = OrderCancellation.create(order,
            "재고 부족",
            200L,
            CancelType.OUT_OF_STOCK
        );
        List<OrderCancellation> cancellations = List.of(orderCancellation, cancellation2);

        given(orderCancellationRepository.findAll())
            .willReturn(cancellations);

        // when
        List<OrderCancellation> result = orderCancellationService.findAll();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(1).getCancelReason()).isEqualTo("재고 부족");
        assertThat(result.get(1).getCancelledBy()).isEqualTo(200L);
        assertThat(result.get(1).getCancelType()).isEqualTo(CancelType.OUT_OF_STOCK);
    }

    @Test
    @DisplayName("주문 취소 정보 저장 - 정상")
    void save_Success() {
        // given
        orderCancellation = create(order, "고객 요청", 100L, CancelType.USER_REQUEST);
        given(orderCancellationRepository.save(any(OrderCancellation.class)))
            .willReturn(orderCancellation);

        // when
        OrderCancellation result = orderCancellationService.save(orderCancellation);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(orderCancellation);
    }

    @Test
    @DisplayName("주문 취소 정보 저장 - null 저장 시도")
    void save_Fail_NullCancellation() {
        // when & then
        assertThatThrownBy(() -> orderCancellationService.save(null))
            .isInstanceOf(BusinessException.class)
            .hasMessage("취소 정보는 필수입니다.");
    }

//    @Test
//    @DisplayName("주문 취소 정보 삭제 - ID로 삭제")
//    void deleteById_Success() {
//        // given
//        orderCancellation = create(order, "고객 요청", 100L, CancelType.USER_REQUEST);
//        given(orderCancellationRepository.findById(cancellationId))
//            .willReturn(Optional.of(orderCancellation));
//
//        // when
//        orderCancellationService.deleteById(cancellationId);
//
//        // then
//        assertThat(orderCancellation)
//    }

    @Test
    @DisplayName("주문이 취소되었는지 확인 - 취소됨")
    void isOrderCancelled_True() {
        // given
        orderCancellation = create(order, "고객 요청", 100L, CancelType.USER_REQUEST);
        given(orderCancellationRepository.findByOrder_OrderId(orderId))
            .willReturn(Optional.of(orderCancellation));

        // when
        boolean result = orderCancellationService.isOrderCancelled(orderId);

        // then
        assertThat(result).isTrue();
    }

}