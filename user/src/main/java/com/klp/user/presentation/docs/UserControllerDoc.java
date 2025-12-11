package com.klp.user.presentation.docs;

import com.klp.common.model.PageResponse;
import com.klp.global.security.model.UserDetailsImpl;
import com.klp.user.presentation.dto.request.UserCreateRequest;
import com.klp.user.presentation.dto.request.UserUpdateRequest;
import com.klp.user.presentation.dto.request.ValidateUserRequest;
import com.klp.user.presentation.dto.response.UserDataResponse;
import com.klp.user.presentation.dto.response.UserDetailResponse;
import com.klp.user.presentation.dto.response.UserInfoResponse;
import com.klp.user.presentation.dto.response.UsernameCheckResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "User API", description = "사용자 관리 API")
public interface UserControllerDoc {

    @Operation(summary = "사용자명 중복 확인", description = "사용자명의 사용 가능 여부를 확인합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<UsernameCheckResponse> checkUsername(
        @Parameter(description = "확인할 사용자명", required = true)
        @RequestParam String username
    );

    @Operation(summary = "대기 사용자 생성", description = "승인 대기 상태의 사용자를 생성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    ResponseEntity<Void> createPendingUser(
        @Parameter(description = "사용자 생성 요청 정보", required = true)
        @RequestBody UserCreateRequest request
    );

    @Operation(summary = "사용자 인증 정보 검증", description = "사용자 인증 정보를 검증합니다. (내부 서비스용)")
    ResponseEntity<UserDataResponse> validateCredentials(
        @RequestBody ValidateUserRequest request,
        @RequestHeader(value = "traceparent", required = false) String traceparent
    );

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    ResponseEntity<UserDetailResponse> getMyDetails(
        @Parameter(hidden = true)
        @AuthenticationPrincipal UserDetailsImpl userDetails
    );

    @Operation(summary = "사용자 목록 조회", description = "MASTER 권한으로 사용자 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    ResponseEntity<PageResponse<UserInfoResponse>> getUserList(
        @Parameter(description = "검색 키워드")
        @RequestParam(required = false) String keyword,
        @Parameter(description = "페이지네이션 정보")
        Pageable pageable
    );

    @Operation(summary = "사용자 상세 조회", description = "MASTER 권한으로 특정 사용자의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    ResponseEntity<UserDetailResponse> getUserDetails(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable Long userId
    );

    @Operation(summary = "사용자 정보 수정", description = "MASTER 권한으로 사용자 정보를 수정합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    ResponseEntity<Void> updateUserInfo(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable Long userId,
        @Parameter(description = "수정할 정보", required = true)
        @Valid @RequestBody UserUpdateRequest request
    );

    @Operation(summary = "대기 사용자 승인", description = "MASTER 권한으로 대기 중인 사용자를 승인합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "승인 성공"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    ResponseEntity<Void> approvePendingUser(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable Long userId
    );

    @Operation(summary = "대기 사용자 거절", description = "MASTER 권한으로 대기 중인 사용자를 거절합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "거절 성공"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    ResponseEntity<Void> rejectPendingUser(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable Long userId
    );
}
