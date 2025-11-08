package com.klp.delivery.delivery.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.klp.common.DeliveryStatus;
import com.klp.delivery.delivery.domain.Delivery;
import java.util.Optional;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(DeliveryRepositoryImpl.class)
public class DeliveryRepositoryImplTest {


  @Autowired
  private DeliveryRepositoryImpl deliveryRepository;


  @Test
  void repository가_null_아님을_검증() {
    Assertions.assertThat(deliveryRepository).isNotNull();
  }


  @Test
  void 배송_등록_성공() {

    // given: 배송 등록 데이터 준비
    UUID vendorDriverId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    UUID departureId = UUID.randomUUID();
    UUID arrivalId = UUID.randomUUID();
    UUID receiverId = UUID.randomUUID();
    String receiverName = "김철수";
    String address = "서울특별시 강남구 테헤란로 123";
    String receiverSlackId = "U123456789";

    // when: 배송 엔티티 생성
    Delivery delivery = Delivery.create(vendorDriverId, orderId, departureId,
        arrivalId, receiverId, receiverName, address, receiverSlackId);

    Delivery result = deliveryRepository.save(delivery);

    // then: 생성 검증
    assertThat(result.getDeliveryId()).isNotNull();
    assertThat(delivery.getOrderId()).isEqualTo(orderId);
    assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.CREATED);

  }


  @Test
  void 배송ID로_배송_조회_성공() {

    // given: 배송 등록 데이터 준비
    UUID vendorDriverId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    UUID departureId = UUID.randomUUID();
    UUID arrivalId = UUID.randomUUID();
    UUID receiverId = UUID.randomUUID();
    String receiverName = "김철수";
    String address = "서울특별시 강남구 테헤란로 123";
    String receiverSlackId = "U123456789";

    // when: 배송 엔티티 생성
    Delivery delivery = Delivery.create(vendorDriverId, orderId, departureId,
        arrivalId, receiverId, receiverName, address, receiverSlackId);

    Delivery result = deliveryRepository.save(delivery);

    Optional<Delivery> findResult = deliveryRepository.findByDeliveryId(result.getDeliveryId());

    // then: 생성 검증
    assertThat(findResult).isPresent().get().extracting(Delivery::getStatus)
        .isEqualTo(DeliveryStatus.CREATED);

  }


}
