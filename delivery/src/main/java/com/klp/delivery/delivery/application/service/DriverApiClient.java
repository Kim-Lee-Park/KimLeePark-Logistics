package com.klp.delivery.delivery.application.service;

import com.klp.delivery.delivery.domain.Driver;

public interface DriverApiClient {

  Driver findDriver(String receiverId);
}
