package com.klp.delivery.routeplan.exception;

import com.klp.delivery.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum RoutePlanErrorCode implements ErrorCode {
    DEPARTURE_ID_REQUIRED(HttpStatus.BAD_REQUEST, "출발 허브 ID는 필수입니다."),
    ARRIVAL_ID_REQUIRED(HttpStatus.BAD_REQUEST, "도착 허브 ID는 필수입니다."),
    DURATION_INVALID(HttpStatus.BAD_REQUEST, "총 소요 시간은 0보다 커야 합니다."),
    DISTANCE_INVALID(HttpStatus.BAD_REQUEST, "총 이동 거리는 0보다 커야 합니다."),
    HUB_NOT_FOUND(HttpStatus.BAD_REQUEST, "허브를 찾을 수 없습니다."),
    HUB_ROUTE_INFO_NOT_FOUND(HttpStatus.BAD_REQUEST, "허브간 이동 정보를 찾을 수 없습니다."),
    ROUTE_INFOS_NEEDED(HttpStatus.BAD_REQUEST, "허브간 이동 정보는 필수입니다."),
    NO_ROUTE_PLAN_FOUND(HttpStatus.BAD_REQUEST, "경로 계획을 찾을 수 없습니다."),
    ALREADY_EXISTS_ROUTE_PLAN(HttpStatus.BAD_REQUEST, "경로 계획이 이미 존재합니다.");

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
