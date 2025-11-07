package com.klp.hub.product.presentation.dto;

import com.klp.hub.product.application.dto.ProductUpdateCommand;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record ProductUpdateRequest(
        @NotBlank(message = "상품명은 필수입니다.")
        String name
) {
    public ProductUpdateCommand toCommand(String productId) {
        return new ProductUpdateCommand(
                UUID.fromString(productId),
                name
        );
    }
}
