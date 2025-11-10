package com.klp.order.domain.service;


import com.klp.order.command.CancelOrderCommand;
import com.klp.order.command.CreateOrderCommand;
import com.klp.order.command.UpdateOrderCommand;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.entity.order.OrderStatus;
import com.klp.order.domain.repository.OrderRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public Order createOrder(CreateOrderCommand command) {
        Order order = Order.create(
            command.supplierId(),
            command.customerId(),
            command.comment(),
            command.items()
        );

        return orderRepository.save(order);
    }

    public Order findById(UUID orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
    }

    @Transactional
    public Order updateOrder(UUID orderId, UpdateOrderCommand command) {
        Order order = findById(orderId);
        order.updateOrder(command.comment(), command.items());

        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(UUID orderId, CancelOrderCommand command) {
        Order order = findById(orderId);
        order.cancel(
            command.cancelReason(),
            command.cancelledBy(),
            command.cancelType()
        );
        return orderRepository.save(order);
    }

    public List<Order> findBySupplierId(Long supplierId) {
        return orderRepository.findBySupplierId(supplierId);
    }

    public List<Order> findByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    public List<Order> findNotDeletedOrders() {
        return orderRepository.findByDeletedAtIsNull();
    }

    @Transactional
    public Order changeOrderStatus(UUID orderId,
        OrderStatus newStatus) {
        Order order = findById(orderId);

        order.changeStatus(newStatus);

        return orderRepository.save(order);
    }

    @Transactional
    public Order deleteOrder(UUID orderId, Long deletedBy) {
        checkDeletedBy(deletedBy);
        Order order = findById(orderId);
        order.delete(deletedBy);

        return orderRepository.save(order);
    }

    private void checkDeletedBy(Long deletedBy) {
        if (deletedBy == null) {
            throw new IllegalArgumentException("삭제자는 필수 정보 입니다.");
        }
    }
}
