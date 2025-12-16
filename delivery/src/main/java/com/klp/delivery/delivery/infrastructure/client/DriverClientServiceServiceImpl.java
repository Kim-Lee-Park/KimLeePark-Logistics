package com.klp.delivery.delivery.infrastructure.client;

import com.klp.delivery.delivery.application.service.DriverClientService;
import com.klp.delivery.delivery.infrastructure.client.dto.DriverResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class DriverClientServiceServiceImpl implements DriverClientService {

    private final DriverFeignClient driverFeignClient;

    @Override
    @CircuitBreaker(name = "userService")
    @Retry(name = "userService")
    public List<DriverResponse> findArrivalHubDrivers(UUID hubId) {
        return driverFeignClient.findArrivalHubDrivers(hubId);
    }

    @Override
    @CircuitBreaker(name = "userService")
    @Retry(name = "userService")
    public DriverResponse findDriverAtArrivalHub(Long receiverId) {
        return driverFeignClient.findDriverAtArrivalHub(receiverId);
    }

    @Override
    @CircuitBreaker(name = "userService")
    @Retry(name = "userService")
    public List<DriverResponse> findLogisticsDrivers() {
        return driverFeignClient.findLogisticsDrivers();
    }
}
