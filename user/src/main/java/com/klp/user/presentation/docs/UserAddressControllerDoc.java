package com.klp.user.presentation.docs;

import com.klp.user.presentation.dto.request.UserAddressCreateRequest;
import com.klp.user.presentation.dto.response.UserAddressListResponse;
import com.klp.user.presentation.dto.response.UserAddressResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "User Address API", description = "사용자 주소 관리 API")
public interface UserAddressControllerDoc {

    @Operation(summary = "사용자 주소 단건 조회", description = "특정 사용자의 주소를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "주소를 찾을 수 없음")
    })
    ResponseEntity<UserAddressResponse> getUserAddress(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable(name = "userId") Long userId,
        @Parameter(description = "주소 ID", required = true)
        @PathVariable(name = "addressId") UUID addressId
    );

    @Operation(summary = "사용자 주소 목록 조회", description = "특정 사용자의 모든 주소를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<UserAddressListResponse> getUserAddressList(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable(name = "userId") Long userId
    );

    @Operation(summary = "사용자 주소 등록", description = "새로운 주소를 등록합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "등록 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    ResponseEntity<Void> createUserAddress(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable(name = "userId") Long userId,
        @Parameter(description = "주소 정보", required = true)
        @Valid @RequestBody UserAddressCreateRequest request
    );

    @Operation(summary = "사용자 주소 수정", description = "기존 주소를 수정합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "404", description = "주소를 찾을 수 없음")
    })
    ResponseEntity<Void> updateUserAddress(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable(name = "userId") Long userId,
        @Parameter(description = "주소 ID", required = true)
        @PathVariable(name = "addressId") UUID addressId,
        @Parameter(description = "수정할 주소 정보", required = true)
        @Valid @RequestBody UserAddressCreateRequest request
    );

    @Operation(summary = "사용자 주소 삭제", description = "주소를 삭제합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "삭제 성공"),
        @ApiResponse(responseCode = "404", description = "주소를 찾을 수 없음")
    })
    ResponseEntity<Void> deleteUserAddress(
        @Parameter(description = "사용자 ID", required = true)
        @PathVariable(name = "userId") Long userId,
        @Parameter(description = "주소 ID", required = true)
        @PathVariable(name = "addressId") UUID addressId
    );
}
