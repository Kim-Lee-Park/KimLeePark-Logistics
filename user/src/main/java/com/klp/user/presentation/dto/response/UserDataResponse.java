package com.klp.user.presentation.dto.response;

import com.klp.user.domain.entity.User;

public record UserDataResponse(
    Long userId,
    String userName,
    String password,
    String role
) {

    public static UserDataResponse from(User user) {
        return new UserDataResponse(
            user.getUserId(),
            user.getName(),
            user.getPassword(),
            user.getRole().name()
        );
    }
}
