package com.klp.promotion.grade.presentation.controller.docs;

import com.klp.promotion.global.security.model.UserDetailsImpl;
import com.klp.promotion.grade.presentation.dto.request.CreateGradeRequest;
import com.klp.promotion.grade.presentation.dto.request.UpdateGradeRequest;
import com.klp.promotion.grade.presentation.dto.response.GradeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Grade API", description = "등급 관리 API")
public interface GradeControllerDoc {

    @Operation(summary = "등급 생성", description = "MASTER 권한으로 새로운 등급을 생성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "등급 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    ResponseEntity<GradeResponse> createGrade(
        @Parameter(description = "등급 생성 요청 정보", required = true)
        @Valid @RequestBody CreateGradeRequest request
    );

    @Operation(summary = "등급 단건 조회", description = "MASTER 권한으로 특정 등급을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "등급을 찾을 수 없음")
    })
    ResponseEntity<GradeResponse> getGrade(
        @Parameter(description = "등급 ID", required = true)
        @PathVariable UUID gradeId
    );

    @Operation(summary = "등급 목록 조회", description = "MASTER 권한으로 모든 등급을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<List<GradeResponse>> getGrades();

    @Operation(summary = "등급 수정", description = "MASTER 권한으로 등급 정보를 수정합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "404", description = "등급을 찾을 수 없음")
    })
    ResponseEntity<GradeResponse> updateGrade(
        @Parameter(description = "등급 ID", required = true)
        @PathVariable UUID gradeId,
        @Parameter(description = "등급 수정 요청 정보", required = true)
        @Valid @RequestBody UpdateGradeRequest request
    );

    @Operation(summary = "등급 삭제", description = "MASTER 권한으로 등급을 삭제합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "삭제 성공"),
        @ApiResponse(responseCode = "404", description = "등급을 찾을 수 없음")
    })
    ResponseEntity<Void> deleteGrade(
        @Parameter(description = "등급 ID", required = true)
        @PathVariable UUID gradeId,
        @Parameter(hidden = true)
        @AuthenticationPrincipal UserDetailsImpl userDetails
    );
}
