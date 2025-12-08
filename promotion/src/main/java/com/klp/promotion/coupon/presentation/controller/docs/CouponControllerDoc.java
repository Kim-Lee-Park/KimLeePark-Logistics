package com.klp.promotion.coupon.presentation.controller.docs;

import com.klp.promotion.coupon.presentation.dto.CouponDetailResponse;
import com.klp.promotion.coupon.presentation.dto.CouponResponse;
import com.klp.promotion.coupon.presentation.dto.CreateCouponRequest;
import com.klp.promotion.coupon.presentation.dto.UpdateCouponRequest;
import com.klp.promotion.global.security.model.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Coupon API", description = "쿠폰 관리 API")
public interface CouponControllerDoc {

    @Operation(
        summary = "쿠폰 생성",
        description = """
            CUSTOMER 권한으로 새로운 쿠폰을 생성합니다.
            
            ## 할인 타입
            - **FIXED**: 정액 할인 (예: 1000원 할인)
            - **RATE**: 정률 할인 (예: 10% 할인)
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "쿠폰 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    ResponseEntity<CouponResponse> createCoupon(@RequestBody @Valid CreateCouponRequest request);

    @Operation(summary = "쿠폰 단건 조회", description = "쿠폰 ID로 쿠폰 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "쿠폰을 찾을 수 없음")
    })
    ResponseEntity<CouponDetailResponse> getCoupon(
        @Parameter(description = "쿠폰 ID", required = true)
        @PathVariable UUID couponId
    );

    @Operation(summary = "쿠폰 수정", description = "MASTER 권한으로 쿠폰 정보를 수정합니다. 이름과 만료일만 수정 가능합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "수정 성공"),
        @ApiResponse(responseCode = "404", description = "쿠폰을 찾을 수 없음")
    })
    ResponseEntity<Void> updateCoupon(
        @Parameter(description = "쿠폰 ID", required = true)
        @PathVariable UUID couponId,
        @RequestBody UpdateCouponRequest request
    );

    @Operation(summary = "쿠폰 삭제", description = "CUSTOMER 권한으로 본인이 생성한 쿠폰을 삭제합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(responseCode = "404", description = "쿠폰을 찾을 수 없음")
    })
    ResponseEntity<Void> deleteCoupon(
        @Parameter(description = "쿠폰 ID", required = true)
        @PathVariable UUID couponId,
        @Parameter(hidden = true)
        @AuthenticationPrincipal UserDetailsImpl userDetails
    );

    @Operation(summary = "쿠폰 목록 조회", description = "MASTER 권한으로 전체 쿠폰 목록을 페이징 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<Page<CouponDetailResponse>> getCoupons(
        @Parameter(description = "페이지네이션 정보")
        Pageable pageable
    );
}
