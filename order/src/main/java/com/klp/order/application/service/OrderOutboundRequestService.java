package com.klp.order.application.service;

import com.klp.common.exception.BusinessException;
import com.klp.order.application.command.CreateOrderOutboundRequestCommand;
import com.klp.order.domain.entity.idempotencykey.OperationType;
import com.klp.order.domain.entity.idempotencykey.OrderOutboundRequest;
import com.klp.order.domain.entity.idempotencykey.Target;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.repository.OrderOutboundRequestRepository;
import com.klp.order.domain.repository.OrderRepository;
import com.klp.order.global.exception.OrderErrorCode;
import com.klp.order.global.exception.OrderOutboundRequestErrorCode;
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
            .orElseThrow(
                () -> new BusinessException(OrderOutboundRequestErrorCode.REQUEST_REQUIRED));

        return OrderOutboundRequestResponse.from(request);
    }

    public OrderOutboundRequestResponse findByIdempotencyKey(String idempotencyKey) {
        OrderOutboundRequest request = orderOutboundRequestRepository.findByIdempotencyKey(
                idempotencyKey)
            .orElseThrow(() -> new BusinessException(
                OrderOutboundRequestErrorCode.REQUEST_BY_IDEMPOTENCY_KEY_NOT_FOUND));
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
            .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));

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
            throw new BusinessException(
                OrderOutboundRequestErrorCode.IDEMPOTENCY_KEY_ALREADY_EXISTS);
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
            throw new BusinessException(OrderOutboundRequestErrorCode.ORDER_ID_REQUIRED);
        }
        if (target == null) {
            throw new BusinessException(OrderOutboundRequestErrorCode.TARGET_REQUIRED);
        }
        if (operationType == null) {
            throw new BusinessException(OrderOutboundRequestErrorCode.OPERATION_TYPE_REQUIRED);
        }
    }
}