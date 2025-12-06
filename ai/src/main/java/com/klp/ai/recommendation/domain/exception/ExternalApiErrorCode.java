package com.klp.ai.recommendation.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExternalApiErrorCode implements ErrorCode {

    ORDER_SERVICE_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "주문 서비스 호출 중 오류가 발생했습니다."),
    ORDER_SERVICE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "주문 서비스 호출 시 잘못된 요청입니다."),
    ORDER_SERVICE_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "주문 서비스 인증에 실패했습니다."),
    ORDER_SERVICE_FORBIDDEN(HttpStatus.FORBIDDEN, "주문 서비스 접근 권한이 없습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문 정보를 찾을 수 없습니다."),

    HUB_SERVICE_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "허브 서비스 호출 중 오류가 발생했습니다."),
    HUB_SERVICE_BAD_REQUEST(HttpStatus.BAD_REQUEST, "허브 서비스 호출 시 잘못된 요청입니다."),
    HUB_SERVICE_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "허브 서비스 인증에 실패했습니다."),
    HUB_SERVICE_FORBIDDEN(HttpStatus.FORBIDDEN, "허브 서비스 접근 권한이 없습니다."),
    HUB_NOT_FOUND(HttpStatus.NOT_FOUND, "허브 정보를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
