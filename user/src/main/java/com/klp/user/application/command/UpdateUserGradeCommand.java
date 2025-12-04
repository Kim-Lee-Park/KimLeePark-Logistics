package com.klp.user.application.command;

public record UpdateUserGradeCommand(
    Long userId,
    String gradeName
) {

}
