package com.klp.order.infrastructure.client;

import com.klp.global.config.FeignTracingConfig;
import com.klp.global.config.UserFeignClientConfig;
import com.klp.order.infrastructure.client.dto.user.UserAddressDto;
import com.klp.order.infrastructure.client.dto.user.UserProfileDto;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Component
@FeignClient(name = "user-service", url = "${clients.user.url:}", configuration = {
    UserFeignClientConfig.class,
    FeignTracingConfig.class})
public interface UserFeignClient {

    @GetMapping("/v1/internal/users/{userId}")
    UserProfileDto getUserProfileById(@PathVariable Long userId);

    @GetMapping("/v1/internal/users/address/{addressId}")
    UserAddressDto getUserAddressHubIdByAddressId(@PathVariable UUID addressId);

}
