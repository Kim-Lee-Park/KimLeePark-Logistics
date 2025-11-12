package com.klp.order.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PerformanceMonitor {

    private final ConcurrentHashMap<String, PerformanceMetrics> metricsMap = new ConcurrentHashMap<>();

    /**
     * 메서드 실행 시간을 측정하고 결과를 반환
     *
     * @param methodName 측정할 메서드 이름
     * @param task 실행할 작업
     * @return 작업 실행 결과
     */
    public <T> T measure(String methodName, Supplier<T> task) {
        long startTime = System.currentTimeMillis();
        long startCpuTime = getCurrentThreadCpuTime();

        try {
            T result = task.get();

            long endTime = System.currentTimeMillis();
            long endCpuTime = getCurrentThreadCpuTime();

            long totalTime = endTime - startTime;
            long cpuTime = (endCpuTime - startCpuTime) / 1_000_000;  // 나노초 -> 밀리초
            long waitTime = totalTime - cpuTime;

            recordMetrics(methodName, totalTime, cpuTime, waitTime);

            log.info("성능 측정 - 메서드: {}, 전체시간: {}ms, CPU시간: {}ms, 대기시간: {}ms",
                methodName, totalTime, cpuTime, waitTime);

            return result;

        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("메서드 실행 실패 - {}, 소요시간: {}ms", methodName, endTime - startTime);
            throw e;
        }
    }

    /**
     * 반환값이 없는 메서드 실행 시간 측정
     */
    public void measure(String methodName, Runnable task) {
        measure(methodName, () -> {
            task.run();
            return null;
        });
    }

    private long getCurrentThreadCpuTime() {
        java.lang.management.ThreadMXBean threadMXBean =
            java.lang.management.ManagementFactory.getThreadMXBean();
        return threadMXBean.getCurrentThreadCpuTime();
    }

    private void recordMetrics(String methodName, long totalTime, long cpuTime, long waitTime) {
        metricsMap.computeIfAbsent(methodName, k -> new PerformanceMetrics())
            .record(totalTime, cpuTime, waitTime);
    }

    public void printStatistics() {
        log.info("========== 성능 통계 ==========");
        metricsMap.forEach((method, metrics) -> {
            log.info("메서드: {}", method);
            log.info("  호출 횟수: {}", metrics.count.sum());
            log.info("  평균 전체 시간: {}ms", String.format("%.2f", metrics.getAverageTotalTime()));
            log.info("  평균 CPU 시간: {}ms", String.format("%.2f", metrics.getAverageCpuTime()));
            log.info("  평균 대기 시간: {}ms", String.format("%.2f", metrics.getAverageWaitTime()));
            log.info("  스레드 풀 권장 크기: {}", metrics.getRecommendedThreadPoolSize());
        });
        log.info("===============================");
    }

    /**
     * 특정 메서드의 통계 초기화
     */
    public void resetMetrics(String methodName) {
        metricsMap.remove(methodName);
    }

    /**
     * 모든 통계 초기화
     */
    public void resetAllMetrics() {
        metricsMap.clear();
    }

    private static class PerformanceMetrics {
        LongAdder count = new LongAdder();
        LongAdder totalTimeSum = new LongAdder();
        LongAdder cpuTimeSum = new LongAdder();
        LongAdder waitTimeSum = new LongAdder();

        void record(long totalTime, long cpuTime, long waitTime) {
            count.increment();
            totalTimeSum.add(totalTime);
            cpuTimeSum.add(cpuTime);
            waitTimeSum.add(waitTime);
        }

        double getAverageTotalTime() {
            return count.sum() > 0 ? (double) totalTimeSum.sum() / count.sum() : 0;
        }

        double getAverageCpuTime() {
            return count.sum() > 0 ? (double) cpuTimeSum.sum() / count.sum() : 0;
        }

        double getAverageWaitTime() {
            return count.sum() > 0 ? (double) waitTimeSum.sum() / count.sum() : 0;
        }

        int getRecommendedThreadPoolSize() {
            double avgCpuTime = getAverageCpuTime();
            double avgWaitTime = getAverageWaitTime();

            if (avgCpuTime == 0) return 5;  // 기본값

            // 스레드 풀 크기 = CPU 코어 수 * (1 + 대기시간/작업시간)
            int cpuCores = Runtime.getRuntime().availableProcessors();
            double ratio = 1 + (avgWaitTime / avgCpuTime);
            return Math.max(5, (int) Math.ceil(cpuCores * ratio));
        }
    }
}
