package com.klp.delivery.delivery.application.service;

import com.klp.delivery.delivery.application.command.DriverCommand;

public interface DriverApiClient {

    DriverCommand findArrivalHubDrivers(String receiverId);

    DriverCommand findDriverAtArrivalHub(Long receiverId);
}
