package com.klp.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.klp.common.exception.BusinessException;
import com.klp.user.domain.entity.User;
import com.klp.user.domain.enums.AffiliationType;
import com.klp.user.domain.enums.UserRole;
import com.klp.user.domain.enums.UserStatus;
import com.klp.user.domain.exception.UserErrorCode;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class UserTest {

    private final UUID affiliationId = UUID.randomUUID();
    private final String name = "testuser";
    private final String password = "Password1!";
    private final String slackId = "slackId";
    private final String phone = "010-1234-5678";

    @Test
    @DisplayName("유저 생성 성공")
    void createUser_success() {
        // given

        // when
        User user = User.create(
            affiliationId, AffiliationType.COMPANY, name, password, slackId, phone, UserRole.COMPANY
        );

        // then
        assertThat(user.getAffiliationId()).isEqualTo(affiliationId);
        assertThat(user.getAffiliationType()).isEqualTo(AffiliationType.COMPANY);
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getSlackId()).isEqualTo(slackId);
        assertThat(user.getPhone()).isEqualTo(phone);
        assertThat(user.getRole()).isEqualTo(UserRole.COMPANY);
        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING);
    }

    @ParameterizedTest
    @DisplayName("필수 값이 null인 경우 실패")
    @MethodSource("userCreateNullFields")
    void nullRequiredField_fail(
        UUID affiliationId,
        AffiliationType affiliationType,
        String name,
        String password,
        String slackId,
        String phone,
        UserRole role,
        String expectedMessage
    ) {
        // given

        // when & then
        assertThatThrownBy(() -> User.create(
            affiliationId,
            affiliationType,
            name,
            password,
            slackId,
            phone,
            role
        )).isInstanceOf(BusinessException.class)
            .hasMessageContaining(expectedMessage);
    }

    @Test
    @DisplayName("이름이 빈 문자열인 경우 실패")
    void emptyName_fail() {
        // given

        // when & then
        assertThatThrownBy(() -> User.create(
            UUID.randomUUID(),
            AffiliationType.HUB,
            "",
            password,
            slackId,
            phone,
            UserRole.HUB_DRIVER
        )).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("이름이 빈 문자열인 경우 실패")
    void whiteSpaceName_fail() {
        // given

        // when & then
        assertThatThrownBy(() -> User.create(
            UUID.randomUUID(),
            AffiliationType.HUB,
            "       ",
            password,
            slackId,
            phone,
            UserRole.HUB_DRIVER
        )).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("유저 승인 상태 변경")
    void approveUser() {
        // given
        User user = User.create(
            UUID.randomUUID(),
            AffiliationType.HUB,
            name,
            password,
            slackId,
            phone,
            UserRole.HUB_DRIVER
        );

        // when
        user.approve();

        // then
        assertThat(user.getStatus()).isEqualTo(UserStatus.APPROVED);
    }

    @Test
    @DisplayName("유저 거절 상태 변경")
    void rejectUser() {
        // given
        User user = User.create(
            UUID.randomUUID(),
            AffiliationType.HUB,
            name,
            password,
            slackId,
            phone,
            UserRole.HUB_DRIVER
        );

        // when
        user.reject();

        // then
        assertThat(user.getStatus()).isEqualTo(UserStatus.REJECTED);
    }

    @Test
    @DisplayName("이미 승인된 유저는 다시 승인할 수 없음")
    void alreadyApprove_approveFail() {
        // given
        User user = User.create(
            UUID.randomUUID(),
            AffiliationType.HUB,
            name,
            password,
            slackId,
            phone,
            UserRole.HUB_DRIVER
        );
        user.approve();

        // when & then
        assertThatThrownBy(() -> user.approve())
            .isInstanceOf(BusinessException.class)
            .hasMessage(UserErrorCode.ALREADY_APPROVED.getMessage());
    }

    private static Stream<Arguments> userCreateNullFields() {
        UUID affiliationId = UUID.randomUUID();
        String name = "testuser";
        String password = "Password1!";
        String slackId = "slackId";
        String phone = "010-1234-5678";

        return Stream.of(
            Arguments.of(null, AffiliationType.HUB, name, password, slackId, phone, UserRole.HUB_DRIVER, "소속 ID"),
            Arguments.of(affiliationId, null, name, password, slackId, phone, UserRole.HUB_DRIVER, "소속 타입"),
            Arguments.of(affiliationId, AffiliationType.HUB, null, password, slackId, phone, UserRole.HUB_DRIVER, "이름"),
            Arguments.of(affiliationId, AffiliationType.HUB, name, null, slackId, phone, UserRole.HUB_DRIVER, "비밀번호"),
            Arguments.of(affiliationId, AffiliationType.HUB, name, password, null, phone, UserRole.HUB_DRIVER, "슬랙 ID"),
            Arguments.of(
                affiliationId, AffiliationType.HUB, name, password, slackId, null, UserRole.HUB_DRIVER, "전화번호"),
            Arguments.of(affiliationId, AffiliationType.HUB, name, password, slackId, phone, null, "권한")
        );
    }
}
