package com.klp.hub.inventory.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record HotProductRequest(
    @NotNull(message = "상품 ID는 필수 값입니다.")
    UUID productId,
    @NotNull(message = "허브 ID는 필수 값입니다.")
    UUID hubId
) {
}
