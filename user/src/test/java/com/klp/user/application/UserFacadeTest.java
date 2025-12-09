package com.klp.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.klp.common.model.PageResponse;
import com.klp.user.domain.entity.User;
import com.klp.user.domain.entity.UserGrade;
import com.klp.user.domain.enums.AffiliationType;
import com.klp.user.domain.enums.UserRole;
import com.klp.user.infrastructure.client.HubClient;
import com.klp.user.infrastructure.client.PromotionClient;
import com.klp.user.infrastructure.client.dto.response.CompanyListResponse;
import com.klp.user.infrastructure.client.dto.response.CompanyResponse;
import com.klp.user.infrastructure.client.dto.response.DefaultGradeResponse;
import com.klp.user.presentation.dto.request.UserCreateRequest;
import com.klp.user.presentation.dto.request.UserUpdateRequest;
import com.klp.user.presentation.dto.response.UserDetailResponse;
import com.klp.user.presentation.dto.response.UserGradeResponse;
import com.klp.user.presentation.dto.response.UserInfoResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserFacade 단위 테스트")
class UserFacadeTest {

    @Mock
    private UserService userService;

    @Mock
    private UserGradeService userGradeService;

    @Mock
    private HubClient hubClient;

    @Mock
    private PromotionClient promotionClient;

    @InjectMocks
    private UserFacade userFacade;

    private User testUser;
    private User customerUser;
    private final Long userId = 1L;
    private final Long customerId = 2L;
    private final UUID affiliationId = UUID.randomUUID();
    private final String username = "testuser";
    private final String customerName = "customer";

    @BeforeEach
    void setUp() {
        testUser = User.create(
            affiliationId,
            AffiliationType.HUB,
            username,
            "Password1!",
            "U12345678",
            "010-1234-5678",
            "test@example.com",
            UserRole.HUB
        );
        ReflectionTestUtils.setField(testUser, "userId", userId);

        customerUser = User.create(
            UUID.randomUUID(),
            AffiliationType.CUSTOMER,
            customerName,
            "Password1!",
            "U87654321",
            "010-8765-4321",
            "customer@example.com",
            UserRole.CUSTOMER
        );
        ReflectionTestUtils.setField(customerUser, "userId", customerId);
    }

    @Nested
    @DisplayName("createPendingUser 테스트")
    class CreatePendingUserTest {

        @Test
        @DisplayName("HUB 권한 회원가입 - Company 서비스 호출하여 소속 ID 조회")
        void createPendingUser_WithHubRole_CallsCompanyClient() {
            // given
            String companyName = "회사명";
            UserCreateRequest request = new UserCreateRequest(
                username,
                "Password1!",
                "U12345678",
                "010-1234-5678",
                "test@example.com",
                UserRole.HUB,
                companyName,
                AffiliationType.HUB
            );

            CompanyListResponse.CompanySummaryResponse companySummaryResponse =
                new CompanyListResponse.CompanySummaryResponse(affiliationId, companyName);
            CompanyListResponse companyListResponse = new CompanyListResponse(List.of(companySummaryResponse));

            given(hubClient.getCompaniesByName(companyName)).willReturn(companyListResponse);
            given(userService.createPendingUser(eq(request), eq(affiliationId))).willReturn(testUser);

            // when
            Long result = userFacade.createPendingUser(request);

            // then
            assertThat(result).isEqualTo(userId);
            then(hubClient).should(times(1)).getCompaniesByName(companyName);
            then(userService).should(times(1)).createPendingUser(eq(request), eq(affiliationId));
        }

        @Test
        @DisplayName("CUSTOMER 권한 회원가입 - Company 서비스 호출하지 않음")
        void createPendingUser_WithCustomerRole_DoesNotCallCompanyClient() {
            // given
            UserCreateRequest request = new UserCreateRequest(
                customerName,
                "Password1!",
                "U87654321",
                "010-8765-4321",
                "customer@example.com",
                UserRole.CUSTOMER,
                "고객",
                AffiliationType.CUSTOMER
            );

            given(userService.createPendingUser(eq(request), eq(null))).willReturn(customerUser);

            // when
            Long result = userFacade.createPendingUser(request);

            // then
            assertThat(result).isEqualTo(customerId);
            then(hubClient).should(never()).getCompaniesByName(anyString());
            then(userService).should(times(1)).createPendingUser(eq(request), eq(null));
        }
    }

