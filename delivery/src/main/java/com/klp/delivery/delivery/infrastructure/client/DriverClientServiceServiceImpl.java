package com.klp.delivery.delivery.infrastructure.client;

import com.klp.delivery.delivery.application.service.DriverClientService;
import com.klp.delivery.delivery.infrastructure.client.dto.DriverInfo;
import com.klp.delivery.delivery.infrastructure.client.dto.HubDriverListResponse;
import com.klp.delivery.delivery.infrastructure.client.dto.LogisticsDriverListResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class DriverClientServiceServiceImpl implements DriverClientService {

    private final DriverFeignClient driverFeignClient;

    @Override
    public HubDriverListResponse findArrivalHubDrivers(UUID hubId) {
        return driverFeignClient.findArrivalHubDrivers(hubId);
    }

    @Override
    public DriverInfo findDriverAtArrivalHub(Long receiverId) {
        return driverFeignClient.findDriverAtArrivalHub(receiverId);
    }

    @Override
    public LogisticsDriverListResponse findLogisticsDrivers() {
        return driverFeignClient.findLogisticsDrivers();
    }
}
