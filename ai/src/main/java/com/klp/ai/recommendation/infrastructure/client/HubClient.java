package com.klp.ai.recommendation.infrastructure.client;

import com.klp.ai.global.config.HubFeignClientConfig;
import com.klp.ai.recommendation.infrastructure.client.dto.response.HubResponse;
import com.klp.ai.recommendation.infrastructure.client.dto.response.ProductResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "hub-service", url = "${clients.hub.url:}", configuration = HubFeignClientConfig.class)
public interface HubClient {

    @GetMapping("/v1/products/{productId}")
    ProductResponse getProduct(@PathVariable UUID productId);

    @GetMapping("/v1/hubs/{hubId}")
    HubResponse getHub(@PathVariable UUID hubId);

    @GetMapping("/v1/inventories/{productId}")
    InventoryResponse getInventory(@PathVariable UUID productId);
}
