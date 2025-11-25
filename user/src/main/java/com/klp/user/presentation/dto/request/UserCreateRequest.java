package com.klp.user.presentation.dto.request;

import com.klp.user.domain.enums.AffiliationType;
import com.klp.user.domain.enums.UserRole;

public record UserCreateRequest(
    String username,
    String password,
    String slackId,
    String phone,
    UserRole role,
    String affiliationName,
    AffiliationType affiliationType
) {

}
