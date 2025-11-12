package com.klp.user.domain.entity;

import com.klp.common.exception.BusinessException;
import com.klp.common.model.BaseEntity;
import com.klp.user.domain.enums.AffiliationType;
import com.klp.user.domain.enums.UserRole;
import com.klp.user.domain.enums.UserStatus;
import com.klp.user.domain.exception.UserErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_users", schema = "user_schema")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false)
    private UUID affiliationId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AffiliationType affiliationType;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String slackId;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private User(
        UUID affiliationId,
        AffiliationType affiliationType,
        String name,
        String password,
        String slackId,
        String phone,
        UserRole role
    ) {
        this.affiliationId = affiliationId;
        this.affiliationType = affiliationType;
        this.name = name;
        this.password = password;
        this.slackId = slackId;
        this.phone = phone;
        this.role = role;
    }

    public static User create(
        UUID affiliationId,
        AffiliationType affiliationType,
        String name,
        String password,
        String slackId,
        String phone,
        UserRole role
    ) {
        validateNotNull(affiliationId, "소속 ID는 필수입니다.");
        validateNotNull(affiliationType, "소속 타입은 필수입니다.");
        validateNotNull(role, "권한은 필수입니다.");
        validateNotBlank(name, "이름은 필수입니다.");
        validateNotBlank(password, "비밀번호는 필수입니다.");
        validateNotBlank(slackId, "슬랙 ID는 필수입니다.");
        validateNotBlank(phone, "전화번호는 필수입니다.");
        validateRoleAndAffiliationType(role, affiliationType);

        User user = new User();
        user.affiliationId = affiliationId;
        user.affiliationType = affiliationType;
        user.name = name;
        user.password = password;
        user.slackId = slackId;
        user.phone = phone;
        user.role = role;
        user.status = UserStatus.PENDING;
        return user;
    }

    private static void validateNotNull(Object o, String message) {
        if (o == null) {
            throw new BusinessException(UserErrorCode.BAD_REQUEST, message);
        }
    }

    private static void validateNotBlank(String s, String message) {
        if (s == null || s.trim().isEmpty()) {
            throw new BusinessException(UserErrorCode.BAD_REQUEST, message);
        }
    }

    private static void validateRoleAndAffiliationType(
        UserRole role,
        AffiliationType affiliationType
    ) {
        switch (role) {
            case HUB, HUB_DRIVER:
                if (affiliationType != AffiliationType.HUB) {
                    throw new BusinessException(
                        UserErrorCode.BAD_REQUEST, "허브 담당자, 허브 배송 담당자는는 HUB 소속이어야 합니다."
                    );
                }
                break;
            case COMPANY, COMPANY_DRIVER:
                if (affiliationType != AffiliationType.COMPANY) {
                    throw new BusinessException(
                        UserErrorCode.BAD_REQUEST, "업체 담당자, 업체 배송 담당자는 COMPANY 소속이어야 합니다."
                    );
                }
                break;
            case MASTER:
                if (affiliationType != AffiliationType.LOGISTICS) {
                    throw new BusinessException(
                        UserErrorCode.BAD_REQUEST, "마스터는 LOGISTICS 소속이어야 합니다."
                    );
                }
                break;
            default:
                throw new BusinessException(UserErrorCode.INVALID_USER_ROLE);
        }
    }

    public void approve() {
        if (this.status == UserStatus.APPROVED) {
            throw new BusinessException(UserErrorCode.ALREADY_APPROVED);
        }
        if (this.status == UserStatus.REJECTED) {
            throw new BusinessException(UserErrorCode.ALREADY_REJECTED);
        }
        this.status = UserStatus.APPROVED;
    }

    public void reject() {
        if (this.status == UserStatus.REJECTED) {
            throw new BusinessException(UserErrorCode.ALREADY_REJECTED);
        }
        if (this.status == UserStatus.APPROVED) {
            throw new BusinessException(UserErrorCode.ALREADY_APPROVED);
        }
        this.status = UserStatus.REJECTED;
    }

    public void update(String username, String password, String slackId, String phone, UserRole role) {
        this.name = username;
        this.password = password;
        this.slackId = slackId;
        this.phone = phone;
        this.role = role;
    }
}

