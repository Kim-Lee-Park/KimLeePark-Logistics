package com.klp.delivery.delivery.presentation.dto;

import com.klp.delivery.common.enums.IdempotencyStatus;
import com.klp.delivery.delivery.application.command.IdempotencyCommand;
import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand;
import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand.OrderItemCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DeliveryCreateRequest(
    @NotNull(message = "orderId는 필수입니다.")
    String orderId,

    @NotBlank(message = "idempotencyKey는 필수입니다.")
    String idempotencyKey,

    @NotNull(message = "userId는 필수입니다.")
    Long userId,

    String supplierId,
    String userCouponId,

    @NotBlank(message = "고객 이메일 주소는 필수입니다.")
    String email,

    @NotBlank(message = "고객명은 필수입니다.")
    String username,

    String comment,

    @NotNull(message = "원가는 필수입니다.")
    @Min(value = 0, message = "원가는 0 이상이어야 합니다")
    Integer originalPrice,

    @NotNull(message = "쿠폰 할인가는 필수입니다.")
    @Min(value = 0, message = "쿠폰 할인가는 0 이상이어야 합니다")
    Integer couponDiscountPrice,

    @NotNull(message = "등급 할인가는 필수입니다.")
    @Min(value = 0, message = "등급 할인가는 0 이상이어야 합니다")
    Integer gradeDiscountPrice,

    @NotNull(message = "최종 주문 금액은 필수입니다.")
    @Min(value = 0, message = "최종 주문 금액은 0 이상이어야 합니다")
    Integer finalOrderPrice,

    String addressId,

    @NotBlank(message = "고객 주소지 허브 ID는 필수입니다.")
    String userAddressHubId,

    @NotBlank(message = "고객 주소는 필수입니다.")
    String address,

    BigDecimal deliveryLatitude,
    BigDecimal deliveryLongitude,

    @NotEmpty(message = "orderItems는 필수이며 최소 1개 이상이어야 합니다.")
    @Valid
    List<OrderItem> orderItems,

    String inventoryIdempotencyKey,

    @NotNull(message = "주문 생성 시간은 필수입니다.")
    LocalDateTime createdAt,

    LocalDateTime occurredAt,

    String paymentId,
    Integer paidAmount,
    String paymentMethod,
    LocalDateTime paidAt
) {

    public OrderToDeliveryCommand toOrderToDeliveryCommand() {
        return new OrderToDeliveryCommand(
            UUID.fromString(orderId),
            userId,
            supplierId != null ? UUID.fromString(supplierId) : null,
            userCouponId != null ? UUID.fromString(userCouponId) : null,
            email,
            username,
            comment,
            originalPrice,
            couponDiscountPrice,
            gradeDiscountPrice,
            finalOrderPrice,
            addressId != null ? UUID.fromString(addressId) : null,
            UUID.fromString(userAddressHubId),
            address,
            deliveryLatitude,
            deliveryLongitude,
            orderItems.stream()
                .map(item -> new OrderItemCommand(
                    UUID.fromString(item.orderItemId()),
                    UUID.fromString(item.productId()),
                    item.productName(),
                    UUID.fromString(item.hubId()),
                    item.quantity(),
                    item.unitPrice(),
                    item.totalPrice()
                ))
                .toList(),
            inventoryIdempotencyKey,
            idempotencyKey,
            createdAt,
            occurredAt,
            paymentId != null ? UUID.fromString(paymentId) : null,
            paidAmount != null ? paidAmount : 0,
            paymentMethod,
            paidAt
        );
    }

    public IdempotencyCommand toIdempotencyCommand() {
        return new IdempotencyCommand(
            idempotencyKey,
            UUID.fromString(orderId),
            IdempotencyStatus.PENDING
        );
    }

    public record OrderItem(
        @NotBlank(message = "orderItemId는 필수입니다.")
        String orderItemId,

        @NotBlank(message = "productId는 필수입니다.")
        String productId,

        @NotBlank(message = "상품명은 필수입니다.")
        String productName,

        @NotBlank(message = "hubId는 필수입니다.")
        String hubId,

        @Min(value = 1, message = "수량은 1 이상이어야 합니다")
        int quantity,

        @NotNull(message = "단가는 필수입니다.")
        @Min(value = 0, message = "단가는 0 이상이어야 합니다")
        Integer unitPrice,

        @NotNull(message = "총 가격은 필수입니다.")
        @Min(value = 0, message = "총 가격은 0 이상이어야 합니다")
        Integer totalPrice
    ) {

    }
}