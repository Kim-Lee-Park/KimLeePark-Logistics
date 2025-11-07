package com.klp.order.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.klp.order.command.OrderItemCommand;
import com.klp.order.domain.idempotencykey.OperationType;
import com.klp.order.domain.idempotencykey.OrderOutboundRequest;
import com.klp.order.domain.idempotencykey.RequestStatus;
import com.klp.order.domain.order.Order;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class OrderOutBoundRequestTest {

    private Order order;
    private String idempotencyKey;
    private String target;
    private OperationType operation;

    @BeforeEach
    void setUp() {
        Long supplierId = 1L;
        Long customerId = 2L;
        List<OrderItemCommand> initialItems = List.of(
            new OrderItemCommand(UUID.randomUUID(), 1)
        );
        order = Order.create(supplierId, customerId, "요청사항", initialItems);
        idempotencyKey = "흠 멱등키는 어떻게 구성해야 잘했다고 소문날까나";
        target = "재고";
        operation = OperationType.DECREASE;

    }

    @Test
    @DisplayName("외부 요청 생성 - 정상")
    void 멱등키생성_정상() {
        //given
        //setUp

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
        assertThat(request.getStatus()).isEqualTo(RequestStatus.PENDING);
    }

    @Test
    @DisplayName("idempotencyKey null이면 예외")
    void 멱등키_Null이면_예외() {
        // when & then
        assertThatThrownBy(() -> OrderOutboundRequest.create(
            order, null, target, operation
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("멱등키는 필수입니다.");
    }

    @Test
    @DisplayName("order null이면 예외")
    void 오더ID가_NULL이면_예외() {
        // when & then
        assertThatThrownBy(() -> OrderOutboundRequest.create(
            null, idempotencyKey, target, operation
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("주문 정보는 필수입니다.");
    }

    @Test
    @DisplayName("target null이면 멱등키의 역할(재고,배송)을 알 수 없으므로 예외")
    void Target_NULL이면_예외() {
        // when & then
        assertThatThrownBy(() -> OrderOutboundRequest.create(
            order, idempotencyKey, null, operation
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("요청 대상은 필수입니다.");
    }

    @Test
    @DisplayName("operation null이면 (증감,감소.배송,취소)인지 알 수 없기 때문에 예외처리")
        //이 부분 또한 Operation은 행위에 관련된건데
        //재고는 증감,감소 배송은 생성과취소를 나타내고 있기 때문에
        // 이 부분이 null이면 예외처리로 진행하였습니다.
    void operation이_NULL이면_예외() {
        // when & then
        assertThatThrownBy(() -> OrderOutboundRequest.create(
            order, idempotencyKey, target, null
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("요청 작업은 필수입니다.");
    }

    @Test
    @DisplayName("요청 상태를 DONE으로 변경")
    void markAsDone_Success() {
        // given
        OrderOutboundRequest request = OrderOutboundRequest.create(
            order, idempotencyKey, target, operation
        );
        // when
        request.markAsDone();

        // then
        assertThat(request.getStatus()).isEqualTo(RequestStatus.DONE);
    }


}
