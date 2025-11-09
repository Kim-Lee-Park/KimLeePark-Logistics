package com.klp.hub.hub.presentation.controller;

import com.klp.hub.hub.application.service.HubRouteInfoService;
import com.klp.hub.hub.presentation.dto.request.hubrouteinfo.RegisterHubRouteInfoRequest;
import com.klp.hub.hub.presentation.dto.request.hubrouteinfo.UpdateHubRouteInfoRequest;
import com.klp.hub.hub.presentation.dto.response.hubrouteinfo.GetHubRouteInfoDetailResponse;
import com.klp.hub.hub.presentation.dto.response.hubrouteinfo.GetHubRouteInfoListResponse;
import com.klp.hub.hub.presentation.dto.response.hubrouteinfo.RegisterHubRouteInfoResponse;
import com.klp.hub.hub.presentation.dto.response.hubrouteinfo.UpdatedHubRouteInfoResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/hubs/routes/info")
public class HubRouteInfoController {
    private final HubRouteInfoService hubRouteInfoService;

    //허브간 이동 정보 생성
    @PostMapping("")
    public ResponseEntity<RegisterHubRouteInfoResponse> registerHubRouteInfo(
        @Valid @RequestBody RegisterHubRouteInfoRequest request
    ){
        RegisterHubRouteInfoResponse response=hubRouteInfoService.registerHubRouteInfo(request.toCommand());
        URI location=URI.create("/v1/hubs/routes/info/"+response.hubRouteInfoId());
        return ResponseEntity.created(location).body(response);
    }

    //허브간 이동 정보 단일 조회
    @GetMapping("/{routeInfoId}")
    public ResponseEntity<GetHubRouteInfoDetailResponse> getHubRouteInfoDetail(@PathVariable UUID routeInfoId){
        return ResponseEntity.ok(hubRouteInfoService.getHubRouteInfoDetail(routeInfoId));
    }

    //허브간 이동 정보 목록 조회
    @GetMapping("")
    public ResponseEntity<GetHubRouteInfoListResponse> getHubRouteInfos(
        @RequestParam(required = false) UUID departureId,
        @RequestParam(required = false) UUID arrivalId,
        Pageable pageable){
        return ResponseEntity.ok(hubRouteInfoService.getHubRouteInfos(departureId,arrivalId,pageable));
    }

    @PatchMapping("/{routeInfoId}")
    public ResponseEntity<UpdatedHubRouteInfoResponse> updateHubRouteInfo(@PathVariable UUID routeInfoId,
        @RequestBody UpdateHubRouteInfoRequest request){
        return ResponseEntity.ok(hubRouteInfoService.updateHubRouteInfo(routeInfoId, request.toCommand()));
    }

    @DeleteMapping("/{routeInfoId}")
    public ResponseEntity<Void> deleteHub(@PathVariable UUID routeInfoId){
        return ResponseEntity.ok().build();
    }
}
