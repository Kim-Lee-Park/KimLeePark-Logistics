package com.klp.hub.hub.presentation.controller;

import com.klp.hub.hub.application.facade.HubFacade;
import com.klp.hub.hub.presentation.dto.request.hub.NearestHubRequest;
import com.klp.hub.hub.presentation.dto.response.NearestHubResponse;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/internal/hubs")
@Hidden
public class HubInternalController {

    HubFacade hubFacade;

    @PostMapping("/nearest")
    public ResponseEntity<NearestHubResponse> getNearestHub(
        @Valid @RequestBody NearestHubRequest request) {
        return ResponseEntity.ok().body(hubFacade.getNearestHub(request.toCommand()));
    }
}
