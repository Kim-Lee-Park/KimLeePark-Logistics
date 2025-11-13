package com.klp.order.application.service;


import com.klp.common.exception.BusinessException;
import com.klp.order.domain.entity.orderitem.OrderItem;
import com.klp.order.domain.repository.OrderItemRepository;
import com.klp.order.global.exception.OrderItemErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;

    @Transactional(readOnly = true)
    public OrderItem findById(UUID orderItemId) {
        return orderItemRepository.findById(orderItemId)
            .orElseThrow(() -> new BusinessException(OrderItemErrorCode.ORDER_ITEM_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<OrderItem> findByOrderId(UUID orderId) {
        return orderItemRepository.findByOrder_OrderId(orderId);
    }

    @Transactional(readOnly = true)
    public List<OrderItem> findUnassignedDeliveryItems() {
        return orderItemRepository.findByDeliveryIdIsNull();
    }

    @Transactional(readOnly = true)
    public List<OrderItem> findNotDeletedItems() {
        return orderItemRepository.findByDeletedAtIsNull();
    }

    @Transactional(readOnly = true)
    public List<OrderItem> findNotDeletedItemsByOrderId(UUID orderId) {
        return orderItemRepository.findByOrder_OrderIdAndDeletedAtIsNull(orderId);
    }

    @Transactional(readOnly = true)
    public List<OrderItem> findAll() {
        return orderItemRepository.findAll();
    }


    @Transactional
    public OrderItem updateQuantity(UUID orderItemId, int newQuantity) {
        OrderItem orderItem = findById(orderItemId);
        orderItem.updateQuantity(newQuantity);

        return orderItemRepository.save(orderItem);
    }


    @Transactional
    public OrderItem assignDeliveryId(UUID orderItemId, UUID deliveryId) {
        OrderItem orderItem = findById(orderItemId);
        orderItem.assignDeliveryId(deliveryId);

        return orderItemRepository.save(orderItem);
    }


    @Transactional
    public List<OrderItem> assignDeliveryIdBatch(List<UUID> orderItemIds, UUID deliveryId) {
        if (orderItemIds == null || orderItemIds.isEmpty()) {
            throw new BusinessException(OrderItemErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        List<OrderItem> updatedItems = new ArrayList<>();

        for (UUID orderItemId : orderItemIds) {
            OrderItem orderItem = findById(orderItemId);
            orderItem.assignDeliveryId(deliveryId);
            OrderItem savedItem = orderItemRepository.save(orderItem);
            updatedItems.add(savedItem);
        }

        return updatedItems;
    }

    @Transactional
    public void deleteOrderItem(UUID orderItemId, Long deletedBy) {
        OrderItem orderItem = findById(orderItemId);
        orderItem.delete(deletedBy);

        orderItemRepository.save(orderItem);
    }
}
