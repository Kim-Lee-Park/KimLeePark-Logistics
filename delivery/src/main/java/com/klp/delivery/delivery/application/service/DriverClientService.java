package com.klp.delivery.delivery.application.service;

import com.klp.delivery.delivery.infrastructure.client.dto.DriverResponse;
import java.util.List;
import java.util.UUID;

public interface DriverClientService {

    List<DriverResponse> findArrivalHubDrivers(UUID receiverId);

    DriverResponse findDriverAtArrivalHub(Long receiverId);

    List<DriverResponse> findLogisticsDrivers();
}
