package com.klp.hub.hub.infrastructure.client;

import com.klp.hub.global.config.DeliveryFeignClientConfig;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Component
@FeignClient(name = "delivery-service", url = "${clients.delivery.url:}", configuration = {DeliveryFeignClientConfig.class,
    FeignTracingConfig.class})
public interface DeliveryFeignClient {

    @DeleteMapping("/v1/routes/plans/delete/{hubId}")
    void deleteRoutePlansByHubId(@PathVariable("hubId") UUID hubId);
}
