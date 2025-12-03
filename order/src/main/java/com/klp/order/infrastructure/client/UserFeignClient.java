package com.klp.order.infrastructure.client;

import com.klp.order.global.config.UserFeignClientConfig;
import com.klp.order.infrastructure.client.dto.user.UserProfileDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Component
@FeignClient(name = "user-service", configuration = UserFeignClientConfig.class)
public interface UserFeignClient {

    @GetMapping("/v1/internal/users/{userId}")
    UserProfileDto getUserProfileById(@PathVariable Long userId);
}
