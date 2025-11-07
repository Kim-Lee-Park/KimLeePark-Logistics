package com.klp.order.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.klp.order.command.OrderItemCommand;
import com.klp.order.domain.cancel.CancelType;
import com.klp.order.domain.cancel.OrderCancellation;
import com.klp.order.domain.order.Order;
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
        String comment = "TDD 어디까지 해야 하는건가!";
        List<OrderItemCommand> initialItems = List.of(
            new OrderItemCommand(UUID.randomUUID(), 1)
        );

        order = Order.create(supplierId, customerId, comment, initialItems);

        cancelReason = "일단 취소 사유";
        cancelledBy = 2L;
        cancelType = CancelType.USER_REQUEST;

    }

    private void injectOrderCancellation(OrderCancellation cancellation) throws Exception {
        Field field = OrderCancellation.class.getDeclaredField("orderCancellationId");
        field.setAccessible(true);
        field.set(cancellation, UUID.randomUUID());
    }

    @Test
    @DisplayName("주문 취소 정보 생성 - 정상")
    void createOrderCancellation_Success() throws Exception {
        //given
        //setup

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
    @DisplayName("주문 취소 정보 생성 - order가 null일 경우 예외 발생")
    void 주문취소정보생성_order가null이면_예외발생() {
        //given
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
    @DisplayName("주문 취소 정보 생성 - cancelledBy가 null일 경우 예외 발생")
    void 주문취소정보생성_취소자가null이면_예외발생() {
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
    @DisplayName("주문 취소 정보 생성 - cancelType이 null일 경우 예외 발생")
    void 주문취소정보생성_취소정보가null이면_예외발생() {
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
