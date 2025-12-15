package com.klp.authservice.auth.infrastructure.external;

import com.klp.authservice.auth.application.client.UserClient;
import com.klp.authservice.auth.infrastructure.external.dto.request.UserCreateRequest;
import com.klp.authservice.auth.infrastructure.external.dto.request.ValidateUserRequest;
import com.klp.authservice.auth.infrastructure.external.dto.response.UserDataResponse;
import com.klp.authservice.auth.infrastructure.external.dto.response.UsernameDuplicateResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserClientImpl implements UserClient {

    private final UserFeignClient userFeignClient;

    @Override
    @CircuitBreaker(name = "userService")
    @Retry(name = "userService")
    public UsernameDuplicateResponse checkUsernameAvailable(String username) {
        return userFeignClient.checkUsernameAvailable(username);
    }

    @Override
    @CircuitBreaker(name = "userService")
    @Retry(name = "userService")
    public void createUser(UserCreateRequest request) {
        userFeignClient.createUser(request);
    }

    @Override
    @CircuitBreaker(name = "userService")
    @Retry(name = "userService")
    public UserDataResponse validateUserCredentials(String username, String password) {
        ValidateUserRequest request = new ValidateUserRequest(username, password);
        return userFeignClient.getUserByUsername(request);
    }
}
