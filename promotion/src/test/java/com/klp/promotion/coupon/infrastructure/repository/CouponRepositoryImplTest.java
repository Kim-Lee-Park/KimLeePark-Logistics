package com.klp.promotion.coupon.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.klp.promotion.global.exception.BusinessException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class CouponRepositoryImplTest {

    @Autowired
    private CouponRepositoryImpl couponRepositoryImpl;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @AfterEach
    void tearDown() {
        Set<String> keys = redisTemplate.keys("coupon:stock:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    @DisplayName("동시성 테스트 - 여러 스레드가 동시에 재고 차감")
    void decreaseStock_Concurrency_Success() throws InterruptedException {
        // given
        UUID couponId = UUID.randomUUID();
        String key = "coupon:stock:" + couponId;
        int initialStock = 100;
        int threadCount = 50;
        redisTemplate.opsForValue().set(key, String.valueOf(initialStock));

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    couponRepositoryImpl.decreaseStock(couponId);
                    successCount.incrementAndGet();
                } catch (BusinessException e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // then
        String remainingStock = redisTemplate.opsForValue().get(key);
        int expectedRemaining = initialStock - successCount.get();
        
        assertThat(Integer.parseInt(remainingStock)).isEqualTo(expectedRemaining);
        assertThat(successCount.get() + failCount.get()).isEqualTo(threadCount);
    }

    @Test
    @DisplayName("동시성 테스트 - 재고 부족 상황에서 여러 스레드가 동시에 차감 시도")
    void decreaseStock_Concurrency_OutOfStock() throws InterruptedException {
        // given
        UUID couponId = UUID.randomUUID();
        String key = "coupon:stock:" + couponId;
        int initialStock = 10;
        int threadCount = 20; // 재고보다 많은 스레드
        redisTemplate.opsForValue().set(key, String.valueOf(initialStock));

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    couponRepositoryImpl.decreaseStock(couponId);
                    successCount.incrementAndGet();
                } catch (BusinessException e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // then
        String remainingStock = redisTemplate.opsForValue().get(key);
        
        // 성공한 개수만큼 재고가 차감되어야 함
        assertThat(Integer.parseInt(remainingStock)).isEqualTo(initialStock - successCount.get());
        // 성공 + 실패 = 전체 스레드 수
        assertThat(successCount.get() + failCount.get()).isEqualTo(threadCount);
        // 성공한 개수는 재고 이하여야 함
        assertThat(successCount.get()).isLessThanOrEqualTo(initialStock);
        // 재고는 0 이상이어야 함 (음수가 되면 안됨)
        assertThat(Integer.parseInt(remainingStock)).isGreaterThanOrEqualTo(0);
    }
}

