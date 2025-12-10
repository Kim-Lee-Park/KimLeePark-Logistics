package com.klp.order.order.domain.repository;

import com.klp.order.order.domain.entity.orderitem.OrderItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderItemRepository {

    OrderItem save(OrderItem orderItem);

    Optional<OrderItem> findById(UUID orderItemId);

    List<OrderItem> findAll();

    List<OrderItem> findByOrder_OrderId(UUID orderId);

    List<OrderItem> findByDeliveryIdIsNull();

    List<OrderItem> findByDeletedAtIsNull();

    List<OrderItem> findByOrder_OrderIdAndDeletedAtIsNull(UUID orderId);
}