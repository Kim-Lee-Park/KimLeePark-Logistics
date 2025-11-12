package com.klp.order.order.domain.entity.cancel;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.klp.order.order.domain.entity.order.Order;
import com.klp.order.order.domain.entity.orderitem.OrderItemCommand;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("OrderCancellation 엔티티 테스트")
public class OrderCancellationTest {

    private Order order;
    private String cancelReason;
    private Long cancelledBy;
    private CancelType cancelType;

    @BeforeEach
    void setUp() {
        Long supplierId = 1L;
        Long customerId = 2L;
        String comment = "comment";
        List<OrderItemCommand> initialItems = List.of(
            new OrderItemCommand(UUID.randomUUID(), 1)
        );

        order = Order.create(supplierId, customerId, comment, initialItems);

        cancelReason = "취소 사유";
        cancelledBy = 2L;
        cancelType = CancelType.USER_REQUEST;

    }

    private void injectOrderCancellation(OrderCancellation cancellation) throws Exception {
        Field field = OrderCancellation.class.getDeclaredField("orderCancellationId");
        field.setAccessible(true);
        field.set(cancellation, UUID.randomUUID());
    }

    @Test
    @DisplayName("주문 취소를 생성할 수 있다")
    void createOrderCancellation_Success() throws Exception {
        //when
        OrderCancellation cancellation = OrderCancellation.create(
            order,
            cancelReason,
            cancelledBy,
            cancelType
        );
        injectOrderCancellation(cancellation);

        //then
        assertThat(cancellation.getOrderCancellationId()).isNotNull();
        assertThat(cancellation.getOrder()).isEqualTo(order);
        assertThat(cancellation.getCancelReason()).isEqualTo(cancelReason);
        assertThat(cancellation.getCancelledBy()).isEqualTo(cancelledBy);
        assertThat(cancellation.getCancelType()).isEqualTo(cancelType);
        assertThat(cancellation.getCancelledAt()).isNotNull();
        assertThat(cancellation.getCancelledAt()).isBefore(LocalDateTime.now().plusSeconds(1));
        assertThat(cancellation.getCancelledAt()).isAfter(LocalDateTime.now().minusSeconds(1));
    }

    @Test
    @DisplayName("주문 취소시 order 가 Null 이면 예외가 발생한다")
    void createOrderCancellation_order_is_null() {
        //when & then
        assertThatThrownBy(() -> OrderCancellation.create(
            null,
            cancelReason,
            cancelledBy,
            cancelType
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("주문 정보는 필수입니다.");
    }

    @Test
    @DisplayName("주문 취소시 취소자가 Null 이면 예외가 발생한다")
    void createOrderCancellation_cancelledBy_is_null() {
        //given
        //when & then
        assertThatThrownBy(() -> OrderCancellation.create(
            order,
            cancelReason,
            null,
            cancelType
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("취소자 정보는 필수입니다.");
    }

    @Test
    @DisplayName("주문 취소시 cancelType이 Null 이면 예외가 발생한다")
    void createOrderCancellation_cancelType_is_null() {
        //given
        //when & then
        assertThatThrownBy(() -> OrderCancellation.create(
            order,
            cancelReason,
            cancelledBy,
            null
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("취소 유형은 필수입니다.");
    }
}
