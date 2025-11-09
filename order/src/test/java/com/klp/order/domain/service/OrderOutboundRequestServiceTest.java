package com.klp.order.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.klp.order.command.OrderItemCommand;
import com.klp.order.domain.entity.idempotencykey.OperationType;
import com.klp.order.domain.entity.idempotencykey.OrderOutboundRequest;
import com.klp.order.domain.entity.idempotencykey.Target;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.repository.OrderOutboundRequestRepository;
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
@DisplayName("OrderOutboundRequestService 테스트")
class OrderOutboundRequestServiceTest {

    @Mock
    private OrderOutboundRequestRepository orderOutboundRequestRepository;

    @InjectMocks
    private OrderOutboundRequestService orderOutboundRequestService;

    private Order order;
    private OrderOutboundRequest outboundRequest;
    private UUID requestId;
    private String idempotencyKey;

    @BeforeEach
    void setUp() {
        List<OrderItemCommand> itemCommands = List.of(
            new OrderItemCommand(UUID.randomUUID(), 10)
        );
        order = Order.create(1L, 2L, "테스트 주문", itemCommands);

        requestId = UUID.randomUUID();
        idempotencyKey = "test-idempotency-key-12345";
    }

    private OrderOutboundRequest create(Order order, String idempotencyKey, Target target,
        OperationType operationType) {
        return OrderOutboundRequest.create(order, idempotencyKey, target, operationType);
    }

