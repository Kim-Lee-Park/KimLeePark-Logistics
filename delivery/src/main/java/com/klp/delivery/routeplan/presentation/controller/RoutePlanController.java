package com.klp.delivery.routeplan.presentation.controller;

import com.klp.delivery.routeplan.application.service.RoutePlanService;
import com.klp.delivery.routeplan.presentation.dto.request.CreateRoutePlanRequest;
import com.klp.delivery.routeplan.presentation.dto.response.CreateRoutePlanResponse;
import com.klp.delivery.routeplan.presentation.dto.response.GetRoutePlanDetailResponse;
import com.klp.delivery.routeplan.presentation.dto.response.GetRoutePlanListResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/routes/plans")
public class RoutePlanController {
    private final RoutePlanService routePlanService;

    //경로 계획 생성
    @PostMapping("")
    public ResponseEntity<CreateRoutePlanResponse> createRoutePlan(
        @Valid @RequestBody CreateRoutePlanRequest request){
        CreateRoutePlanResponse response = routePlanService.createRoutePlan(request.toCommand());
        URI location=URI.create("/v1/routes/plans/"+response.routePlanId());
        return ResponseEntity.created(location).body(response);
    }

    //출발 ID, 도착 ID로 조회
    @GetMapping("/{departureId}/{arrivalId}")
    public ResponseEntity<GetRoutePlanDetailResponse> getRoutePlan(@PathVariable UUID departureId, @PathVariable UUID arrivalId){
        return ResponseEntity.ok(routePlanService.getRoutePlan(departureId, arrivalId));
    }

    //경로 계획 ID로 조회
    @GetMapping("/{routePlanId}")
    public ResponseEntity<GetRoutePlanDetailResponse> getRoutePlan(@PathVariable UUID routePlanId){
        return ResponseEntity.ok(routePlanService.getRoutePlan(routePlanId));
    }

    //경로 계획 목록 조회
    @GetMapping("")
    public ResponseEntity<GetRoutePlanListResponse> getRoutePlans(
        @RequestParam(required = false) UUID depId,
        @RequestParam(required = false) UUID arrId,
        Pageable pageable
    ){
        return ResponseEntity.ok(routePlanService.getRoutePlans(depId, arrId, pageable));
    }
}
