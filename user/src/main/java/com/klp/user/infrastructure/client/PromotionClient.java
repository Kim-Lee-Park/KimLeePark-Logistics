package com.klp.user.infrastructure.client;

import com.klp.user.global.config.PromotionFeignClientConfig;
import com.klp.user.infrastructure.client.dto.request.CreateUserGradeRequest;
import com.klp.user.infrastructure.client.dto.response.UserGradeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "promotion-service", configuration = PromotionFeignClientConfig.class)
public interface PromotionClient {

    /**
     * 회원 등급을 생성합니다.
     */
    @PostMapping("/v1/internal/promotions/user-grades")
    UserGradeResponse createUserGrade(@RequestBody CreateUserGradeRequest request);
}
