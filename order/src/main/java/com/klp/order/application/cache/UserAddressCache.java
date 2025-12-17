package com.klp.order.application.cache;

import static com.klp.common.util.JitterUtil.jitterSeconds;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.klp.common.exception.ExternalApiException;
import com.klp.order.application.service.UserClient;
import com.klp.order.domain.vo.CachedUserAddress;
import com.klp.order.domain.vo.UserAddress;
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
public class UserAddressCache {

    private static final String USER_ADDRESS_KEY_PREFIX = "user:address:";
    private static final String NEGATIVE_TOKEN = "__NULL__";
    private static final String CACHE_NAME = "user-address";

    // TTL 설정
    private static final long POSITIVE_L2_TTL_SECONDS = 20 * 60;
    private static final long NEGATIVE_L2_TTL_SECONDS = 60;

    // PER에 쓸 최소 TTL 보호선 (ttl <= 0, -1, -2 같은 경우)
    private static final long MIN_TTL_FOR_CACHE_SECONDS = 1;

    private final Cache<String, CachedUserAddress> userAddressLocalCache;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final UserClient userClient;
    private final MultiLevelCacheMetrics cacheMetrics;

    public UserAddress getUserAddress(UUID addressId) {
        String key = buildKey(addressId);

        cacheMetrics.recordRequest(CACHE_NAME, "L1");

        // L1 Caffeine 로컬 캐시 조회
        CachedUserAddress localCacheValue = userAddressLocalCache.getIfPresent(key);
        if (localCacheValue != null) {
            cacheMetrics.recordL1Hit(CACHE_NAME, "L1");

            if (localCacheValue.isNegative()) {
                log.debug("[UserAddressCache] L1 NEGATIVE hit for key={}", key);
                return null;
            }
            log.debug("[UserAddressCache] L1 hit for key={}", key);
            return localCacheValue.getUserAddress();
        }

        cacheMetrics.recordL1Miss(CACHE_NAME, "L1");

        cacheMetrics.recordRequest(CACHE_NAME, "L2");

        // L2 Redis 공유 캐시 조회
        try {
            String redisCacheValue = redisTemplate.opsForValue().get(key);
            if (redisCacheValue != null) {
                cacheMetrics.recordL2Hit(CACHE_NAME, "L2");

                if (NEGATIVE_TOKEN.equals(redisCacheValue)) {
                    log.debug("[UserAddressCache] L2 NEGATIVE hit for key={}", key);
                    // L1에도 Negative 캐시
                    userAddressLocalCache.put(key, new CachedUserAddress(null, true));
                    return null;
                }

                // TTL 체크
                Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);

                // Redis getExpire() 값이 null, -1(만료 없음), -2(키 없음) 등일 수 있으니 방어
                if (ttl == null || ttl <= MIN_TTL_FOR_CACHE_SECONDS) {
                    log.debug(
                        "[UserAddressCache] L2 ttl={} (invalid or about to expire) for key={}, reload from origin",
                        ttl, key);
                    cacheMetrics.recordL2Miss(CACHE_NAME, "L2");
                    return loadFromDbAndCache(addressId, key);
                }

                // Probabilistic Early Refresh (PER)
                double baseTtl = (double) POSITIVE_L2_TTL_SECONDS;
                double ttlRatio = Math.max(0.0, Math.min(1.0, ttl / baseTtl));
                double random = ThreadLocalRandom.current().nextDouble();

                // refresh 확률 = 1 - ttlRatio
                boolean shouldRefresh = random < ttlRatio;
                if (shouldRefresh) {
                    log.debug(
                        "[UserAddressCache] L2 PER refresh triggered (ttl={}s, ratio={}, random={}) for key={}",
                        ttl, ttlRatio, random, key);
                    return loadFromDbAndCache(addressId, key);
                }

                try {
                    UserAddress userAddress = objectMapper.readValue(redisCacheValue,
                        UserAddress.class);
                    log.debug("[UserAddressCache] L2 hit for key={}", key);
                    // L1에 채워넣기
                    userAddressLocalCache.put(key, new CachedUserAddress(userAddress, false));
                    return userAddress;
                } catch (JsonProcessingException e) {
                    log.warn(
                        "[UserAddressCache] Failed to deserialize Product from Redis for key={}",
                        key, e);
                    // JSON이 깨졌으면 원본 호출
                    return loadFromDbAndCache(addressId, key);
                }
            }
        } catch (DataAccessException e) {
            log.error(
                "[UserAddressCache] Redis 장애 발생, 캐시를 건너뛰고 원본(UserService) 호출로 폴백합니다. key={}",
                key, e);
        }

        // L2 miss → DB 조회
        cacheMetrics.recordL2Miss(CACHE_NAME, "L2");
        return loadFromDbAndCache(addressId, key);
    }

    private String buildKey(UUID addressId) {
        return USER_ADDRESS_KEY_PREFIX + addressId;
    }

    private UserAddress loadFromDbAndCache(UUID addressId, String key) {
        long start = System.currentTimeMillis();
        try {
            UserAddress userAddress = userClient.getUserAddressHubIdByAddressId(addressId);
            long duration = System.currentTimeMillis() - start;
            cacheMetrics.recordLoadDuration(CACHE_NAME, duration);
            if (userAddress == null) {
                cacheNegative(key);
                return null;
            } else {
                cachePositive(key, userAddress);
                return userAddress;
            }
        } catch (ExternalApiException e) {
            log.error("[UserAddressCache] UserService 장애, key={}, addressId={}", key, addressId, e);
            throw e;
        }
    }

    private void cachePositive(String key, UserAddress userAddress) {
        try {
            String json = objectMapper.writeValueAsString(userAddress);

            // L2 저장
            long ttlSec = jitterSeconds(POSITIVE_L2_TTL_SECONDS);
            redisTemplate.opsForValue()
                .set(key, json, ttlSec, TimeUnit.SECONDS);

            // L1 저장
            CachedUserAddress cached = new CachedUserAddress(userAddress, false);
            userAddressLocalCache.put(key, cached);

            log.debug("[UserAddressCache] Cached POSITIVE userAddress for key={}", key);
        } catch (JsonProcessingException e) {
            log.warn("[UserAddressCache] Failed to serialize userAddress for key={}", key, e);
        }
    }

    private void cacheNegative(String key) {
        // L2 Negative cache
        long ttlSec = jitterSeconds(NEGATIVE_L2_TTL_SECONDS);
        redisTemplate.opsForValue()
            .set(key, NEGATIVE_TOKEN, ttlSec, TimeUnit.SECONDS);
        // L1 Negative cache
        userAddressLocalCache.put(key, new CachedUserAddress(null, true));
        log.debug("[UserAddressCache] Cached NEGATIVE userAddress for key={}", key);
    }

    public void evictProduct(UUID addressId) {
        String key = buildKey(addressId);
        userAddressLocalCache.invalidate(key);
        redisTemplate.delete(key);
        log.debug("[UserAddressCache] Evicted userAddress cache for key={}", key);
    }
}
