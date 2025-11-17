package com.klp.order.application.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "delivery-service", url = "http://localhost:8040")
public interface DeliveryClient {

    @PostMapping("/v1/deliveries")
    CreateDeliveryResponse createDelivery(@RequestBody CreateDeliveryRequest request);
}
