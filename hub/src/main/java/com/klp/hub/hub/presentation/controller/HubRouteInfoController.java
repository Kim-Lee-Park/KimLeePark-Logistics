package com.klp.hub.hub.presentation.controller;

import com.klp.hub.hub.presentation.dto.request.hubrouteinfo.RegisterHubRouteInfoRequest;
import com.klp.hub.hub.presentation.dto.request.hubrouteinfo.UpdateHubRouteInfoRequest;
import com.klp.hub.hub.presentation.dto.response.hubrouteinfo.GetHubRouteInfoDetailResponse;
import com.klp.hub.hub.presentation.dto.response.hubrouteinfo.GetHubRouteInfoListResponse;
import com.klp.hub.hub.presentation.dto.response.hubrouteinfo.RegisterHubRouteInfoResponse;
import com.klp.hub.hub.presentation.dto.response.hubrouteinfo.UpdatedHubRouteInfoResponse;
import java.awt.print.Pageable;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hubs/routes/info")
public class HubRouteInfoController {

    @PostMapping("")
    public ResponseEntity<RegisterHubRouteInfoResponse> registerHubRouteInfo(
        @RequestBody RegisterHubRouteInfoRequest request
    ){
        return null;
    }

    @GetMapping("/{hubId}")
    public ResponseEntity<GetHubRouteInfoDetailResponse> getHubRouteInfoDetail(@PathVariable UUID hubId){
        return null;
    }

    @GetMapping("")
    public ResponseEntity<GetHubRouteInfoListResponse> getHubRouteInfos(Pageable pageable){
        return null;
    }

    @PatchMapping("/{hubId}")
    public ResponseEntity<UpdatedHubRouteInfoResponse> updateHubRouteInfo(@PathVariable UUID hubId,
        @RequestBody UpdateHubRouteInfoRequest request){
        return null;
    }

    @DeleteMapping("/{hubId}")
    public ResponseEntity<Void> deleteHub(@PathVariable UUID hubId){
        return ResponseEntity.ok().build();
    }
}
