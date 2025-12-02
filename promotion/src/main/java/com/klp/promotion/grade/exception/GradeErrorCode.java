package com.klp.promotion.grade.exception;

import com.klp.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GradeErrorCode implements ErrorCode {
    GRADE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 등급을 찾을 수 없습니다"),
    GRADE_NAME_DUPLICATE(HttpStatus.BAD_REQUEST, "이미 존재하는 등급 이름입니다"),
    INVALID_DISCOUNT_RATE(HttpStatus.BAD_REQUEST, "할인율은 0에서 100 사이여야 합니다"),
    INVALID_AMOUNT_RANGE(HttpStatus.BAD_REQUEST, "최소 금액은 최대 금액보다 작아야 합니다");

    private final HttpStatus status;
    private final String message;
}
