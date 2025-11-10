package com.klp.order.application.service;

import com.klp.common.exception.BusinessException;
import com.klp.order.domain.entity.cancel.OrderCancellation;
import com.klp.order.domain.repository.OrderCancellationRepository;
import com.klp.order.global.exception.OrderCancellationErrorCode;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderCancellationService {

    private final OrderCancellationRepository orderCancellationRepository;


    public OrderCancellation findById(UUID cancellationId) {
        return orderCancellationRepository.findById(cancellationId)
            .orElseThrow(
                () -> new BusinessException(OrderCancellationErrorCode.CANCELLATION_NOT_FOUND));
    }


    public OrderCancellation findByOrderId(UUID orderId) {
        return orderCancellationRepository.findByOrder_OrderId(orderId)
            .orElseThrow(() -> new BusinessException(
                OrderCancellationErrorCode.CANCELLATION_BY_ORDER_NOT_FOUND));
    }


    public List<OrderCancellation> findAll() {
        return orderCancellationRepository.findAll();
    }

    @Transactional
    public OrderCancellation save(OrderCancellation cancellation) {
        checkCancellationisNull(cancellation);
        return orderCancellationRepository.save(cancellation);
    }

    private void checkCancellationisNull(OrderCancellation cancellation) {
        if (cancellation == null) {
            throw new BusinessException(OrderCancellationErrorCode.CANCELLATION_REQUIRED);
        }
    }

//    @Transactional
//    public void deleteById(UUID cancellationId) {
//        // 삭제 전 존재 여부 확인
//        findById(cancellationId);
//        orderCancellationRepository.deleteById(cancellationId);
//    }


    public boolean isOrderCancelled(UUID orderId) {
        return orderCancellationRepository.findByOrder_OrderId(orderId).isPresent();
    }

}
