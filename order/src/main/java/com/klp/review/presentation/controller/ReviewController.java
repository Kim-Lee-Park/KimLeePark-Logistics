package com.klp.review.presentation.controller;

import com.klp.common.PageResponse;
import com.klp.global.model.UserDetailsImpl;
import com.klp.review.application.service.ReviewService;
import com.klp.review.domain.entity.Review;
import com.klp.review.presentation.docs.ReviewControllerDoc;
import com.klp.review.presentation.dto.request.CreateReviewRequest;
import com.klp.review.presentation.dto.request.UpdateReviewRequest;
import com.klp.review.presentation.dto.response.CreateReviewResponse;
import com.klp.review.presentation.dto.response.DeleteReviewResponse;
import com.klp.review.presentation.dto.response.GetReviewResponse;
import com.klp.review.presentation.dto.response.UpdateReviewResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/reviews")
@RequiredArgsConstructor
public class ReviewController implements ReviewControllerDoc {

    private final ReviewService reviewService;

    @Override
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CreateReviewResponse> createReview(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @Valid @RequestBody CreateReviewRequest request
    ) {
        Review review = reviewService.createReview(request.toCommand(userDetails.getUserId()));
        CreateReviewResponse response = CreateReviewResponse.from(review);

        return ResponseEntity
            .created(URI.create("/v1/reviews/" + response.reviewId()))
            .body(response);
    }

    @Override
    @GetMapping("/{reviewId}")
    public ResponseEntity<GetReviewResponse> getReview(
        @PathVariable UUID reviewId
    ) {
        Review review = reviewService.getReview(reviewId);
        GetReviewResponse response = GetReviewResponse.from(review);

        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/products/{productId}")
    public ResponseEntity<PageResponse<GetReviewResponse>> getProductReviews(
        @PathVariable UUID productId,
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable
    ) {
        Page<Review> reviews = reviewService.getReviewsByProductId(productId, pageable);
        Page<GetReviewResponse> responsePage = reviews.map(GetReviewResponse::from);
        PageResponse<GetReviewResponse> response = PageResponse.of(
            responsePage.getContent(),
            responsePage
        );

        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PageResponse<GetReviewResponse>> getMyReviews(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable
    ) {
        Page<Review> reviews = reviewService.getReviewsByUserId(userDetails.getUserId(), pageable);
        Page<GetReviewResponse> responsePage = reviews.map(GetReviewResponse::from);
        PageResponse<GetReviewResponse> response = PageResponse.of(
            responsePage.getContent(),
            responsePage
        );

        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<GetReviewResponse> getOrderReview(
        @PathVariable UUID orderId
    ) {
        Review review = reviewService.getReviewByOrderId(orderId);
        GetReviewResponse response = GetReviewResponse.from(review);

        return ResponseEntity.ok(response);
    }

    @Override
    @PatchMapping("/{reviewId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<UpdateReviewResponse> updateReview(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable UUID reviewId,
        @Valid @RequestBody UpdateReviewRequest request
    ) {
        Review review = reviewService.updateReview(reviewId, userDetails.getUserId(),
            request.toCommand());
        UpdateReviewResponse response = UpdateReviewResponse.from(review);

        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'MASTER')")
    public ResponseEntity<DeleteReviewResponse> deleteReview(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable UUID reviewId
    ) {
        Review review = reviewService.deleteReview(reviewId, userDetails.getUserId(),
            userDetails.getRole());
        DeleteReviewResponse response = DeleteReviewResponse.from(review);

        return ResponseEntity.ok(response);
    }
}
