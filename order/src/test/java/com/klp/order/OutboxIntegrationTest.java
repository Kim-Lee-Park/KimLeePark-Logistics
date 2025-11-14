package com.klp.order;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.klp.order.order.application.service.OrderService;
import com.klp.order.order.application.service.dto.OrderCreateCommand;
import com.klp.order.order.application.service.dto.OrderResponse;
import com.klp.order.outbox.domain.entity.OutboxEvent;
import com.klp.order.outbox.domain.entity.OutboxStatus;
import com.klp.order.outbox.infrastructure.repository.OutboxRepository;
import com.klp.order.payment.domain.entity.Payment;
import com.klp.order.payment.infrastructure.clients.ExternalPaymentClient;
import com.klp.order.payment.infrastructure.repository.PaymentRepository;
import com.klp.order.product.domain.entity.Product;
import com.klp.order.product.infrastructure.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
public class OutboxIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @MockitoBean
    private ExternalPaymentClient externalPaymentClient;

    private Long supplierId = 1L;

    private Long customerId = 1L;

    private String comments = "comments";

    @Test
    void test() {
        int initStock = 1;
        Product savedProduct = createProduct(initStock);
        OrderCreateCommand orderCreateCommand = new OrderCreateCommand(
            supplierId,
            customerId,
            List.of(new OrderCreateCommand.Product(
                savedProduct.getProductId(),
                1,
                1000
            )),
            comments
        );

        OrderResponse response = orderService.createOrder(orderCreateCommand);

        await()
            .atMost(Duration.ofSeconds(10))
            .pollInterval(Duration.ofMillis(100))
            .untilAsserted(() -> {
                List<OutboxEvent> outboxEvents = outboxRepository.findAll();
                assertThat(outboxEvents).hasSize(1);
                assertThat(outboxEvents.get(0).getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
            });

        await()
            .atMost(Duration.ofSeconds(10))
            .pollInterval(Duration.ofMillis(100))
            .untilAsserted(() -> {
                Payment payment = paymentRepository.findByOrderId(response.orderId()).orElseThrow();
                assertThat(payment).isNotNull();
            });
    }

    private Product createProduct(int initStock) {
        Product savedProduct = productRepository.save(new Product(
            "상품명",
            initStock
        ));
        return savedProduct;
    }
}
