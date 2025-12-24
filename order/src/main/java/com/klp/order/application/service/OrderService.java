package com.klp.order.application.service;


import com.klp.common.PageResponse;
import com.klp.global.exception.BusinessException;
import com.klp.global.exception.OrderErrorCode;
import com.klp.order.application.command.CancelOrderCommand;
import com.klp.order.application.command.CreateOrderCommand;
import com.klp.order.application.command.UpdateOrderCommand;
import com.klp.order.domain.entity.cancel.OrderCancellation;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.entity.order.OrderStatus;
import com.klp.order.domain.repository.OrderRepository;
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
    private final OrderCancellationService orderCancellationRepository;

    @Transactional
    public Order createOrder(CreateOrderCommand command) {
        Order order = Order.create(
            command.userId(),
            command.userCouponId(),
            command.supplierId(),
            command.comment(),
            command.addressId(),
            command.deliveryLatitude(),
            command.deliveryLongitude(),
            command.orderItems()
        );

        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Order findById(UUID orderId) {
        return orderRepository.findByIdWithDetails(orderId)
            .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
    }

    @Transactional
    public Order updateOrder(UUID orderId, UpdateOrderCommand command) {
        Order order = findById(orderId);
        order.updateOrder(command.comment(), command.items());

        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(CancelOrderCommand command) {
        Order order = findById(command.orderId());
        OrderCancellation cancellation = order.cancel(
            command.cancelReason(),
            command.cancelledBy(),
            command.cancelType()
        );
        orderCancellationRepository.save(cancellation);
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<Order> findBySupplierId(UUID supplierId) {
        return orderRepository.findBySupplierId(supplierId);
    }

    @Transactional(readOnly = true)
    public List<Order> findByCustomerId(Long userId) {
        return orderRepository.findByUserId(userId);
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
        UUID supplierId,
        Long userId,
        Long createdBy,
        LocalDate startDate,
        LocalDate endDate,
        Pageable pageable
    ) {
        LocalDateTime startDateTime = convertToStartDateTime(startDate);
        LocalDateTime endDateTime = convertToEndDateTime(endDate);

        Page<Order> orderPage = orderRepository.searchOrders(
            supplierId,
            userId,
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

    @Transactional(readOnly = true)
    public boolean hasProgressingOrders(UUID hubId) {
        checkHubId(hubId);
        return orderRepository.existsByHubIdAndOrderStatusNotComplete(hubId);
    }

    @Transactional
    public void updateDiscountPrice(Order order, int couponDiscountPrice, int gradeDiscountPrice,
        int finalPrice) {
        checkCouponDiscountPrice(couponDiscountPrice);
        checkGradeDiscountPrice(gradeDiscountPrice);
        order.updateDiscountPrice(couponDiscountPrice, gradeDiscountPrice, finalPrice);
        orderRepository.save(order);
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

    private void checkHubId(UUID hubId) {
        if (hubId == null) {
            throw new BusinessException(OrderErrorCode.HUB_ID_REQUIRED);
        }
    }

    private void checkCouponDiscountPrice(int couponDiscountPrice) {
        if (couponDiscountPrice < 0) {
            throw new BusinessException(OrderErrorCode.INVALID_COUPON_DISCOUNT);
        }
    }

    private void checkGradeDiscountPrice(int gradeDiscountPrice) {
        if (gradeDiscountPrice < 0) {
            throw new BusinessException(OrderErrorCode.INVALID_GRADE_DISCOUNT);
        }
    }
}
