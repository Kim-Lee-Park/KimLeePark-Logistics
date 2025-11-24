package com.klp.delivery.delivery.presentation.controller;

import com.klp.delivery.delivery.application.facade.DeliveryFacade;
import com.klp.delivery.delivery.application.service.DeliveryService;
import com.klp.delivery.delivery.domain.entity.Delivery;
import com.klp.delivery.delivery.presentation.dto.DeliveryCreateRequest;
import com.klp.delivery.delivery.presentation.dto.DeliveryDetailResponse;
import com.klp.delivery.delivery.presentation.dto.DeliveryResponse;
import com.klp.delivery.delivery.presentation.dto.DeliveryStatusUpdateRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryFacade deliveryFacade;
    private final DeliveryService deliveryService;

    @PostMapping
    public ResponseEntity<DeliveryResponse> createDelivery(
        @Valid @RequestBody DeliveryCreateRequest request) {

        log.info("배송 생성 요청: orderId={}", request.orderId());

        DeliveryResponse response = deliveryFacade.createDelivery(
            request.toOrderToDeliveryCommand(), request.toIdempotencyCommand());

        log.info("배송 생성 성공: orderId={}, deliveryCount={}", request.orderId(),
            response.items().size());
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{deliveryId}")
    public ResponseEntity<DeliveryDetailResponse> getDelivery(@PathVariable UUID deliveryId) {
        Delivery delivery = deliveryService.getDelivery(deliveryId);
        DeliveryDetailResponse response = DeliveryDetailResponse.from(delivery);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{deliveryId}/status")
    public ResponseEntity<Void> updateDeliveryStatus(
        @PathVariable UUID deliveryId,
        @Valid @RequestBody DeliveryStatusUpdateRequest request) {
        deliveryService.updateDeliveryStatus(deliveryId, request.status());
        return ResponseEntity.noContent().build();
    }

}

