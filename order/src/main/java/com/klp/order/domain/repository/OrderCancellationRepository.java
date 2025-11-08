package com.klp.order.domain.repository;

import com.klp.order.domain.entity.cancel.OrderCancellation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderCancellationRepository {

    OrderCancellation save(OrderCancellation orderCancellation);

    Optional<OrderCancellation> findById(UUID orderCancellationId);

    List<OrderCancellation> findAll();

    void deleteById(UUID orderCancellationId);

    Optional<OrderCancellation> findByOrder_OrderId(UUID orderId);
}