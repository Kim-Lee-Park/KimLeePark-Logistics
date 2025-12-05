package com.klp.hub.company.application.dto;

import com.klp.hub.company.domain.CompanyType;
import java.util.UUID;

public record CreateCompanyCommand(
    UUID hubId,
    CompanyType type,
    String name,
    String address
) {

}
