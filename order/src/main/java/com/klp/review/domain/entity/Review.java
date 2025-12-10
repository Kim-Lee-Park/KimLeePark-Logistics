package com.klp.review.domain.entity;

import com.klp.common.BaseEntity;
import com.klp.global.exception.BusinessException;
import com.klp.global.exception.ReviewErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_reviews", schema = "order_schema")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "review_id", nullable = false)
    private UUID reviewId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    public static Review create(
        UUID orderId,
        UUID productId,
        Long userId,
        int rating,
        String content
    ) {
        validateOrderId(orderId);
        validateProductId(productId);
        validateUserId(userId);
        validateRating(rating);

        Review review = new Review();
        review.orderId = orderId;
        review.productId = productId;
        review.userId = userId;
        review.rating = rating;
        review.content = content;

        return review;
    }

    public void update(int rating, String content) {
        validateRating(rating);
        this.rating = rating;
        this.content = content;
    }

    public boolean canUpdate(Long requestUserId) {
        return this.userId.equals(requestUserId);
    }

    public boolean canDelete(Long requestUserId, String role) {
        return this.userId.equals(requestUserId) || "MASTER".equals(role);
    }
    
    private static void validateOrderId(UUID orderId) {
        if (orderId == null) {
            throw new BusinessException(ReviewErrorCode.ORDER_ID_REQUIRED);
        }
    }

    private static void validateProductId(UUID productId) {
        if (productId == null) {
            throw new BusinessException(ReviewErrorCode.PRODUCT_ID_REQUIRED);
        }
    }

    private static void validateUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException(ReviewErrorCode.USER_ID_REQUIRED);
        }
    }

    private static void validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new BusinessException(ReviewErrorCode.INVALID_RATING_RANGE);
        }
    }
}
