package com.klp.delivery.delivery.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.klp.common.DeliveryStatus;
import com.klp.delivery.delivery.MockTest;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class DeliveryTest extends MockTest {

  @Test
  void 배송_생성시_배송상태는_CREATED_설정_검증() {

    // given: 배송 등록 데이터 준비
    UUID vendorDriverId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    UUID departureId = UUID.randomUUID();
    UUID arrivalId = UUID.randomUUID();
    UUID receiverId = UUID.randomUUID();
    String receiverName = "홍길동";
    String address = "서울특별시 강남구 테헤란로 123";
    String receiverSlackId = "U123456";

    // when: 배송 생성
    Delivery delivery = Delivery.create(
        vendorDriverId,
        orderId,
        departureId,
        arrivalId,
        receiverId,
        receiverName,
        address,
        receiverSlackId
    );

    // then: 배송 상태값 검증
    assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.CREATED);
  }
}
