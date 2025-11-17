package com.klp.delivery.routeplan.domain.policy;

import org.springframework.stereotype.Component;

@Component
public class RoutePlanPolicy {

    private static final double DISTANCE_THRESHOLD_KM = 170.0;

    public boolean isDirectAllowed(double distanceKm) {
        return distanceKm < DISTANCE_THRESHOLD_KM;
    }
}
