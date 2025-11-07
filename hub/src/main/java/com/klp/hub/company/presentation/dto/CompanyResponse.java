package com.klp.hub.company.presentation.dto;

import java.util.UUID;

public record CompanyResponse(
        UUID id,
        String type,
        String name,
        String address
) {
}
