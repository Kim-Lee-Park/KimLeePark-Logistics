package com.klp.delivery.delivery.presentation.controller;

import com.klp.delivery.delivery.application.facade.DeliveryFacade;
import com.klp.delivery.delivery.application.service.DeliveryService;
import com.klp.delivery.delivery.domain.entity.Delivery;
import com.klp.delivery.delivery.presentation.controller.docs.DeliveryControllerDocs;
import com.klp.delivery.delivery.presentation.dto.DeliveryCreateRequest;
import com.klp.delivery.delivery.presentation.dto.DeliveryDetailResponse;
import com.klp.delivery.delivery.presentation.dto.DeliveryResponse;
import com.klp.delivery.delivery.presentation.dto.DeliveryUpdateRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryController implements DeliveryControllerDocs {

    private final DeliveryFacade deliveryFacade;
    private final DeliveryService deliveryService;

    @Override
    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    public ResponseEntity<DeliveryResponse> createDelivery(
        @Valid @RequestBody DeliveryCreateRequest request) {

        log.info("배송 생성 요청: orderId={}", request.orderId());

        DeliveryResponse response = deliveryFacade.createDelivery(
            request.toOrderToDeliveryCommand(), request.toIdempotencyCommand());

        log.info("배송 생성 성공: orderId={}, deliveryCount={}", request.orderId(),
            response.items().size());
        return ResponseEntity.ok().body(response);
    }

    @Override
    @GetMapping("/{deliveryId}")
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    public ResponseEntity<DeliveryDetailResponse> getDelivery(@PathVariable UUID deliveryId) {
        Delivery delivery = deliveryService.findDelivery(deliveryId);
        DeliveryDetailResponse response = DeliveryDetailResponse.from(delivery);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{deliveryId}/{orderId}")
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    public ResponseEntity<List<DeliveryDetailResponse>> getDeliveriesByOrderId(
        @PathVariable UUID orderId) {
        List<DeliveryDetailResponse> responses = deliveryService.findDeliveriesByOrderId(orderId);
        return ResponseEntity.ok(responses);
    }

    @Override
    @GetMapping
    @PreAuthorize("hasAnyRole('MASTER')")
    public ResponseEntity<Page<DeliveryDetailResponse>> getAllDeliveries(@ParameterObject Pageable pageable) {
        Page<DeliveryDetailResponse> responses = deliveryService.findDeliveryAll(pageable);
        return ResponseEntity.ok(responses);
    }

    @Override
    @PatchMapping("/{deliveryId}")
    @PreAuthorize("hasAnyRole('MASTER')")
    public ResponseEntity<Void> updateDeliveryStatus(@PathVariable UUID deliveryId,
        @RequestBody DeliveryUpdateRequest request) {
        deliveryFacade.updateVendorDriver(deliveryId, request.vendorDrvierId());
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping("/{deliveryId}")
    @PreAuthorize("hasAnyRole('MASTER')")
    public ResponseEntity<Void> deleteDelivery(
        @PathVariable UUID deliveryId,
        @RequestHeader(value = "X-User-Id", required = false, defaultValue = "0") Long deletedBy) {
        deliveryService.deleteDelivery(deliveryId, deletedBy);
        return ResponseEntity.noContent().build();
    }

}
