package com.klp.ai.recommendation.application.dto;

import java.time.LocalTime;

public enum TimeSlot {
    MORNING("출근 시간대", "출퇴근용품, 커피, 간편식, 우산"),
    DAYTIME("낮 시간대", "일반 상품, 점심 메뉴"),
    EVENING("퇴근 시간대", "여가용품, 운동용품, 간식, 편의점 음식"),
    NIGHT("밤 시간대", "야식, 편의점 상품");

    private final String description;
    private final String recommendedCategories;

    TimeSlot(String description, String recommendedCategories) {
        this.description = description;
        this.recommendedCategories = recommendedCategories;
    }

    public static TimeSlot from(LocalTime time) {
        int hour = time.getHour();

        if (hour >= 6 && hour < 10) {
            return MORNING;
        }
        if (hour >= 10 && hour < 18) {
            return DAYTIME;
        }
        if (hour >= 18 && hour < 22) {
            return EVENING;
        }
        return NIGHT;
    }

    public static TimeSlot now() {
        return from(LocalTime.now());
    }

    public String getDescription() {
        return description;
    }

    public String getRecommendedCategories() {
        return recommendedCategories;
    }
}
