package com.klp.user.domain.exception;

import com.klp.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류입니다"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다"),
    CONFLICT(HttpStatus.CONFLICT, "요청이 현재 서버 상태와 충돌합니다"),

    INVALID_USER_ROLE(HttpStatus.BAD_REQUEST, "지원하지 않는 회원 권한입니다"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다"),
    ALREADY_APPROVED(HttpStatus.CONFLICT, "이미 승인된 상태입니다"),
    ALREADY_REJECTED(HttpStatus.CONFLICT, "이미 승인 거절된 상태입니다"),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 유저가 존재하지 않습니다"),

        INVALID_AFFILIATION(HttpStatus.BAD_REQUEST, "소속 정보가 없습니다."),

        USER_GRADE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 회원의 등급 정보를 찾을 수 없습니다"),

        ;

    private final HttpStatus status;
    private final String message;
}
