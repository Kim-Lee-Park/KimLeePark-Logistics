package com.klp.hub.global.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtLeastFor = "10s", defaultLockAtMostFor = "90s") //Lock 이 유지되는 최소,최대 시간
public class SchedulingConfig {

    @Value("${spring.profiles.active}")
    private String profile;

    @Value("${spring.application.name}")
    private String appName;

    @Bean
    public LockProvider lockProvider(RedisConnectionFactory redisConnectionFactory) {
        String prefix = appName + ":" + profile;
        return new RedisLockProvider(redisConnectionFactory, prefix);
    }
}