    @Nested
    @DisplayName("approvePendingUser 테스트")
    class ApprovePendingUserTest {

        @Test
        @DisplayName("CUSTOMER 권한 승인 - Promotion 서비스에서 기본 등급 조회 후 UserGrade 생성")
        void approvePendingUser_WithCustomerRole_CreatesUserGrade() {
            // given
            String defaultGradeName = "등급 없음";
            DefaultGradeResponse defaultGradeResponse = new DefaultGradeResponse(defaultGradeName);

            UserGrade createdGrade = UserGrade.create(customerUser, defaultGradeName);
            ReflectionTestUtils.setField(createdGrade, "userGradeId", UUID.randomUUID());
            ReflectionTestUtils.setField(createdGrade, "evaluatedAt", LocalDateTime.now());

            given(userService.findNotDeletedUser(customerId)).willReturn(customerUser);
            given(promotionClient.getDefaultGrade()).willReturn(defaultGradeResponse);
            given(userGradeService.createUserGrade(customerUser, defaultGradeName)).willReturn(createdGrade);

            // when
            userFacade.approvePendingUser(customerId);

            // then
            then(userService).should(times(1)).findNotDeletedUser(customerId);
            then(userService).should(times(1)).approvePendingUser(customerUser);
            then(promotionClient).should(times(1)).getDefaultGrade();
            then(userGradeService).should(times(1)).createUserGrade(customerUser, defaultGradeName);
        }

        @Test
        @DisplayName("HUB 권한 승인 - UserGrade 생성하지 않음")
        void approvePendingUser_WithHubRole_DoesNotCreateUserGrade() {
            // given
            given(userService.findNotDeletedUser(userId)).willReturn(testUser);

            // when
            userFacade.approvePendingUser(userId);

            // then
            then(userService).should(times(1)).findNotDeletedUser(userId);
            then(userService).should(times(1)).approvePendingUser(testUser);
            then(promotionClient).should(never()).getDefaultGrade();
            then(userGradeService).should(never()).createUserGrade(any(), anyString());
        }
    }

    @Nested
    @DisplayName("rejectPendingUser 테스트")
    class RejectPendingUserTest {

        @Test
        @DisplayName("회원 거부 처리")
        void rejectPendingUser_RejectsUser() {
            // given
            given(userService.findNotDeletedUser(userId)).willReturn(testUser);

            // when
            userFacade.rejectPendingUser(userId);

            // then
            then(userService).should(times(1)).findNotDeletedUser(userId);
            then(userService).should(times(1)).rejectPendingUser(testUser);
        }
    }

    @Nested
    @DisplayName("getUserDetails 테스트")
    class GetUserDetailsTest {

        @Test
        @DisplayName("CUSTOMER 유저 상세 조회 - 등급 정보 포함")
        void getUserDetails_WithCustomerRole_IncludesGrade() {
            // given
            String gradeName = "브론즈";

            given(userService.findNotDeletedUser(customerId)).willReturn(customerUser);
            given(userGradeService.getCurrentGradeName(customerId)).willReturn(gradeName);

            // when
            UserDetailResponse response = userFacade.getUserDetails(customerId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.userId()).isEqualTo(customerId);
            assertThat(response.username()).isEqualTo(customerName);
            assertThat(response.gradeName()).isEqualTo(gradeName);
            assertThat(response.affiliationName()).isEqualTo("고객");
            then(userGradeService).should(times(1)).getCurrentGradeName(customerId);
        }

        @Test
        @DisplayName("HUB 유저 상세 조회 - Company 서비스 호출하여 소속명 조회")
        void getUserDetails_WithHubRole_CallsCompanyClient() {
            // given
            String companyName = "회사명";
            CompanyResponse companyResponse = new CompanyResponse(
                affiliationId, UUID.randomUUID(), "type", companyName, "주소"
            );

            given(userService.findNotDeletedUser(userId)).willReturn(testUser);
            given(hubClient.getCompanyById(affiliationId)).willReturn(companyResponse);

            // when
            UserDetailResponse response = userFacade.getUserDetails(userId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.userId()).isEqualTo(userId);
            assertThat(response.affiliationName()).isEqualTo(companyName);
            assertThat(response.gradeName()).isNull();
            then(hubClient).should(times(1)).getCompanyById(affiliationId);
            then(userGradeService).should(never()).getCurrentGradeName(anyLong());
        }
    }

