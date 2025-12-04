package com.klp.payment.infrastructure.client.dto;

import java.util.UUID;

public record UserDetailResponse(
    Long userId,
    UUID affiliationId,
    String affiliationType,
    String affiliationName,
    String username,
    String slackId,
    String phone,
    String email,
    String role,
    boolean activate
) {

}
