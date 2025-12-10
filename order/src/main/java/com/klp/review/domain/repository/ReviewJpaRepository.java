package com.klp.review.domain.repository;

import com.klp.review.domain.entity.Review;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewJpaRepository extends JpaRepository<Review, UUID> {

    /**
     * 주문 ID로 리뷰 조회
     */
    @Query("SELECT r FROM Review r WHERE r.orderId = :orderId AND r.deletedAt IS NULL")
    Optional<Review> findByOrderIdAndDeletedAtIsNull(@Param("orderId") UUID orderId);

    /**
     * 주문 ID로 리뷰 존재 여부 확인
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
        "FROM Review r WHERE r.orderId = :orderId AND r.deletedAt IS NULL")
    boolean existsByOrderIdAndDeletedAtIsNull(@Param("orderId") UUID orderId);

    /**
     * 리뷰 ID로 조회
     */
    @Query("SELECT r FROM Review r WHERE r.reviewId = :reviewId AND r.deletedAt IS NULL")
    Optional<Review> findByIdAndDeletedAtIsNull(@Param("reviewId") UUID reviewId);

    /**
     * 상품 ID로 리뷰 목록 조회
     */
    @Query("SELECT r FROM Review r WHERE r.productId = :productId AND r.deletedAt IS NULL")
    Page<Review> findByProductIdAndDeletedAtIsNull(
        @Param("productId") UUID productId,
        Pageable pageable
    );

    /**
     * 사용자 ID로 리뷰 목록 조회
     */
    @Query("SELECT r FROM Review r WHERE r.userId = :userId AND r.deletedAt IS NULL")
    Page<Review> findByUserIdAndDeletedAtIsNull(
        @Param("userId") Long userId,
        Pageable pageable
    );

    /**
     * 전체 리뷰 목록 조회
     */
    @Query("SELECT r FROM Review r WHERE r.deletedAt IS NULL")
    Page<Review> findAllAndDeletedAtIsNull(Pageable pageable);
}
