package com.klp.user.presentation;

import com.klp.user.application.UserFacade;
import com.klp.user.presentation.dto.request.UserGradeUpdateRequest;
import com.klp.user.presentation.dto.response.UserGradeResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/users/grades")
@RequiredArgsConstructor
public class UserGradeController {

    private final UserFacade userFacade;

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('MASTER') or (hasRole('CUSTOMER') and #userId == authentication.principal.userId)")
    public ResponseEntity<UserGradeResponse> getUserGrade(
        @PathVariable Long userId
    ) {
        UserGradeResponse response = userFacade.getCurrentUserGrade(userId);
        return ResponseEntity.ok().body(response);
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("hasRole('MASTER')")
    public ResponseEntity<UserGradeResponse> updateUserGrade(
        @PathVariable Long userId,
        @Valid @RequestBody UserGradeUpdateRequest request
    ) {
        UserGradeResponse response = userFacade.updateUserGrade(userId, request.gradeName());
        return ResponseEntity.ok().body(response);
    }
}
