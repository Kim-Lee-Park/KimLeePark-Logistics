package com.klp.hub.product.presentation.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record ProductsPageResponse(
        List<ProductsPageRowResponse> products,
        PageMeta pageable
) {
    public static ProductsPageResponse from(Page<ProductsPageRowResponse> response) {
        return new ProductsPageResponse(
                response.getContent(),
                new PageMeta(
                        response.getNumber(),
                        response.getSize(),
                        response.getTotalElements(),
                        response.getTotalPages(),
                        response.hasNext(),
                        response.isFirst(),
                        response.isLast()
                )
        );
    }

    public record PageMeta(
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext,
            boolean isFirst,
            boolean isLast
    ) {}
}
