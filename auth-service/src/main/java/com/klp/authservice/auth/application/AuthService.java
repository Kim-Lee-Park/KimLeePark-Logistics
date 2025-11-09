package com.klp.authservice.auth.application;

import com.klp.authservice.auth.AuthErrorCode;
import com.klp.authservice.auth.application.client.UserClient;
import com.klp.authservice.auth.application.command.SignUpCommand;
import com.klp.authservice.auth.infrastructure.external.dto.request.UserCreateRequest;
import com.klp.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserClient userClient;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입: 유저 이름 중복 확인 요청 -> 패스워드 암호화 -> 유저 생성 요청
     */
    public void signUp(SignUpCommand command) {
        if (checkDuplicateUserName(command.userName())) {
            throw new BusinessException(AuthErrorCode.USERNAME_IS_EXIST);
        }

        String encodedPassword = passwordEncoder.encode(command.password());

        UserCreateRequest request = new UserCreateRequest(
            command.userName(),
            encodedPassword,
            command.slackId(),
            command.affiliationName(),
            command.affiliationType()
        );

        userClient.createUser(request);
    }

    private boolean checkDuplicateUserName(String userName) {
        return userClient.checkUserNameAvailable(userName);
    }
}
