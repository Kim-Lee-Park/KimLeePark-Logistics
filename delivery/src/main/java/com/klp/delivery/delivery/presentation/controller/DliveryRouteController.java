package com.klp.delivery.delivery.presentation.controller;

import com.klp.delivery.delivery.application.facade.DeliveryRouteFacade;
import com.klp.delivery.delivery.application.service.DeliveryRouteService;
import com.klp.delivery.delivery.domain.entity.DeliveryRoute;
import com.klp.delivery.delivery.presentation.dto.DeliveryRouteDetailResponse;
import com.klp.delivery.delivery.presentation.dto.DeliveryRouteResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/deliveries")
@RequiredArgsConstructor
public class DliveryRouteController {

    private final DeliveryRouteFacade  deliveryRouteFacade;
    private final DeliveryRouteService deliveryRouteService;


    @PostMapping("/{deliveryId}/routes")
    public ResponseEntity<DeliveryRouteResponse> appendDeliveryRoute(@PathVariable UUID deliveryId) {

        log.info("배송 경로 생성 요청: deliveryId={}", deliveryId);

        DeliveryRouteResponse response = deliveryRouteFacade.appendDeliveryRoute(deliveryId);

        log.info("배송 경로 생성 성공: deliveryId={}, routeId={}", response.deliveryId(), response.routeId());
        return ResponseEntity.ok().body(response);
    }


    @GetMapping("/{deliveryId}/routes")
    public ResponseEntity<List<DeliveryRouteDetailResponse>> findDeliveryRoutes(@PathVariable UUID deliveryId) {

        log.info("배송 경로 조회 요청: deliveryId={}", deliveryId);

        List<DeliveryRoute> routes = deliveryRouteService.findByDeliveryId(deliveryId);

        List<DeliveryRouteDetailResponse> response = routes.stream()
            .map(route -> new DeliveryRouteDetailResponse(
                route.getDeliveryRouteId(),
                route.getDeliveryId(),
                route.getDriverId(),
                route.getDepartureHubId(),
                route.getArrivalHubId(),
                route.getSequence(),
                route.getEstimatedDistance(),
                route.getEstimatedTime(),
                route.getRealDistance(),
                route.getRealTime(),
                route.getStatus()
            ))
            .toList();

        return ResponseEntity.ok().body(response);
    }


}
