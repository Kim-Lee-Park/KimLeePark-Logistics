package com.klp.order.order.infrastructure.repository;

import com.klp.order.order.domain.entity.order.Order;
import com.klp.order.order.domain.repository.OrderRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        return orderJpaRepository.findById(orderId);
    }

    @Override
    public List<Order> findAll() {
        return orderJpaRepository.findAll();
    }

    @Override
    public Page<Order> findAll(Pageable pageable) {
        return orderJpaRepository.findAll(pageable);
    }

    @Override
    public void deleteById(UUID orderId) {
        orderJpaRepository.deleteById(orderId);
    }

    @Override
    public void delete(Order order) {
        orderJpaRepository.delete(order);
    }

    @Override
    public List<Order> findByDeletedAtIsNull() {
        return orderJpaRepository.findByDeletedAtIsNull();
    }

    @Override
    public List<Order> findBySupplierId(UUID supplierId) {
        return orderJpaRepository.findBySupplierId(supplierId);
    }

    @Override
    public List<Order> findByUserId(Long userId) {
        return orderJpaRepository.findByUserId(userId);
    }

    @Override
    public Optional<Order> findByOrderIdAndDeletedAtIsNull(UUID orderId) {
        return orderJpaRepository.findByOrderIdAndDeletedAtIsNull(orderId);
    }

    @Override
    public Page<Order> searchOrders(
        UUID supplierId,
        Long userId,
        Long createdBy,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    ) {
        return orderJpaRepository.searchOrders(
            supplierId,
            userId,
            createdBy,
            startDate,
            endDate,
            pageable
        );
    }

    @Override
    public boolean existsByHubIdAndOrderStatusNotComplete(UUID hubId) {
        return orderJpaRepository.existsByHubIdAndOrderStatusNotComplete(hubId);
    }

    @Override
    public Optional<Order> findByIdWithDetails(UUID orderId) {
        return orderJpaRepository.findByIdWithDetails(orderId);
    }
}