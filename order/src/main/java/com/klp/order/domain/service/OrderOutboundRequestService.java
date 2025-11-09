package com.klp.order.domain.service;

import com.klp.order.domain.entity.idempotencykey.OperationType;
import com.klp.order.domain.entity.idempotencykey.OrderOutboundRequest;
import com.klp.order.domain.entity.idempotencykey.Target;
import com.klp.order.domain.repository.OrderOutboundRequestRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderOutboundRequestService {

    private final OrderOutboundRequestRepository orderOutboundRequestRepository;

    public OrderOutboundRequest findById(UUID requestId) {
        return orderOutboundRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("요청 정보를 찾을 수 없습니다."));
    }


    public OrderOutboundRequest findByIdempotencyKey(String idempotencyKey) {
        return orderOutboundRequestRepository.findByIdempotencyKey(idempotencyKey)
            .orElseThrow(() -> new IllegalArgumentException("해당 멱등키의 요청을 찾을 수 없습니다."));
    }


    public List<OrderOutboundRequest> findAll() {
        return orderOutboundRequestRepository.findAll();
    }


    @Transactional
    public OrderOutboundRequest save(OrderOutboundRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("요청 정보는 필수입니다.");
        }
        return orderOutboundRequestRepository.save(request);
    }


    public boolean existsByIdempotencyKey(String idempotencyKey) {
        return orderOutboundRequestRepository.findByIdempotencyKey(idempotencyKey).isPresent();
    }


    @Transactional
    public OrderOutboundRequest saveIfNotExists(OrderOutboundRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("요청 정보는 필수입니다.");
        }

        String idempotencyKey = request.getIdempotencyKey();
        Optional<OrderOutboundRequest> existing =
            orderOutboundRequestRepository.findByIdempotencyKey(idempotencyKey);

        if (existing.isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 멱등키 입니다.");
        }

        return orderOutboundRequestRepository.save(request);
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