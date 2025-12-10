package com.klp.review.presentation.docs;

import com.klp.common.PageResponse;
import com.klp.global.model.UserDetailsImpl;
import com.klp.review.presentation.dto.request.CreateReviewRequest;
import com.klp.review.presentation.dto.request.UpdateReviewRequest;
import com.klp.review.presentation.dto.response.CreateReviewResponse;
import com.klp.review.presentation.dto.response.DeleteReviewResponse;
import com.klp.review.presentation.dto.response.GetReviewResponse;
import com.klp.review.presentation.dto.response.UpdateReviewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Review API", description = "리뷰 관련 API")
public interface ReviewControllerDoc {

    @Operation(summary = "리뷰 생성", description = "완료된 주문에 대해 리뷰를 작성합니다.")
    ResponseEntity<CreateReviewResponse> createReview(
        @Parameter(hidden = true)
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @Parameter(description = "리뷰 생성 요청 정보", required = true)
        @Valid @RequestBody CreateReviewRequest request
    );

    @Operation(summary = "리뷰 조회 (단건)", description = "특정 리뷰의 상세 정보를 조회합니다.")
    ResponseEntity<GetReviewResponse> getReview(
        @Parameter(description = "리뷰 ID", required = true)
        @PathVariable UUID reviewId
    );

    @Operation(summary = "상품 리뷰 목록 조회", description = "특정 상품에 대한 리뷰 목록을 조회합니다.")
    ResponseEntity<PageResponse<GetReviewResponse>> getProductReviews(
        @Parameter(description = "상품 ID", required = true)
        @PathVariable UUID productId,
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable
    );

    @Operation(summary = "내가 작성한 리뷰 목록 조회", description = "로그인한 사용자가 작성한 리뷰 목록을 조회합니다.")
    ResponseEntity<PageResponse<GetReviewResponse>> getMyReviews(
        @Parameter(hidden = true)
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable
    );

    @Operation(summary = "주문 리뷰 상세 조회", description = "특정 주문에 대한 리뷰를 조회합니다.")
    ResponseEntity<GetReviewResponse> getOrderReview(
        @Parameter(description = "주문 ID", required = true)
        @PathVariable UUID orderId
    );

    @Operation(summary = "리뷰 수정", description = "자신이 작성한 리뷰를 수정합니다.")
    ResponseEntity<UpdateReviewResponse> updateReview(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @Parameter(description = "리뷰 ID", required = true)
        @PathVariable UUID reviewId,
        @Valid @RequestBody UpdateReviewRequest request
    );

    @Operation(summary = "리뷰 삭제", description = "리뷰를 삭제합니다 (Soft Delete).")
    ResponseEntity<DeleteReviewResponse> deleteReview(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @Parameter(description = "리뷰 ID", required = true)
        @PathVariable UUID reviewId
    );
}
