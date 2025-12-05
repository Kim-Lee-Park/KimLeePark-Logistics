package com.klp.hub.company.presentation.dto.response;

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
