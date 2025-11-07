package com.klp.authservice.auth.infrastructure.external;

import com.klp.authservice.auth.AuthErrorCode;
import com.klp.authservice.auth.application.client.UserClient;
import com.klp.authservice.auth.infrastructure.external.dto.request.UserCreateRequest;
import com.klp.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserClientImpl implements UserClient {

    private final RestClient userRestClient;

    /**
     * 요청에 대한 응답이 200이면 true, 409면 false, 이외는 예외로 처리
     */
    @Override
    public boolean checkUserNameAvailable(String userName) {
        try {
            userRestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/v1/users/exists")
                    .queryParam("username", userName)
                    .build())
                .retrieve()
                .toBodilessEntity();

            return true;

        } catch (HttpClientErrorException.Conflict e) {
            return false;

        } catch (HttpClientErrorException e) {
            log.error("유저 이름 중복 확인 요청 실패: status: {}", e.getStatusCode());
            throw new BusinessException(AuthErrorCode.USER_SERVICE_ERROR);

        } catch (HttpServerErrorException e) {
            log.error("유저 서비스 오류 발생: status: {}", e.getStatusCode());
            throw new BusinessException(AuthErrorCode.USER_SERVICE_INTERNAL_ERROR);
        }
    }

    @Override
    public void createUser(UserCreateRequest request) {
        try {
            userRestClient.post()
                .uri("/v1/users")
                .body(request)
                .retrieve()
                .toBodilessEntity();
        } catch (HttpClientErrorException e) {
            log.error("유저 생성 요청 실패: status: {}", e.getStatusCode());
            throw new BusinessException(AuthErrorCode.USER_SERVICE_ERROR);

        } catch (HttpServerErrorException e) {
            log.error("유저 서비스 오류 발생: status: {}", e.getStatusCode());
            throw new BusinessException(AuthErrorCode.USER_SERVICE_INTERNAL_ERROR);
        }
    }
}
