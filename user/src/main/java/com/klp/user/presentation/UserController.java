package com.klp.user.presentation; // 기존 패키지 유지

import com.klp.common.model.PageResponse;
import com.klp.global.security.model.UserDetailsImpl;
import com.klp.user.application.UserService;
import com.klp.user.presentation.dto.request.UserChangeRequest;
import com.klp.user.presentation.dto.request.UserUpdateRequest;
import com.klp.user.presentation.dto.response.UserChangeResponse;
import com.klp.user.presentation.dto.response.UserDetailResponse;
import com.klp.user.presentation.dto.response.UserInfoResponse;
import com.klp.user.presentation.dto.response.UsernameCheckResponse;
import jakarta.validation.Valid;
import java.awt.print.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/check")
    public ResponseEntity<UsernameCheckResponse> checkUsername(@RequestParam String username) {
        UsernameCheckResponse response = userService.checkUserNameAvailable(username);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDetailResponse> getMyDetails(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserDetailResponse response = userService.getMyDetails(userDetails.getUserId());
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/change-requests")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<PageResponse<UserChangeResponse>> getChangeRequests(Pageable pageable) {
        PageResponse<UserChangeResponse> response = userService.getChangeRequests(pageable);

        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/me/change-request")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> infoChangeRequest(@Valid @RequestBody UserChangeRequest request) {
        userService.infoChangeRequest(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<PageResponse<UserInfoResponse>> getUserList(Pageable pageable) {
        PageResponse<UserInfoResponse> response = userService.getUserList(pageable);

        return ResponseEntity.ok().body(response);
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<?> updateUserInfo(@PathVariable Long userId, @Valid @RequestBody UserUpdateRequest request) {
        userService.updateUserInfo(userId, request);

        return ResponseEntity.ok().build();
    }
}