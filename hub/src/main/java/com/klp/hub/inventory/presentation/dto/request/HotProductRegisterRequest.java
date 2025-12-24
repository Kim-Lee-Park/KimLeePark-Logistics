package com.klp.hub.inventory.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record HotProductRegisterRequest(
    @NotNull(message = "상품 ID는 필수 값입니다.")
    UUID productId,
    @NotNull(message = "허브 ID는 필수 값입니다.")
    UUID hubId,
    @NotNull(message = "TTL은 필수 값입니다.")
    @Positive(message = "TTL은 0보다 작거나 같을 수 없습니다.")
    Long ttlSeconds
) {

}
