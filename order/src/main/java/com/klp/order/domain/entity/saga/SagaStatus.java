package com.klp.order.domain.entity.saga;

public enum SagaStatus {
    STARTED("시작됨"),
    ORDER_CREATED("주문 생성 완료"),           // Step 1
    INVENTORY_DEDUCTED("재고 차감 완료"),      // Step 2
    DELIVERY_CREATED("배송 생성 완료"),        // Step 3
    COMPLETED("완료"),                        // Step 4

    COMPENSATING("보상 트랜잭션 진행 중"),
    DELIVERY_COMPENSATION_FAILED("배송 보상 실패"),
    DELIVERY_COMPENSATION_COMPLETED("배송 보상 성공"),
    INVENTORY_COMPENSATION_COMPLETED("재고 보상 완료"),
    ORDER_COMPENSATION_COMPLETED("주문 보상 완료"),
    COMPENSATED("보상 트랜잭션 완료"),

    FAILED("실패");

    private final String description;

    SagaStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}