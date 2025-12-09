package com.klp.review.domain.repository;

import com.klp.review.domain.entity.Review;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewRepository {

    /**
     * 리뷰 저장
     *
     * @param review 저장할 리뷰
     * @return 저장된 리뷰
     */
    Review save(Review review);

    /**
     * 리뷰 ID로 조회 (삭제된 것 포함)
     *
     * @param reviewId 리뷰 ID
     * @return 리뷰 Optional
     */
    Optional<Review> findById(UUID reviewId);

    /**
     * 리뷰 ID로 조회 (삭제되지 않은 것만)
     *
     * @param reviewId 리뷰 ID
     * @return 리뷰 Optional
     */
    Optional<Review> findByIdAndDeletedAtIsNull(UUID reviewId);

    /**
     * 주문 ID로 리뷰 조회 (삭제되지 않은 것만)
     *
     * @param orderId 주문 ID
     * @return 리뷰 Optional
     */
    Optional<Review> findByOrderId(UUID orderId);

    /**
     * 주문 ID로 리뷰 존재 여부 확인 (삭제되지 않은 것만)
     *
     * @param orderId 주문 ID
     * @return 존재 여부
     */
    boolean existsByOrderId(UUID orderId);

    /**
     * 상품 ID로 리뷰 목록 조회 (삭제되지 않은 것만)
     *
     * @param productId 상품 ID
     * @param pageable 페이징 정보
     * @return 리뷰 페이지
     */
    Page<Review> findByProductId(UUID productId, Pageable pageable);

    /**
     * 사용자 ID로 리뷰 목록 조회 (삭제되지 않은 것만)
     *
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 리뷰 페이지
     */
    Page<Review> findByUserId(Long userId, Pageable pageable);

    /**
     * 전체 리뷰 목록 조회 (삭제되지 않은 것만)
     *
     * @param pageable 페이징 정보
     * @return 리뷰 페이지
     */
    Page<Review> findAll(Pageable pageable);
}
