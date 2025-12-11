package com.klp.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.klp.user.domain.entity.User;
import com.klp.user.domain.enums.AffiliationType;
import com.klp.user.domain.enums.UserRole;
import com.klp.user.domain.repository.UserRepository;
import com.klp.user.presentation.dto.request.UserUpdateRequest;
import com.klp.user.presentation.dto.response.UsernameCheckResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private final Long userId = 1L;
    private final UUID affiliationId = UUID.randomUUID();
    private final String username = "testuser";
    private final String slackId = "U12345678";
    private final String phone = "010-1234-5678";

    @BeforeEach
    void setUp() {
        final String password = "Password1!";

        testUser = User.create(
            affiliationId,
            AffiliationType.HUB,
            username,
            password,
            slackId,
            phone,
            "test@example.com",
            UserRole.HUB
        );
        ReflectionTestUtils.setField(testUser, "userId", userId);
    }

    @Nested
    @DisplayName("checkUserNameAvailable 테스트")
    class CheckUserNameAvailableTest {

        @Test
        @DisplayName("사용 가능한 username일 경우 true 반환")
        void checkUserNameAvailable_WhenUsernameNotExists_ReturnsTrue() {
            // given
            given(userRepository.existsByUsername(username)).willReturn(false);

            // when
            UsernameCheckResponse response = userService.checkUserNameAvailable(username);

            // then
            assertThat(response.available()).isTrue();
            then(userRepository).should(times(1)).existsByUsername(username);
        }

        @Test
        @DisplayName("이미 존재하는 username일 경우 false 반환")
        void checkUserNameAvailable_WhenUsernameExists_ReturnsFalse() {
            // given
            given(userRepository.existsByUsername(username)).willReturn(true);

            // when
            UsernameCheckResponse response = userService.checkUserNameAvailable(username);

            // then
            assertThat(response.available()).isFalse();
            then(userRepository).should(times(1)).existsByUsername(username);
        }
    }

    @Nested
    @DisplayName("findNotDeletedUser 테스트")
    class FindNotDeletedUserTest {

        @Test
        @DisplayName("삭제되지 않은 유저를 성공적으로 조회")
        void findNotDeletedUser_WhenUserExists_ReturnsUser() {
            // given
            given(userRepository.findById(userId)).willReturn(Optional.ofNullable(testUser));

            // when
            User result = userService.findNotDeletedUser(userId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getName()).isEqualTo(username);
            then(userRepository).should(times(1)).findById(userId);
        }
    }

    @Nested
    @DisplayName("getUserList 테스트")
    class GetUserListTest {

        private Pageable pageable;
        private List<User> userList;

        @BeforeEach
        void setUp() {
            pageable = PageRequest.of(0, 10);

            User user1 = User.create(
                UUID.randomUUID(),
                AffiliationType.HUB,
                "user1",
                "Password1!",
                "slackId1",
                "010-1111-1111",
                "user1@example.com",
                UserRole.HUB
            );
            ReflectionTestUtils.setField(user1, "userId", 1L);

            User user2 = User.create(
                UUID.randomUUID(),
                AffiliationType.COMPANY,
                "user2",
                "Password2!",
                "slackId2",
                "010-2222-2222",
                "user2@example.com",
                UserRole.COMPANY
            );
            ReflectionTestUtils.setField(user2, "userId", 2L);

            userList = List.of(user1, user2);
        }

        @Test
        @DisplayName("keyword 없이 전체 유저 목록 조회")
        void getUserList_WithoutKeyword_ReturnsAllUsers() {
            // given
            Page<User> userPage = new PageImpl<>(userList, pageable, userList.size());
            given(userRepository.findAll(pageable)).willReturn(userPage);

            // when
            Page<User> result = userService.getUserList(null, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            then(userRepository).should(times(1)).findAll(pageable);
            then(userRepository).should(never()).searchByKeyword(anyString(), any(Pageable.class));
        }

        @Test
        @DisplayName("빈 keyword로 전체 유저 목록 조회")
        void getUserList_WithEmptyKeyword_ReturnsAllUsers() {
            // given
            Page<User> userPage = new PageImpl<>(userList, pageable, userList.size());
            given(userRepository.findAll(pageable)).willReturn(userPage);

            // when
            Page<User> result = userService.getUserList("", pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            then(userRepository).should(times(1)).findAll(pageable);
            then(userRepository).should(never()).searchByKeyword(anyString(), any(Pageable.class));
        }

        @Test
        @DisplayName("keyword로 유저 검색")
        void getUserList_WithKeyword_ReturnsSearchedUsers() {
            // given
            String keyword = "user1";
            Page<User> userPage = new PageImpl<>(List.of(userList.get(0)), pageable, 1);
            given(userRepository.searchByKeyword(keyword, pageable)).willReturn(userPage);

            // when
            Page<User> result = userService.getUserList(keyword, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            then(userRepository).should(times(1)).searchByKeyword(keyword, pageable);
            then(userRepository).should(never()).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("검색 결과가 없을 경우 빈 리스트 반환")
        void getUserList_WithKeywordNoResults_ReturnsEmptyList() {
            // given
            String keyword = "invalidKeyword";
            Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            given(userRepository.searchByKeyword(keyword, pageable)).willReturn(emptyPage);

            // when
            Page<User> result = userService.getUserList(keyword, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            then(userRepository).should(times(1)).searchByKeyword(keyword, pageable);
        }
    }

    @Disabled
    @Nested
    @DisplayName("updateUserInfo 테스트")
    class UpdateUserInfoTest {

        @Test
        @DisplayName("유저 정보를 성공적으로 수정")
        void updateUserInfo_WhenUserExists_UpdatesSuccessfully() {
            // given
            String newUsername = "newusername";
            String newPassword = "newPassword1!";
            String encodedPassword = "encodedPassword";
            String newSlackId = "newSlackId";
            String newPhone = "010-1111-1111";
            String newEmail = "new@example.com";
            UserRole newRole = UserRole.DRIVER;

            UserUpdateRequest request = new UserUpdateRequest(
                newUsername,
                newPassword,
                newSlackId,
                newPhone,
                newEmail,
                newRole
            );

            given(passwordEncoder.encode(newPassword)).willReturn(encodedPassword);

            // when
            userService.updateUserInfo(testUser, request);

            // then
            then(passwordEncoder).should(times(1)).encode(newPassword);
            assertThat(testUser.getName()).isEqualTo(newUsername);
            assertThat(testUser.getSlackId()).isEqualTo(newSlackId);
        }
    }

    @Nested
    @DisplayName("findDriversByHubId 테스트")
    class FindDriversByHubIdTest {

        @Test
        @DisplayName("허브 ID로 배송 담당자 조회 성공")
        void findDriversByHubId_ReturnsDriverList() {
            // given
            UUID hubId = UUID.randomUUID();
            User driver1 = User.create(
                hubId, AffiliationType.HUB, "driver1", "password", "slack1", "010-1111-1111",
                "driver1@example.com",
                UserRole.DRIVER
            );
            ReflectionTestUtils.setField(driver1, "userId", 1L);

            User driver2 = User.create(
                hubId, AffiliationType.HUB, "driver2", "password", "slack2", "010-2222-2222",
                "driver2@example.com",
                UserRole.DRIVER
            );
            ReflectionTestUtils.setField(driver2, "userId", 2L);

            List<User> drivers = List.of(driver1, driver2);

            given(userRepository.findDriversByHubId(hubId)).willReturn(drivers);

            // when
            List<User> result = userService.findDriversByHubId(hubId);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getUserId()).isEqualTo(1L);
            assertThat(result.get(0).getName()).isEqualTo("driver1");
            then(userRepository).should(times(1)).findDriversByHubId(hubId);
        }
    }

    @Nested
    @DisplayName("findDriversByLogistics 테스트")
    class FindDriversByLogisticsTest {

        @Test
        @DisplayName("물류회사 소속 배송 담당자 조회 성공")
        void findDriversByLogistics_ReturnsDriverList() {
            // given
            UUID logisticsId = UUID.randomUUID();
            User driver1 = User.create(
                logisticsId, AffiliationType.LOGISTICS, "driver1", "password", "slack1",
                "010-1111-1111",
                "driver1@example.com", UserRole.DRIVER
            );
            ReflectionTestUtils.setField(driver1, "userId", 1L);

            User driver2 = User.create(
                logisticsId, AffiliationType.LOGISTICS, "driver2", "password", "slack2",
                "010-2222-2222",
                "driver2@example.com", UserRole.DRIVER
            );
            ReflectionTestUtils.setField(driver2, "userId", 2L);

            List<User> drivers = List.of(driver1, driver2);

            given(userRepository.findDriversByLogistics()).willReturn(drivers);

            // when
            List<User> result = userService.findDriversByLogistics();

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getUserId()).isEqualTo(1L);
            assertThat(result.get(0).getName()).isEqualTo("driver1");
            then(userRepository).should(times(1)).findDriversByLogistics();
        }
    }

    @Nested
    @DisplayName("findDriverById 테스트")
    class FindDriverByIdTest {

        @Test
        @DisplayName("배송 담당자 ID로 상세 정보 조회 성공")
        void findDriverById_ReturnsDriver() {
            // given
            Long driverId = 1L;
            UUID hubId = UUID.randomUUID();
            User driver = User.create(
                hubId, AffiliationType.HUB, "driver1", "password", "slack1", "010-1111-1111",
                "driver1@example.com",
                UserRole.DRIVER
            );
            ReflectionTestUtils.setField(driver, "userId", driverId);

            given(userRepository.findDriverById(driverId)).willReturn(Optional.of(driver));

            // when
            User result = userService.findDriverById(driverId);

            // then
            assertThat(result.getUserId()).isEqualTo(driverId);
            assertThat(result.getAffiliationId()).isEqualTo(hubId);
            assertThat(result.getName()).isEqualTo("driver1");
            assertThat(result.getSlackId()).isEqualTo("slack1");
            assertThat(result.getPhone()).isEqualTo("010-1111-1111");
            then(userRepository).should(times(1)).findDriverById(driverId);
        }
    }

    @Nested
    @DisplayName("createPendingUser 테스트")
    class CreatePendingUserTest {

        @Test
        @DisplayName("회원가입 시 PENDING 상태로 생성")
        void createPendingUser_CreatesPendingUser() {
            // given
            UUID affiliationId = UUID.randomUUID();
            String password = "Password1!";
            String encodedPassword = "encodedPassword";

            var request = new com.klp.user.presentation.dto.request.UserCreateRequest(
                username,
                password,
                slackId,
                phone,
                "test@example.com",
                UserRole.HUB,
                "회사명",
                AffiliationType.HUB
            );

            given(passwordEncoder.encode(password)).willReturn(encodedPassword);
            given(userRepository.save(any(User.class))).willAnswer(invocation -> {
                User user = invocation.getArgument(0);
                ReflectionTestUtils.setField(user, "userId", userId);
                return user;
            });

            // when
            User result = userService.createPendingUser(request, affiliationId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getName()).isEqualTo(username);
            then(passwordEncoder).should(times(1)).encode(password);
            then(userRepository).should(times(1)).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("approvePendingUser 테스트")
    class ApprovePendingUserTest {

        @Test
        @DisplayName("유저 승인 처리")
        void approvePendingUser_ApprovesUser() {
            // given

            // when
            userService.approvePendingUser(testUser);

            // then
            assertThat(testUser.getStatus()).isEqualTo(
                com.klp.user.domain.enums.UserStatus.APPROVED);
        }
    }

    @Nested
    @DisplayName("rejectPendingUser 테스트")
    class RejectPendingUserTest {

        @Test
        @DisplayName("유저 거부 처리")
        void rejectPendingUser_RejectsUser() {
            // given

            // when
            userService.rejectPendingUser(testUser);

            // then
            assertThat(testUser.getStatus()).isEqualTo(
                com.klp.user.domain.enums.UserStatus.REJECTED);
        }
    }
}
