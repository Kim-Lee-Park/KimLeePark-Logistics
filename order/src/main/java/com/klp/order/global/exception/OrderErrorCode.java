package com.klp.order.global.exception;

import com.klp.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {

    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    SUPPLIER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "공급 업체 ID는 필수입니다."),
    CUSTOMER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "수령 업체 ID는 필수입니다."),
    ORDER_ITEMS_REQUIRED(HttpStatus.BAD_REQUEST, "주문 상품은 필수입니다."),
    ORDER_ITEMS_MIN_REQUIRED(HttpStatus.BAD_REQUEST, "주문 상품은 최소 1개 이상이어야 합니다."),
    ORDER_STATUS_REQUIRED(HttpStatus.BAD_REQUEST, "변경할 주문 상태가 존재해야 합니다."),
    DELETED_BY_REQUIRED(HttpStatus.BAD_REQUEST, "삭제자는 필수 정보 입니다."),

    CANNOT_UPDATE_DELIVERY_ASSIGNED(HttpStatus.BAD_REQUEST, "배송이 할당된 주문은 수정할 수 없습니다."),
    CANNOT_UPDATE_CANCELLED_ORDER(HttpStatus.BAD_REQUEST, "취소된 주문은 수정할 수 없습니다."),
    CANNOT_UPDATE_COMPLETED_ORDER(HttpStatus.BAD_REQUEST, "완료된 주문은 수정할 수 없습니다."),
    CANNOT_CHANGE_CANCELLED_ORDER_STATUS(HttpStatus.BAD_REQUEST, "취소된 주문은 상태를 변경할 수 없습니다."),
    CANNOT_CANCEL_DELIVERY_ASSIGNED(HttpStatus.BAD_REQUEST, "배송이 할당된 주문은 취소할 수 없습니다."),
    CANNOT_CANCEL_COMPLETED_ORDER(HttpStatus.BAD_REQUEST, "완료된 주문은 취소할 수 없습니다."),
    ALREADY_CANCELLED_ORDER(HttpStatus.BAD_REQUEST, "이미 취소된 주문입니다.");

    private final HttpStatus status;
    private final String message;
}
