package com.klp.delivery.delivery.infrastructure.client;

import com.klp.delivery.delivery.infrastructure.client.dto.DriverResponse;
import com.klp.delivery.global.config.DeliveryFeignClientConfig;
import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service",
    configuration = DeliveryFeignClientConfig.class)
public interface DriverFeignClient {

    @GetMapping("/v1/users/driver/{hubId}")
    List<DriverResponse> findArrivalHubDrivers(@PathVariable("hubId") UUID hubId);

    @GetMapping("/v1/users/driver")
    DriverResponse findDriverAtArrivalHub(@RequestParam("id") Long driverId);

    @GetMapping("/driver/logistics")
    List<DriverResponse> findLogisticsDrivers();

}
