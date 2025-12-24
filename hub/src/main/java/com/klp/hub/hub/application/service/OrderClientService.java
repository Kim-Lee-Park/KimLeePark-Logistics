package com.klp.hub.hub.application.service;

import java.util.UUID;

public interface OrderClientService {

    boolean hasProgressingOrders(UUID hubId);
}
