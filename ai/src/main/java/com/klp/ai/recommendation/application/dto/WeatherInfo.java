package com.klp.ai.recommendation.application.dto;

public record WeatherInfo(
    String condition,
    double temperature,
    int humidity,
    String description
) {

    public boolean isRainyOrSnowy() {
        if (condition == null) {
            return false;
        }
        String upper = condition.toUpperCase();
        return upper.contains("RAIN")
            || upper.contains("SNOW")
            || upper.contains("DRIZZLE")
            || upper.contains("THUNDERSTORM");
    }

    public boolean isHot() {
        return temperature >= 30;
    }

    public boolean isCold() {
        return temperature <= 5;
    }

}
