package com.klp.payment.infrastructure.client;

import com.klp.payment.global.config.FeignTracingConfig;
import com.klp.payment.infrastructure.client.dto.UserDetailResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", configuration = {FeignTracingConfig.class})
public interface UserClient {

    @GetMapping("/v1/internal/users/{userId}")
    UserDetailResponse getUserDetails(@PathVariable("userId") Long userId);
}
