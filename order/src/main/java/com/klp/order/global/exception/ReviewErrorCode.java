package com.klp.order.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReviewErrorCode implements ErrorCode {
    // 조회 관련
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."),

    // 생성 관련
    ORDER_NOT_COMPLETE(HttpStatus.BAD_REQUEST, "완료된 주문만 리뷰를 작성할 수 있습니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 해당 주문에 대한 리뷰가 존재합니다."),

    // 검증 관련
    ORDER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "주문 ID는 필수입니다."),
    PRODUCT_ID_REQUIRED(HttpStatus.BAD_REQUEST, "상품 ID는 필수입니다."),
    USER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "사용자 ID는 필수입니다."),
    RATING_REQUIRED(HttpStatus.BAD_REQUEST, "평점은 필수입니다."),
    INVALID_RATING_RANGE(HttpStatus.BAD_REQUEST, "평점은 1~5 사이의 값이어야 합니다."),

    // 권한 관련
    REVIEW_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "본인의 리뷰만 수정할 수 있습니다."),
    REVIEW_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "리뷰를 삭제할 권한이 없습니다.");

    private final HttpStatus status;
    private final String message;
}
