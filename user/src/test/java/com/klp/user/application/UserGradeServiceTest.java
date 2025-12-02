package com.klp.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.klp.common.exception.BusinessException;
import com.klp.user.domain.entity.User;
import com.klp.user.domain.entity.UserGrade;
import com.klp.user.domain.enums.AffiliationType;
import com.klp.user.domain.enums.UserRole;
import com.klp.user.domain.exception.UserErrorCode;
import com.klp.user.domain.repository.UserGradeRepository;
import com.klp.user.domain.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserGradeService 단위 테스트")
class UserGradeServiceTest {

    @Mock
    private UserGradeRepository userGradeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserGradeService userGradeService;

    private User testUser;
    private final Long userId = 1L;
    private final String gradeName = "BRONZE";

    @BeforeEach
    void setUp() {
        testUser = User.create(
            UUID.randomUUID(),
            AffiliationType.CUSTOMER,
            "testuser",
            "Password1!",
            "U12345678",
            "010-1234-5678",
            "test@example.com",
            UserRole.CUSTOMER
        );
        ReflectionTestUtils.setField(testUser, "userId", userId);
    }

    @Nested
    @DisplayName("getCurrentGradeName 테스트")
    class GetCurrentGradeNameTest {

        @Test
        @DisplayName("등급이 존재하는 경우 등급명 반환")
        void getCurrentGradeName_WhenGradeExists_ReturnsGradeName() {
            // given
            UserGrade userGrade = UserGrade.create(testUser, gradeName);
            given(userGradeRepository.findFirstByUser_UserIdOrderByEvaluatedAtDesc(userId))
                .willReturn(Optional.of(userGrade));

            // when
            String result = userGradeService.getCurrentGradeName(userId);

            // then
            assertThat(result).isEqualTo(gradeName);
            then(userGradeRepository).should(times(1))
                .findFirstByUser_UserIdOrderByEvaluatedAtDesc(userId);
        }

        @Test
        @DisplayName("등급이 존재하지 않는 경우 null 반환")
        void getCurrentGradeName_WhenGradeNotExists_ReturnsNull() {
            // given
            given(userGradeRepository.findFirstByUser_UserIdOrderByEvaluatedAtDesc(userId))
                .willReturn(Optional.empty());

            // when
            String result = userGradeService.getCurrentGradeName(userId);

            // then
            assertThat(result).isNull();
            then(userGradeRepository).should(times(1))
                .findFirstByUser_UserIdOrderByEvaluatedAtDesc(userId);
        }

        @Test
        @DisplayName("여러 등급이 존재하는 경우 가장 최근 등급명 반환")
        void getCurrentGradeName_WhenMultipleGradesExist_ReturnsLatestGradeName() {
            // given
            String latestGradeName = "GOLD";
            UserGrade latestGrade = UserGrade.create(testUser, latestGradeName);
            ReflectionTestUtils.setField(latestGrade, "evaluatedAt", LocalDateTime.now());

            given(userGradeRepository.findFirstByUser_UserIdOrderByEvaluatedAtDesc(userId))
                .willReturn(Optional.of(latestGrade));

            // when
            String result = userGradeService.getCurrentGradeName(userId);

            // then
            assertThat(result).isEqualTo(latestGradeName);
            then(userGradeRepository).should(times(1))
                .findFirstByUser_UserIdOrderByEvaluatedAtDesc(userId);
        }
    }

    @Nested
    @DisplayName("getCurrentUserGrade 테스트")
    class GetCurrentUserGradeTest {

        @Test
        @DisplayName("등급이 존재하는 경우 등급 엔티티 반환")
        void getCurrentUserGrade_WhenGradeExists_ReturnsUserGrade() {
            // given
            UserGrade userGrade = UserGrade.create(testUser, gradeName);
            UUID gradeId = UUID.randomUUID();
            ReflectionTestUtils.setField(userGrade, "userGradeId", gradeId);

            given(userGradeRepository.findFirstByUser_UserIdOrderByEvaluatedAtDesc(userId))
                .willReturn(Optional.of(userGrade));

            // when
            UserGrade result = userGradeService.getCurrentUserGrade(userId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUserGradeId()).isEqualTo(gradeId);
            assertThat(result.getGradeName()).isEqualTo(gradeName);
            then(userGradeRepository).should(times(1))
                .findFirstByUser_UserIdOrderByEvaluatedAtDesc(userId);
        }

        @Test
        @DisplayName("등급이 존재하지 않는 경우 예외 발생")
        void getCurrentUserGrade_WhenGradeNotExists_ThrowsException() {
            // given
            given(userGradeRepository.findFirstByUser_UserIdOrderByEvaluatedAtDesc(userId))
                .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userGradeService.getCurrentUserGrade(userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_GRADE_NOT_FOUND);

            then(userGradeRepository).should(times(1))
                .findFirstByUser_UserIdOrderByEvaluatedAtDesc(userId);
        }

