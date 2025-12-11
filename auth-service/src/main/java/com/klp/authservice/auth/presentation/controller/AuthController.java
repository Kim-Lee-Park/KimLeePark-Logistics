package com.klp.authservice.auth.presentation.controller;

import com.klp.authservice.auth.application.AuthService;
import com.klp.authservice.auth.exception.AuthErrorCode;
import com.klp.authservice.auth.infrastructure.jwt.RefreshTokenCookieFactory;
import com.klp.authservice.auth.infrastructure.jwt.TokenExtractor;
import com.klp.authservice.auth.infrastructure.jwt.TokenProvider;
import com.klp.authservice.auth.presentation.controller.docs.AuthControllerDoc;
import com.klp.authservice.auth.presentation.dto.request.LoginRequest;
import com.klp.authservice.auth.presentation.dto.request.SignUpRequest;
import com.klp.authservice.auth.presentation.dto.response.LoginResponse;
import com.klp.authservice.auth.presentation.dto.response.ReissueResponse;
import com.klp.authservice.global.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDoc {

    private final AuthService authService;
    private final TokenProvider refreshTokenProvider;

    @PostMapping("/signup")
    public ResponseEntity<Void> signUp(@Valid @RequestBody SignUpRequest request) {
        authService.signUp(request.toCommand());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request.toCommand());

        String refreshToken = refreshTokenProvider.generate(response.userId(), response.username(),
            response.role());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE,
            RefreshTokenCookieFactory.create(refreshToken).toString());

        return ResponseEntity.ok()
            .headers(headers)
            .body(response);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String accessToken = TokenExtractor.extractAccessToken(request)
            .orElseThrow(() -> new BusinessException(AuthErrorCode.TOKEN_NOT_FOUND));
        String refreshToken = TokenExtractor.extractRefreshToken(request)
            .orElse(null);

        authService.logout(accessToken, refreshToken);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, RefreshTokenCookieFactory.invalidate().toString());

        return ResponseEntity.ok()
            .headers(headers)
            .build();
    }

    @PostMapping("/token/reissue")
    public ResponseEntity<ReissueResponse> reissue(HttpServletRequest request) {
        String accessToken = TokenExtractor.extractAccessToken(request)
            .orElse(null);
        String refreshToken = TokenExtractor.extractRefreshToken(request)
            .orElseThrow(() -> new BusinessException(AuthErrorCode.TOKEN_NOT_FOUND));

        ReissueResponse response = authService.reissue(accessToken, refreshToken);

        String newRefreshToken = refreshTokenProvider.generate(response.userId(),
            response.username(), response.role());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE,
            RefreshTokenCookieFactory.create(newRefreshToken).toString());

        return ResponseEntity.ok()
            .headers(headers)
            .body(response);
    }

    @PostMapping
    public String test() {
        return "test v1";
    }
}
