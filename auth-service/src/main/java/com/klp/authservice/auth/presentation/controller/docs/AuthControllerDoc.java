package com.klp.authservice.auth.presentation.controller.docs;

import com.klp.authservice.auth.presentation.dto.request.LoginRequest;
import com.klp.authservice.auth.presentation.dto.request.SignUpRequest;
import com.klp.authservice.auth.presentation.dto.response.LoginResponse;
import com.klp.authservice.auth.presentation.dto.response.ReissueResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Auth API", description = "인증 관련 API")
public interface AuthControllerDoc {

    @Operation(
        summary = "회원가입",
        description = """
            새로운 사용자를 등록합니다. 새로운 사용자는 대기 상태가 되어, MASTER가 승인을 진행해주어야 합니다.
            
            ## 권한별 가입 방법
            
            | 권한 | role 값 | affiliationType | affiliationName | 비고 |
            |------|---------|-----------------|-----------------|------|
            | MASTER | MASTER | LOGISTICS | 물류 회사명 | 시스템 최고 관리자 (DB 직접 생성 권장) |
            | HUB | HUB | HUB | 허브명 | 허브 관리자 |
            | COMPANY | COMPANY | COMPANY | 업체명 | 업체 담당자 |
            | DRIVER | DRIVER | HUB 또는 LOGISTICS | 소속 허브명/회사명 | 배송 담당자 |
            | CUSTOMER | CUSTOMER | COMPANY | 업체명 | 일반 고객 |
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "회원가입 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검사 실패)"),
        @ApiResponse(responseCode = "409", description = "이미 존재하는 사용자명")
    })
    ResponseEntity<Void> signUp(@Valid @RequestBody SignUpRequest request);

    @Operation(
        summary = "로그인",
        description = """
            이메일, 패스워드를 이용하여 사용자 인증 후 JWT 토큰을 발급합니다.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그인 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패 (잘못된 사용자명 또는 비밀번호)")
    })
    ResponseEntity<LoginResponse> login(
        @Parameter(description = "로그인 요청 정보", required = true)
        @Valid @RequestBody LoginRequest request
    );

    @Operation(
        summary = "로그아웃",
        description = """
            현재 사용자의 토큰을 무효화합니다. 사용중인 토큰은 블랙리스트로 등록됩니다.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    ResponseEntity<Void> logout(@Parameter(hidden = true) HttpServletRequest request);

    @Operation(
        summary = "토큰 재발급",
        description = """
            Refresh Token을 사용하여 새로운 Access Token을 발급합니다.
            기존에 사용하던 토큰은 블랙리스트로 등록됩니다.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
        @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
    })
    ResponseEntity<ReissueResponse> reissue(@Parameter(hidden = true) HttpServletRequest request);
}
