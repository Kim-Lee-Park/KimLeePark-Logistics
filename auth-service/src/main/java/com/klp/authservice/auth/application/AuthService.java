package com.klp.authservice.auth.application;

import com.klp.authservice.auth.AuthErrorCode;
import com.klp.authservice.auth.application.client.UserClient;
import com.klp.authservice.auth.application.command.LoginCommand;
import com.klp.authservice.auth.application.command.SignUpCommand;
import com.klp.authservice.auth.entrypoint.dto.response.LoginResponse;
import com.klp.authservice.auth.infrastructure.external.dto.request.UserCreateRequest;
import com.klp.authservice.auth.infrastructure.external.dto.response.UserDataDTO;
import com.klp.authservice.auth.infrastructure.jwt.TokenProvider;
import com.klp.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserClient userClient;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider accessTokenProvider;

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

    public LoginResponse login(LoginCommand command) {
        UserDataDTO dto = userClient.getUserByUserName(command.userName());

        if (!passwordEncoder.matches(command.password(), dto.password())) {
            throw new BusinessException(AuthErrorCode.INVALID_PASSWORD);
        }

        String accessToken = accessTokenProvider.generate(dto.userId(), dto.userName(), dto.role());

        return new LoginResponse(dto.userId(), dto.userName(), dto.role(), accessToken);
    }

    private boolean checkDuplicateUserName(String userName) {
        return userClient.checkUserNameAvailable(userName);
    }
}
