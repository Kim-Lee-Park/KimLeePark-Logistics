package com.klp.user.infrastructure.client.dto.response;

import java.util.UUID;

public record CompanyResponse(
    UUID companyId,
    UUID hubId,
    String type,
    String name,
    String address
) {

}
