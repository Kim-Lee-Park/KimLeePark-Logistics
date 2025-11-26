package com.klp.delivery.routeplan.application.service;

import java.util.UUID;

public interface DeliveryClientService {

    boolean hasActiveDeliveries(UUID routePlanId);
}
