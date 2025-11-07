package com.klp.hub.hub.presentation.controller;

import com.klp.hub.hub.application.service.HubService;
import com.klp.hub.hub.domain.model.Hub;
import com.klp.hub.hub.presentation.dto.request.hub.UpdateHubRequest;
import com.klp.hub.hub.presentation.dto.request.hub.RegisterHubRequest;
import com.klp.hub.hub.presentation.dto.response.hub.GetHubDetailResponse;
import com.klp.hub.hub.presentation.dto.response.hub.GetHubListResponse;
import com.klp.hub.hub.presentation.dto.response.hub.RegisterHubResponse;
import com.klp.hub.hub.presentation.dto.response.hub.UpdatedHubResponse;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/hubs")
@RequiredArgsConstructor
public class HubController {
    private final HubService hubService;

    @PostMapping("")
    public ResponseEntity<RegisterHubResponse> registerHub(
        @RequestBody RegisterHubRequest request
    ){
        RegisterHubResponse response=hubService.registerHub(request.toCommand());
        URI uri=URI.create("/v1/hubs/"+response.hubId());
        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping("/{hubId}")
    public ResponseEntity<GetHubDetailResponse> getHubDetail(@PathVariable UUID hubId){
        return ResponseEntity.ok().body(hubService.getHubDetail(hubId));
    }

    @GetMapping("")
    public ResponseEntity<GetHubListResponse> getHubs(Pageable pageable){
        return ResponseEntity.ok().body(hubService.getHubs(pageable));
    }

    @PatchMapping("/{hubId}")
    public ResponseEntity<UpdatedHubResponse> updateHub(@PathVariable UUID hubId,
        @RequestBody UpdateHubRequest request){
        UpdatedHubResponse response=hubService.updateHub(hubId, request.toCommand());
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{hubId}")
    public ResponseEntity<Void> deleteHub(@PathVariable UUID hubId){
        return ResponseEntity.ok().build();
    }
}
