package com.klp.user.presentation;

import com.klp.user.application.UserFacade;
import com.klp.user.presentation.dto.request.UserAddressCreateRequest;
import com.klp.user.presentation.dto.response.UserAddressListResponse;
import com.klp.user.presentation.dto.response.UserAddressResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/users/address")
@RequiredArgsConstructor
public class UserAddressController {

    private final UserFacade userFacade;

    @GetMapping("/{userId}/{addressId}")
    @PreAuthorize("hasRole('MASTER') or (hasRole('CUSTOMER') and #userId == authentication.principal.userId)")
    public ResponseEntity<UserAddressResponse> getUserAddress(
        @PathVariable(name = "userId") Long userId,
        @PathVariable(name = "addressId") UUID addressId
    ) {
        UserAddressResponse response = userFacade.getUserAddress(addressId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('MASTER') or (hasRole('CUSTOMER') and #userId == authentication.principal.userId)")
    public ResponseEntity<UserAddressListResponse> getUserAddressList(
        @PathVariable(name = "userId") Long userId
    ) {
        UserAddressListResponse response = userFacade.getUserAddressList(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}")
    @PreAuthorize("hasRole('MASTER') or (hasRole('CUSTOMER') and #userId == authentication.principal.userId)")
    public ResponseEntity<Void> createUserAddress(
        @PathVariable(name = "userId") Long userId,
        @Valid @RequestBody UserAddressCreateRequest request
    ) {
        userFacade.createUserAddress(request.toCommand(userId));
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{userId}/{addressId}")
    @PreAuthorize("hasRole('MASTER') or (hasRole('CUSTOMER') and #userId == authentication.principal.userId)")
    public ResponseEntity<Void> updateUserAddress(
        @PathVariable(name = "userId") Long userId,
        @PathVariable(name = "addressId") UUID addressId,
        @Valid @RequestBody UserAddressCreateRequest request
    ) {
        userFacade.updateUserAddress(addressId, request.toCommand(userId));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/{addressId}")
    @PreAuthorize("hasRole('MASTER') or (hasRole('CUSTOMER') and #userId == authentication.principal.userId)")
    public ResponseEntity<Void> deleteUserAddress(
        @PathVariable(name = "userId") Long userId,
        @PathVariable(name = "addressId") UUID addressId
    ) {
        userFacade.deleteUserAddress(addressId, userId);
        return ResponseEntity.ok().build();
    }
}
