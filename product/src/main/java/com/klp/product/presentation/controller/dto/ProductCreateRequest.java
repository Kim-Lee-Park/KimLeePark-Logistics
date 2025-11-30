package com.klp.product.presentation.controller.dto;

public record ProductCreateRequest(
    String name,
    Integer stock
) {

}
