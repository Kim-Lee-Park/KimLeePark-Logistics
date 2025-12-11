package com.klp.hub.hub.infrastructure.client;

import com.klp.hub.global.config.FeignTracingConfig;
import com.klp.hub.global.config.OrderFeignClientConfig;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Component
@FeignClient(name = "order-service", configuration = {OrderFeignClientConfig.class,
    FeignTracingConfig.class})
public interface OrderFeignClient {

    @GetMapping("/v1/orders/progressing")
    boolean hasProgressingOrder(@RequestParam UUID hubId);
}
