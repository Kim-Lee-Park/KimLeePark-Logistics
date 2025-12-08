package com.klp.order.domain.entity.order;

public enum OrderStatus {
    PENDING("주문 진행 중"),
    CREATED("주문 생성"),
    PAID("결제 완료"),
    PAID_FAILED("결제 실패"),
    STOCK_CONFIRMED("재고 차감 확정"),
    STOCK_CONFIRMED_FAILED("재고 차감 실패"),
    COUPON_CONFIRMED("쿠폰 사용 확정"),
    COUPON_CONFIRMED_FAILED("쿠폰 사용 실패"),
    DELIVERY_CREATED("배송 대기 중"),
    DELIVERY_CREATED_FAILED("배송 생성 중 실패"),
    DELIVERY_SHIPPING("배송 중"),
    DELIVERY_SHIPPING_FAILED("배송 중 상태 변화 중 실패"),
    DELIVERY_ARRIVED_FAILED("배송 중 상태 변화 중 실패"),
    COMPLETE("완료"),
    CANCELLED("취소됨"),
    FAILED("실패");

    private String description;

    OrderStatus(String description) {
        this.description = description;
    }
}
