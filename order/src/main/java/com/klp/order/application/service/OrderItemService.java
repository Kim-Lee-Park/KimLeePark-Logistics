package com.klp.order.application.service;


import com.klp.order.domain.entity.orderitem.OrderItem;
import com.klp.order.domain.repository.OrderItemRepository;
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


    public OrderItem findById(UUID orderItemId) {
        return orderItemRepository.findById(orderItemId)
            .orElseThrow(() -> new IllegalArgumentException("주문 아이템을 찾을 수 없습니다."));
    }


    public List<OrderItem> findByOrderId(UUID orderId) {
        return orderItemRepository.findByOrder_OrderId(orderId);
    }


    public List<OrderItem> findUnassignedDeliveryItems() {
        return orderItemRepository.findByDeliveryIdIsNull();
    }


    public List<OrderItem> findNotDeletedItems() {
        return orderItemRepository.findByDeletedAtIsNull();
    }

    public List<OrderItem> findNotDeletedItemsByOrderId(UUID orderId) {
        return orderItemRepository.findByOrder_OrderIdAndDeletedAtIsNull(orderId);
    }

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
            throw new IllegalArgumentException("주문 아이템이 존재하지 않습니다.");
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
