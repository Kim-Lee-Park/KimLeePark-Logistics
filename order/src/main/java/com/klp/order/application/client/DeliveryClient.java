package com.klp.order.application.client;

import com.klp.order.application.client.dto.delivery.request.CreateDeliveryRequest;
import com.klp.order.application.client.dto.delivery.response.CreateDeliveryResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "delivery-service", url = "http://localhost:8040")
public interface DeliveryClient {

    @PostMapping("/v1/deliveries")
    CreateDeliveryResponse createDelivery(@RequestBody CreateDeliveryRequest request);

    @DeleteMapping("/v1/deliveries/{deliveryId}")
    void deleteDelivery(@PathVariable("deliveryId") UUID deliveryId);

}
