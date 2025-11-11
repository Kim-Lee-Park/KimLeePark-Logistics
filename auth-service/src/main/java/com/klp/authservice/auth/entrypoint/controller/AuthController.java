package com.klp.authservice.auth.entrypoint.controller;

import com.klp.authservice.auth.application.AuthService;
import com.klp.authservice.auth.entrypoint.dto.request.LoginRequest;
import com.klp.authservice.auth.entrypoint.dto.request.SignUpRequest;
import com.klp.authservice.auth.entrypoint.dto.response.LoginResponse;
import com.klp.authservice.auth.entrypoint.dto.response.ReissueResponse;
import com.klp.authservice.auth.infrastructure.jwt.RefreshTokenCookieFactory;
import com.klp.authservice.auth.infrastructure.jwt.TokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenProvider refreshTokenProvider;

    private static final int AUTHORIZATION_PREFIX_LENGTH = 7;

    @PostMapping("/signUp")
    public ResponseEntity<Void> signUp(@Valid @RequestBody SignUpRequest request) {
        authService.signUp(request.toCommand());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request.toCommand());

        String refreshToken = refreshTokenProvider.generate(response.userId(), response.userName(), response.role());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, RefreshTokenCookieFactory.create(refreshToken).toString());

        return ResponseEntity.ok()
            .headers(headers)
            .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization) {
        String accessToken = authorization.substring(AUTHORIZATION_PREFIX_LENGTH);
        authService.logout(accessToken);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, RefreshTokenCookieFactory.invalidate().toString());

        return ResponseEntity.ok()
            .headers(headers)
            .build();
    }

    @PostMapping("/token/reissue")
    public ResponseEntity<ReissueResponse> reissue(HttpServletRequest request) {
        String refreshToken = Arrays.stream(request.getCookies())
            .filter(cookie -> "refreshToken".equals(cookie.getName()))
            .findFirst()
            .map(Cookie::getValue)
            .orElse(null);

        ReissueResponse response = authService.reissue(refreshToken);
        String newRefreshToken = refreshTokenProvider.generate(response.userId(), response.userName(), response.role());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, RefreshTokenCookieFactory.create(newRefreshToken).toString());

        return ResponseEntity.ok()
            .headers(headers)
            .body(response);
    }
}
