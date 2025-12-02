package com.klp.user.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.klp.global.exception.GlobalExceptionHandler;
import com.klp.global.security.config.SecurityConfig;
import com.klp.global.security.filter.AuthorizationFilter;
import com.klp.user.application.UserService;
import com.klp.user.presentation.dto.response.DriverDetailResponse;
import com.klp.user.presentation.dto.response.DriverInfo;
import com.klp.user.presentation.dto.response.HubDriverListResponse;
import com.klp.user.presentation.dto.response.LogisticsDriverListResponse;
import com.klp.user.presentation.dto.response.UsernameCheckResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@Disabled
@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, AuthorizationFilter.class, GlobalExceptionHandler.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Nested
    @DisplayName("유저 이름 사용 가능 여부 확인 테스트")
    class heckUserNameDuplicateTest {

        @Test
        @DisplayName("성공 시 200 OK를 반환한다")
        void checkUserNameDuplication_success() throws Exception {
            // given
            final String username = "testuser";

            // when
            when(userService.checkUserNameAvailable(username)).thenReturn(
                any(UsernameCheckResponse.class));

            // then
            mockMvc.perform(get("/v1/users/check")
                    .param("username", username))
                .andExpect(status().isOk());
        }
    }

    // ================== 아래 쪽 권한이 필요한 테스트의 경우 현재 커스텀 UserDetailsImpl을 사용하기 때문에 테스트 불가 ===================
    // ==================@WitMockUser로는 커스텀 UserDetailsImpl을 가져올 수 없어 별도의 어노테이션을 구성해서 사용 예정 ================

    @Nested
    @DisplayName("본인 정보 상세 조회 테스트")
    class getMyDetailsTest {

    }

    @Nested
    @DisplayName("유저 목록 조회 테스트")
    class getUserListTest {

    }

    @Nested
    @DisplayName("유저 정보 변경 테스트")
    class updateUserInfo {

    }

    @Nested
    @DisplayName("허브 소속 배송 담당자 조회 테스트")
    class GetDriversByHubIdTest {

        @Test
        @DisplayName("성공 시 200 OK와 배송 담당자 목록을 반환한다")
        void getDriversByHubId_success() throws Exception {
            // given
            UUID hubId = UUID.randomUUID();
            DriverInfo driver1 = new DriverInfo(1L, "driver1", "slack1", "010-1111-1111",
                "driver1@example.com");
            DriverInfo driver2 = new DriverInfo(2L, "driver2", "slack2", "010-2222-2222",
                "driver2@example.com");
            HubDriverListResponse response = HubDriverListResponse.of(hubId,
                List.of(driver1, driver2));

            // when
            when(userService.getDriversByHubId(hubId)).thenReturn(response);

            // then
            mockMvc.perform(get("/v1/users/driver/" + hubId))
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
            DriverInfo driver1 = new DriverInfo(1L, "driver1", "slack1", "010-1111-1111",
                "driver1@example.com");
            DriverInfo driver2 = new DriverInfo(2L, "driver2", "slack2", "010-2222-2222",
                "driver2@example.com");
            LogisticsDriverListResponse response = LogisticsDriverListResponse.of(
                List.of(driver1, driver2));

            // when
            when(userService.getDriversByLogistics()).thenReturn(response);

            // then
            mockMvc.perform(get("/v1/users/driver/logistics"))
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
            when(userService.getDriverById(driverId)).thenReturn(response);

            // then
            mockMvc.perform(get("/v1/users/driver")
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
