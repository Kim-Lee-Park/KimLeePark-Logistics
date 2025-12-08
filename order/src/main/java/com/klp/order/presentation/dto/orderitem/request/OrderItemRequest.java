package com.klp.order.presentation.dto.orderitem.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "주문 상품 정보")
public record OrderItemRequest(

    @Schema(description = "상품 ID", example = "UUID", required = true)
    @NotNull(message = "상품 ID는 필수입니다.")
    UUID productId,

    @Schema(description = "상품명", example = "맥", required = true)
    @NotNull(message = "상품명은 필수입니다.")
    String productName,

    @Schema(description = "허브 ID", example = "UUID", required = true)
    @NotNull(message = "허브 ID는 필수입니다.")
    UUID hubId,

    @Schema(description = "주문 수량", example = "2", required = true, minimum = "1")
    @NotNull(message = "주문 수량은 필수입니다.")
    @Min(value = 1, message = "주문 수량은 1개 이상이어야 합니다.")
    Integer quantity,

    @Schema(description = "상품 단가", example = "1500000", required = true, minimum = "1")
    @NotNull(message = "상품 단가는 필수입니다.")
    @Min(value = 1, message = "상품 단가는 1원 이상이어야 합니다.")
    Integer price
) {

}
