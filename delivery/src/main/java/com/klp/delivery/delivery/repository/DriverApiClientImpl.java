package com.klp.delivery.delivery.repository;

import com.klp.delivery.delivery.application.service.DriverApiClient;
import com.klp.delivery.delivery.domain.Driver;
import org.springframework.stereotype.Component;


@Component
public class DriverApiClientImpl implements DriverApiClient {


  @Override
  public Driver findDriver(String receiverId) {
    return null;
  }
}
