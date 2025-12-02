package com.klp.hub.company.presentation.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.klp.hub.company.application.CompanyService;
import com.klp.hub.company.domain.CompanyType;
import com.klp.hub.company.presentation.dto.CompanyListResponse.CompanySummaryResponse;
import com.klp.hub.company.presentation.dto.CompanyResponse;
import com.klp.hub.global.config.SecurityConfig;
import com.klp.hub.global.exception.GlobalExceptionHandler;
import com.klp.hub.global.filter.AuthorizationFilter;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CompanyController.class)
@Import({SecurityConfig.class, AuthorizationFilter.class, GlobalExceptionHandler.class})
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompanyService companyService;

    private UUID hubID = UUID.randomUUID();

    @Test
    @DisplayName("업체 ID 로 업체를 조회할 수 있다")
    void getCompanyById() throws Exception {
        UUID companyId = UUID.randomUUID();
        when(companyService.getByCompanyId(companyId))
            .thenReturn(
                new CompanyResponse(
                    companyId,
                    hubID,
                    CompanyType.SUPPLIER.name(),
                    "업체명",
                    "업체주소"
                )
            );

        mockMvc.perform(get("/v1/companies/{companyId}", companyId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.companyId").isString())
            .andExpect(jsonPath("$.hubId").isString())
            .andExpect(jsonPath("$.type").isString())
            .andExpect(jsonPath("$.name").isString())
            .andExpect(jsonPath("$.address").isString());
    }

    @Test
    @DisplayName("업체명을 통해 업체 목록을 조회할 수 있다")
    void getAllByName() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<CompanySummaryResponse> mockList = List.of(
            new CompanySummaryResponse(id1, "업체A"),
            new CompanySummaryResponse(id2, "업체A")
        );
        when(companyService.getAllByName("업체A"))
            .thenReturn(mockList);

        mockMvc.perform(get("/v1/companies")
                .param("name", "업체A"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.companies").isArray())
            .andExpect(jsonPath("$.companies.length()").value(2))
            .andExpect(jsonPath("$.companies[0].companyId").value(id1.toString()))
            .andExpect(jsonPath("$.companies[0].companyName").value("업체A"))
            .andExpect(jsonPath("$.companies[1].companyId").value(id2.toString()))
            .andExpect(jsonPath("$.companies[1].companyName").value("업체A"));
    }

    @Disabled
    @Test
    @DisplayName("name 파라미터가 없다면 전체 업체 목록 조회를 할 수 있다")
    void getAllCompaniesWithoutName() throws Exception {
        List<CompanySummaryResponse> mockList = List.of(
            new CompanySummaryResponse(UUID.randomUUID(), "업체A"),
            new CompanySummaryResponse(UUID.randomUUID(), "업체B")
        );
        when(companyService.getAllByName(null))
            .thenReturn(mockList);

        mockMvc.perform(get("/v1/companies"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.companies").isArray())
            .andExpect(jsonPath("$.companies.length()").value(2));
    }
}
