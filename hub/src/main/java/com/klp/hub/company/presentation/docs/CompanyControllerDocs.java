package com.klp.hub.company.presentation.docs;

import com.klp.hub.company.presentation.dto.request.CreateCompanyRequest;
import com.klp.hub.company.presentation.dto.response.CompanyListResponse;
import com.klp.hub.company.presentation.dto.response.CompanyResponse;
import com.klp.hub.company.presentation.dto.response.CreateCompanyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Company API", description = "회사 관리 API")
public interface CompanyControllerDocs {

    @Operation(summary = "단일 업체 조회", description = "업체 ID 를 통해 업체를 조회합니다.")
    ResponseEntity<CompanyResponse> getById(
        @Parameter(description = "업체 ID(UUID 문자열)") String companyId
    );

    @Operation(summary = "업체 목록 조회 (이름 검색)", description = "업체 이름을 통해 업체를 조회합니다.")
    ResponseEntity<CompanyListResponse> getAllByName(
        @RequestParam(value = "name", required = true) String name
    );

    @Operation(summary = "업체 생성", description = "업체를 생성합니다.")
    ResponseEntity<CreateCompanyResponse> create(
        @RequestBody CreateCompanyRequest request
    );
}
