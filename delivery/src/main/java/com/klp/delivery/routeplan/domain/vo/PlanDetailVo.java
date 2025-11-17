package com.klp.delivery.routeplan.domain.vo;

import java.util.List;
import java.util.UUID;

public record PlanDetailVo (
    UUID hubId,
    List<RouteInfoVo> planItems,
    Long totalDurationMin,
    Double totalDistanceKm
) implements Comparable<PlanDetailVo> {
    @Override
    public int compareTo(PlanDetailVo o) {
        int durationCompare = this.totalDurationMin.compareTo(o.totalDurationMin);

        if (durationCompare != 0) {
            return durationCompare;
        }

        return this.totalDistanceKm.compareTo(o.totalDistanceKm);
    }
}
