package com.klp.hub.inventory.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.klp.hub.inventory.application.dto.InventoryDeductCommand;
import com.klp.hub.inventory.application.dto.InventoryDeductCommand.Product;
import com.klp.hub.inventory.application.dto.InventoryReplenishCommand;
import com.klp.hub.inventory.domain.Inventory;
import com.klp.hub.inventory.domain.InventoryIdempotency;
import com.klp.hub.inventory.domain.InventoryIdempotencyStatus;
import com.klp.hub.inventory.domain.repository.InventoryRepository;
import com.klp.hub.inventory.infrastructure.repository.InventoryIdempotencyJpaRepository;
import com.klp.hub.inventory.infrastructure.repository.InventoryJpaRepository;
import com.klp.hub.inventory.presentation.dto.InventoryDeductResponse;
import com.klp.hub.inventory.presentation.dto.InventoryDeductResponse.Status;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Disabled
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.sql.init.mode=never"
})
public class InventoryServiceConcurrencyTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryIdempotencyJpaRepository idempotencyRepository;

    @Autowired
    private InventoryJpaRepository jpaRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private UUID productId = UUID.randomUUID();

    private UUID hubId = UUID.randomUUID();

    private final int threadCount = 100;

    private final int initQuantity = 1000;

    @BeforeEach
    void setUp() {
        inventoryRepository.save(new Inventory(productId, hubId, initQuantity));
    }

    @AfterEach
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void tearDown() {
        jpaRepository.deleteAll();

        Set<String> keys = stringRedisTemplate.keys("inv:idemp*");
        if (!keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }

    @Nested
    class Deduct {

        @Test
        @DisplayName("다수의 동시 요청 시 최종 재고는 0이 되어야 한다")
        void deductAllConcurrency() throws InterruptedException {
            int qtyPerThread = initQuantity / threadCount;
            ExecutorService executorService = Executors.newFixedThreadPool(32);
            CountDownLatch latch = new CountDownLatch(threadCount);

            IntStream.range(0, threadCount).forEach(i -> executorService.submit(() -> {
                try {
                    String uniqueIdempotencyKey = UUID.randomUUID().toString();
                    InventoryDeductCommand command = new InventoryDeductCommand(
                        uniqueIdempotencyKey,
                        List.of(new Product(productId, hubId, qtyPerThread))
                    );
                    inventoryService.deduct(command);
                } finally {
                    latch.countDown();
                }
            }));
            latch.await();
            executorService.shutdown();

            Inventory inventory = inventoryRepository.findByProductId(productId).orElseThrow();
            assertEquals(0, inventory.getQuantity());
        }
    }

    @Nested
    class Replenish {

        @Test
        @DisplayName("다수의 동시 요청시 최종 재고는 정확히 증가해야 한다")
        void replenishConcurrency() throws InterruptedException {
            int qtyPerThread = initQuantity / threadCount;
            int totalQuantity = qtyPerThread * threadCount;
            ExecutorService executorService = Executors.newFixedThreadPool(32);
            CountDownLatch latch = new CountDownLatch(threadCount);

            IntStream.range(0, threadCount).forEach(i -> executorService.submit(() -> {
                try {
                    String uniqueIdempotencyKey = UUID.randomUUID().toString();
                    InventoryReplenishCommand command = new InventoryReplenishCommand(
                        uniqueIdempotencyKey,
                        List.of(
                            new InventoryReplenishCommand.Product(productId, hubId, qtyPerThread))
                    );
                    inventoryService.replenish(command);
                } finally {
                    latch.countDown();
                }
            }));
            latch.await();
            executorService.shutdown();

            Inventory inventory = inventoryRepository.findByProductId(productId).orElseThrow();
            int expectedQuantity = initQuantity + totalQuantity;
            assertEquals(expectedQuantity, inventory.getQuantity());
        }
    }

    @Nested
    class Idempotency {

        @Test
        @DisplayName("단 하나의 요청만 성공하고 나머지는 중복으로 처리되어야 한다")
        void idempotencyConcurrency() throws InterruptedException {
            String sharedIdempotencyKey = "sharedIdempotencyKey";
            int qtyPerThread = initQuantity / threadCount; // 10개
            InventoryDeductCommand command = new InventoryDeductCommand(
                sharedIdempotencyKey,
                List.of(new Product(productId, hubId, qtyPerThread))
            );
            ExecutorService executorService = Executors.newFixedThreadPool(32);
            CountDownLatch latch = new CountDownLatch(threadCount);
            int[] successCont = {0};
            int[] alreadyCount = {0};

            IntStream.range(0, threadCount).forEach(i -> executorService.submit(() -> {
                try {
                    InventoryDeductResponse response = inventoryService.deduct(command);

                    if (response.status() == Status.SUCCESS) {
                        successCont[0]++;
                    } else if (response.status() == Status.ALREADY_DEDUCTED) {
                        alreadyCount[0]++;
                    }
                } finally {
                    latch.countDown();
                }
            }));
            latch.await();
            executorService.shutdown();

            Inventory inventory = inventoryRepository.findByProductId(productId).orElseThrow();
            InventoryIdempotency inventoryIdempotency = idempotencyRepository.findByIdempotencyKey(
                sharedIdempotencyKey
            ).orElseThrow();
            int expectedQuantity = initQuantity - qtyPerThread; // 990 (처음 10개 재고 차감만 성공)
            assertEquals(1, successCont[0]);
            assertEquals(expectedQuantity, inventory.getQuantity());
            assertEquals(InventoryIdempotencyStatus.SUCCESS, inventoryIdempotency.getStatus());
        }
    }
}
