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

    List<Order> findBySupplierId(UUID supplierId);

    List<Order> findByUserId(Long userId);

    Optional<Order> findByOrderIdAndDeletedAtIsNull(UUID orderId);

    @Query("SELECT o FROM Order o WHERE o.deletedAt IS NULL " +
        "AND (:supplierId IS NULL OR o.supplierId = :supplierId) " +
        "AND (:userId IS NULL OR o.userId = :userId) " +
        "AND (:createdBy IS NULL OR o.createdBy = :createdBy) " +
        "AND (CAST(:startDate AS timestamp) IS NULL OR o.createdAt >= :startDate) " +
        "AND (CAST(:endDate AS timestamp) IS NULL OR o.createdAt <= :endDate)")
    Page<Order> searchOrders(
        @Param("supplierId") UUID supplierId,
        @Param("userId") Long userId,
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

    @Query("SELECT o FROM Order o " +
        "LEFT JOIN FETCH o.orderItems " +
        "LEFT JOIN FETCH o.cancellation " +
        "WHERE o.orderId = :orderId")
    Optional<Order> findByIdWithDetails(@Param("orderId") UUID orderId);
}