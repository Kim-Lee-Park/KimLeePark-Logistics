package com.klp.authservice.auth;

import com.klp.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {
    USERNAME_IS_EXIST(HttpStatus.CONFLICT, "이미 존재하는 유저이름입니다"),
    INVALID_AFFILIATION_TYPE(HttpStatus.BAD_REQUEST, "올바르지 않은 소속 타입입니다"),

    USER_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "유저 서비스 요청 오류가 발생했습니다"),
    USER_SERVICE_INTERNAL_ERROR(HttpStatus.BAD_GATEWAY, "유저 서비스에 일시적인 오류가 발생했습니다"),

    // 블랙리스트 도메인 에러 코드
    BLACKLIST_CREATE_ERROR(HttpStatus.BAD_REQUEST, "블랙리스트 토큰 생성에 실패했습니다");

    private final HttpStatus status;
    private final String message;
}
