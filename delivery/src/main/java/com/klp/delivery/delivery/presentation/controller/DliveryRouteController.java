package com.klp.delivery.delivery.presentation.controller;

import com.klp.delivery.delivery.application.facade.DeliveryRouteFacade;
import com.klp.delivery.delivery.presentation.dto.DeliveryRouteResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/deliveries")
@RequiredArgsConstructor
public class DliveryRouteController {

    private DeliveryRouteFacade  deliveryRouteFacade;

    @PostMapping("/{deliveryId}/routes")
    public ResponseEntity<DeliveryRouteResponse> appendDeliveryRoute(@PathVariable UUID deliveryId) {

        log.info("배송 경로 생성 요청: deliveryId={}", deliveryId);

        DeliveryRouteResponse response = deliveryRouteFacade.appendDeliveryRoute(deliveryId);

        log.info("배송 경로 생성 성공: deliveryId={}, routeId={}", response.deliveryId(), response.routeId());
        return ResponseEntity.ok().body(response);
    }


}
