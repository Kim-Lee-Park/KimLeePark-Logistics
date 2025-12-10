package com.klp.order.order.domain.repository;

import com.klp.order.order.domain.entity.order.Order;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(UUID orderId);

    List<Order> findAll();

    Page<Order> findAll(Pageable pageable);

    void deleteById(UUID orderId);

    void delete(Order order);

    List<Order> findByDeletedAtIsNull();

    List<Order> findBySupplierId(UUID supplierId);

    List<Order> findByUserId(Long userId);

    Optional<Order> findByOrderIdAndDeletedAtIsNull(UUID orderId);

    Page<Order> searchOrders(
        UUID supplierId,
        Long userId,
        Long createdBy,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    );

    boolean existsByHubIdAndOrderStatusNotComplete(UUID hubId);

    Optional<Order> findByIdWithDetails(UUID orderId);
}