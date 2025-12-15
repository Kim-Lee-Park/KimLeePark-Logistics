package com.klp.ai.recommendation.application.service;

import com.klp.ai.recommendation.application.dto.OrderedProduct;
import com.klp.ai.recommendation.infrastructure.client.feign.OrderClient;
import com.klp.ai.recommendation.infrastructure.client.feign.dto.response.OrderResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderDataService {

    private final OrderClient orderClient;

    public List<OrderedProduct> getOrderedProducts(UUID orderId) {
        try {
            OrderResponse order = orderClient.getOrder(orderId);

            return order.orderItems().stream()
                .map(item -> new OrderedProduct(
                    item.productId(),
                    item.productName()
                ))
                .toList();
        } catch (Exception e) {
            log.error("주문 조회 실패: orderId={}", orderId, e);
            return List.of();
        }
    }
}
