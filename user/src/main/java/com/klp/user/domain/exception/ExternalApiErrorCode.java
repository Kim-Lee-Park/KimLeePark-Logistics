package com.klp.user.domain.exception;

import com.klp.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExternalApiErrorCode implements ErrorCode {

    COMPANY_SERVICE_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "업체 서비스 호출 중 오류가 발생했습니다."),
    COMPANY_SERVICE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "업체 서비스 호출 시 잘못된 요청입니다."),
    COMPANY_SERVICE_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "업체 서비스 인증에 실패했습니다."),
    COMPANY_SERVICE_FORBIDDEN(HttpStatus.FORBIDDEN, "업체 서비스 접근 권한이 없습니다."),
    COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "업체 정보를 찾을 수 없습니다."),

    PROMOTION_SERVICE_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "프로모션 서비스 호출 중 오류가 발생했습니다."),
    PROMOTION_SERVICE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "프로모션 서비스 호출 시 잘못된 요청입니다."),
    PROMOTION_SERVICE_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "프로모션 서비스 인증에 실패했습니다."),
    PROMOTION_SERVICE_FORBIDDEN(HttpStatus.FORBIDDEN, "프로모션 서비스 접근 권한이 없습니다."),
    USER_GRADE_NOT_FOUND(HttpStatus.NOT_FOUND, "회원 등급 정보를 찾을 수 없습니다."),

    HUB_SERVICE_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "허브 서비스 호출 중 오류가 발생했습니다."),
    HUB_SERVICE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "허브 서비스 호출 시 잘못된 요청입니다."),
    HUB_SERVICE_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "허브 서비스 인증에 실패했습니다."),
    HUB_SERVICE_FORBIDDEN(HttpStatus.FORBIDDEN, "허브 서비스 접근 권한이 없습니다."),
    HUB_NOT_FOUND(HttpStatus.NOT_FOUND, "허브 정보를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    public String getErrorCode() {
        return this.name();
    }
}
