package com.klp.order.order.domain.entity.order;

import com.klp.order.common.BaseEntity;
import com.klp.order.global.exception.BusinessException;
import com.klp.order.global.exception.OrderErrorCode;
import com.klp.order.order.application.command.OrderItemCommand;
import com.klp.order.order.domain.entity.cancel.CancelType;
import com.klp.order.order.domain.entity.cancel.OrderCancellation;
import com.klp.order.order.domain.entity.idempotencykey.OrderOutboundRequest;
import com.klp.order.order.domain.entity.orderitem.OrderItem;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_orders", schema = "order_schema")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_coupon_id")
    private UUID userCouponId;


    @Column(name = "supplier_id", nullable = false)
    private UUID supplierId;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    @Column(name = "original_price", nullable = false)
    private int originalPrice;

    @Column(name = "coupon_discount_price", nullable = false)
    private int couponDiscountPrice = 0;

    @Column(name = "grade_discount_price", nullable = false)
    private int gradeDiscountPrice = 0;

    @Column(name = "order_price", nullable = false)
    private int orderPrice;

    @Column(name = "address_id", nullable = false)
    private UUID addressId;

    @Column(name = "delivery_latitude", nullable = false, precision = 10, scale = 8)
    private BigDecimal deliveryLatitude;

    @Column(name = "delivery_longitude", nullable = false, precision = 11, scale = 8)
    private BigDecimal deliveryLongitude;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private OrderCancellation cancellation;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderOutboundRequest> outboundRequests = new ArrayList<>();


    public static Order create(
        Long userId,
        UUID userCouponId,
        UUID supplierId,
        String comment,
        UUID addressId,
        BigDecimal deliveryLatitude,
        BigDecimal deliveryLongitude,
        List<OrderItemCommand> itemCommands
    ) {
        Order order = new Order();

        order.validateUserId(userId);
        order.validateSupplierId(supplierId);
        order.validateDeliveryInfo(addressId, deliveryLatitude, deliveryLongitude);
        order.validateItemCommands(itemCommands);

        order.userId = userId;
        order.userCouponId = userCouponId;
        order.supplierId = supplierId;
        order.comment = comment;
        order.addressId = addressId;
        order.deliveryLatitude = deliveryLatitude;
        order.deliveryLongitude = deliveryLongitude;
        order.orderStatus = OrderStatus.PENDING;

        // 주문 아이템 생성 및 원가 계산
        int totalOriginalPrice = 0;
        for (OrderItemCommand command : itemCommands) {
            OrderItem orderItem = OrderItem.of(order, command);
            order.orderItems.add(orderItem);
            totalOriginalPrice += orderItem.getTotalPrice();
        }

        // 가격 설정
        order.originalPrice = totalOriginalPrice;
        order.couponDiscountPrice = 0;
        order.gradeDiscountPrice = 0;
        return order;
    }


    public void updateOrder(String comment, List<OrderItemCommand> itemCommands) {
        checkCanUpdate();
        this.comment = comment;

        if (itemCommands != null && !itemCommands.isEmpty()) {
            this.orderItems.clear();

            int totalOriginalPrice = 0;
            for (OrderItemCommand command : itemCommands) {
                OrderItem orderItem = OrderItem.of(this, command);
                this.orderItems.add(orderItem);
                totalOriginalPrice += orderItem.getTotalPrice();
            }

            this.originalPrice = totalOriginalPrice;
            this.calculateOrderPrice();
        }
    }

    // 최종 금액 계산
    private void calculateOrderPrice() {
        int calculatedPrice = originalPrice - couponDiscountPrice - gradeDiscountPrice;

        if (calculatedPrice < 0) {
            throw new BusinessException(OrderErrorCode.INVALID_ORDER_PRICE);
        }

        this.orderPrice = calculatedPrice;
    }

    public void changeStatus(OrderStatus newStatus) {
        if (this.orderStatus == OrderStatus.CANCELLED) {
            throw new BusinessException(OrderErrorCode.CANNOT_CHANGE_CANCELLED_ORDER_STATUS);
        }
        if (newStatus == null) {
            throw new BusinessException(OrderErrorCode.ORDER_STATUS_REQUIRED);
        }
        this.orderStatus = newStatus;
    }


    public OrderCancellation cancel(
        String cancelReason,
        Long cancelledBy,
        CancelType cancelType
    ) {
        checkCanCancel();
        this.orderStatus = OrderStatus.CANCELLED;
        this.cancellation = OrderCancellation.create(this, cancelReason, cancelledBy, cancelType);
        return this.cancellation;
    }

    public void updateDiscountPrice(int couponDiscountPrice, int gradeDiscountPrice,
        int finalPrice) {
        validateDiscounts(couponDiscountPrice, gradeDiscountPrice, finalPrice);
        this.couponDiscountPrice = couponDiscountPrice;
        this.gradeDiscountPrice = gradeDiscountPrice;
        this.orderPrice = finalPrice;
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(OrderErrorCode.USER_ID_REQUIRED);
        }
    }

    private void validateSupplierId(UUID supplierId) {
        if (supplierId == null) {
            throw new BusinessException(OrderErrorCode.SUPPLIER_ID_REQUIRED);
        }
    }

    private void validateDeliveryInfo(
        UUID deliveryAddress,
        BigDecimal deliveryLatitude,
        BigDecimal deliveryLongitude
    ) {
        if (deliveryAddress == null) {
            throw new BusinessException(OrderErrorCode.DELIVERY_ADDRESS_REQUIRED);
        }
        if (deliveryLatitude == null) {
            throw new BusinessException(OrderErrorCode.DELIVERY_LATITUDE_REQUIRED);
        }
        if (deliveryLongitude == null) {
            throw new BusinessException(OrderErrorCode.DELIVERY_LONGITUDE_REQUIRED);
        }

        // 위도 범위: -90 ~ 90
        if (deliveryLatitude.compareTo(BigDecimal.valueOf(-90)) < 0
            || deliveryLatitude.compareTo(BigDecimal.valueOf(90)) > 0) {
            throw new BusinessException(OrderErrorCode.INVALID_LATITUDE_RANGE);
        }

        // 경도 범위: -180 ~ 180
        if (deliveryLongitude.compareTo(BigDecimal.valueOf(-180)) < 0
            || deliveryLongitude.compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new BusinessException(OrderErrorCode.INVALID_LONGITUDE_RANGE);
        }
    }

    private void validateItemCommands(List<OrderItemCommand> itemCommands) {
        if (itemCommands == null) {
            throw new BusinessException(OrderErrorCode.ORDER_ITEMS_REQUIRED);
        }
        if (itemCommands.isEmpty()) {
            throw new BusinessException(OrderErrorCode.ORDER_ITEMS_MIN_REQUIRED);
        }
    }

    private void validateDiscounts(int couponDiscount, int gradeDiscount, int finalPrice) {
        if (userCouponId == null && couponDiscount > 0) {
            throw new BusinessException(OrderErrorCode.COUPON_DISCOUNT_WITHOUT_COUPON);
        }
        if (originalPrice - couponDiscount - gradeDiscount != finalPrice) {
            throw new BusinessException(OrderErrorCode.INVALID_DISCOUNT_AMOUNT);
        }

        if (couponDiscount < 0) {
            throw new BusinessException(OrderErrorCode.INVALID_COUPON_DISCOUNT);
        }
        if (gradeDiscount < 0) {
            throw new BusinessException(OrderErrorCode.INVALID_GRADE_DISCOUNT);
        }
    }


    private void checkCanCancel() {
        if (this.orderStatus == OrderStatus.DELIVERY_CREATED) {
            throw new BusinessException(OrderErrorCode.CANNOT_CANCEL_DELIVERY_ASSIGNED);
        }
        if (this.orderStatus == OrderStatus.DELIVERY_SHIPPING) {
            throw new BusinessException(OrderErrorCode.CANNOT_CANCEL_DELIVERY_SHIPPING);
        }

        if (this.orderStatus == OrderStatus.COMPLETE) {
            throw new BusinessException(OrderErrorCode.CANNOT_CANCEL_COMPLETED_ORDER);
        }
        if (this.orderStatus == OrderStatus.CANCELLED) {
            throw new BusinessException(OrderErrorCode.ALREADY_CANCELLED_ORDER);
        }
    }

    private void checkCanUpdate() {
        if (this.orderStatus == OrderStatus.DELIVERY_CREATED) {
            throw new BusinessException(OrderErrorCode.CANNOT_UPDATE_DELIVERY_ASSIGNED);
        }
        if (this.orderStatus == OrderStatus.DELIVERY_SHIPPING) {
            throw new BusinessException(OrderErrorCode.CANNOT_UPDATE_DELIVERY_SHIPPING);
        }
        if (this.orderStatus == OrderStatus.CANCELLED) {
            throw new BusinessException(OrderErrorCode.CANNOT_UPDATE_CANCELLED_ORDER);
        }
        if (this.orderStatus == OrderStatus.COMPLETE) {
            throw new BusinessException(OrderErrorCode.CANNOT_UPDATE_COMPLETED_ORDER);
        }
    }
}