package com.klp.hub.company.presentation.dto;

import java.util.UUID;

public record CompanyResponse(
    UUID companyId,
    UUID hubId,
    String type,
    String name,
    String address
) {

}
