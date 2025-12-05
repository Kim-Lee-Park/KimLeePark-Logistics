package com.klp.user.presentation;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.klp.global.exception.GlobalExceptionHandler;
import com.klp.global.security.config.SecurityConfig;
import com.klp.global.security.filter.AuthorizationFilter;
import com.klp.user.application.UserFacade;
import com.klp.user.presentation.dto.response.DriverDetailResponse;
import com.klp.user.presentation.dto.response.DriverInfo;
import com.klp.user.presentation.dto.response.HubDriverListResponse;
import com.klp.user.presentation.dto.response.LogisticsDriverListResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserInternalController.class)
@Import({SecurityConfig.class, AuthorizationFilter.class, GlobalExceptionHandler.class})
class UserInternalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserFacade userFacade;

    @Nested
    @DisplayName("허브 소속 배송 담당자 조회 테스트")
    class GetDriversByHubIdTest {

        @Test
        @DisplayName("성공 시 200 OK와 배송 담당자 목록을 반환한다")
        void getDriversByHubId_success() throws Exception {
            // given
            UUID hubId = UUID.randomUUID();
            DriverInfo driver1 = new DriverInfo(1L, "driver1", "slack1", "010-1111-1111", "driver1@example.com");
            DriverInfo driver2 = new DriverInfo(2L, "driver2", "slack2", "010-2222-2222", "driver2@example.com");
            HubDriverListResponse response = HubDriverListResponse.of(hubId, List.of(driver1, driver2));

            // when
            when(userFacade.getDriversByHubId(hubId)).thenReturn(response);

            // then
            mockMvc.perform(get("/v1/internal/users/driver/" + hubId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hubId").value(hubId.toString()))
                .andExpect(jsonPath("$.drivers.length()").value(2))
                .andExpect(jsonPath("$.drivers[0].userId").value(1))
                .andExpect(jsonPath("$.drivers[0].username").value("driver1"));
        }
    }

    @Nested
    @DisplayName("물류회사 소속 배송 담당자 조회 테스트")
    class GetDriversByLogisticsTest {

        @Test
        @DisplayName("성공 시 200 OK와 배송 담당자 목록을 반환한다")
        void getDriversByLogistics_success() throws Exception {
            // given
            DriverInfo driver1 = new DriverInfo(1L, "driver1", "slack1", "010-1111-1111", "driver1@example.com");
            DriverInfo driver2 = new DriverInfo(2L, "driver2", "slack2", "010-2222-2222", "driver2@example.com");
            LogisticsDriverListResponse response = LogisticsDriverListResponse.of(List.of(driver1, driver2));

            // when
            when(userFacade.getDriversByLogistics()).thenReturn(response);

            // then
            mockMvc.perform(get("/v1/internal/users/driver/logistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.drivers.length()").value(2))
                .andExpect(jsonPath("$.drivers[0].userId").value(1))
                .andExpect(jsonPath("$.drivers[0].username").value("driver1"));
        }
    }

    @Nested
    @DisplayName("배송 담당자 상세 조회 테스트")
    class GetDriverByIdTest {

        @Test
        @DisplayName("성공 시 200 OK와 배송 담당자 상세 정보를 반환한다")
        void getDriverById_success() throws Exception {
            // given
            Long driverId = 1L;
            UUID hubId = UUID.randomUUID();
            DriverDetailResponse response = new DriverDetailResponse(
                driverId, hubId, "driver1", "slack1", "010-1111-1111", "driver1@example.com"
            );

            // when
            when(userFacade.getDriverById(driverId)).thenReturn(response);

            // then
            mockMvc.perform(get("/v1/internal/users/driver")
                    .param("id", driverId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.hubId").value(hubId.toString()))
                .andExpect(jsonPath("$.username").value("driver1"))
                .andExpect(jsonPath("$.slackId").value("slack1"))
                .andExpect(jsonPath("$.phone").value("010-1111-1111"));
        }
    }
}
