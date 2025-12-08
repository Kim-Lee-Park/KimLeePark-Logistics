package com.klp.hub.company.presentation.dto.request;

import com.klp.hub.company.application.dto.CreateCompanyCommand;
import com.klp.hub.company.domain.CompanyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateCompanyRequest(
    @NotNull(message = "허브 ID 는 필수 값입니다.")
    UUID hubId,
    @NotBlank(message = "업체 타입은 필수 값입니다.")
    String type,
    @NotBlank(message = "업체명은 필수 값입니다.")
    String name,
    @NotBlank(message = "주소는 필수 값입니다.")
    String address
) {

    public CreateCompanyCommand toCommand() {
        CompanyType companyType = CompanyType.from(type);

        return new CreateCompanyCommand(
            hubId,
            companyType,
            name,
            address
        );
    }
}
