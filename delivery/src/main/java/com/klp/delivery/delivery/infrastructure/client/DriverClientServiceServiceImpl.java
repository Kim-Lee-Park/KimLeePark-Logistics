package com.klp.delivery.delivery.infrastructure.client;

import com.klp.delivery.delivery.application.service.DriverClientService;
import com.klp.delivery.delivery.infrastructure.client.dto.DriverResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class DriverClientServiceServiceImpl implements DriverClientService {

    private final DriverFeignClient driverFeignClient;

    @Override
    public List<DriverResponse> findArrivalHubDrivers(UUID hubId) {
        return driverFeignClient.findArrivalHubDrivers(hubId);
    }

    @Override
    public DriverResponse findDriverAtArrivalHub(Long receiverId) {
        return driverFeignClient.findDriverAtArrivalHub(receiverId);
    }
}
