package com.klp.order.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.klp.order.application.command.OrderItemCommand;
import com.klp.order.domain.entity.idempotencykey.OperationType;
import com.klp.order.domain.entity.idempotencykey.OrderOutboundRequest;
import com.klp.order.domain.entity.idempotencykey.Target;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.global.AuditConfig;
import com.klp.order.infrastructure.repository.OrderJpaRepository;
import com.klp.order.infrastructure.repository.OrderOutboundRequestJpaRepository;
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
@DisplayName("멱등성 관련 테스트")
public class OrderOutboundRequestRepositoryTest {

    @Autowired
    private OrderOutboundRequestJpaRepository requestRepository;

    @Autowired
    private OrderJpaRepository orderRepository;

    private String idempotentKey1;
    private String idempotentKey2;
    private Order order1;
    private Order order2;
    private OrderOutboundRequest outboundRequest1;
    private List<OrderItemCommand> itemCommands1;
    private List<OrderItemCommand> itemCommands2;


    @BeforeEach
    void setup() {
        idempotentKey1 = "재고용 멱등키";
        idempotentKey2 = "배송용 멱등키";

        itemCommands1 = new ArrayList<>();
        itemCommands2 = new ArrayList<>();

        itemCommands1.add(new OrderItemCommand(UUID.randomUUID(), 10));
        itemCommands1.add(new OrderItemCommand(UUID.randomUUID(), 5));
        itemCommands2.add(new OrderItemCommand(UUID.randomUUID(), 20));

        order1 = Order.create(1L, 2L, "주문1", itemCommands1);
        order2 = Order.create(2L, 3L, "주문2", itemCommands2);

        order1 = orderRepository.save(order1);
        order2 = orderRepository.save(order2);

        outboundRequest1 = OrderOutboundRequest.create(
            order1,
            idempotentKey1,
            Target.DELIVERY,
            OperationType.MAKING
        );
        requestRepository.save(outboundRequest1);
    }

    @Test
    @DisplayName("외부 요청 저장 - 정상")
    void 외부요청_저장_정상() {
        // given
        OrderOutboundRequest newRequest = OrderOutboundRequest.create(
            order2,
            idempotentKey2,
            Target.INVENTORY,
            OperationType.DECREASE
        );

        // when
        OrderOutboundRequest savedRequest = requestRepository.save(newRequest);

        // then
        assertThat(savedRequest.getReqeustId()).isNotNull();
        assertThat(savedRequest.getIdempotencyKey()).isEqualTo(idempotentKey2);
        assertThat(savedRequest.getTarget()).isEqualTo(Target.INVENTORY);
        assertThat(savedRequest.getOperation()).isEqualTo(OperationType.DECREASE);
        assertThat(savedRequest.getOrder()).isEqualTo(order2);
    }

    @Test
    @DisplayName("외부 요청 ID로 조회 - 정상")
    void 외부요청_ID로_조회_정상() {
        // given
        //setup
        // requestRepository.save(outboundRequest1);
        UUID requestId = outboundRequest1.getReqeustId();

        // when
        Optional<OrderOutboundRequest> foundRequest = requestRepository.findById(requestId);

        // then
        assertThat(foundRequest).isPresent();
        assertThat(foundRequest.get().getReqeustId()).isEqualTo(requestId);
        assertThat(foundRequest.get().getIdempotencyKey()).isEqualTo(idempotentKey1);
    }

    @Test
    @DisplayName("외부 요청 ID로 조회 - 존재하지 않는 요청")
    void 존재하지않는_ID로_조회() {
        // given
        UUID nonExistentId = UUID.randomUUID();

        // when
        Optional<OrderOutboundRequest> foundRequest = requestRepository.findById(nonExistentId);

        // then
        assertThat(foundRequest).isEmpty();
    }

    @Test
    @DisplayName("전체 외부 요청 조회")
    void 전체_외부요청_조회() {
        // given
        // setup
        // requestRepository.save(outboundRequest1);

        // when
        List<OrderOutboundRequest> requests = requestRepository.findAll();

        // then
        assertThat(requests).hasSize(1);
    }

    @Test
    @DisplayName("멱등키로 외부 요청 조회 - 정상")
    void 멱등키로_외부요청_조회_정상() {
        // given
        // setup
        // requestRepository.save(outboundRequest1);
        String idempotencyKey = idempotentKey1;

        // when
        Optional<OrderOutboundRequest> foundRequest = requestRepository
            .findByIdempotencyKey(idempotencyKey);

        // then
        assertThat(foundRequest).isPresent();
        assertThat(foundRequest.get().getIdempotencyKey()).isEqualTo(idempotencyKey);
        assertThat(foundRequest.get().getTarget()).isEqualTo(Target.DELIVERY);
        assertThat(foundRequest.get().getOperation()).isEqualTo(OperationType.MAKING);
    }

    @Test
    @DisplayName("멱등키로 외부 요청 조회 - 존재하지 않는 키")
    void 존재하지않는_멱등키로_조회() {
        // given
        // requestRepository.save(outboundRequest1);
        String nonExistentKey = "존재하지-않는-키";

        // when
        Optional<OrderOutboundRequest> foundRequest = requestRepository
            .findByIdempotencyKey(nonExistentKey);

        // then
        assertThat(foundRequest).isEmpty();
    }

}
