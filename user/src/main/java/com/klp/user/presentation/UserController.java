package com.klp.user.presentation;

import com.klp.common.model.PageResponse;
import com.klp.global.security.model.UserDetailsImpl;
import com.klp.user.application.UserService;
import com.klp.user.presentation.dto.request.UserCreateRequest;
import com.klp.user.presentation.dto.request.UserUpdateRequest;
import com.klp.user.presentation.dto.request.ValidateUserRequest;
import com.klp.user.presentation.dto.response.UserDataResponse;
import com.klp.user.presentation.dto.response.UserDetailResponse;
import com.klp.user.presentation.dto.response.UserInfoResponse;
import com.klp.user.presentation.dto.response.UsernameCheckResponse;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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

    @PostMapping("/pending")
    public ResponseEntity<Void> createPendingUser(@RequestBody UserCreateRequest request) {
        Long userId = userService.createPendingUser(request);

        URI location = URI.create("/v1/users/" + userId);

        return ResponseEntity.created(location).build();
    }

    @PostMapping("/validate-credentials")
    public ResponseEntity<UserDataResponse> validateCredentials(@RequestBody ValidateUserRequest request) {
        return ResponseEntity.ok().body(userService.getUserByUsername(request.toCommand()));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDetailResponse> getMyDetails(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserDetailResponse response = userService.getMyDetails(userDetails.getUserId());
        return ResponseEntity.ok().body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<PageResponse<UserInfoResponse>> getUserList(
        @RequestParam(required = false) String keyword,
        Pageable pageable
    ) {
        PageResponse<UserInfoResponse> response = userService.getUserList(keyword, pageable);

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<UserDetailResponse> getUserDetails(
        @PathVariable Long userId
    ) {
        UserDetailResponse response = userService.getUserDetails(userId);
        return ResponseEntity.ok().body(response);
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<Void> updateUserInfo(
        @PathVariable Long userId,
        @Valid @RequestBody UserUpdateRequest request
    ) {
        userService.updateUserInfo(userId, request);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/approve/{userId}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<Void> approvePendingUser(
        @PathVariable Long userId
    ) {
        userService.approvePendingUser(userId);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/reject/{userId}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<Void> rejectPendingUser(
        @PathVariable Long userId
    ) {
        userService.rejectPendingUser(userId);

        return ResponseEntity.ok().build();
    }
}