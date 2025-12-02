package com.klp.user.presentation;

import com.klp.user.application.UserService;
import com.klp.user.presentation.dto.response.DriverDetailResponse;
import com.klp.user.presentation.dto.response.HubDriverListResponse;
import com.klp.user.presentation.dto.response.LogisticsDriverListResponse;
import com.klp.user.presentation.dto.response.UserDetailResponse;
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
public class UserInternalController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserDetailResponse> getUserDetails(
        @PathVariable Long userId
    ) {
        UserDetailResponse response = userService.getUserDetails(userId);
        return ResponseEntity.ok().body(response);
    }


    @GetMapping("/driver/logistics")
    public ResponseEntity<LogisticsDriverListResponse> getDriversByLogistics() {
        LogisticsDriverListResponse response = userService.getDriversByLogistics();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/driver/{hubId}")
    public ResponseEntity<HubDriverListResponse> getDriversByHubId(@PathVariable UUID hubId) {
        HubDriverListResponse response = userService.getDriversByHubId(hubId);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/driver")
    public ResponseEntity<DriverDetailResponse> getDriverById(@RequestParam("id") Long driverId) {
        DriverDetailResponse response = userService.getDriverById(driverId);
        return ResponseEntity.ok().body(response);
    }
}
