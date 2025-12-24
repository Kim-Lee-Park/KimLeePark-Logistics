package com.klp.order.application.cache;

import static com.klp.common.util.JitterUtil.jitterSeconds;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.klp.common.exception.ExternalApiException;
import com.klp.order.application.service.UserClient;
import com.klp.order.domain.vo.CachedUserProfile;
import com.klp.order.domain.vo.UserProfile;
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
public class UserProfileCache {

    private static final String USER_PROFILE_KEY_PREFIX = "user:profile:";
    private static final String NEGATIVE_TOKEN = "__NULL__";
    private static final String CACHE_NAME = "user_profile"; // 메트릭 tag용

    // TTL 설정
    private static final long POSITIVE_L2_TTL_SECONDS = 20 * 60;
    private static final long NEGATIVE_L2_TTL_SECONDS = 60;

    // PER에 쓸 최소 TTL 보호선 (ttl <= 0, -1, -2 같은 경우)
    private static final long MIN_TTL_FOR_CACHE_SECONDS = 1;

    private final Cache<String, CachedUserProfile> userProfileLocalCache;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final UserClient userClient;
    private final MultiLevelCacheMetrics cacheMetrics;

    public UserProfile getUserProfile(Long userId) {
        String key = buildKey(userId);

        cacheMetrics.recordRequest(CACHE_NAME, "L1");

        // L1 Caffeine 로컬 캐시 조회
        CachedUserProfile localCacheValue = userProfileLocalCache.getIfPresent(key);
        if (localCacheValue != null) {
            cacheMetrics.recordL1Hit(CACHE_NAME, "L1");

            if (localCacheValue.isNegative()) {
                log.debug("[UserProfileCache] L1 NEGATIVE hit for key={}", key);
                return null;
            }
            log.debug("[UserProfileCache] L1 hit for key={}", key);
            return localCacheValue.getProfile();
        }

        cacheMetrics.recordL1Miss(CACHE_NAME, "L1");

        cacheMetrics.recordRequest(CACHE_NAME, "L2");

        // L2 Redis 공유 캐시 조회
        try {
            String redisCacheValue = redisTemplate.opsForValue().get(key);
            if (redisCacheValue != null) {
                cacheMetrics.recordL2Hit(CACHE_NAME, "L2");

                if (NEGATIVE_TOKEN.equals(redisCacheValue)) {
                    log.debug("[UserProfileCache] L2 NEGATIVE hit for key={}", key);
                    // L1에도 Negative 캐시
                    userProfileLocalCache.put(key, new CachedUserProfile(null, true));
                    return null;
                }

                // TTL 체크
                Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);

                // Redis getExpire() 값이 null, -1(만료 없음), -2(키 없음) 등일 수 있으니 방어
                if (ttl == null || ttl <= MIN_TTL_FOR_CACHE_SECONDS) {
                    log.debug(
                        "[UserProfileCache] L2 ttl={} (invalid or about to expire) for key={}, reload from DB",
                        ttl, key);
                    return loadFromDbAndCache(userId, key);
                }

                // Probabilistic Early Refresh (PER) 알고리즘 적용
                // baseTTL(정상 TTL) 대비 남은 TTL 비율
                double baseTtl = (double) POSITIVE_L2_TTL_SECONDS;
                double ttlRatio = Math.max(0.0, Math.min(1.0, ttl / baseTtl));

                double random = ThreadLocalRandom.current().nextDouble();

                // refresh 확률 = 1 - ttlRatio
                boolean shouldRefresh = random < ttlRatio;
                if (shouldRefresh) {
                    log.debug(
                        "[UserProfileCache] L2 PER refresh triggered (ttl={}s, ratio={}, random={}) for key={}",
                        ttl, ttlRatio, random, key);
                    cacheMetrics.recordL2Miss(CACHE_NAME, "L2");
                    return loadFromDbAndCache(userId, key);
                }

                try {
                    UserProfile profile = objectMapper.readValue(redisCacheValue,
                        UserProfile.class);
                    log.debug("[UserProfileCache] L2 hit for key={}", key);
                    // L1에 채워넣기
                    userProfileLocalCache.put(key, new CachedUserProfile(profile, false));
                    return profile;
                } catch (JsonProcessingException e) {
                    log.warn(
                        "[UserProfileCache] Failed to deserialize UserProfile from Redis for key={}",
                        key, e);
                    // JSON 깨졌으면 DB로
                    return loadFromDbAndCache(userId, key);
                }
            }
        } catch (DataAccessException e) {
            log.error("[UserProfileCache] Redis 장애 발생, 캐시를 건너뛰고 DB/원본 호출로 폴백합니다. key={}",
                key, e);
        }

        // L2 miss → DB 조회
        cacheMetrics.recordL2Miss(CACHE_NAME, "L2");
        return loadFromDbAndCache(userId, key);
    }

    private UserProfile loadFromDbAndCache(Long userId, String key) {
        long start = System.currentTimeMillis();
        try {
            UserProfile userProfile = userClient.getUserProfileById(userId);
            long duration = System.currentTimeMillis() - start;
            cacheMetrics.recordLoadDuration(CACHE_NAME, duration);
            if (userProfile == null) {
                cacheNegative(key);
                return null;
            } else {
                cachePositive(key, userProfile);
                return userProfile;
            }
        } catch (ExternalApiException e) {
            log.error("[UserProfileCache] UserService/HubService 장애, key={}, userId={}", key,
                userId, e);
            throw e;
        }
    }

    private void cachePositive(String key, UserProfile profile) {
        try {
            String json = objectMapper.writeValueAsString(profile);

            // L2 저장
            long ttlSec = jitterSeconds(POSITIVE_L2_TTL_SECONDS);
            redisTemplate.opsForValue()
                .set(key, json, ttlSec, TimeUnit.SECONDS);

            // L1 저장
            CachedUserProfile cached = new CachedUserProfile(profile, false);
            userProfileLocalCache.put(key, cached);

            log.debug("[UserProfileCache] Cached POSITIVE user profile for key={}", key);
        } catch (JsonProcessingException e) {
            log.warn("[UserProfileCache] Failed to serialize UserProfile for key={}", key, e);
        }
    }

    private void cacheNegative(String key) {
        // L2 Negative cache
        long ttlSec = jitterSeconds(NEGATIVE_L2_TTL_SECONDS);
        redisTemplate.opsForValue()
            .set(key, NEGATIVE_TOKEN, ttlSec, TimeUnit.SECONDS);
        // L1 Negative cache
        userProfileLocalCache.put(key, new CachedUserProfile(null, true));
        log.debug("[UserProfileCache] Cached NEGATIVE user profile for key={}", key);
    }

    private String buildKey(Long userId) {
        return USER_PROFILE_KEY_PREFIX + userId;
    }

    public void evictUserProfile(Long userId) {
        String key = buildKey(userId);
        userProfileLocalCache.invalidate(key);
        redisTemplate.delete(key);
        log.debug("[UserProfileCache] Evicted cache for key={}", key);
    }
}