    @Test
    @DisplayName("외부 요청 조회 - ID로 조회 성공")
    void findById_Success() {
        // given
        outboundRequest = create(order, idempotencyKey, Target.INVENTORY, OperationType.DECREASE);
        given(orderOutboundRequestRepository.findById(requestId))
            .willReturn(Optional.of(outboundRequest));

        // when
        OrderOutboundRequest result = orderOutboundRequestService.findById(requestId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(outboundRequest);
        assertThat(result.getIdempotencyKey()).isEqualTo(idempotencyKey);
        assertThat(result.getTarget()).isEqualTo(Target.INVENTORY);
        assertThat(result.getOperation()).isEqualTo(OperationType.DECREASE);
    }

    @Test
    @DisplayName("외부 요청 조회 - 존재하지 않는 ID")
    void findById_NotFound() {
        // given
        UUID nonExistentId = UUID.randomUUID();
        given(orderOutboundRequestRepository.findById(nonExistentId))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderOutboundRequestService.findById(nonExistentId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("요청 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("멱등키로 외부 요청 조회 - 정상")
    void findByIdempotencyKey_Success() {
        // given
        outboundRequest = create(order, idempotencyKey, Target.INVENTORY, OperationType.DECREASE);
        given(orderOutboundRequestRepository.findByIdempotencyKey(idempotencyKey))
            .willReturn(Optional.of(outboundRequest));

        // when
        OrderOutboundRequest result = orderOutboundRequestService
            .findByIdempotencyKey(idempotencyKey);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getIdempotencyKey()).isEqualTo(idempotencyKey);
        assertThat(result.getTarget()).isEqualTo(Target.INVENTORY);
        assertThat(result.getOperation()).isEqualTo(OperationType.DECREASE);
    }

    @Test
    @DisplayName("멱등키로 외부 요청 조회 - 존재하지 않는 키")
    void findByIdempotencyKey_NotFound() {
        // given
        String nonExistentKey = "non-existent-key";
        given(orderOutboundRequestRepository.findByIdempotencyKey(nonExistentKey))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderOutboundRequestService
            .findByIdempotencyKey(nonExistentKey))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("해당 멱등키의 요청을 찾을 수 없습니다.");

    }


    @Test
    @DisplayName("전체 외부 요청 조회")
    void findAll_Success() {
        // given
        outboundRequest = create(order, idempotencyKey, Target.INVENTORY, OperationType.DECREASE);
        OrderOutboundRequest request2 = OrderOutboundRequest.create(
            order,
            "another-key",
            Target.DELIVERY,
            OperationType.MAKING
        );
        List<OrderOutboundRequest> requests = List.of(outboundRequest, request2);

        given(orderOutboundRequestRepository.findAll())
            .willReturn(requests);

        // when
        List<OrderOutboundRequest> result = orderOutboundRequestService.findAll();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(1).getIdempotencyKey()).isEqualTo("another-key");
        assertThat(result.get(1).getTarget()).isEqualTo(Target.DELIVERY);
        assertThat(result.get(1).getOperation()).isEqualTo(OperationType.MAKING);
    }

    @Test
    @DisplayName("외부 요청 저장 - 정상")
    void save_Success() {
        // given
        outboundRequest = create(order, idempotencyKey, Target.INVENTORY, OperationType.DECREASE);
        given(orderOutboundRequestRepository.save(any(OrderOutboundRequest.class)))
            .willReturn(outboundRequest);

        // when
        OrderOutboundRequest result = orderOutboundRequestService.save(outboundRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(outboundRequest);
    }

    @Test
    @DisplayName("외부 요청 저장 - null 저장 시도")
    void save_Fail_NullRequest() {
        // when & then
        assertThatThrownBy(() -> orderOutboundRequestService.save(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("요청 정보는 필수입니다.");
    }

    @Test
    @DisplayName("멱등키 존재 여부 확인 - 존재함")
    void existsByIdempotencyKey_True() {
        // given
        outboundRequest = create(order, idempotencyKey, Target.INVENTORY, OperationType.DECREASE);
        given(orderOutboundRequestRepository.findByIdempotencyKey(idempotencyKey))
            .willReturn(Optional.of(outboundRequest));

        // when
        boolean result = orderOutboundRequestService.existsByIdempotencyKey(idempotencyKey);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("멱등키 존재 여부 확인 - 존재하지 않음")
    void existsByIdempotencyKey_False() {
        // given
        String nonExistentKey = "non-existent-key";
        given(orderOutboundRequestRepository.findByIdempotencyKey(nonExistentKey))
            .willReturn(Optional.empty());

        // when
        boolean result = orderOutboundRequestService.existsByIdempotencyKey(nonExistentKey);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("멱등성 체크 및 저장 - 새로운 요청 (저장)")
    void saveIfNotExists_NewRequest() {
        // given
        outboundRequest = create(order, idempotencyKey, Target.INVENTORY, OperationType.DECREASE);
        given(orderOutboundRequestRepository.findByIdempotencyKey(idempotencyKey))
            .willReturn(Optional.empty());
        given(orderOutboundRequestRepository.save(any(OrderOutboundRequest.class)))
            .willReturn(outboundRequest);

        // when
        OrderOutboundRequest result = orderOutboundRequestService
            .saveIfNotExists(outboundRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(outboundRequest);
        assertThat(result.getIdempotencyKey()).isEqualTo(idempotencyKey);
        assertThat(result.getTarget()).isEqualTo(Target.INVENTORY);
        assertThat(result.getOperation()).isEqualTo(OperationType.DECREASE);
    }

    @Test
    @DisplayName("멱등성 체크 및 저장 - 이미 존재하는 요청")
    void saveIfNotExists_ExistingRequest() {
        // given
        outboundRequest = create(order, idempotencyKey, Target.INVENTORY, OperationType.DECREASE);
        given(orderOutboundRequestRepository.findByIdempotencyKey(idempotencyKey))
            .willReturn(Optional.of(outboundRequest));

        //when&then

        assertThatThrownBy(() -> orderOutboundRequestService.saveIfNotExists(
            outboundRequest
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("이미 존재하는 멱등키 입니다.");

    }


    @Test
    @DisplayName("멱등키 생성 - 주문 ID, 타겟, 작업 타입 기반")
    void generateIdempotencyKey_Success() {
        // given
        UUID orderId = UUID.randomUUID();
        Target target = Target.DELIVERY;
        OperationType operation = OperationType.MAKING;

        // when
        String result = orderOutboundRequestService.generateIdempotencyKey(
            orderId, target, operation
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result).contains(orderId.toString());
        assertThat(result).contains(target.name());
        assertThat(result).contains(operation.name());
    }

    @Test
    @DisplayName("멱등키 생성 - order가 null일때")
    void generateIdempotencyKey_Fail_order_Null() {

        // when & then
        assertThatThrownBy(() -> orderOutboundRequestService.generateIdempotencyKey(
            null, Target.DELIVERY, OperationType.MAKING
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("주문 ID는 필수입니다.");
    }

    @Test
    @DisplayName("멱등키 생성 - 타겟이 null일때")
    void generateIdempotencyKey_Fail_target_Null() {

        // when & then
        assertThatThrownBy(() -> orderOutboundRequestService.generateIdempotencyKey(
            UUID.randomUUID(), null, OperationType.MAKING
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("타겟은 필수입니다.");
    }

    @Test
    @DisplayName("멱등키 생성 - operation이 null일때")
    void generateIdempotencyKey_Fail_operation_Null() {

        // when & then
        assertThatThrownBy(() -> orderOutboundRequestService.generateIdempotencyKey(
            UUID.randomUUID(), Target.DELIVERY, null
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("작업 타입은 필수입니다.");
    }
}
