package com.klp.delivery.delivery.application;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.klp.delivery.delivery.MockTest;
import com.klp.delivery.delivery.application.command.DeliveryCommand;
import com.klp.delivery.delivery.application.service.DeliveryService;
import com.klp.delivery.delivery.domain.Delivery;
import com.klp.delivery.delivery.domain.DeliveryRepository;
import com.klp.delivery.delivery.presentation.dto.DeliveryResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

public class DeliveryServiceTest extends MockTest {


  @InjectMocks
  DeliveryService deliveryService;

  @Mock
  DeliveryRepository deliveryRepository;

  @Test
  void 배송_생성_성공() {

    // given: 배송 등록 데이터 준비
    UUID vendorDriverId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    UUID departureId = UUID.randomUUID();
    UUID arrivalId = UUID.randomUUID();
    UUID receiverId = UUID.randomUUID();
    String receiverName = "홍길동";
    String address = "서울특별시 강남구 테헤란로 123";
    String receiverSlackId = "U123456";

    DeliveryCommand command = new DeliveryCommand(vendorDriverId, orderId, departureId, arrivalId,
        receiverId, receiverName, address, receiverSlackId);

    // when 배송 생성
    Delivery delivery = Delivery.create(vendorDriverId, orderId, departureId, arrivalId, receiverId,
        receiverName, address, receiverSlackId);

    Mockito.doReturn(delivery).when(deliveryRepository).save(any(Delivery.class));

    //when
    DeliveryResponse result = deliveryService.registerDelivery(command);

    // when: 응답 존재 확인 및 mock 반환 id 확인
    verify(deliveryRepository, times(1)).save(any(Delivery.class));
    assertThat(result).isNotNull();
    assertThat(result.deliveryId()).isEqualTo(delivery.getDeliveryId());


  }

}
