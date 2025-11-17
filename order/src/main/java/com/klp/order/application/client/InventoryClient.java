package com.klp.order.application.client;


import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "hub-service", url = "http://localhost:8030")
public interface InventoryClient {

    @PostMapping("/v1/inventory/decrease")
    void stockDecrease(@RequestBody DecreaseStockRequest request);

    @GetMapping("/v1/product/{productId}")
    GetProductResponse getProductInfo(@PathVariable UUID productId);
}
