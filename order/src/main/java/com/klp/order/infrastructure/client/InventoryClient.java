package com.klp.order.infrastructure.client;


import com.klp.global.config.FeignTracingConfig;
import com.klp.global.config.InventoryFeignClientConfig;
import com.klp.order.infrastructure.client.dto.inventory.request.AllocationsProductRequest;
import com.klp.order.infrastructure.client.dto.inventory.request.DeductInventoryRequest;
import com.klp.order.infrastructure.client.dto.inventory.request.ReplenishInventoryRequest;
import com.klp.order.infrastructure.client.dto.inventory.response.AllocationsProductResponse;
import com.klp.order.infrastructure.client.dto.inventory.response.DeductInventoryResponse;
import com.klp.order.infrastructure.client.dto.inventory.response.GetProductResponse;
import com.klp.order.infrastructure.client.dto.inventory.response.ReplenishInventoryResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "hub-service", configuration = {InventoryFeignClientConfig.class,
    FeignTracingConfig.class})
public interface InventoryClient {

    @PostMapping("/v1/inventories/deduct")
    DeductInventoryResponse deductInventory(@RequestBody DeductInventoryRequest request);

    @GetMapping("/v1/products/{productId}")
    GetProductResponse getProductInfo(@PathVariable UUID productId);

    @PostMapping("/v1/inventories/replenish")
    ReplenishInventoryResponse replenishInventory(@RequestBody ReplenishInventoryRequest request);

    @PostMapping("v1/inventories/allocations")
    AllocationsProductResponse allocateProduct(@RequestBody AllocationsProductRequest request);
}
