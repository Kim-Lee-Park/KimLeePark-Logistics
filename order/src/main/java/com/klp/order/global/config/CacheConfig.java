package com.klp.order.global.config;

import static com.klp.order.common.util.JitterUtil.jitterSeconds;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.klp.order.domain.vo.CachedProduct;
import com.klp.order.domain.vo.CachedUserProfile;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public Cache<String, CachedUserProfile> userProfileLocalCache() {

        final long POSITIVE_TTL_BASE_SECONDS = 5 * 60;
        final long POSITIVE_TTI_SECONDS = 150;

        final long NEGATIVE_TTL_BASE_SECONDS = 15;
        final long NEGATIVE_TTI_SECONDS = 7;

        return Caffeine.newBuilder()
            .expireAfter(new Expiry<String, CachedUserProfile>() {

                @Override
                public long expireAfterCreate(String key,
                    CachedUserProfile value,
                    long currentTime) {

                    long baseSec = value.isNegative()
                        ? NEGATIVE_TTL_BASE_SECONDS
                        : POSITIVE_TTL_BASE_SECONDS;

                    long jitteredSec = jitterSeconds(baseSec);

                    return TimeUnit.NANOSECONDS.convert(jitteredSec, TimeUnit.SECONDS);
                }

                @Override
                public long expireAfterUpdate(String key,
                    CachedUserProfile value,
                    long currentTime,
                    long currentDuration) {

                    long baseSec = value.isNegative()
                        ? NEGATIVE_TTL_BASE_SECONDS
                        : POSITIVE_TTL_BASE_SECONDS;

                    long jitteredSec = jitterSeconds(baseSec);

                    return TimeUnit.NANOSECONDS.convert(jitteredSec, TimeUnit.SECONDS);
                }

                @Override
                public long expireAfterRead(String key,
                    CachedUserProfile value,
                    long currentTime,
                    long currentDuration) {

                    long ttlRemaining = currentDuration;

                    long ttiSec = value.isNegative()
                        ? NEGATIVE_TTI_SECONDS
                        : POSITIVE_TTI_SECONDS;
                    long ttiNanos = TimeUnit.NANOSECONDS.convert(ttiSec, TimeUnit.SECONDS);

                    // 남은 ttl 시간과 tti 시간 비교
                    long nextDuration = Math.min(ttlRemaining, ttiNanos);

                    // 안전 방어: 0 이하가 나오면 즉시 만료시키도록 0 반환
                    return Math.max(nextDuration, 0L);
                }
            })
            .maximumSize(100_000)
            .build();
    }

    @Bean
    public Cache<String, CachedProduct> productLocalCache() {

        final long POSITIVE_TTL_BASE_SECONDS = 5 * 60;
        final long POSITIVE_TTI_SECONDS = 150;

        final long NEGATIVE_TTL_BASE_SECONDS = 15;
        final long NEGATIVE_TTI_SECONDS = 7;

        return Caffeine.newBuilder()
            .expireAfter(new Expiry<String, CachedProduct>() {

                @Override
                public long expireAfterCreate(String key,
                    CachedProduct value,
                    long currentTime) {

                    long baseSec = value.isNegative()
                        ? NEGATIVE_TTL_BASE_SECONDS
                        : POSITIVE_TTL_BASE_SECONDS;

                    long jitteredSec = jitterSeconds(baseSec);

                    return TimeUnit.NANOSECONDS.convert(jitteredSec, TimeUnit.SECONDS);
                }

                @Override
                public long expireAfterUpdate(String key,
                    CachedProduct value,
                    long currentTime,
                    long currentDuration) {

                    long baseSec = value.isNegative()
                        ? NEGATIVE_TTL_BASE_SECONDS
                        : POSITIVE_TTL_BASE_SECONDS;

                    long jitteredSec = jitterSeconds(baseSec);

                    return TimeUnit.NANOSECONDS.convert(jitteredSec, TimeUnit.SECONDS);
                }

                @Override
                public long expireAfterRead(String key,
                    CachedProduct value,
                    long currentTime,
                    long currentDuration) {

                    long ttlRemaining = currentDuration;

                    long ttiSec = value.isNegative()
                        ? NEGATIVE_TTI_SECONDS
                        : POSITIVE_TTI_SECONDS;
                    long ttiNanos = TimeUnit.NANOSECONDS.convert(ttiSec, TimeUnit.SECONDS);

                    // 남은 ttl 시간과 tti 시간 비교
                    long nextDuration = Math.min(ttlRemaining, ttiNanos);

                    // 안전 방어: 0 이하가 나오면 즉시 만료시키도록 0 반환
                    return Math.max(nextDuration, 0L);
                }
            })
            .maximumSize(100_000)
            .build();
    }
}
