package com.klp.ai.recommendation.infrastructure.client.rest;

import com.klp.ai.recommendation.application.dto.WeatherInfo;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
public class WeatherClient {

    private final RestClient weatherRestClient;
    private final String serviceKey;

    public WeatherClient(
        RestClient weatherRestClient,
        @Value("${weather.api.service-key:}") String serviceKey
    ) {
        this.weatherRestClient = weatherRestClient;
        this.serviceKey = serviceKey;
    }

    public WeatherInfo getCurrentWeather(double latitude, double longitude) {
        if (serviceKey == null || serviceKey.isBlank()) {
            log.warn("기상청 API를 이용할 수 없습니다. 날씨 정보 없이 진행합니다.");
            return null;
        }

        try {
            log.info("latitud: {}, longitude: {}", latitude, longitude);

            int[] grid = convertToGrid(latitude, longitude);
            String[] baseDateTime = getBaseDateTime();

            log.info("기상청 API 요청 파라미터: serviceKey={}, nx={}, ny={}, base_date={}, base_time={}",
                serviceKey, grid[0], grid[1], baseDateTime[0], baseDateTime[1]);

            Map<String, Object> response = weatherRestClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/getUltraSrtNcst")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("pageNo", 1)
                    .queryParam("numOfRows", 10)
                    .queryParam("dataType", "JSON")
                    .queryParam("base_date", baseDateTime[0])
                    .queryParam("base_time", baseDateTime[1])
                    .queryParam("nx", grid[0])
                    .queryParam("ny", grid[1])
                    .build())
                .retrieve()
                .body(Map.class);

            log.info("기상청 API 응답: {}", response);

            return parseResponse(response);
        } catch (RestClientException e) {
            log.error("기상청 API 호출 실패: lat={}, lon={}, error={}",
                latitude, longitude, e.getMessage());
            return null;
        }
    }

    /**
     * 위경도를 기상청 격자 좌표로 변환
     */
    private int[] convertToGrid(double lat, double lon) {
        double RE = 6371.00877;
        double GRID = 5.0;
        double SLAT1 = 30.0;
        double SLAT2 = 60.0;
        double OLON = 126.0;
        double OLAT = 38.0;
        double XO = 43;
        double YO = 136;

        double DEGRAD = Math.PI / 180.0;

        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon = OLON * DEGRAD;
        double olat = OLAT * DEGRAD;

        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);

        double ra = Math.tan(Math.PI * 0.25 + lat * DEGRAD * 0.5);
        ra = re * sf / Math.pow(ra, sn);
        double theta = lon * DEGRAD - olon;
        if (theta > Math.PI) {
            theta -= 2.0 * Math.PI;
        }
        if (theta < -Math.PI) {
            theta += 2.0 * Math.PI;
        }
        theta *= sn;

        int nx = (int) Math.floor(ra * Math.sin(theta) + XO + 0.5);
        int ny = (int) Math.floor(ro - ra * Math.cos(theta) + YO + 0.5);

        return new int[]{nx, ny};
    }

    /**
     * 초단기실황 발표 시각 계산 (매시 정각 발표, 40분 이후 제공)
     */
    private String[] getBaseDateTime() {
        LocalDateTime now = LocalDateTime.now();

        if (now.getMinute() < 40) {
            now = now.minusHours(1);
        }

        String baseDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = now.format(DateTimeFormatter.ofPattern("HH")) + "00";

        return new String[]{baseDate, baseTime};
    }

    @SuppressWarnings("unchecked")
    private WeatherInfo parseResponse(Map<String, Object> response) {
        if (response == null) {
            return null;
        }

        try {
            Map<String, Object> responseBody = (Map<String, Object>) response.get("response");
            Map<String, Object> body = (Map<String, Object>) responseBody.get("body");
            Map<String, Object> items = (Map<String, Object>) body.get("items");
            List<Map<String, Object>> itemList = (List<Map<String, Object>>) items.get("item");

            double temperature = 0.0;
            int humidity = 0;
            String ptyCode = "0";

            for (Map<String, Object> item : itemList) {
                String category = (String) item.get("category");
                String obsrValue = String.valueOf(item.get("obsrValue"));

                switch (category) {
                    case "T1H" -> temperature = Double.parseDouble(obsrValue);
                    case "REH" -> humidity = Integer.parseInt(obsrValue);
                    case "PTY" -> ptyCode = obsrValue;
                }
            }

            String condition = convertPtyToCondition(ptyCode);
            String description = convertPtyToDescription(ptyCode);

            return new WeatherInfo(condition, temperature, humidity, description);

        } catch (Exception e) {
            log.error("기상청 API 응답 파싱 실패: {}", e.getMessage());
            return null;
        }
    }

    private String convertPtyToCondition(String ptyCode) {
        return switch (ptyCode) {
            case "1", "5" -> "RAIN";
            case "2", "6" -> "SLEET";
            case "3", "7" -> "SNOW";
            default -> "CLEAR";
        };
    }

    private String convertPtyToDescription(String ptyCode) {
        return switch (ptyCode) {
            case "1" -> "비";
            case "2" -> "비/눈";
            case "3" -> "눈";
            case "5" -> "빗방울";
            case "6" -> "빗방울눈날림";
            case "7" -> "눈날림";
            default -> "맑음";
        };
    }
}
