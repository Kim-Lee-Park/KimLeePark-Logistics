package com.klp.hub.company.presentation.controller;

import com.klp.hub.company.application.CompanyService;
import com.klp.hub.company.application.dto.CreateCompanyCommand;
import com.klp.hub.company.presentation.docs.CompanyControllerDocs;
import com.klp.hub.company.presentation.dto.request.CreateCompanyRequest;
import com.klp.hub.company.presentation.dto.response.CompanyListResponse;
import com.klp.hub.company.presentation.dto.response.CompanyListResponse.CompanySummaryResponse;
import com.klp.hub.company.presentation.dto.response.CompanyResponse;
import com.klp.hub.company.presentation.dto.response.CreateCompanyResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/v1/companies")
public class CompanyController implements CompanyControllerDocs {

    private final CompanyService companyService;

    @Override
    @GetMapping("/{companyId}")
    public ResponseEntity<CompanyResponse> getById(@PathVariable("companyId") String companyId) {
        log.info("== 단일 업체 조회 companyId: {} ==", companyId);
        CompanyResponse response = companyService.getByCompanyId(UUID.fromString(companyId));
        log.info("== 단일 업체 조회 성공 ==");
        return ResponseEntity.ok().body(response);
    }

    @Override
    @GetMapping
    public ResponseEntity<CompanyListResponse> getAllByName(
        @RequestParam(value = "name", required = true) String name
    ) {
        log.info("== 업체 목록 조회 name : {} ==", name);
        List<CompanySummaryResponse> response = companyService.getAllByName(name);
        log.info("== 업체 목록 조회 성공 ==");
        return ResponseEntity.ok().body(new CompanyListResponse(response));
    }

    @Override
    @PostMapping
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<CreateCompanyResponse> create(
        @Valid @RequestBody CreateCompanyRequest request
    ) {
        log.info("== 업체 생성 companyName : {} ==", request.name());
        CreateCompanyCommand command = request.toCommand();
        CreateCompanyResponse response = companyService.create(command);
        log.info("== 업체 생성 성공 ==");
        return ResponseEntity.ok().body(response);
    }
}
