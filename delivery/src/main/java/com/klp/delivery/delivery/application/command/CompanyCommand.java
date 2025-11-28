package com.klp.delivery.delivery.application.command;

import com.klp.delivery.delivery.infrastructure.client.dto.CompanyResponse;

public record CompanyCommand(
    String companyId,
    String hubId,
    String type,
    String name,
    String address
) {

    public static CompanyCommand of(CompanyResponse response) {
        return new CompanyCommand(
            response.companyId(),
            response.hubId(),
            response.type(),
            response.name(),
            response.address()
        );
    }

}