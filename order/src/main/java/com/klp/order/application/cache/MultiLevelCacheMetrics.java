package com.klp.order.application.cache;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MultiLevelCacheMetrics {

    private final MeterRegistry meterRegistry;

    // L1 전체 요청 수
    public void recordRequest(String cacheName, String level) {
        Counter.builder("order_mlc_cache_requests_total")
            .description("Multi-level cache requests")
            .tag("cache_name", cacheName)
            .tag("level", level)
            .register(meterRegistry)
            .increment();
    }

    // L1 hit
    public void recordL1Hit(String cacheName, String level) {
        Counter.builder("order_mlc_cache_hits_total")
            .description("l1 cache hits")
            .tag("cache_name", cacheName)
            .tag("level", level)
            .register(meterRegistry)
            .increment();
    }

    // L1 miss
    public void recordL1Miss(String cacheName, String level) {
        Counter.builder("order_mlc_cache_misses_total")
            .description("l1 cache misses")
            .tag("cache_name", cacheName)
            .tag("level", level)
            .register(meterRegistry)
            .increment();
    }

    // L2 hit
    public void recordL2Hit(String cacheName, String level) {
        Counter.builder("order_mlc_cache_l2_hit_total")
            .description("L2 hit")
            .tag("cache_name", cacheName)
            .tag("level", level)
            .register(meterRegistry)
            .increment();
    }

    // L2 miss
    public void recordL2Miss(String cacheName, String level) {
        Counter.builder("order_mlc_cache_l2_miss_total")
            .description("L2 miss")
            .tag("cache_name", cacheName)
            .tag("level", level)
            .register(meterRegistry)
            .increment();
    }

    // load 시간 (DB 조회)
    public void recordLoadDuration(String cacheName, long durationMs) {
        Timer.builder("order_mlc_cache_load_duration_ms")
            .description("Time spent loading data on cache miss")
            .tag("cache_name", cacheName)
            .register(meterRegistry)
            .record(durationMs, TimeUnit.MILLISECONDS);
    }
}
