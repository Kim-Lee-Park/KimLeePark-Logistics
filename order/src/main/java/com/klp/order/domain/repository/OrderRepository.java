package com.klp.order.domain.repository;

import com.klp.order.domain.entity.order.Order;
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

    List<Order> findBySupplierId(Long supplierId);

    List<Order> findByCustomerId(Long customerId);

    Optional<Order> findByOrderIdAndDeletedAtIsNull(UUID orderId);

    Page<Order> searchOrders(
        Long supplierId,
        Long customerId,
        Long createdBy,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    );
}