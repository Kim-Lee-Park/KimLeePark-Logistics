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

    @Schema(description = "공급 업체 ID", example = "UUID", required = true)
    @NotNull(message = "공급 업체 ID는 필수입니다.")
    UUID supplierId,

    @Schema(description = "사용자 쿠폰 ID", example = "UUID")
    UUID userCouponId,

    @Schema(description = "주문 코멘트", example = "빠른 배송 부탁드립니다.")
    String comment,

    @Schema(description = "배송지 주소", example = "그대의 마음 속", required = true)
    @NotBlank(message = "배송지 주소는 필수입니다.")
    String deliveryAddress,

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
            deliveryAddress,
            deliveryLatitude,
            deliveryLongitude,
            itemCommands
        );
    }
}







