package com.klp.authservice.auth.domain.entity;

import com.klp.authservice.auth.AuthErrorCode;
import com.klp.authservice.auth.infrastructure.jpa.model.BaseEntity;
import com.klp.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "p_blacklist_token", schema = "auth_schema")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlackListToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID blackListTokenId;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiration;

    private BlackListToken(String token, LocalDateTime expiration) {
        this.token = token;
        this.expiration = expiration;
    }

    public static BlackListToken create(String token, LocalDateTime expiration) {
        validateBlackListToken(token, expiration);
        
        return new BlackListToken(token, expiration);
    }

    private static void validateBlackListToken(String token, LocalDateTime expiration) {
        if (token == null || token.isBlank()) {
            throw new BusinessException(AuthErrorCode.BLACKLIST_CREATE_ERROR);
        }

        if (expiration == null || expiration.isBefore(LocalDateTime.now())) {
            throw new BusinessException(AuthErrorCode.BLACKLIST_CREATE_ERROR);
        }
    }
}
