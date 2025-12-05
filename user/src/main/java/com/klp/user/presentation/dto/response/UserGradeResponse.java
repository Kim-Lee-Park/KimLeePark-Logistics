package com.klp.user.presentation.dto.response;

import com.klp.user.domain.entity.UserGrade;
import java.time.LocalDateTime;

public record UserGradeResponse(
    Long userId,
    String gradeName,
    LocalDateTime evaluatedAt
) {

    public static UserGradeResponse from(UserGrade userGrade) {
        return new UserGradeResponse(
            userGrade.getUser().getUserId(),
            userGrade.getGradeName(),
            userGrade.getEvaluatedAt()
        );
    }
}
