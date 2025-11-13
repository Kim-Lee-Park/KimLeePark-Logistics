package com.klp.order;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.doThrow;

import com.klp.order.order.application.service.OrderService;
import com.klp.order.order.application.service.dto.OrderCreateCommand;
import com.klp.order.order.application.service.dto.OrderResponse;
import com.klp.order.order.domain.entity.order.Order;
import com.klp.order.order.infrastructure.repository.OrderRepository;
import com.klp.order.payment.domain.entity.Payment;
import com.klp.order.payment.infrastructure.clients.ExternalPaymentClient;
import com.klp.order.payment.infrastructure.repository.PaymentRepository;
import com.klp.order.product.domain.entity.Product;
import com.klp.order.product.infrastructure.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
public class OrderEventFlowIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ProductRepository productRepository;

    @MockitoBean
    private ExternalPaymentClient externalPaymentClient;

    private Long supplierId = 1L;

    private Long customerId = 1L;

    private String comments = "comments";

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        paymentRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("주문 생성 -> 결제 처리 -> 재고 차감 순서로 이벤트 플로우가 정상 동작한다")
    void createOrder() {
        int price = 1000;
        int initStock = 1;
        int decreaseStock = 1;
        Product savedProduct = createProduct(initStock);
        OrderCreateCommand orderCreateCommand = createOrderCommand(
            savedProduct,
            decreaseStock,
            price
        );

        OrderResponse response = orderService.createOrder(orderCreateCommand);

        Order order = orderRepository.findById(response.orderId()).orElseThrow();
        assertThat(order).isNotNull();
        await()
            .atMost(Duration.ofSeconds(5))
            .pollInterval(Duration.ofMillis(100))
            .untilAsserted(() -> {
                Payment payment = paymentRepository.findByOrderId(order.getOrderId()).orElseThrow();
                assertThat(payment).isNotNull();
                assertThat(payment.getTotalAmount().compareTo(new BigDecimal(price))).isZero();
            });

        await()
            .atMost(Duration.ofSeconds(5))
            .pollInterval(Duration.ofMillis(100))
            .untilAsserted(() -> {
                Product product = productRepository.findById(savedProduct.getProductId())
                    .orElseThrow();
                assertThat(product.getStock()).isEqualTo(
                    initStock - decreaseStock
                );
            });
    }

    @Test
    @DisplayName("주문 생성 실패시 결제, 재고 차감은 수행되지 않는다")
    void createOrderFailed() {
        int initStock = 1;
        Product savedProduct = createProduct(initStock);
        OrderCreateCommand invalidCommand = new OrderCreateCommand(
            supplierId,
            customerId,
            List.of(),
            comments
        );

        assertThatThrownBy(() -> orderService.createOrder(invalidCommand))
            .isInstanceOf(RuntimeException.class);
        List<Order> orders = orderRepository.findAll();
        assertThat(orders).isEmpty();
        await()
            .atMost(Duration.ofSeconds(5))
            .pollInterval(Duration.ofMillis(100))
            .untilAsserted(() -> {
                List<Payment> payments = paymentRepository.findAll();
                assertThat(payments).isEmpty();
            });
        await()
            .atMost(Duration.ofSeconds(5))
            .pollInterval(Duration.ofMillis(100))
            .untilAsserted(() -> {
                Product product = productRepository.findById(savedProduct.getProductId())
                    .orElseThrow();
                assertThat(product.getStock()).isEqualTo(initStock);
            });
    }

    @Test
    @DisplayName("주문 생성 -> 결제 실패시 재고 차감은 수행되지 않는다")
    void paymentFailed() {
        int initStock = 1;
        int price = 1000;
        int decreaseStock = 1;
        Product savedProduct = createProduct(initStock);
        OrderCreateCommand orderCreateCommand = createOrderCommand(
            savedProduct,
            decreaseStock,
            price
        );
        doThrow(new RuntimeException("결제 실패"))
            .when(externalPaymentClient).payment();

        OrderResponse response = orderService.createOrder(orderCreateCommand);

        Order order = orderRepository.findById(response.orderId()).orElseThrow();
        assertThat(order).isNotNull();
        await()
            .atMost(Duration.ofSeconds(5))
            .pollInterval(Duration.ofMillis(100))
            .untilAsserted(() -> {
                List<Payment> payments = paymentRepository.findAll();
                assertThat(payments).isEmpty();
            });

        await()
            .atMost(Duration.ofSeconds(5))
            .pollInterval(Duration.ofMillis(100))
            .untilAsserted(() -> {
                Product product = productRepository.findById(savedProduct.getProductId())
                    .orElseThrow();
                assertThat(product.getStock()).isEqualTo(initStock);
            });
    }

    @Test
    @DisplayName("주문 생성 -> 결제 성공 -> 재고 차감 실패시 주문과 결제는 생성된다")
    void decreaseStockFailed() {
        int initStock = 1;
        int price = 1000;
        int decreaseStock = 2;
        Product savedProduct = createProduct(initStock);
        OrderCreateCommand orderCreateCommand = createOrderCommand(
            savedProduct,
            decreaseStock,
            price
        );

        OrderResponse response = orderService.createOrder(orderCreateCommand);

        Order order = orderRepository.findById(response.orderId()).orElseThrow();
        assertThat(order).isNotNull();
        await()
            .atMost(Duration.ofSeconds(5))
            .pollInterval(Duration.ofMillis(100))
            .untilAsserted(() -> {
                Payment payment = paymentRepository.findByOrderId(order.getOrderId()).orElseThrow();
                assertThat(payment).isNotNull();
            });

        await()
            .atMost(Duration.ofSeconds(5))
            .pollInterval(Duration.ofMillis(100))
            .untilAsserted(() -> {
                Product product = productRepository.findById(savedProduct.getProductId())
                    .orElseThrow();
                assertThat(product.getStock()).isEqualTo(initStock);
            });
    }

    private Product createProduct(int initStock) {
        Product savedProduct = productRepository.save(new Product(
            "상품명",
            initStock
        ));
        return savedProduct;
    }

    private OrderCreateCommand createOrderCommand(
        Product savedProduct,
        int decreaseStock,
        int price
    ) {
        return new OrderCreateCommand(
            supplierId,
            customerId,
            List.of(new OrderCreateCommand.Product(
                savedProduct.getProductId(),
                decreaseStock,
                price
            )),
            comments
        );
    }
}
