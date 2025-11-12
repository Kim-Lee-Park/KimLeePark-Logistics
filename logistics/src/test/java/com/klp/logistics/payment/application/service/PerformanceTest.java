package com.klp.logistics.payment.application.service;

import com.klp.logistics.order.application.service.OrderService;
import com.klp.logistics.order.application.service.dto.OrderCreateCommand;
import com.klp.logistics.order.application.service.dto.OrderCreateCommand.Product;
import com.klp.logistics.order.infrastructure.repository.OrderRepository;
import com.klp.logistics.util.PerformanceMonitor;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PerformanceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PerformanceMonitor performanceMonitor;

    @Autowired
    private OrderRepository orderRepository;

    private Long supplierId = 1L;
    private Long customerId = 1L;
    private OrderCreateCommand.Product product = new Product(
        UUID.randomUUID(),
        10,
        10
    );
    private String comments = "comments";

    @AfterEach
    public void tearDown() {
        orderRepository.deleteAll();
        performanceMonitor.printStatistics();
    }

    @Test
    void performance() throws InterruptedException {
        int numberOfThreads = 50;   // 동시 요청 수
        int numberOfRequests = 100;  // 총 요청 수

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfRequests);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfRequests; i++) {
            executorService.submit(() -> {
                try {
                    OrderCreateCommand command = new OrderCreateCommand(supplierId, customerId,
                        List.of(product), comments);
                    orderService.createOrder(command);
                } catch (Exception e) {
                    System.err.println("주문 생성 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();  // 모든 요청 완료 대기
        executorService.shutdown();

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        System.out.println("========== 전체 테스트 결과 ==========");
        System.out.println("총 요청 수: " + numberOfRequests);
        System.out.println("동시 스레드 수: " + numberOfThreads);
        System.out.println("총 소요 시간: " + totalTime + "ms");
        System.out.println("평균 응답 시간: " + (totalTime / numberOfRequests) + "ms");
        System.out.println("=====================================");
    }
}
