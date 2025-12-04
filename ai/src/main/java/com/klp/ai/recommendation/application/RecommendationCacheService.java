package com.klp.ai.recommendation.application;

import com.klp.ai.recommendation.application.dto.ProductRecommendation;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecommendationCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String KEY_PREFIX = "recommendation:";
    private static final Duration TTL = Duration.ofHours(1);

    public void saveRecommendations(UUID orderId, List<ProductRecommendation> recommendations) {
        String key = KEY_PREFIX + orderId;
        redisTemplate.opsForValue().set(key, recommendations, TTL);
    }

    @SuppressWarnings("unchecked")
    public Optional<List<ProductRecommendation>> getRecommendations(UUID orderId) {
        String key = KEY_PREFIX + orderId;
        Object cached = redisTemplate.opsForValue().get(key);

        if (cached instanceof List<?> list) {
            return Optional.of((List<ProductRecommendation>) list);
        }
        return Optional.empty();
    }
}
