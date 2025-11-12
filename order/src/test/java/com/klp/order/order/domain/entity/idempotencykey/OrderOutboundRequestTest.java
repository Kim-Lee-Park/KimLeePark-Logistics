package com.klp.order.order.domain.entity.idempotencykey;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.klp.order.order.domain.entity.order.Order;
import com.klp.order.order.domain.entity.orderitem.OrderItemCommand;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("OrderOutboundRequest 엔티티 테스트")
public class OrderOutboundRequestTest {

    private Order order;
    private String idempotencyKey;
    private Target target;
    private OperationType operation;

    @BeforeEach
    void setUp() {
        Long supplierId = 1L;
        Long customerId = 2L;
        List<OrderItemCommand> initialItems = List.of(
            new OrderItemCommand(UUID.randomUUID(), 1)
        );
        order = Order.create(supplierId, customerId, "요청사항", initialItems);
        idempotencyKey = "멱등키";
        target = Target.INVENTORY;
        operation = OperationType.DECREASE;

    }

    @Test
    @DisplayName("멱등키를 생성할 수 있다")
    void createIdempotencyKey() {
        //when
        OrderOutboundRequest request = OrderOutboundRequest.create(
            order,
            idempotencyKey,
            target,
            operation
        );

        //then
        assertThat(request.getOrder()).isEqualTo(order);
        assertThat(request.getIdempotencyKey()).isEqualTo(idempotencyKey);
        assertThat(request.getTarget()).isEqualTo(target);
        assertThat(request.getOperation()).isEqualTo(operation);
    }

    @Test
    @DisplayName("멱등키가 Null 이면 예외가 발생한다")
    void idempotencyKey_is_null() {
        // when & then
        assertThatThrownBy(() -> OrderOutboundRequest.create(
            order, null, target, operation
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("멱등키는 필수입니다.");
    }

    @Test
    @DisplayName("멱등키 생성시 주문ID 가 Null 이면 예외가 발생한다")
    void createIdempotencyKey_orderId_is_null() {
        // when & then
        assertThatThrownBy(() -> OrderOutboundRequest.create(
            null, idempotencyKey, target, operation
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("주문 정보는 필수입니다.");
    }

    @Test
    @DisplayName("멱등키 생성시 Target 이 Null 이면 예외가 발생한다")
    void createIdempotencyKey_target_is_null() {
        // when & then
        assertThatThrownBy(() -> OrderOutboundRequest.create(
            order, idempotencyKey, null, operation
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("요청 대상은 필수입니다.");
    }

    @Test
    @DisplayName("멱등키 생성시 Operation 이 Null 이면 예외가 발생한다")
    void createIdempotencyKey_operation_is_null() {
        // when & then
        assertThatThrownBy(() -> OrderOutboundRequest.create(
            order, idempotencyKey, target, null
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("요청 작업은 필수입니다.");
    }

}