    @Nested
    @DisplayName("getUserList 테스트")
    class GetUserListTest {

        @Test
        @DisplayName("유저 목록 조회 - Company 및 등급 정보 포함")
        void getUserList_IncludesCompanyAndGradeInfo() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            List<User> userList = List.of(testUser, customerUser);
            Page<User> userPage = new PageImpl<>(userList, pageable, 2);

            String companyName = "회사명";
            String gradeName = "브론즈";
            CompanyResponse companyResponse = new CompanyResponse(
                affiliationId, UUID.randomUUID(), "type", companyName, "주소"
            );

            given(userService.getUserList(null, pageable)).willReturn(userPage);
            given(hubClient.getCompanyById(affiliationId)).willReturn(companyResponse);
            given(userGradeService.getCurrentGradeName(customerId)).willReturn(gradeName);

            // when
            PageResponse<UserInfoResponse> response = userFacade.getUserList(null, pageable);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getData()).hasSize(2);
            assertThat(response.getData().get(0).affiliationName()).isEqualTo(companyName);
            assertThat(response.getData().get(1).gradeName()).isEqualTo(gradeName);
            then(hubClient).should(times(1)).getCompanyById(affiliationId);
            then(userGradeService).should(times(1)).getCurrentGradeName(customerId);
        }
    }

    @Nested
    @DisplayName("updateUserInfo 테스트")
    class UpdateUserInfoTest {

        @Test
        @DisplayName("유저 정보 수정")
        void updateUserInfo_UpdatesSuccessfully() {
            // given
            UserUpdateRequest request = new UserUpdateRequest(
                "newname",
                "newPassword1!",
                "newSlackId",
                "010-1111-1111",
                "new@example.com",
                UserRole.DRIVER
            );

            given(userService.findNotDeletedUser(userId)).willReturn(testUser);

            // when
            userFacade.updateUserInfo(userId, request);

            // then
            then(userService).should(times(1)).findNotDeletedUser(userId);
            then(userService).should(times(1)).updateUserInfo(testUser, request);
        }
    }

    @Nested
    @DisplayName("getCurrentUserGrade 테스트")
    class GetCurrentUserGradeTest {

        @Test
        @DisplayName("현재 유저 등급 조회")
        void getCurrentUserGrade_ReturnsUserGrade() {
            // given
            String gradeName = "브론즈";
            UserGrade userGrade = UserGrade.create(customerUser, gradeName);
            ReflectionTestUtils.setField(userGrade, "userGradeId", UUID.randomUUID());
            ReflectionTestUtils.setField(userGrade, "evaluatedAt", LocalDateTime.now());

            given(userService.findNotDeletedUser(customerId)).willReturn(customerUser);
            given(userGradeService.getCurrentUserGrade(customerId)).willReturn(userGrade);

            // when
            UserGradeResponse response = userFacade.getCurrentUserGrade(customerId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.userId()).isEqualTo(customerId);
            assertThat(response.gradeName()).isEqualTo(gradeName);
            then(userService).should(times(1)).findNotDeletedUser(customerId);
            then(userGradeService).should(times(1)).getCurrentUserGrade(customerId);
        }
    }

    @Nested
    @DisplayName("updateUserGrade 테스트")
    class UpdateUserGradeTest {

        @Test
        @DisplayName("유저 등급 변경")
        void updateUserGrade_UpdatesSuccessfully() {
            // given
            String newGradeName = "골드";
            UserGrade newGrade = UserGrade.create(customerUser, newGradeName);
            ReflectionTestUtils.setField(newGrade, "userGradeId", UUID.randomUUID());
            ReflectionTestUtils.setField(newGrade, "evaluatedAt", LocalDateTime.now());

            given(userService.findNotDeletedUser(customerId)).willReturn(customerUser);
            given(userGradeService.updateUserGrade(customerId, newGradeName)).willReturn(newGrade);

            // when
            UserGradeResponse response = userFacade.updateUserGrade(customerId, newGradeName);

            // then
            assertThat(response).isNotNull();
            assertThat(response.gradeName()).isEqualTo(newGradeName);
            then(userService).should(times(1)).findNotDeletedUser(customerId);
            then(userGradeService).should(times(1)).updateUserGrade(customerId, newGradeName);
        }
    }
}