        @Test
        @DisplayName("여러 등급이 존재하는 경우 가장 최근 등급 반환")
        void getCurrentUserGrade_WhenMultipleGradesExist_ReturnsLatestGrade() {
            // given
            String latestGradeName = "GOLD";
            UserGrade latestGrade = UserGrade.create(testUser, latestGradeName);
            UUID latestGradeId = UUID.randomUUID();
            ReflectionTestUtils.setField(latestGrade, "userGradeId", latestGradeId);
            ReflectionTestUtils.setField(latestGrade, "evaluatedAt", LocalDateTime.now());

            given(userGradeRepository.findFirstByUser_UserIdOrderByEvaluatedAtDesc(userId))
                .willReturn(Optional.of(latestGrade));

            // when
            UserGrade result = userGradeService.getCurrentUserGrade(userId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUserGradeId()).isEqualTo(latestGradeId);
            assertThat(result.getGradeName()).isEqualTo(latestGradeName);
            then(userGradeRepository).should(times(1))
                .findFirstByUser_UserIdOrderByEvaluatedAtDesc(userId);
        }
    }

    @Nested
    @DisplayName("createUserGrade 테스트")
    class CreateUserGradeTest {

        @Test
        @DisplayName("유저 등급을 성공적으로 생성")
        void createUserGrade_CreatesSuccessfully() {
            // given
            UserGrade userGrade = UserGrade.create(testUser, gradeName);
            UUID gradeId = UUID.randomUUID();
            ReflectionTestUtils.setField(userGrade, "userGradeId", gradeId);
            ReflectionTestUtils.setField(userGrade, "evaluatedAt", LocalDateTime.now());

            given(userGradeRepository.save(any(UserGrade.class))).willReturn(userGrade);

            // when
            UserGrade result = userGradeService.createUserGrade(testUser, gradeName);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUserGradeId()).isEqualTo(gradeId);
            assertThat(result.getGradeName()).isEqualTo(gradeName);
            then(userGradeRepository).should(times(1)).save(any(UserGrade.class));
        }

        @Test
        @DisplayName("기본 등급으로 유저 등급 생성")
        void createUserGrade_WithDefaultGrade_CreatesSuccessfully() {
            // given
            String defaultGradeName = "NONE";
            UserGrade userGrade = UserGrade.create(testUser, defaultGradeName);
            UUID gradeId = UUID.randomUUID();
            ReflectionTestUtils.setField(userGrade, "userGradeId", gradeId);

            given(userGradeRepository.save(any(UserGrade.class))).willReturn(userGrade);

            // when
            UserGrade result = userGradeService.createUserGrade(testUser, defaultGradeName);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getGradeName()).isEqualTo(defaultGradeName);
            then(userGradeRepository).should(times(1)).save(any(UserGrade.class));
        }
    }

    @Nested
    @DisplayName("updateUserGrade 테스트")
    class UpdateUserGradeTest {

        @Test
        @DisplayName("유저 등급 변경 - 새로운 이력 생성")
        void updateUserGrade_CreatesNewGradeHistory() {
            // given
            String newGradeName = "GOLD";
            UserGrade newGrade = UserGrade.create(testUser, newGradeName);
            UUID newGradeId = UUID.randomUUID();
            ReflectionTestUtils.setField(newGrade, "userGradeId", newGradeId);
            ReflectionTestUtils.setField(newGrade, "evaluatedAt", LocalDateTime.now());

            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(userGradeRepository.save(any(UserGrade.class))).willReturn(newGrade);

            // when
            UserGrade result = userGradeService.updateUserGrade(userId, newGradeName);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUserGradeId()).isEqualTo(newGradeId);
            assertThat(result.getGradeName()).isEqualTo(newGradeName);
            then(userRepository).should(times(1)).findById(userId);
            then(userGradeRepository).should(times(1)).save(any(UserGrade.class));
        }

        @Test
        @DisplayName("유저가 존재하지 않는 경우 예외 발생")
        void updateUserGrade_WhenUserNotFound_ThrowsException() {
            // given
            String newGradeName = "GOLD";
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userGradeService.updateUserGrade(userId, newGradeName))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);

            then(userRepository).should(times(1)).findById(userId);
            then(userGradeRepository).should(times(0)).save(any(UserGrade.class));
        }

        @Test
        @DisplayName("삭제된 유저인 경우 예외 발생")
        void updateUserGrade_WhenUserDeleted_ThrowsException() {
            // given
            String newGradeName = "GOLD";
            testUser.delete(1L);

            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));

            // when & then
            assertThatThrownBy(() -> userGradeService.updateUserGrade(userId, newGradeName))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);

            then(userRepository).should(times(1)).findById(userId);
            then(userGradeRepository).should(times(0)).save(any(UserGrade.class));
        }

        @Test
        @DisplayName("등급을 하향 변경하는 경우 새로운 이력 생성")
        void updateUserGrade_WhenDowngrade_CreatesNewHistory() {
            // given
            String downgradeName = "SILVER";
            UserGrade downgradeGrade = UserGrade.create(testUser, downgradeName);
            UUID downgradeGradeId = UUID.randomUUID();
            ReflectionTestUtils.setField(downgradeGrade, "userGradeId", downgradeGradeId);

            given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
            given(userGradeRepository.save(any(UserGrade.class))).willReturn(downgradeGrade);

            // when
            UserGrade result = userGradeService.updateUserGrade(userId, downgradeName);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getGradeName()).isEqualTo(downgradeName);
            then(userRepository).should(times(1)).findById(userId);
            then(userGradeRepository).should(times(1)).save(any(UserGrade.class));
        }
    }
}
