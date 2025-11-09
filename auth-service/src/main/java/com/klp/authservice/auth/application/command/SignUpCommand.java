package com.klp.authservice.auth.application.command;

import com.klp.authservice.auth.domain.enums.AffiliationType;

public record SignUpCommand(
    String userName,
    String password,
    String slackId,
    String affiliationName,
    AffiliationType affiliationType
) {

}
