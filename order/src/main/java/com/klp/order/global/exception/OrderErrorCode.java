package com.klp.order.global.exception;

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
    HUB_ID_REQUIRED(HttpStatus.BAD_REQUEST, "허브 ID는 필수입니다."),

    CANNOT_UPDATE_DELIVERY_ASSIGNED(HttpStatus.BAD_REQUEST, "배송이 할당된 주문은 수정할 수 없습니다."),
    CANNOT_UPDATE_CANCELLED_ORDER(HttpStatus.BAD_REQUEST, "취소된 주문은 수정할 수 없습니다."),
    CANNOT_UPDATE_COMPLETED_ORDER(HttpStatus.BAD_REQUEST, "완료된 주문은 수정할 수 없습니다."),
    CANNOT_CHANGE_CANCELLED_ORDER_STATUS(HttpStatus.BAD_REQUEST, "취소된 주문은 상태를 변경할 수 없습니다."),
    CANNOT_CANCEL_DELIVERY_ASSIGNED(HttpStatus.BAD_REQUEST, "배송이 할당된 주문은 취소할 수 없습니다."),
    CANNOT_CANCEL_DELIVERY_SHIPPING(HttpStatus.BAD_REQUEST, "배송 중인 주문은 취소할 수 없습니다."),
    CANNOT_UPDATE_DELIVERY_SHIPPING(HttpStatus.BAD_REQUEST, "배송 중인 상품은 수정할 수 없습니다."),
    CANNOT_CANCEL_COMPLETED_ORDER(HttpStatus.BAD_REQUEST, "완료된 주문은 취소할 수 없습니다."),
    ALREADY_CANCELLED_ORDER(HttpStatus.BAD_REQUEST, "이미 취소된 주문입니다."),

    ORDER_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "주문 생성 중 오류가 발생했습니다."),
    INVENTORY_DEDUCTION_FAILED(HttpStatus.BAD_REQUEST, "재고 차감에 실패 하였습니다."),
    USER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "사용자 ID는 필수입니다."),
    DELIVERY_ADDRESS_REQUIRED(HttpStatus.BAD_REQUEST, "배송지 주소는 필수입니다."),
    DELIVERY_LATITUDE_REQUIRED(HttpStatus.BAD_REQUEST, "배송지 위도는 필수입니다."),
    DELIVERY_LONGITUDE_REQUIRED(HttpStatus.BAD_REQUEST, "배송지 경도는 필수입니다."),
    INVALID_LATITUDE_RANGE(HttpStatus.BAD_REQUEST, "위도는 -90 ~ 90 사이의 값이어야 합니다."),
    INVALID_LONGITUDE_RANGE(HttpStatus.BAD_REQUEST, "경도는 -180 ~ 180 사이의 값이어야 합니다."),
    INVALID_ORDER_PRICE(HttpStatus.BAD_REQUEST, "주문 금액이 올바르지 않습니다."),
    INVALID_DISCOUNT_AMOUNT(HttpStatus.BAD_REQUEST, "할인 금액이 올바르지 않습니다."),
    DISCOUNT_EXCEEDS_ORIGINAL_PRICE(HttpStatus.BAD_REQUEST, "할인 금액이 원가를 초과할 수 없습니다."),
    INVALID_COUPON_DISCOUNT(HttpStatus.BAD_REQUEST, "유효하지 않은 쿠폰 할인 금액 입니다."),
    INVALID_GRADE_DISCOUNT(HttpStatus.BAD_REQUEST, "유효하지 않은 등급 할인 금액 입니다."),
    COUPON_DISCOUNT_WITHOUT_COUPON(HttpStatus.BAD_REQUEST, "쿠폰없이 쿠폰 할인 금액이 존재할 수 없습니다."),

    ;

    private final HttpStatus status;
    private final String message;
}
