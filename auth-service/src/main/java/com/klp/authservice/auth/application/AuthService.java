package com.klp.authservice.auth.application;

import com.klp.authservice.auth.application.client.UserClient;
import com.klp.authservice.auth.application.command.LoginCommand;
import com.klp.authservice.auth.application.command.SignUpCommand;
import com.klp.authservice.auth.domain.entity.BlackListToken;
import com.klp.authservice.auth.domain.repository.BlackListTokenRepository;
import com.klp.authservice.auth.entrypoint.dto.response.LoginResponse;
import com.klp.authservice.auth.entrypoint.dto.response.ReissueResponse;
import com.klp.authservice.auth.exception.AuthErrorCode;
import com.klp.authservice.auth.infrastructure.external.dto.request.UserCreateRequest;
import com.klp.authservice.auth.infrastructure.external.dto.response.UserDataDTO;
import com.klp.authservice.auth.infrastructure.jwt.TokenProvider;
import com.klp.common.exception.BusinessException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserClient userClient;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider accessTokenProvider;
    private final TokenProvider refreshTokenProvider;
    private final BlackListTokenRepository blackListTokenRepository;

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

        validatePassword(command.password(), dto.password());

        String accessToken = accessTokenProvider.generate(dto.userId(), dto.userName(), dto.role());

        return new LoginResponse(dto.userId(), dto.userName(), dto.role(), accessToken);
    }

    @Transactional
    public void logout(String accessToken, String refreshToken) {
        if (!accessTokenProvider.validateToken(accessToken)) {
            throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
        }

        String role = accessTokenProvider.getRole(accessToken);

        if (isAdmin(role)) {
            addBlacklist(accessToken, accessTokenProvider.getExpiration(accessToken));
            addBlacklist(refreshToken, refreshTokenProvider.getExpiration(refreshToken));
        }
    }

    public ReissueResponse reissue(String accessToken, String refreshToken) {
        validateRefreshToken(refreshToken);

        Long userId = Long.valueOf(refreshTokenProvider.getUserId(refreshToken));
        String userName = refreshTokenProvider.getUserName(refreshToken);
        String role = refreshTokenProvider.getRole(refreshToken);

        String newAccessToken = accessTokenProvider.generate(userId, userName, role);

        addBlacklist(refreshToken, refreshTokenProvider.getExpiration(refreshToken));
        if (accessToken != null && !accessToken.isBlank()) {
            addBlacklist(accessToken, accessTokenProvider.getExpiration(accessToken));
        }

        return new ReissueResponse(userId, userName, role, newAccessToken);
    }

    private boolean checkDuplicateUserName(String userName) {
        return userClient.checkUserNameAvailable(userName);
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new BusinessException(AuthErrorCode.INVALID_PASSWORD);
        }
    }

    private boolean isAdmin(String role) {
        return "MASTER".equals(role) || "HUB".equals(role);
    }

    private void validateRefreshToken(String refreshToken) {
        if (!refreshTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(AuthErrorCode.INVALID_TOKEN);
        }

        if (blackListTokenRepository.existsByToken(refreshToken)) {
            throw new BusinessException(AuthErrorCode.TOKEN_ALREADY_BLACKLISTED);
        }
    }

    private void addBlacklist(String token, LocalDateTime expiration) {
        if (blackListTokenRepository.existsByToken(token)) {
            return;
        }

        BlackListToken blackListToken = BlackListToken.create(token, expiration);
        blackListTokenRepository.save(blackListToken);
    }
}
