package com.klp.logistics.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

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
}
