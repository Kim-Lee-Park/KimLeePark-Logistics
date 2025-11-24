package com.klp.order.application.service;


import com.klp.common.exception.BusinessException;
import com.klp.order.application.command.CancelOrderCommand;
import com.klp.order.application.command.CreateOrderCommand;
import com.klp.order.application.command.UpdateOrderCommand;
import com.klp.order.common.PageResponse;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.entity.order.OrderStatus;
import com.klp.order.domain.repository.OrderRepository;
import com.klp.order.global.exception.OrderErrorCode;
import com.klp.order.presentation.dto.order.response.get.GetOrdersResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
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

    @Transactional(readOnly = true)
    public Order findById(UUID orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
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

    @Transactional(readOnly = true)
    public List<Order> findBySupplierId(Long supplierId) {
        return orderRepository.findBySupplierId(supplierId);
    }

    @Transactional(readOnly = true)
    public List<Order> findByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public PageResponse<GetOrdersResponse> searchOrders(
        Long supplierId,
        Long customerId,
        Long createdBy,
        LocalDate startDate,
        LocalDate endDate,
        Pageable pageable
    ) {
        LocalDateTime startDateTime = convertToStartDateTime(startDate);
        LocalDateTime endDateTime = convertToEndDateTime(endDate);

        Page<Order> orderPage = orderRepository.searchOrders(
            supplierId,
            customerId,
            createdBy,
            startDateTime,
            endDateTime,
            pageable
        );

        List<GetOrdersResponse> data = orderPage.getContent().stream()
            .map(GetOrdersResponse::from)
            .toList();

        return PageResponse.of(data, orderPage);
    }

    private void checkDeletedBy(Long deletedBy) {
        if (deletedBy == null) {
            throw new BusinessException(OrderErrorCode.DELETED_BY_REQUIRED);
        }
    }

    private LocalDateTime convertToStartDateTime(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }

    private LocalDateTime convertToEndDateTime(LocalDate date) {
        return date != null ? date.atTime(LocalTime.MAX) : null;
    }
}
