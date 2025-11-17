package com.klp.delivery.delivery.application;

import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_ARRIVAL_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_COMPANY_ADDRESS;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_COMPANY_NAME;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_DEPARTURE_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_ORDER_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_RECEIVER_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_RECEIVER_SLACK_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_SENDER_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createCompany;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.createDriver;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.defaultDelivery;
import static com.klp.delivery.delivery.fixture.OrderItemFixture.orderItemCommandsDefault;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.klp.common.exception.BusinessException;
import com.klp.delivery.delivery.MockTest;
import com.klp.delivery.delivery.application.command.DeliveryCommand;
import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand.OrderItemCommand;
import com.klp.delivery.delivery.application.service.CompanyApiClient;
import com.klp.delivery.delivery.application.service.DeliveryService;
import com.klp.delivery.delivery.application.service.DriverApiClient;
import com.klp.delivery.delivery.domain.entity.Delivery;
import com.klp.delivery.delivery.domain.repository.DeliveryRepository;
import com.klp.delivery.delivery.exception.DeliveryErrorCode;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class DeliveryServiceTest extends MockTest {


  @InjectMocks
  DeliveryService deliveryService;

  @Mock
  DeliveryRepository deliveryRepository;

  @Mock
  CompanyApiClient companyApiClient;

  @Mock
  DriverApiClient driverApiClient;


  @Test
  void 배송_생성_성공() {
    // given: 배송 등록 데이터 준비
    UUID orderId = DEFAULT_ORDER_ID;
    UUID departureId = DEFAULT_DEPARTURE_ID;
    UUID arrivalId = DEFAULT_ARRIVAL_ID;
    UUID senderId = DEFAULT_SENDER_ID;
    UUID receiverId = DEFAULT_RECEIVER_ID;
    String receiverName = DEFAULT_COMPANY_NAME;
    String address = DEFAULT_COMPANY_ADDRESS;
    String receiverSlackId = DEFAULT_RECEIVER_SLACK_ID;
    Long vendorDriverId = 1234L;

    DeliveryCommand command = new DeliveryCommand(
        orderId, departureId, arrivalId, senderId, receiverId,
        receiverName, address, receiverSlackId, vendorDriverId);

      Delivery delivery = defaultDelivery();
    when(deliveryRepository.save(any(Delivery.class))).thenReturn(delivery);
      List<OrderItemCommand> items =  orderItemCommandsDefault();
    // when: 배송 생성
    Delivery result = deliveryService.registerDelivery(command, items);

    // then: 생성 검증
    verify(deliveryRepository, times(1)).save(any(Delivery.class));
    assertThat(result).isNotNull();
    assertThat(result.getOrderId()).isEqualTo(orderId);
  }


  @Test
  void 업체조회api_성공() {
    // given: 업체 조회 데이터 준비
    UUID receiverId = DEFAULT_RECEIVER_ID;

    when(companyApiClient.findCompany(receiverId.toString())).thenReturn(createCompany());

    // when: 업체 조회
    var result = deliveryService.findCompany(receiverId.toString());

    // then: 조회 검증
    verify(companyApiClient, times(1)).findCompany(receiverId.toString());
    assertThat(result).isNotNull();
    assertThat(result.name()).isEqualTo(DEFAULT_COMPANY_NAME);
  }

  @ParameterizedTest
  @CsvSource({
      "업체 조회 실패",
      "네트워크 오류",
      "타임아웃 발생"
  })
  void 업체조회api_실패_예외발생(String errorMessage) {
    // given: 업체 조회 데이터 준비
    UUID receiverId = DEFAULT_RECEIVER_ID;

    when(companyApiClient.findCompany(receiverId.toString()))
        .thenThrow(new RuntimeException(errorMessage));

    // when & then: 예외 발생 검증
    assertThatThrownBy(() -> deliveryService.findCompany(receiverId.toString()))
        .isInstanceOf(BusinessException.class)
        .satisfies(exception -> {
          BusinessException businessException = (BusinessException) exception;
          assertThat(businessException.getErrorCode()).isEqualTo(
              DeliveryErrorCode.EXTERNAL_API_ERROR);
        });

    // then: 외부 API 호출 검증
    verify(companyApiClient, times(1)).findCompany(receiverId.toString());
  }

  @Test
  void 담당자조회api_성공() {
    // given: 담당자 조회 데이터 준비
    UUID receiverId = DEFAULT_RECEIVER_ID;

    when(driverApiClient.findDriver(receiverId.toString())).thenReturn(createDriver());

    // when: 담당자 조회
    var result = deliveryService.findDriver(receiverId.toString());

    // then: 조회 검증
    verify(driverApiClient, times(1)).findDriver(receiverId.toString());
    assertThat(result).isNotNull();
    assertThat(result.receiverSlackId()).isEqualTo(DEFAULT_RECEIVER_SLACK_ID);
  }

  @Test
  void 담당자조회api_실패_예외발생() {
    // given: 담당자 조회 데이터 준비
    UUID receiverId = DEFAULT_RECEIVER_ID;

    when(driverApiClient.findDriver(receiverId.toString()))
        .thenThrow(new RuntimeException("담당자 조회 실패"));

    // when & then: 예외 발생 검증
    assertThatThrownBy(() -> deliveryService.findDriver(receiverId.toString()))
        .isInstanceOf(BusinessException.class)
        .satisfies(exception -> {
          BusinessException businessException = (BusinessException) exception;
          assertThat(businessException.getErrorCode()).isEqualTo(
              DeliveryErrorCode.EXTERNAL_API_ERROR);
        });

    // then: 외부 API 호출 검증
    verify(driverApiClient, times(1)).findDriver(receiverId.toString());
  }
}
