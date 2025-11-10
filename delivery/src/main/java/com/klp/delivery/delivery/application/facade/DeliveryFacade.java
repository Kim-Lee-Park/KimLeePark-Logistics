package com.klp.delivery.delivery.application.facade;

import com.klp.delivery.delivery.application.command.DeliveryCommand;
import com.klp.delivery.delivery.application.command.IdempotencyCommand;
import com.klp.delivery.delivery.application.service.DeliveryService;
import com.klp.delivery.delivery.application.service.IdempotencyKeyService;
import com.klp.delivery.delivery.domain.Company;
import com.klp.delivery.delivery.domain.Delivery;
import com.klp.delivery.delivery.domain.Driver;
import com.klp.delivery.delivery.presentation.dto.DeliveryCreateRequest;
import com.klp.delivery.delivery.presentation.dto.DeliveryResponse;
import com.klp.delivery.delivery.presentation.dto.OrderItemDto;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryFacade {

  private final DeliveryService deliveryService;
  private final IdempotencyKeyService idempotencyKeyService;


  public DeliveryResponse createDelivery(UUID orderId, DeliveryCreateRequest request) {
    log.info("배송 생성 시작: orderId={}", orderId);

    // 멱등키 등록 (주문 단위로 관리)
    IdempotencyCommand idempotencyCommand = new IdempotencyCommand(request.idempotencyKey(),
        orderId);
    idempotencyKeyService.registerIdempotencyKey(idempotencyCommand);

    try {

      Company company = deliveryService.findCompany(request.customerId().toString());
      Driver driver = deliveryService.findDriver(request.customerId().toString());

      // 항목별 배송 생성
      List<DeliveryResponse.DeliveryItemResponse> deliveryItems = createDeliveriesForOrderItems(
          orderId, request, company, driver);

      idempotencyKeyService.updateIdempotencyStatus(idempotencyCommand);

      log.info("배송 생성 완료: orderId={}, deliveryCount={}", orderId, deliveryItems.size());
      return new DeliveryResponse(deliveryItems);
    } catch (Exception e) {
      log.error("배송 생성 실패: orderId={}, error={}", orderId, e.getMessage(), e);
      throw e;
    }
  }

  private List<DeliveryResponse.DeliveryItemResponse> createDeliveriesForOrderItems(UUID orderId,
      DeliveryCreateRequest request, Company company, Driver driver) {

    List<DeliveryResponse.DeliveryItemResponse> deliveryItems = new ArrayList<>();

    UUID arrivalId = UUID.fromString(company.hubId());

    UUID receiverId = UUID.nameUUIDFromBytes(request.customerId().toString().getBytes());
    UUID vendorDriverId = UUID.fromString(driver.vendorDrvierId());

    for (OrderItemDto orderItem : request.orderItems()) {
      // departureId는 각 orderItem의 hubId
      UUID departureId = orderItem.hubId();

      DeliveryCommand deliveryCommand = new DeliveryCommand(
          orderId,
          orderItem.orderItemId(),
          orderItem.hubId(),
          departureId,
          arrivalId,
          receiverId,
          company.name(),
          company.address(),
          driver.receiverSlackId(),
          vendorDriverId
      );

      Delivery delivery = deliveryService.registerDelivery(deliveryCommand);

      deliveryItems.add(new DeliveryResponse.DeliveryItemResponse(
          orderItem.orderItemId(),
          delivery.getDeliveryId()
      ));

      // TODO: 각 배송 생성 시 경로 생성 이벤트 발행 (비동기)
      // deliveryService.publishDeliveryCreatedEvent(delivery);
    }

    return deliveryItems;
  }
}

