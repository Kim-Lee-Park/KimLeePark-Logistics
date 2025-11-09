package com.klp.order.infrastructure.repository;

import com.klp.order.domain.entity.orderitem.OrderItem;
import com.klp.order.domain.repository.OrderItemRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderItemRepositoryImpl implements OrderItemRepository {

    private final OrderItemJpaRepository orderItemJpaRepository;

    @Override
    public OrderItem save(OrderItem orderItem) {
        return orderItemJpaRepository.save(orderItem);
    }

    @Override
    public Optional<OrderItem> findById(UUID orderItemId) {
        return orderItemJpaRepository.findById(orderItemId);
    }

    @Override
    public List<OrderItem> findAll() {
        return orderItemJpaRepository.findAll();
    }

    @Override
    public List<OrderItem> findByOrder_OrderId(UUID orderId) {
        return orderItemJpaRepository.findByOrder_OrderId(orderId);
    }

    @Override
    public List<OrderItem> findByDeliveryIdIsNull() {
        return orderItemJpaRepository.findByDeliveryIdIsNull();
    }

    @Override
    public List<OrderItem> findByDeletedAtIsNull() {
        return orderItemJpaRepository.findByDeletedAtIsNull();
    }

    @Override
    public List<OrderItem> findByOrder_OrderIdAndDeletedAtIsNull(UUID orderId) {
        return orderItemJpaRepository.findByOrder_OrderIdAndDeletedAtIsNull(orderId);
    }
}