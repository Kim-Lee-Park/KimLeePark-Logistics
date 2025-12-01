package com.klp.delivery.delivery.application.service;

import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_ARRIVAL_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_COMPANY_ADDRESS;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_COMPANY_NAME;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_DELIVERY_ID_FIRST;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_DEPARTURE_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_ORDER_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_RECEIVER_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_RECEIVER_SLACK_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.DEFAULT_SENDER_ID;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.defaultDelivery;
import static com.klp.delivery.delivery.fixture.DeliveryFixture.deliveryList;
import static com.klp.delivery.delivery.fixture.OrderItemFixture.orderItemCommandsDefault;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.klp.common.exception.BusinessException;
import com.klp.delivery.common.enums.CustomerDeliveryStatus;
import com.klp.delivery.delivery.MockTest;
import com.klp.delivery.delivery.application.command.DeliveryCommand;
import com.klp.delivery.delivery.application.command.OrderToDeliveryCommand.OrderItemCommand;
import com.klp.delivery.delivery.domain.entity.Delivery;
import com.klp.delivery.delivery.domain.repository.DeliveryRepository;
import com.klp.delivery.delivery.exception.DeliveryErrorCode;
import com.klp.delivery.delivery.presentation.dto.DeliveryDetailResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class DeliveryServiceTest extends MockTest {


    @InjectMocks
    DeliveryService deliveryService;

    @Mock
    DeliveryRepository deliveryRepository;

    @Mock
    CompanyClientService companyClientService;


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
        List<OrderItemCommand> items = orderItemCommandsDefault();
        // when: 배송 생성
        Delivery result = deliveryService.registerDelivery(command, items);

        // then: 생성 검증
        verify(deliveryRepository, times(1)).save(any(Delivery.class));
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(orderId);
    }




    @Test
    void 주문ID로_배송조회_성공() {
        // given: 주문 ID로 배송 조회 데이터 준비
        List<Delivery> expectedDeliveries = deliveryList();
        UUID orderId = DEFAULT_ORDER_ID;

        when(deliveryRepository.findDeliveryByOrderId(orderId))
            .thenReturn(expectedDeliveries);

        // when: 주문 ID로 배송 조회
        List<DeliveryDetailResponse> result = deliveryService.findDeliveriesByOrderId(orderId);

        // then: 조회 결과 검증
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).orderId()).isEqualTo(orderId);
        assertThat(result.get(1).orderId()).isEqualTo(orderId);

        // then: Repository 호출 검증
        verify(deliveryRepository, times(1)).findDeliveryByOrderId(orderId);
    }

    @Test
    void 배송_전체_조회_성공() {
        // given: 배송 전체 조회 데이터 준비
        List<Delivery> deliveries = deliveryList();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").ascending());
        Page<Delivery> deliveryPage = new PageImpl<>(deliveries, pageable, deliveries.size());

        when(deliveryRepository.findDeliveryAll(pageable)).thenReturn(deliveryPage);

        // when: 배송 전체 조회
        Page<DeliveryDetailResponse> result = deliveryService.findDeliveryAll(pageable);

        // then: 조회 결과 검증
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).orderId()).isEqualTo(DEFAULT_ORDER_ID);

        // then: Repository 호출 검증
        verify(deliveryRepository, times(1)).findDeliveryAll(pageable);
    }

    @Test
    void 배송_전체_조회_빈_결과() {
        // given: 빈 배송 조회 데이터 준비
        Pageable pageable = PageRequest.of(0, 10);
        Page<Delivery> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(deliveryRepository.findDeliveryAll(pageable)).thenReturn(emptyPage);

        // when: 배송 전체 조회
        Page<DeliveryDetailResponse> result = deliveryService.findDeliveryAll(pageable);

        // then: 조회 결과 검증
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);

        // then: Repository 호출 검증
        verify(deliveryRepository, times(1)).findDeliveryAll(pageable);
    }

    @Test
    void 배송_전체_조회_정렬_확인() {
        // given: 정렬된 배송 조회 데이터 준비
        List<Delivery> deliveries = deliveryList();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Page<Delivery> deliveryPage = new PageImpl<>(deliveries, pageable, deliveries.size());

        when(deliveryRepository.findDeliveryAll(pageable)).thenReturn(deliveryPage);

        // when: 배송 전체 조회
        Page<DeliveryDetailResponse> result = deliveryService.findDeliveryAll(pageable);

        // then: 조회 결과 검증
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getSort().getOrderFor("createdAt").getDirection())
            .isEqualTo(Sort.Direction.DESC);

        // then: Repository 호출 검증
        verify(deliveryRepository, times(1)).findDeliveryAll(pageable);
    }




    @Test
    void 배송_삭제_성공_CREATED상태에서() {
        // given: CREATED 상태의 배송
        Delivery delivery = defaultDelivery();
        UUID deliveryId = DEFAULT_DELIVERY_ID_FIRST;
        Long deletedBy = 1L;
        when(deliveryRepository.findByDeliveryId(deliveryId)).thenReturn(delivery);

        // when: 배송 삭제
        deliveryService.deleteDelivery(deliveryId, deletedBy);

        // then: 삭제 확인
        assertThat(delivery.isDeleted()).isTrue();
        verify(deliveryRepository, times(1)).findByDeliveryId(deliveryId);
    }

    @ParameterizedTest
    @CsvSource({"SHIPPING", "ARRIVED"})
    void CREATED가_아닌_상태일때_배송삭제_실패(String statusName) {
        // given: CREATED가 아닌 상태의 배송
        Delivery delivery = defaultDelivery();
        delivery.updateStatus(CustomerDeliveryStatus.valueOf(statusName));
        UUID deliveryId = DEFAULT_DELIVERY_ID_FIRST;
        Long deletedBy = 1L;
        when(deliveryRepository.findByDeliveryId(deliveryId)).thenReturn(delivery);

        // when & then: 삭제 실패 예외 검증
        assertThatThrownBy(() -> deliveryService.deleteDelivery(deliveryId, deletedBy))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(DeliveryErrorCode.DELIVERY_CANNOT_BE_MODIFIED);
    }

}
