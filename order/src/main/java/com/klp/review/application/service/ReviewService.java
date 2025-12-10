package com.klp.review.application.service;

import com.klp.global.exception.BusinessException;
import com.klp.global.exception.ReviewErrorCode;
import com.klp.order.application.service.OrderService;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.domain.entity.order.OrderStatus;
import com.klp.review.application.command.CreateReviewCommand;
import com.klp.review.application.command.UpdateReviewCommand;
import com.klp.review.domain.entity.Review;
import com.klp.review.domain.repository.ReviewRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderService orderService;

    /**
     * 리뷰 생성
     */
    @Transactional
    public Review createReview(CreateReviewCommand command) {
        log.info("리뷰 생성 시작 - orderId: {}, userId: {}", command.orderId(), command.userId());

        Order order = orderService.findById(command.orderId());
        if (order.getOrderStatus() != OrderStatus.COMPLETE) {
            log.warn("주문이 완료되지 않음 - orderId: {}, status: {}",
                command.orderId(), order.getOrderStatus());
            throw new BusinessException(ReviewErrorCode.ORDER_NOT_COMPLETE);
        }

        if (reviewRepository.existsByOrderId(command.orderId())) {
            log.warn("이미 리뷰가 존재함 - orderId: {}", command.orderId());
            throw new BusinessException(ReviewErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = Review.create(
            command.orderId(),
            command.productId(),
            command.userId(),
            command.rating(),
            command.content()
        );

        Review savedReview = reviewRepository.save(review);
        log.info("리뷰 생성 완료 - reviewId: {}", savedReview.getReviewId());

        return savedReview;
    }

    /**
     * 리뷰 수정
     */
    @Transactional
    public Review updateReview(UUID reviewId, Long userId, UpdateReviewCommand command) {
        log.info("리뷰 수정 시작 - reviewId: {}, userId: {}", reviewId, userId);

        Review review = reviewRepository.findByIdAndDeletedAtIsNull(reviewId)
            .orElseThrow(() -> {
                log.warn("리뷰를 찾을 수 없음 - reviewId: {}", reviewId);
                return new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND);
            });

        if (!review.canUpdate(userId)) {
            log.warn("리뷰 수정 권한 없음 - reviewId: {}, requestUserId: {}, reviewUserId: {}",
                reviewId, userId, review.getUserId());
            throw new BusinessException(ReviewErrorCode.REVIEW_UPDATE_FORBIDDEN);
        }

        review.update(command.rating(), command.content());
        log.info("리뷰 수정 완료 - reviewId: {}", reviewId);

        return review;
    }

    /**
     * 리뷰 삭제
     */
    @Transactional
    public Review deleteReview(UUID reviewId, Long userId, String role) {
        log.info("리뷰 삭제 시작 - reviewId: {}, userId: {}, role: {}", reviewId, userId, role);

        Review review = reviewRepository.findByIdAndDeletedAtIsNull(reviewId)
            .orElseThrow(() -> {
                log.warn("리뷰를 찾을 수 없음 - reviewId: {}", reviewId);
                return new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND);
            });

        if (!review.canDelete(userId, role)) {
            log.warn("리뷰 삭제 권한 없음 - reviewId: {}, requestUserId: {}, role: {}",
                reviewId, userId, role);
            throw new BusinessException(ReviewErrorCode.REVIEW_DELETE_FORBIDDEN);
        }

        review.delete(userId);
        log.info("리뷰 삭제 완료 - reviewId: {}", reviewId);

        return review;
    }

    /**
     * 리뷰 단건 조회
     */
    @Transactional(readOnly = true)
    public Review getReview(UUID reviewId) {
        log.info("리뷰 조회 - reviewId: {}", reviewId);

        return reviewRepository.findByIdAndDeletedAtIsNull(reviewId)
            .orElseThrow(() -> {
                log.warn("리뷰를 찾을 수 없음 - reviewId: {}", reviewId);
                return new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND);
            });
    }

    /**
     * 상품별 리뷰 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<Review> getReviewsByProductId(UUID productId, Pageable pageable) {
        log.info("상품별 리뷰 목록 조회 - productId: {}", productId);
        return reviewRepository.findByProductId(productId, pageable);
    }

    /**
     * 사용자별 리뷰 목록 조회 (내가 작성한 리뷰)
     */
    @Transactional(readOnly = true)
    public Page<Review> getReviewsByUserId(Long userId, Pageable pageable) {
        log.info("사용자별 리뷰 목록 조회 - userId: {}", userId);
        return reviewRepository.findByUserId(userId, pageable);
    }

    /**
     * 주문별 리뷰 조회 (주문당 리뷰 1개)
     */
    @Transactional(readOnly = true)
    public Review getReviewByOrderId(UUID orderId) {
        log.info("주문별 리뷰 조회 - orderId: {}", orderId);
        return reviewRepository.findByOrderId(orderId)
            .orElseThrow(() -> {
                log.warn("주문에 대한 리뷰를 찾을 수 없음 - orderId: {}", orderId);
                return new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND);
            });
    }
}
