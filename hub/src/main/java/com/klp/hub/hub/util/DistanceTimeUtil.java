package com.klp.hub.hub.util;

public final class DistanceTimeUtil {

    private DistanceTimeUtil() {}

    /** 지구 반지름 (m) — 평균값 */
    private static final double EARTH_RADIUS_M = 6_371_000.0;

    /** 기준 시속 (km/h) */
    private static final double AVG_SPEED_KMH = 70.0;

    /**
     * 두 좌표 간 직선거리 (Haversine) 계산
     * @return 거리 (meter)
     */
    public static long calculateDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double dPhi = Math.toRadians(lat2 - lat1);
        double dLam = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dPhi / 2) * Math.sin(dPhi / 2)
            + Math.cos(phi1) * Math.cos(phi2)
            * Math.sin(dLam / 2) * Math.sin(dLam / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return Math.round(EARTH_RADIUS_M * c);
    }

    /**
     * 직선거리 (m)를 km 단위로 반환 (소수 2자리)
     */
    public static double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        long meters = calculateDistanceMeters(lat1, lon1, lat2, lon2);
        return Math.round((meters / 1000.0) * 100) / 100.0;
    }

    /**
     * 직선거리와 평균속도(70km/h)로 예상 소요시간 계산
     * @return 시간(분)
     */
    public static long estimateDurationMinutes(double lat1, double lon1, double lat2, double lon2) {
        long distanceMeters = calculateDistanceMeters(lat1, lon1, lat2, lon2);
        double distanceKm = distanceMeters / 1000.0;
        double hours = distanceKm / AVG_SPEED_KMH;
        return Math.round(hours * 60); // 분 단위
    }

    /**
     * 거리(km)와 예상 소요시간(분)을 함께 리턴
     */
    public static DistanceTimeResult calculateDistanceAndTime(double lat1, double lon1, double lat2, double lon2) {
        double distanceKm = calculateDistanceKm(lat1, lon1, lat2, lon2);
        long durationMin = estimateDurationMinutes(lat1, lon1, lat2, lon2);
        return new DistanceTimeResult(distanceKm, durationMin);
    }

    /** DTO record: 거리/시간 결과 */
    public record DistanceTimeResult(double distanceKm, long durationMin) {}
}
