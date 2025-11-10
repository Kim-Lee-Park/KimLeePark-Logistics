package com.klp.order.application.service;

import com.klp.order.application.command.CreateOrderOutboundRequestCommand;
import com.klp.order.domain.entity.idempotencykey.OperationType;
import com.klp.order.domain.entity.idempotencykey.OrderOutboundRequest;
import com.klp.order.domain.entity.idempotencykey.Target;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.repository.OrderOutboundRequestRepository;
import com.klp.order.domain.repository.OrderRepository;
import com.klp.order.presentation.dto.OrderOutboundRequestResponse;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderOutboundRequestService {

    private final OrderOutboundRequestRepository orderOutboundRequestRepository;
    private final OrderRepository orderRepository;

    public OrderOutboundRequestResponse findById(UUID requestId) {
        OrderOutboundRequest request = orderOutboundRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("요청 정보를 찾을 수 없습니다."));
        return OrderOutboundRequestResponse.from(request);
    }

    public OrderOutboundRequestResponse findByIdempotencyKey(String idempotencyKey) {
        OrderOutboundRequest request = orderOutboundRequestRepository.findByIdempotencyKey(
                idempotencyKey)
            .orElseThrow(() -> new IllegalArgumentException("해당 멱등키의 요청을 찾을 수 없습니다."));
        return OrderOutboundRequestResponse.from(request);
    }

    public List<OrderOutboundRequestResponse> findAll() {
        return orderOutboundRequestRepository.findAll().stream()
            .map(OrderOutboundRequestResponse::from)
            .collect(Collectors.toList());
    }

    @Transactional
    public OrderOutboundRequestResponse save(CreateOrderOutboundRequestCommand command) {
        Order order = orderRepository.findById(command.orderId())
            .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        OrderOutboundRequest request = OrderOutboundRequest.create(
            order,
            command.idempotencyKey(),
            command.target(),
            command.operation()
        );

        OrderOutboundRequest saved = orderOutboundRequestRepository.save(request);
        return OrderOutboundRequestResponse.from(saved);
    }

    public boolean existsByIdempotencyKey(String idempotencyKey) {
        return orderOutboundRequestRepository.findByIdempotencyKey(idempotencyKey).isPresent();
    }

    @Transactional
    public OrderOutboundRequestResponse saveIfNotExists(CreateOrderOutboundRequestCommand command) {
        // 멱등키 존재 여부 확인
        if (existsByIdempotencyKey(command.idempotencyKey())) {
            throw new IllegalArgumentException("이미 존재하는 멱등키입니다.");
        }

        // 존재하지 않으면 저장
        return save(command);
    }

    public String generateIdempotencyKey(
        UUID orderId,
        Target target,
        OperationType operationType
    ) {
        validateParameters(orderId, target, operationType);
        long timestamp = System.currentTimeMillis();

        return String.format("%s-%s-%s-%d",
            orderId.toString(),
            target.name(),
            operationType.name(),
            timestamp
        );
    }

    private void validateParameters(UUID orderId, Target target, OperationType operationType) {
        if (orderId == null) {
            throw new IllegalArgumentException("주문 ID는 필수입니다.");
        }
        if (target == null) {
            throw new IllegalArgumentException("타겟은 필수입니다.");
        }
        if (operationType == null) {
            throw new IllegalArgumentException("작업 타입은 필수입니다.");
        }
    }
}