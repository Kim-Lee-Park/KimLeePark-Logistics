package com.klp.hub.inventory.infrastructure.cache;

import com.klp.hub.global.exception.BusinessException;
import com.klp.hub.inventory.application.InventoryCacheService;
import com.klp.hub.inventory.exception.InventoryErrorCode;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryCacheServiceImpl implements InventoryCacheService {

    private final StringRedisTemplate redisTemplate;
    private RedisScript<List> reserveScript;
    private RedisScript<List> restoreScript;

    private static final String KEY_PREFIX = "inventory:";
    public static final long RESULT_INSUFFICIENT_STOCK = -1;
    public static final long NEED_DB_FALLBACK = -2;

    @PostConstruct
    public void init() {
        reserveScript = RedisScript.of(
            new ClassPathResource("lua/reserve.lua"),
            List.class
        );
        restoreScript = RedisScript.of(
            new ClassPathResource("lua/restore.lua"),
            List.class
        );
    }

    private String buildKey(UUID productId, UUID hubId) {
        return KEY_PREFIX + productId + ":" + hubId;
    }

    @Override
    public boolean existsCache(UUID productId, UUID hubId) {
        try {
            return redisTemplate.hasKey(buildKey(productId, hubId));
        } catch (Exception e) {
            log.warn("Redis existsCache 실패, false 반환: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public long reserveInventory(UUID productId, UUID hubId, int quantity) {
        String key = buildKey(productId, hubId);
        try {
            List<Long> result = redisTemplate.execute(reserveScript, List.of(key), String.valueOf(quantity));

            if (result.size() < 2) {
                log.warn("Lua Script 반환값 이상: {}", result);
                return NEED_DB_FALLBACK;
            }

            long statusCode = result.get(0);
            long remainingInventory = result.get(1);

            if (statusCode == 0) {
                log.info("Redis 재고 선점 성공: key={}, remaining={}", key, remainingInventory);
            } else if (statusCode == RESULT_INSUFFICIENT_STOCK) {
                log.warn("Redis 재고 부족: key={}, current={}", key, remainingInventory);
            } else {
                log.info("Redis 키 없음, DB 폴백 필요: key={}", key);
            }

            return statusCode == 0 ? remainingInventory : statusCode;
        } catch (Exception e) {
            log.warn("Redis reserveInventory 실패, DB 폴백: {}", e.getMessage());
            return NEED_DB_FALLBACK;
        }
    }

    @Override
    public long restoreInventory(UUID productId, UUID hubId, int quantity) {
        String key = buildKey(productId, hubId);
        try {
            List<Long> result = redisTemplate.execute(restoreScript, List.of(key), String.valueOf(quantity));

            if (result.size() < 2) {
                return NEED_DB_FALLBACK;
            }

            long statusCode = result.get(0);
            long newInventory = result.get(1);

            if (statusCode == 0) {
                log.info("Redis 재고 복구 성공: key={}, newInventory={}", key, newInventory);
            }

            return statusCode == 0 ? newInventory : statusCode;

        } catch (Exception e) {
            log.warn("Redis restoreInventory 실패: {}", e.getMessage());
            return NEED_DB_FALLBACK;
        }
    }

    @Override
    public Optional<Integer> getInventory(UUID productId, UUID hubId) {
        try {
            String value = redisTemplate.opsForValue().get(buildKey(productId, hubId));
            return value != null ? Optional.of(Integer.parseInt(value)) : Optional.empty();
        } catch (Exception e) {
            log.warn("Redis getInventory 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void setInventory(UUID productId, UUID hubId, int quantity, long ttlSeconds) {
        try {
            redisTemplate.opsForValue().set(
                buildKey(productId, hubId),
                String.valueOf(quantity),
                Duration.ofSeconds(ttlSeconds)
            );
            log.info("Redis 재고 설정: productId={}, hubId={}, qty={}, ttl={}sec",
                productId, hubId, quantity, ttlSeconds);
        } catch (Exception e) {
            log.error("Redis setInventory 실패: {}", e.getMessage());
            throw new BusinessException(InventoryErrorCode.CACHE_SET_FAILED);
        }
    }

    @Override
    public void deleteCache(UUID productId, UUID hubId) {
        try {
            redisTemplate.delete(buildKey(productId, hubId));
            log.info("Redis 캐시 삭제: productId={}, hubId={}", productId, hubId);
        } catch (Exception e) {
            log.warn("Redis deleteCache 실패: {}", e.getMessage());
        }
    }

    @Override
    public Long getTtl(UUID productId, UUID hubId) {
        try {
            return redisTemplate.getExpire(buildKey(productId, hubId), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis getTtl 실패: {}", e.getMessage());
            return null;
        }
    }
}
