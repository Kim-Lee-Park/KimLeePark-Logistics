package com.klp.order.order.application.cache;

import static com.klp.order.common.util.JitterUtil.jitterSeconds;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.klp.order.common.exception.ExternalApiException;
import com.klp.order.order.application.service.ProductClient;
import com.klp.order.order.domain.vo.CachedProduct;
import com.klp.order.order.domain.vo.Product;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductCache {

    private static final String PRODUCT_KEY_PREFIX = "product:";
    private static final String NEGATIVE_TOKEN = "__NULL__";
    private static final String CACHE_NAME = "product"; // 메트릭 tag용

    // TTL 설정
    private static final long POSITIVE_L2_TTL_SECONDS = 20 * 60;
    private static final long NEGATIVE_L2_TTL_SECONDS = 60;

    // PER에 쓸 최소 TTL 보호선 (ttl <= 0, -1, -2 같은 경우)
    private static final long MIN_TTL_FOR_CACHE_SECONDS = 1;

    private final Cache<String, CachedProduct> productLocalCache;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ProductClient productClient;
    private final MultiLevelCacheMetrics cacheMetrics;

    public Product getProductById(UUID productId) {
        String key = buildKey(productId);

        cacheMetrics.recordRequest(CACHE_NAME, "L1");

        // L1 Caffeine 로컬 캐시 조회
        CachedProduct localCacheValue = productLocalCache.getIfPresent(key);
        if (localCacheValue != null) {
            cacheMetrics.recordL1Hit(CACHE_NAME, "L1");

            if (localCacheValue.isNegative()) {
                log.debug("[ProductCache] L1 NEGATIVE hit for key={}", key);
                return null;
            }
            log.debug("[ProductCache] L1 hit for key={}", key);
            return localCacheValue.getProduct();
        }

        cacheMetrics.recordL1Miss(CACHE_NAME, "L1");

        cacheMetrics.recordRequest(CACHE_NAME, "L2");

        // L2 Redis 공유 캐시 조회
        try {
            String redisCacheValue = redisTemplate.opsForValue().get(key);
            if (redisCacheValue != null) {
                cacheMetrics.recordL2Hit(CACHE_NAME, "L2");

                if (NEGATIVE_TOKEN.equals(redisCacheValue)) {
                    log.debug("[ProductCache] L2 NEGATIVE hit for key={}", key);
                    // L1에도 Negative 캐시
                    productLocalCache.put(key, new CachedProduct(null, true));
                    return null;
                }

                // TTL 체크
                Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);

                // Redis getExpire() 값이 null, -1(만료 없음), -2(키 없음) 등일 수 있으니 방어
                if (ttl == null || ttl <= MIN_TTL_FOR_CACHE_SECONDS) {
                    log.debug(
                        "[ProductCache] L2 ttl={} (invalid or about to expire) for key={}, reload from origin",
                        ttl, key);
                    cacheMetrics.recordL2Miss(CACHE_NAME, "L2");
                    return loadFromDbAndCache(productId, key);
                }

                // Probabilistic Early Refresh (PER)
                double baseTtl = (double) POSITIVE_L2_TTL_SECONDS;
                double ttlRatio = Math.max(0.0, Math.min(1.0, ttl / baseTtl));
                double random = ThreadLocalRandom.current().nextDouble();

                // refresh 확률 = 1 - ttlRatio
                boolean shouldRefresh = random < ttlRatio;
                if (shouldRefresh) {
                    log.debug(
                        "[ProductCache] L2 PER refresh triggered (ttl={}s, ratio={}, random={}) for key={}",
                        ttl, ttlRatio, random, key);
                    return loadFromDbAndCache(productId, key);
                }

                try {
                    Product product = objectMapper.readValue(redisCacheValue, Product.class);
                    log.debug("[ProductCache] L2 hit for key={}", key);
                    // L1에 채워넣기
                    productLocalCache.put(key, new CachedProduct(product, false));
                    return product;
                } catch (JsonProcessingException e) {
                    log.warn(
                        "[ProductCache] Failed to deserialize Product from Redis for key={}",
                        key, e);
                    // JSON이 깨졌으면 원본 호출
                    return loadFromDbAndCache(productId, key);
                }
            }
        } catch (DataAccessException e) {
            log.error(
                "[ProductCache] Redis 장애 발생, 캐시를 건너뛰고 원본(ProductService) 호출로 폴백합니다. key={}",
                key, e);
        }

        // L2 miss → DB 조회
        cacheMetrics.recordL2Miss(CACHE_NAME, "L2");
        return loadFromDbAndCache(productId, key);
    }

    private String buildKey(UUID productId) {
        return PRODUCT_KEY_PREFIX + productId;
    }

    private Product loadFromDbAndCache(UUID productId, String key) {
        long start = System.currentTimeMillis();
        try {
            Product product = productClient.getProductById(productId);
            long duration = System.currentTimeMillis() - start;
            cacheMetrics.recordLoadDuration(CACHE_NAME, duration);
            if (product == null) {
                cacheNegative(key);

                return null;
            } else {
                cachePositive(key, product);
                return product;
            }
        } catch (ExternalApiException e) {
            log.error("[ProductCache] ProductService 장애, key={}, productId={}", key, productId, e);
            throw e;
        }
    }

    private void cachePositive(String key, Product product) {
        try {
            String json = objectMapper.writeValueAsString(product);

            // L2 저장
            long ttlSec = jitterSeconds(POSITIVE_L2_TTL_SECONDS);
            redisTemplate.opsForValue()
                .set(key, json, ttlSec, TimeUnit.SECONDS);

            // L1 저장
            CachedProduct cached = new CachedProduct(product, false);
            productLocalCache.put(key, cached);

            log.debug("[ProductCache] Cached POSITIVE product for key={}", key);
        } catch (JsonProcessingException e) {
            log.warn("[ProductCache] Failed to serialize Product for key={}", key, e);
        }
    }

    private void cacheNegative(String key) {
        // L2 Negative cache
        long ttlSec = jitterSeconds(NEGATIVE_L2_TTL_SECONDS);
        redisTemplate.opsForValue()
            .set(key, NEGATIVE_TOKEN, ttlSec, TimeUnit.SECONDS);
        // L1 Negative cache
        productLocalCache.put(key, new CachedProduct(null, true));
        log.debug("[ProductCache] Cached NEGATIVE product for key={}", key);
    }

    public void evictProduct(UUID productId) {
        String key = buildKey(productId);
        productLocalCache.invalidate(key);
        redisTemplate.delete(key);
        log.debug("[ProductCache] Evicted product cache for key={}", key);
    }
}
