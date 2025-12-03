package com.klp.user.infrastructure.client.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserGradeResponse(
    UUID userGradeId,
    Long userId,
    String gradeType,
    String gradeDisplayName,
    Integer discountRate,
    LocalDateTime evaluatedAt
) {

}
