package com.klp.authservice.auth.infrastructure.external.dto.request;

import com.klp.authservice.auth.domain.enums.AffiliationType;

public record UserCreateRequest(
    String username,
    String password,
    String slackId,
    String phone,
    String email,
    String role,
    String affiliationName,
    AffiliationType affiliationType
) {

}
