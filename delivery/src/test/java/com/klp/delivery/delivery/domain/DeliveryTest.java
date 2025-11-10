package com.klp.delivery.delivery.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.klp.common.exception.BusinessException;
import com.klp.delivery.common.DeliveryStatus;
import com.klp.delivery.delivery.MockTest;
import com.klp.delivery.delivery.exception.DeliveryErrorCode;
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

  @Test
  void 배송_생성시_배송담당자가없으면_예외발생() {
    // given: 배송 담당자가 null인 데이터
    UUID vendorDriverId = null;
    UUID orderId = UUID.randomUUID();
    UUID departureId = UUID.randomUUID();
    UUID arrivalId = UUID.randomUUID();
    UUID receiverId = UUID.randomUUID();
    String receiverName = "홍길동";
    String address = "서울특별시 강남구 테헤란로 123";
    String receiverSlackId = "U123456";

    // when & then: 예외 발생 검증
    assertThatThrownBy(() -> Delivery.create(
        vendorDriverId,
        orderId,
        departureId,
        arrivalId,
        receiverId,
        receiverName,
        address,
        receiverSlackId
    ))
        .isInstanceOf(BusinessException.class)
        .satisfies(exception -> {
          BusinessException businessException = (BusinessException) exception;
          assertThat(businessException.getErrorCode()).isEqualTo(
              DeliveryErrorCode.INVALID_DELIVERY_DATA);
        });
  }

  @Test
  void 배송_생성시_주소가null이면_예외발생() {
    // given: 주소가 null인 데이터
    UUID vendorDriverId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    UUID departureId = UUID.randomUUID();
    UUID arrivalId = UUID.randomUUID();
    UUID receiverId = UUID.randomUUID();
    String receiverName = "홍길동";
    String address = null;
    String receiverSlackId = "U123456";

    // when & then: 예외 발생 검증
    assertThatThrownBy(() -> Delivery.create(
        vendorDriverId,
        orderId,
        departureId,
        arrivalId,
        receiverId,
        receiverName,
        address,
        receiverSlackId
    ))
        .isInstanceOf(BusinessException.class)
        .satisfies(exception -> {
          BusinessException businessException = (BusinessException) exception;
          assertThat(businessException.getErrorCode()).isEqualTo(
              DeliveryErrorCode.INVALID_DELIVERY_DATA);
        });
  }

  @Test
  void 배송_생성시_주소가빈문자열이면_예외발생() {
    // given: 주소가 빈 문자열인 데이터
    UUID vendorDriverId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    UUID departureId = UUID.randomUUID();
    UUID arrivalId = UUID.randomUUID();
    UUID receiverId = UUID.randomUUID();
    String receiverName = "홍길동";
    String address = "";
    String receiverSlackId = "U123456";

    // when & then: 예외 발생 검증
    assertThatThrownBy(() -> Delivery.create(
        vendorDriverId,
        orderId,
        departureId,
        arrivalId,
        receiverId,
        receiverName,
        address,
        receiverSlackId
    ))
        .isInstanceOf(BusinessException.class)
        .satisfies(exception -> {
          BusinessException businessException = (BusinessException) exception;
          assertThat(businessException.getErrorCode()).isEqualTo(
              DeliveryErrorCode.INVALID_DELIVERY_DATA);
        });
  }

  @Test
  void 배송_생성시_주소가공백이면_예외발생() {
    // given: 주소가 공백만 있는 데이터
    UUID vendorDriverId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    UUID departureId = UUID.randomUUID();
    UUID arrivalId = UUID.randomUUID();
    UUID receiverId = UUID.randomUUID();
    String receiverName = "홍길동";
    String address = "   ";
    String receiverSlackId = "U123456";

    // when & then: 예외 발생 검증
    assertThatThrownBy(() -> Delivery.create(
        vendorDriverId,
        orderId,
        departureId,
        arrivalId,
        receiverId,
        receiverName,
        address,
        receiverSlackId
    ))
        .isInstanceOf(BusinessException.class)
        .satisfies(exception -> {
          BusinessException businessException = (BusinessException) exception;
          assertThat(businessException.getErrorCode()).isEqualTo(
              DeliveryErrorCode.INVALID_DELIVERY_DATA);
        });
  }

  @Test
  void 배송_생성시_수령인이름이null이면_예외발생() {
    // given: 수령인 이름이 null인 데이터
    UUID vendorDriverId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    UUID departureId = UUID.randomUUID();
    UUID arrivalId = UUID.randomUUID();
    UUID receiverId = UUID.randomUUID();
    String receiverName = null;
    String address = "서울특별시 강남구 테헤란로 123";
    String receiverSlackId = "U123456";

    // when & then: 예외 발생 검증
    assertThatThrownBy(() -> Delivery.create(
        vendorDriverId,
        orderId,
        departureId,
        arrivalId,
        receiverId,
        receiverName,
        address,
        receiverSlackId
    ))
        .isInstanceOf(BusinessException.class)
        .satisfies(exception -> {
          BusinessException businessException = (BusinessException) exception;
          assertThat(businessException.getErrorCode()).isEqualTo(
              DeliveryErrorCode.INVALID_DELIVERY_DATA);
        });
  }

  @Test
  void 배송_생성시_수령인이름이빈문자열이면_예외발생() {
    // given: 수령인 이름이 빈 문자열인 데이터
    UUID vendorDriverId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    UUID departureId = UUID.randomUUID();
    UUID arrivalId = UUID.randomUUID();
    UUID receiverId = UUID.randomUUID();
    String receiverName = "";
    String address = "서울특별시 강남구 테헤란로 123";
    String receiverSlackId = "U123456";

    // when & then: 예외 발생 검증
    assertThatThrownBy(() -> Delivery.create(
        vendorDriverId,
        orderId,
        departureId,
        arrivalId,
        receiverId,
        receiverName,
        address,
        receiverSlackId
    ))
        .isInstanceOf(BusinessException.class)
        .satisfies(exception -> {
          BusinessException businessException = (BusinessException) exception;
          assertThat(businessException.getErrorCode()).isEqualTo(
              DeliveryErrorCode.INVALID_DELIVERY_DATA);
        });
  }

  @Test
  void 배송_생성시_수령인슬랙ID가null이면_예외발생() {
    // given: 수령인 슬랙 ID가 null인 데이터
    UUID vendorDriverId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    UUID departureId = UUID.randomUUID();
    UUID arrivalId = UUID.randomUUID();
    UUID receiverId = UUID.randomUUID();
    String receiverName = "홍길동";
    String address = "서울특별시 강남구 테헤란로 123";
    String receiverSlackId = null;

    // when & then: 예외 발생 검증
    assertThatThrownBy(() -> Delivery.create(
        vendorDriverId,
        orderId,
        departureId,
        arrivalId,
        receiverId,
        receiverName,
        address,
        receiverSlackId
    ))
        .isInstanceOf(BusinessException.class)
        .satisfies(exception -> {
          BusinessException businessException = (BusinessException) exception;
          assertThat(businessException.getErrorCode()).isEqualTo(
              DeliveryErrorCode.INVALID_DELIVERY_DATA);
        });
  }

  @Test
  void 배송_생성시_수령인슬랙ID가빈문자열이면_예외발생() {
    // given: 수령인 슬랙 ID가 빈 문자열인 데이터
    UUID vendorDriverId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    UUID departureId = UUID.randomUUID();
    UUID arrivalId = UUID.randomUUID();
    UUID receiverId = UUID.randomUUID();
    String receiverName = "홍길동";
    String address = "서울특별시 강남구 테헤란로 123";
    String receiverSlackId = "";

    // when & then: 예외 발생 검증
    assertThatThrownBy(() -> Delivery.create(
        vendorDriverId,
        orderId,
        departureId,
        arrivalId,
        receiverId,
        receiverName,
        address,
        receiverSlackId
    ))
        .isInstanceOf(BusinessException.class)
        .satisfies(exception -> {
          BusinessException businessException = (BusinessException) exception;
          assertThat(businessException.getErrorCode()).isEqualTo(
              DeliveryErrorCode.INVALID_DELIVERY_DATA);
        });
  }

  @Test
  void 배송중일때_상태가_ARRIVED_AT_FINAL_HUB로_변경() {
    // given: 배송 생성
    UUID vendorDriverId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    UUID departureId = UUID.randomUUID();
    UUID arrivalId = UUID.randomUUID();
    UUID receiverId = UUID.randomUUID();
    String receiverName = "홍길동";
    String address = "서울특별시 강남구 테헤란로 123";
    String receiverSlackId = "U123456";

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

    // when: 배송 중 상태로 변경 후 최종 허브 도착 상태로 변경
    delivery.updateStatus(DeliveryStatus.HUB_TRANSIT);
    delivery.updateStatus(DeliveryStatus.ARRIVED_AT_FINAL_HUB);

    // then: 상태 변경 검증
    assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.ARRIVED_AT_FINAL_HUB);
  }

  @Test
  void 배송완료시_상태가_DELIVERED로_변경() {
    // given: 배송 생성
    UUID vendorDriverId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    UUID departureId = UUID.randomUUID();
    UUID arrivalId = UUID.randomUUID();
    UUID receiverId = UUID.randomUUID();
    String receiverName = "홍길동";
    String address = "서울특별시 강남구 테헤란로 123";
    String receiverSlackId = "U123456";

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

    // when: 배송 완료 상태로 변경
    delivery.updateStatus(DeliveryStatus.OUT_FOR_DELIVERY);
    delivery.updateStatus(DeliveryStatus.DELIVERED);

    // then: 상태 변경 검증
    assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
  }

  @Test
  void 배송생성후_주소등주요정보수정불가능() {
    // given: 배송 생성
    UUID vendorDriverId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    UUID departureId = UUID.randomUUID();
    UUID arrivalId = UUID.randomUUID();
    UUID receiverId = UUID.randomUUID();
    String receiverName = "홍길동";
    String address = "서울특별시 강남구 테헤란로 123";
    String receiverSlackId = "U123456";

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

    // then: Delivery 엔티티에는 주소나 수령인 정보를 수정하는 메서드가 없음
    // 리플렉션으로 메서드 존재 여부 확인
    try {
      delivery.getClass().getMethod("updateAddress", String.class);
      throw new AssertionError("주소 수정 메서드가 존재합니다.");
    } catch (NoSuchMethodException e) {
      // 메서드가 없으면 정상 (예상된 동작)
    }

    try {
      delivery.getClass().getMethod("updateReceiverName", String.class);
      throw new AssertionError("수령인 이름 수정 메서드가 존재합니다.");
    } catch (NoSuchMethodException e) {
      // 메서드가 없으면 정상 (예상된 동작)
    }

    // then: 원본 정보가 변경되지 않았는지 확인
    assertThat(delivery.getAddress()).isEqualTo(address);
    assertThat(delivery.getReceiverName()).isEqualTo(receiverName);
  }
}
