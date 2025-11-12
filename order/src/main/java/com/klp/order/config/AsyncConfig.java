package com.klp.order.config;

import com.klp.order.notification.applicaiton.service.NotificationService;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    private final NotificationService notificationService;

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(15);
        executor.setMaxPoolSize(49); // 임시로 측정: ((3.42/1.11) + 1) * 12 -> 49
        executor.setQueueCapacity(147); // maxPoolSize * 3
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, objects) -> {
            log.error("비동기 예외 발생 exception : {}", ex, ex.getMessage());
            notificationService.notification("비동기 예외 알림 message : " + ex.getMessage());
        };
    }
}
