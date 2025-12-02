package com.klp.user.infrastructure.client.dto.response;

import java.util.List;
import java.util.UUID;

public record CompanyListResponse(
    List<CompanySummaryResponse> companies
) {

    public record CompanySummaryResponse(
        UUID companyId,
        String companyName
    ) {

    }
}
