package com.klp.hub.inventory.exception;

import com.klp.hub.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InventoryErrorCode implements ErrorCode {
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "재고 수량이 부족합니다."),
    NOT_FOUND_INVENTORY(HttpStatus.BAD_REQUEST, "재고를 찾을 수 없습니다."),
    PARTIAL_INVENTORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "일부 재고를 찾을 수 없습니다."),
    INVENTORY_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 해당 상품의 재고가 존재합니다"),
    IDEMPOTENCY_ALREADY_PROCESSING(HttpStatus.CONFLICT, "동일한 멱등키로 요청이 이미 처리 중 입니다."),
    OUTBOX_SERIALIZATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이벤트 직렬화에 실패했습니다."),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "선점 정보를 찾을 수 없습니다."),
    RESERVATION_ALREADY_PROCESSED(HttpStatus.CONFLICT, "이미 처리된 선점입니다."),
    INVALID_EVENT_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 이벤트 타입입니다."),
    EVENT_DESERIALIZATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이벤트 역직렬화에 실패했습니다."),
    CACHE_SET_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "캐시 설정에 실패했습니다."),
    INVALID_REGISTER_TTL(HttpStatus.BAD_REQUEST, "올바르지 않은 TTL입니다."),
    NOT_HOT_PRODUCT(HttpStatus.BAD_REQUEST, "Hot Product로 등록되지 않은 상품입니다.");

    private final HttpStatus status;
    private final String message;
}
