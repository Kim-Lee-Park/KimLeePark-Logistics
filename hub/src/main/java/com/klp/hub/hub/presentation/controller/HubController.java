package com.klp.hub.hub.presentation.controller;

import com.klp.hub.hub.presentation.dto.request.hub.UpdateHubRequest;
import com.klp.hub.hub.presentation.dto.request.hub.RegisterHubRequest;
import com.klp.hub.hub.presentation.dto.response.hub.GetHubDetailResponse;
import com.klp.hub.hub.presentation.dto.response.hub.GetHubListResponse;
import com.klp.hub.hub.presentation.dto.response.hub.RegisterHubResponse;
import com.klp.hub.hub.presentation.dto.response.hub.UpdatedHubResponse;
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
@RequestMapping("/hubs")
@RequiredArgsConstructor
public class HubController {

    @PostMapping("")
    public ResponseEntity<RegisterHubResponse> registerHub(
        @RequestBody RegisterHubRequest request
    ){
        return null;
    }

    @GetMapping("/{hubId}")
    public ResponseEntity<GetHubDetailResponse> getHubDetail(@PathVariable UUID hubId){
        return null;
    }

    @GetMapping("")
    public ResponseEntity<GetHubListResponse> getHubs(Pageable pageable){
        return null;
    }

    @PatchMapping("/{hubId}")
    public ResponseEntity<UpdatedHubResponse> updateHub(@PathVariable UUID hubId,
        @RequestBody UpdateHubRequest request){
        return null;
    }

    @DeleteMapping("/{hubId}")
    public ResponseEntity<Void> deleteHub(@PathVariable UUID hubId){
        return ResponseEntity.ok().build();
    }
}
