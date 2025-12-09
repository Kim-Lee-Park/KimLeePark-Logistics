package com.klp.user.presentation;

import com.klp.user.application.UserFacade;
import com.klp.user.presentation.dto.response.DriverDetailResponse;
import com.klp.user.presentation.dto.response.HubDriverListResponse;
import com.klp.user.presentation.dto.response.LogisticsDriverListResponse;
import com.klp.user.presentation.dto.response.UserAddressHubResponse;
import com.klp.user.presentation.dto.response.UserDetailResponse;
import io.swagger.v3.oas.annotations.Hidden;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/internal/users")
@RequiredArgsConstructor
@Hidden
public class UserInternalController {

    private final UserFacade userFacade;

    @GetMapping("/{addressId}")
    public ResponseEntity<UserAddressHubResponse> getUserAddressHub(
        @PathVariable UUID addressId
    ) {
        UserAddressHubResponse response = userFacade.getUserAddressHub(addressId);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDetailResponse> getUserDetails(
        @PathVariable Long userId
    ) {
        UserDetailResponse response = userFacade.getUserDetails(userId);
        return ResponseEntity.ok().body(response);
    }


    @GetMapping("/driver/logistics")
    public ResponseEntity<LogisticsDriverListResponse> getDriversByLogistics() {
        LogisticsDriverListResponse response = userFacade.getDriversByLogistics();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/driver/{hubId}")
    public ResponseEntity<HubDriverListResponse> getDriversByHubId(@PathVariable UUID hubId) {
        HubDriverListResponse response = userFacade.getDriversByHubId(hubId);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/driver")
    public ResponseEntity<DriverDetailResponse> getDriverById(@RequestParam("id") Long driverId) {
        DriverDetailResponse response = userFacade.getDriverById(driverId);
        return ResponseEntity.ok().body(response);
    }
}