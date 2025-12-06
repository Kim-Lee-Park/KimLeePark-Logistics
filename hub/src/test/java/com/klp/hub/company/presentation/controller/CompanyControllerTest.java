package com.klp.hub.company.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.hub.company.application.CompanyService;
import com.klp.hub.company.application.dto.CreateCompanyCommand;
import com.klp.hub.company.domain.CompanyType;
import com.klp.hub.company.presentation.dto.request.CreateCompanyRequest;
import com.klp.hub.company.presentation.dto.response.CompanyListResponse.CompanySummaryResponse;
import com.klp.hub.company.presentation.dto.response.CompanyResponse;
import com.klp.hub.company.presentation.dto.response.CreateCompanyResponse;
import com.klp.hub.global.config.SecurityConfig;
import com.klp.hub.global.exception.GlobalExceptionHandler;
import com.klp.hub.global.filter.AuthorizationFilter;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CompanyController.class)
@Import({SecurityConfig.class, AuthorizationFilter.class, GlobalExceptionHandler.class})
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Test
    @DisplayName("업체 생성 요청 시 업체를 생성할 수 있다")
    @WithMockUser(roles = "MASTER")
    void createCompany() throws Exception {
        UUID hubId = UUID.randomUUID();
        UUID companyId = UUID.randomUUID();
        CreateCompanyRequest request = new CreateCompanyRequest(
            hubId,
            CompanyType.SUPPLIER.name(),
            "업체명",
            "업체주소"
        );
        CreateCompanyResponse response = new CreateCompanyResponse(companyId);
        when(companyService.create(any(CreateCompanyCommand.class)))
            .thenReturn(response);

        mockMvc.perform(post("/v1/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.companyId").isString());
    }
}
