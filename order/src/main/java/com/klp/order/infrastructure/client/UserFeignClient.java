package com.klp.order.infrastructure.client;

import com.klp.order.global.config.UserFeignClientConfig;
import com.klp.order.infrastructure.client.dto.user.UserProfileDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

@Component
@FeignClient(name = "user-service", configuration = UserFeignClientConfig.class)
public interface UserFeignClient {

    @GetMapping("")
        //TODO: 수정하기
    UserProfileDto getUserProfileById(Long userId);
}
