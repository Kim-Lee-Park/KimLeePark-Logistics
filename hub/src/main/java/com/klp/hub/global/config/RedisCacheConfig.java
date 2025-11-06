package com.klp.hub.global.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

    private static final String KEY_PREFIX = "hub-cache:";
    private static final Duration HUB_CACHE_EXPIRATION = Duration.ofHours(360);
    private static final Duration HUB_ROUTE_INFO_EXPIRATION = Duration.ofHours(60);

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        // ObjectMapper 커스텀
        ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // Json 직렬화
        GenericJackson2JsonRedisSerializer serializer
            = new GenericJackson2JsonRedisSerializer(objectMapper);

        // config
        RedisCacheConfiguration redisCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))  // TTL 기본 설정
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(serializer)
            )
            .prefixCacheNameWith(KEY_PREFIX); // 키 접두어

        // 캐시 이름마다 다른 TTL 설정 Option
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("hub", redisCacheConfig.entryTtl(HUB_CACHE_EXPIRATION));
        cacheConfigurations.put("hub-route-info", redisCacheConfig.entryTtl(HUB_ROUTE_INFO_EXPIRATION));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(redisCacheConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
    }
}
