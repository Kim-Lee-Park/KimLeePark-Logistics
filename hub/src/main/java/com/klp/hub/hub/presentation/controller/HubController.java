package com.klp.hub.hub.presentation.controller;

import com.klp.hub.common.model.UserDetailsImpl;
import com.klp.hub.hub.application.facade.HubFacade;
import com.klp.hub.hub.application.service.HubService;
import com.klp.hub.hub.presentation.dto.request.hub.RegisterHubRequest;
import com.klp.hub.hub.presentation.dto.request.hub.UpdateHubRequest;
import com.klp.hub.hub.presentation.dto.response.GetHubByNameResponse;
import com.klp.hub.hub.presentation.dto.response.hub.GetHubDetailResponse;
import com.klp.hub.hub.presentation.dto.response.hub.GetHubListResponse;
import com.klp.hub.hub.presentation.dto.response.hub.RegisterHubResponse;
import com.klp.hub.hub.presentation.dto.response.hub.UpdatedHubResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/v1/hubs")
@RequiredArgsConstructor
public class HubController {

    private final HubFacade hubFacade;
    private final HubService hubService;

    //허브 등록
    @PostMapping("")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<RegisterHubResponse> registerHub(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @Valid @RequestBody RegisterHubRequest request
    ) {
        RegisterHubResponse response = hubService.registerHub(request.toCommand(), userDetails);
        URI uri = URI.create("/v1/hubs/" + response.hubId());
        return ResponseEntity.created(uri).body(response);
    }

    //허브 단일 조회
    @GetMapping("/{hubId}")
    public ResponseEntity<GetHubDetailResponse> getHubDetail(@PathVariable UUID hubId) {
        return ResponseEntity.ok().body(hubService.getHubDetail(hubId));
    }

    //허브 목록 조회
    @GetMapping("")
    public ResponseEntity<GetHubListResponse> getHubs(Pageable pageable) {
        return ResponseEntity.ok().body(hubService.getHubs(pageable));
    }

    //허브 수정
    @PatchMapping("/{hubId}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<UpdatedHubResponse> updateHub(@PathVariable UUID hubId,
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestBody UpdateHubRequest request) {
        UpdatedHubResponse response = hubService.updateHub(hubId, request.toCommand(), userDetails);
        return ResponseEntity.ok().body(response);
    }

    //허브 삭제
    @DeleteMapping("/{hubId}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<Void> deleteHub(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable UUID hubId) {
        hubFacade.markPendingDelete(hubId, userDetails);
        return ResponseEntity.ok().build();
    }

    //허브 이름으로 조회
    @GetMapping("/by-name")
    public ResponseEntity<GetHubByNameResponse> getHubByName(@RequestParam("name") String hubName) {
        return ResponseEntity.ok().body(hubService.getHubByName(hubName));
    }
}
