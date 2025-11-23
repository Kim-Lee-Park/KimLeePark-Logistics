package com.klp.delivery.routeplan.exception;

import com.klp.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum RoutePlanItemErrorCode implements ErrorCode {
    NO_ROUTE_PLAN_ITEM_FOUND(HttpStatus.NOT_FOUND, "경로 계획 구간 정보를 찾을 수 없습니다."),
    ;

    private final HttpStatus httpStatus;
    @Getter
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return this.httpStatus;
    }

    public String getErrorCode() {
        return this.name();
    }
}
