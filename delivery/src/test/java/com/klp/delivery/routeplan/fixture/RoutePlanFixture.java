package com.klp.delivery.routeplan.fixture;

import com.klp.delivery.routeplan.domain.model.RoutePlan;
import java.util.UUID;

public class RoutePlanFixture {
    public static UUID ROUTE_PLAN_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static UUID DEPARTURE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static UUID ARRIVAL_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    public static Long TOTAL_DURATION = 30L;
    public static Double TOTAL_DISTANCE = 100.;

    public static RoutePlan createRoutePlan(){
        return RoutePlan.create(DEPARTURE_ID, ARRIVAL_ID, TOTAL_DURATION, TOTAL_DISTANCE);
    }
}
