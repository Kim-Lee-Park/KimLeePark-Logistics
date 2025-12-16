package com.klp.delivery.delivery.application.service;

import com.klp.delivery.delivery.infrastructure.client.dto.DriverInfo;
import com.klp.delivery.delivery.infrastructure.client.dto.HubDriverListResponse;
import com.klp.delivery.delivery.infrastructure.client.dto.LogisticsDriverListResponse;
import java.util.List;
import java.util.UUID;

public interface DriverClientService {

    HubDriverListResponse findArrivalHubDrivers(UUID receiverId);

    DriverInfo findDriverAtArrivalHub(Long receiverId);

    LogisticsDriverListResponse findLogisticsDrivers();
}
