package com.klp.order.domain.repository;

import com.klp.order.domain.entity.orderitem.OrderItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    List<OrderItem> findByOrder_OrderId(UUID orderId);

    List<OrderItem> findByDeliveryIdIsNull();

    List<OrderItem> findByDeletedAtIsNull();

    List<OrderItem> findByOrder_OrderIdAndDeletedAtIsNull(UUID orderId);
}
