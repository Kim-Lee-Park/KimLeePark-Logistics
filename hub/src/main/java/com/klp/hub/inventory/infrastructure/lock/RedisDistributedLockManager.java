package com.klp.hub.inventory.infrastructure.lock;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisDistributedLockManager implements DistributedLockManager {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "inv:idemp:";
    private static final Duration TTL = Duration.ofMinutes(1);

    @Override
    public boolean tryLock(String key) {
        String lockKey = KEY_PREFIX + key;

        try {
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", TTL);
            log.info("Redis 분산락 획득 key = {}", lockKey);
            return Boolean.TRUE.equals(acquired);
        } catch (Exception e) {
            // Redis 장애시 DB 가 SSOT 이므로 DB가 책임
            log.warn("Redis 분산락 처리 중 예외 발생, DB로 진행 필요 key = {}", lockKey);
            return true;
        }
    }

    @Override
    public void releaseLock(String key) {
        String lockKey = KEY_PREFIX + key;

        redisTemplate.delete(lockKey);
    }
}
