package com.klp.authservice.auth.infrastructure.external.dto.request;

import com.klp.authservice.auth.domain.enums.AffiliationType;

public record UserCreateRequest(
    String userName,
    String password,
    String slackId,
    String affiliationName,
    AffiliationType affiliationType
) {

}
