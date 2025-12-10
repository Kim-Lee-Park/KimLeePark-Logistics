package com.klp.review.infrastructure.repository;

import com.klp.review.domain.entity.Review;
import com.klp.review.domain.repository.ReviewJpaRepository;
import com.klp.review.domain.repository.ReviewRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepository {

    private final ReviewJpaRepository reviewJpaRepository;

    @Override
    public Review save(Review review) {
        return reviewJpaRepository.save(review);
    }

    @Override
    public Optional<Review> findById(UUID reviewId) {
        return reviewJpaRepository.findById(reviewId);
    }

    @Override
    public Optional<Review> findByIdAndDeletedAtIsNull(UUID reviewId) {
        return reviewJpaRepository.findByIdAndDeletedAtIsNull(reviewId);
    }

    @Override
    public Optional<Review> findByOrderId(UUID orderId) {
        return reviewJpaRepository.findByOrderIdAndDeletedAtIsNull(orderId);
    }

    @Override
    public boolean existsByOrderId(UUID orderId) {
        return reviewJpaRepository.existsByOrderIdAndDeletedAtIsNull(orderId);
    }

    @Override
    public Page<Review> findByProductId(UUID productId, Pageable pageable) {
        return reviewJpaRepository.findByProductIdAndDeletedAtIsNull(productId, pageable);
    }

    @Override
    public Page<Review> findByUserId(Long userId, Pageable pageable) {
        return reviewJpaRepository.findByUserIdAndDeletedAtIsNull(userId, pageable);
    }

    @Override
    public Page<Review> findAll(Pageable pageable) {
        return reviewJpaRepository.findAllAndDeletedAtIsNull(pageable);
    }
}
