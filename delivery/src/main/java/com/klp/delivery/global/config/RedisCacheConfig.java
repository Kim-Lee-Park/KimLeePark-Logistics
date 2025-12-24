package com.klp.delivery.global.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@EnableCaching
@Configuration
public class RedisCacheConfig {

    private static final String KEY_PREFIX = "delivery-cache:";
    private static final Duration ROUTE_PLAN_CACHE_EXPIRATION = Duration.ofHours(60);

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        var valueSerializer = new GenericJackson2JsonRedisSerializer();
        var keySerializer   = new org.springframework.data.redis.serializer.StringRedisSerializer();

        // config
        RedisCacheConfiguration redisCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))  // TTL 기본 설정
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
            .prefixCacheNameWith(KEY_PREFIX); // 키 접두어

        // 캐시 이름마다 다른 TTL 설정 Option
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("route-plan", redisCacheConfig.entryTtl(ROUTE_PLAN_CACHE_EXPIRATION));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(redisCacheConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .transactionAware() // 트랜잭션 커밋 이후에 캐시 작업
            .build();
    }
}
