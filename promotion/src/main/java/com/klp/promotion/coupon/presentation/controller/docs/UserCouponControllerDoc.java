package com.klp.promotion.coupon.presentation.controller.docs;

import com.klp.promotion.coupon.presentation.dto.IssueUserCouponResponse;
import com.klp.promotion.coupon.presentation.dto.UserCouponDetailResponse;
import com.klp.promotion.global.security.model.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "User Coupon API", description = "사용자 쿠폰 관리 API")
public interface UserCouponControllerDoc {

    @Operation(
        summary = "쿠폰 발급",
        description = """
            CUSTOMER 권한으로 쿠폰을 발급받습니다.
            
            ## 발급 조건
            - 쿠폰의 남은 수량이 있어야 함
            - 쿠폰 유효기간 내여야 함
            - 동일 쿠폰 중복 발급 불가
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "쿠폰 발급 성공"),
        @ApiResponse(responseCode = "400", description = "발급 조건 미충족"),
        @ApiResponse(responseCode = "404", description = "쿠폰을 찾을 수 없음")
    })
    ResponseEntity<IssueUserCouponResponse> issueUserCoupon(
        @Parameter(hidden = true)
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @Parameter(description = "발급받을 쿠폰 ID", required = true)
        @RequestBody UUID couponId
    );

    @Operation(
        summary = "쿠폰 사용",
        description = """
            CUSTOMER 권한으로 보유한 쿠폰을 사용합니다.
            
            ## 사용 조건
            - 본인이 발급받은 쿠폰이어야 함
            - 아직 사용하지 않은 쿠폰이어야 함
            - 쿠폰 유효기간 내여야 함
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "쿠폰 사용 성공"),
        @ApiResponse(responseCode = "400", description = "사용 조건 미충족"),
        @ApiResponse(responseCode = "404", description = "쿠폰을 찾을 수 없음")
    })
    ResponseEntity<Void> useUserCoupon(
        @Parameter(hidden = true)
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @Parameter(description = "사용할 쿠폰 ID", required = true)
        @PathVariable UUID couponId
    );

    @Operation(summary = "사용자 쿠폰 단건 조회", description = "발급받은 쿠폰의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "쿠폰을 찾을 수 없음")
    })
    ResponseEntity<UserCouponDetailResponse> getUserCoupon(
        @Parameter(description = "사용자 쿠폰 ID", required = true)
        @PathVariable UUID userCouponId
    );

    @Operation(summary = "내 쿠폰 목록 조회", description = "CUSTOMER 권한으로 본인이 발급받은 모든 쿠폰을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<List<UserCouponDetailResponse>> getUserCoupons(
        @Parameter(hidden = true)
        @AuthenticationPrincipal UserDetailsImpl userDetails
    );

    @Operation(summary = "사용자 쿠폰 삭제", description = "CUSTOMER 권한으로 본인이 발급받은 쿠폰을 삭제합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(responseCode = "404", description = "쿠폰을 찾을 수 없음")
    })
    ResponseEntity<Void> deleteUserCoupons(
        @Parameter(hidden = true)
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @Parameter(description = "삭제할 사용자 쿠폰 ID", required = true)
        @PathVariable UUID userCouponId
    );
}
