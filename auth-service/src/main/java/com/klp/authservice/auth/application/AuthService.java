package com.klp.authservice.auth.application;

import com.klp.authservice.auth.application.client.UserClient;
import com.klp.authservice.auth.application.command.LoginCommand;
import com.klp.authservice.auth.application.command.SignUpCommand;
import com.klp.authservice.auth.domain.entity.BlackListToken;
import com.klp.authservice.auth.domain.repository.BlackListTokenRepository;
import com.klp.authservice.auth.exception.AuthErrorCode;
import com.klp.authservice.auth.infrastructure.external.dto.request.UserCreateRequest;
import com.klp.authservice.auth.infrastructure.external.dto.response.UserDataResponse;
import com.klp.authservice.auth.infrastructure.external.dto.response.UsernameDuplicateResponse;
import com.klp.authservice.auth.infrastructure.jwt.TokenProvider;
import com.klp.authservice.auth.presentation.dto.response.LoginResponse;
import com.klp.authservice.auth.presentation.dto.response.ReissueResponse;
import com.klp.common.exception.BusinessException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserClient userClient;
    private final TokenProvider accessTokenProvider;
    private final TokenProvider refreshTokenProvider;
    private final BlackListTokenRepository blackListTokenRepository;

    /**
     * 회원가입: 유저 이름 중복 확인 요청 -> 패스워드 암호화 -> 유저 생성 요청
     */
    public void signUp(SignUpCommand command) {
        if (!availableUsername(command.username())) {
            throw new BusinessException(AuthErrorCode.USERNAME_IS_EXIST);
        }

        UserCreateRequest request = new UserCreateRequest(
            command.username(),
            command.password(),
            command.slackId(),
            command.phone(),
            command.role(),
            command.affiliationName(),
            command.affiliationType()
        );

        userClient.createUser(request);
    }

    /**
     * 로그인: 유저 자격 증명 검증 요청 -> 액세스 토큰 생성
     */
    public LoginResponse login(LoginCommand command) {
        UserDataResponse dto = userClient.validateUserCredentials(command.username(), command.password());

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

    @Transactional
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

    private boolean availableUsername(String userName) {
        UsernameDuplicateResponse response = userClient.checkUsernameAvailable(userName);

        return response.available();
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
