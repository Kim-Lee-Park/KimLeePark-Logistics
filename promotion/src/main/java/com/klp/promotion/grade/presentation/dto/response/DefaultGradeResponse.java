package com.klp.promotion.grade.presentation.dto.response;

public record DefaultGradeResponse(
    String gradeName
) {
    public static DefaultGradeResponse of(String gradeName) {
        return new DefaultGradeResponse(gradeName);
    }
}
