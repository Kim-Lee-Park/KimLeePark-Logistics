package com.klp.hub.company.presentation.controller;

import com.klp.hub.company.application.CompanyService;
import com.klp.hub.company.presentation.dto.CompanyListResponse;
import com.klp.hub.company.presentation.dto.CompanyListResponse.CompanySummaryResponse;
import com.klp.hub.company.presentation.dto.CompanyResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/v1/companies")
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/{companyId}")
    public ResponseEntity<CompanyResponse> getById(@PathVariable("companyId") String companyId) {
        log.info("== 단일 업체 조회 companyId: {} ==", companyId);
        CompanyResponse response = companyService.getByCompanyId(UUID.fromString(companyId));
        log.info("== 단일 업체 조회 성공 ==");
        return ResponseEntity.ok().body(response);
    }

    @GetMapping
    public ResponseEntity<CompanyListResponse> getAllByName(
        @RequestParam(value = "name", required = false) String name
    ) {
        log.info("== 업체 목록 조회 name : {} ==", name);
        List<CompanySummaryResponse> response = companyService.getAllByName(name);
        log.info("== 업체 목록 조회 성공 ==");
        return ResponseEntity.ok().body(new CompanyListResponse(response));
    }
}
