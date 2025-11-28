package com.klp.delivery.delivery.infrastructure.client.dto;

public record CompanyResponse(
    String companyId,
    String hubId,
    String type,
    String name,
    String address
) {
}