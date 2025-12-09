package com.klp.order.presentation.dto.order.request.create;

import com.klp.order.application.command.CreateOrderCommand;
import com.klp.order.application.command.OrderItemCommand;
import com.klp.order.presentation.dto.orderitem.request.OrderItemRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Schema(description = "주문 생성 요청")
public record CreateOrderRequest(

    @Schema(description = "사용자 ID", example = "1", required = true)
    @NotNull(message = "사용자 ID는 필수입니다.")
    Long userId,

    @Schema(description = "공급 업체 ID", example = "3b8d1f7e-6c4a-4d2b-9e5f-7a3c6b9d2e4f", required = true)
    @NotNull(message = "공급 업체 ID는 필수입니다.")
    UUID supplierId,

    @Schema(description = "사용자 쿠폰 ID", example = "d7f3c8a2-4b91-4e3d-9f6a-2c1e5b8d4a7f")
    UUID userCouponId,

    @Schema(description = "주문 코멘트", example = "빠른 배송 부탁드립니다.")
    String comment,

    @Schema(description = "배송지 주소 Id", example = "6e2a9f5c-1d84-4c6b-a3e7-8f0b2d5c9e1a", required = true)
    @NotBlank(message = "배송지 주소는 필수입니다.")
    UUID addressId,

    @Schema(description = "배송지 위도", example = "37.5665", required = true)
    @NotNull(message = "배송지 위도는 필수입니다.")
    BigDecimal deliveryLatitude,

    @Schema(description = "배송지 경도", example = "126.9780", required = true)
    @NotNull(message = "배송지 경도는 필수입니다.")
    BigDecimal deliveryLongitude,

    @Schema(description = "주문 상품 목록", required = true)
    @NotNull(message = "주문 상품은 필수입니다.")
    @NotEmpty(message = "주문 상품은 최소 1개 이상이어야 합니다.")
    @Valid
    List<OrderItemRequest> orderItems
) {

    public CreateOrderCommand toCommand() {
        List<OrderItemCommand> itemCommands = orderItems.stream()
            .map(item -> new OrderItemCommand(
                item.productId(),
                item.productName(),
                item.hubId(),
                item.quantity(),
                item.price()
            ))
            .toList();

        return new CreateOrderCommand(
            userId,
            supplierId,
            userCouponId,
            comment,
            addressId,
            deliveryLatitude,
            deliveryLongitude,
            itemCommands
        );
    }
}







