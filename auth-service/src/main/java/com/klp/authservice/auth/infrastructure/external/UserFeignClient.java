package com.klp.authservice.auth.infrastructure.external;

import com.klp.authservice.auth.infrastructure.external.config.UserFeignClientConfig;
import com.klp.authservice.auth.infrastructure.external.dto.request.UserCreateRequest;
import com.klp.authservice.auth.infrastructure.external.dto.response.UserDataResponse;
import com.klp.authservice.auth.infrastructure.external.dto.response.UsernameDuplicateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", configuration = UserFeignClientConfig.class)
public interface UserFeignClient {

    @GetMapping("/v1/users/check")
    UsernameDuplicateResponse checkUsernameAvailable(@RequestParam("username") String userName);

    @PostMapping("/v1/users")
    void createUser(@RequestBody UserCreateRequest request);

    @GetMapping("/v1/users")
    UserDataResponse getUserByUsername(@RequestParam("username") String userName);
}
