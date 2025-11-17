package com.klp.authservice.auth.application.command;

import com.klp.authservice.auth.domain.enums.AffiliationType;

public record SignUpCommand(
    String username,
    String password,
    String slackId,
    String affiliationName,
    AffiliationType affiliationType
) {

}
