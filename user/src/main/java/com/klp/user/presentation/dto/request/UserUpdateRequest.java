package com.klp.user.presentation.dto.request;

import com.klp.user.domain.enums.UserRole;

public record UserUpdateRequest(
    String username,
    String password,
    String slackId,
    String phone,
    UserRole role
) {

}
