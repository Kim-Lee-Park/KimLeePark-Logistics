package com.klp.hub.company.presentation.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.klp.hub.company.application.CompanyService;
import com.klp.hub.company.domain.CompanyType;
import com.klp.hub.company.presentation.dto.CompanyResponse;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CompanyController.class)
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
}
