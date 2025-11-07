package com.klp.hub.product.presentation.dto;

import com.klp.hub.product.application.dto.ProductCreateCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProductCreateRequest(
        @NotNull(message = "업체 ID 는 필수입니다.")
        UUID companyId,

        @NotNull(message = "허브 ID 는 필수입니다.")
        UUID hubId,

        @NotBlank(message = "상품명은 필수입니다.")
        String name,

        @Min(value = 0, message = "초기 재고 수량은 0 이상이어야 합니다.")
        int quantity
) {
    public ProductCreateCommand toCommand() {
        return new ProductCreateCommand(
                companyId,
                hubId,
                name,
                quantity
        );
    }
}
