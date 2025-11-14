package com.klp.delivery.routeplan.presentation.controller;

import com.klp.delivery.routeplan.application.service.RoutePlanService;
import com.klp.delivery.routeplan.presentation.dto.request.CreateRoutePlanRequest;
import com.klp.delivery.routeplan.presentation.dto.response.CreateRoutePlanResponse;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/routes")
public class RoutePlanController {
    private final RoutePlanService routePlanService;

    //경로 계획 생성
    @PostMapping("/plans")
    public ResponseEntity<CreateRoutePlanResponse> createRoutePlan(
        @Valid @RequestBody CreateRoutePlanRequest request){
        CreateRoutePlanResponse response = routePlanService.createRoutePlan(request.toCommand());
        URI location=URI.create("/v1/routes/plans/"+response.routePlanId());
        return ResponseEntity.created(location).body(response);
    }

}
