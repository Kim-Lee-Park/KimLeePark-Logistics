package com.klp.delivery.delivery.repository;

import com.klp.delivery.delivery.application.service.DriverApiClient;
import com.klp.delivery.delivery.application.command.DriverCommand;
import org.springframework.stereotype.Component;


@Component
public class DriverApiClientImpl implements DriverApiClient {


  @Override
  public DriverCommand findDriver(String receiverId) {
    return null;
  }
}
