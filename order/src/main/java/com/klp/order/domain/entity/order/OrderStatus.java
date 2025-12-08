package com.klp.order.domain.entity.order;

public enum OrderStatus {
    PENDING("주문 진행 중"),
    CREATED("주문 생성"),
    PAID("결제 완료"),
    STOCK_CONFIRMED("재고 차감 확정"),
    COUPON_CONFIRMED("쿠폰 사용 확정"),
    DELIVERY_CREATED("배송 대기 중"),
    DELIVERY_SHIPPING("배송 중"),
    COMPLETE("완료"),
    CANCELLED("취소됨"),
    FAILED("실패");

    private String description;

    OrderStatus(String description) {
        this.description = description;
    }
}
