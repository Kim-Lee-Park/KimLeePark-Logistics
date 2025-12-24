package com.klp.order.infrastructure.repository;

import com.klp.order.domain.entity.orderitem.OrderItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemJpaRepository extends JpaRepository<OrderItem, UUID> {

    List<OrderItem> findByOrder_OrderId(UUID orderId);

    List<OrderItem> findByDeliveryIdIsNull();

    List<OrderItem> findByDeletedAtIsNull();

    List<OrderItem> findByOrder_OrderIdAndDeletedAtIsNull(UUID orderId);
}