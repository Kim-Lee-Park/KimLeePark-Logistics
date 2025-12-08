package com.klp.user.presentation.docs;

import com.klp.user.presentation.dto.request.UserGradeUpdateRequest;
import com.klp.user.presentation.dto.response.UserGradeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "User Grade API", description = "사용자 등급 관리 API")
public interface UserGradeControllerDoc {

    @Operation(summary = "사용자 등급 조회", description = "특정 사용자의 현재 등급을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    ResponseEntity<UserGradeResponse> getUserGrade(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable Long userId
    );

    @Operation(summary = "사용자 등급 수정", description = "MASTER 권한으로 사용자의 등급을 수정합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    ResponseEntity<UserGradeResponse> updateUserGrade(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable Long userId,
        @Parameter(description = "등급 수정 정보", required = true)
        @Valid @RequestBody UserGradeUpdateRequest request
    );
}
