package com.klp.order.infrastructure.repository;

import com.klp.order.domain.entity.order.Order;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderJpaRepository extends JpaRepository<Order, UUID> {

    List<Order> findByDeletedAtIsNull();

    List<Order> findBySupplierId(Long supplierId);

    List<Order> findByCustomerId(Long customerId);

    Optional<Order> findByOrderIdAndDeletedAtIsNull(UUID orderId);

    @Query("SELECT o FROM Order o WHERE o.deletedAt IS NULL " +
        "AND (:supplierId IS NULL OR o.supplierId = :supplierId) " +
        "AND (:customerId IS NULL OR o.customerId = :customerId) " +
        "AND (:createdBy IS NULL OR o.createdBy = :createdBy) " +
        "AND (:startDate IS NULL OR o.createdAt >= :startDate) " +
        "AND (:endDate IS NULL OR o.createdAt <= :endDate)")
    Page<Order> searchOrders(
        @Param("supplierId") Long supplierId,
        @Param("customerId") Long customerId,
        @Param("createdBy") Long createdBy,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END " +
        "FROM Order o " +
        "JOIN o.orderItems oi " +
        "WHERE oi.hubId = :hubId " +
        "AND o.orderStatus != 'COMPLETE' " +
        "AND o.deletedAt IS NULL")
    boolean existsByHubIdAndOrderStatusNotComplete(@Param("hubId") UUID hubId);
}