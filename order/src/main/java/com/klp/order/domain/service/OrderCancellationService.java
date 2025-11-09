package com.klp.order.domain.service;

import com.klp.order.domain.entity.cancel.OrderCancellation;
import com.klp.order.domain.repository.OrderCancellationRepository;
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
            .orElseThrow(() -> new IllegalArgumentException("주문 취소 정보를 찾을 수 없습니다."));
    }


    public OrderCancellation findByOrderId(UUID orderId) {
        return orderCancellationRepository.findByOrder_OrderId(orderId)
            .orElseThrow(() -> new IllegalArgumentException("해당 주문의 취소 정보를 찾을 수 없습니다."));
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
            throw new IllegalArgumentException("취소 정보는 필수입니다.");
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
