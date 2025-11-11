package com.klp.delivery.routeplan.exception;

import com.klp.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum RoutePlanErrorCode implements ErrorCode {
    DEPARTURE_ID_REQUIRED(HttpStatus.BAD_REQUEST, "출발 허브 ID는 필수입니다."),
    ARRIVAL_ID_REQUIRED(HttpStatus.BAD_REQUEST, "도착 허브 ID는 필수입니다."),
    DURATION_INVALID(HttpStatus.BAD_REQUEST, "총 소요 시간은 0보다 커야 합니다."),
    DISTANCE_INVALID(HttpStatus.BAD_REQUEST, "총 이동 거리는 0보다 커야 합니다.")
    ,;

    private final HttpStatus httpStatus;
    @Getter
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return this.httpStatus;
    }

    public String getErrorCode(){
        return this.name();
    }
}
