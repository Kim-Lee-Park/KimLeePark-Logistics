package com.klp.order.infrastructure.client;

import com.klp.order.application.service.UserClient;
import com.klp.order.common.exception.ExternalApiErrorCode;
import com.klp.order.common.exception.ExternalApiException;
import com.klp.order.domain.vo.UserProfile;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserClientImpl implements UserClient {

    private final UserFeignClient userFeignClient;

    @Override
    public UserProfile getUserProfileById(Long userId) {
        try {
            return userFeignClient.getUserProfileById(userId).toVo();
        } catch (FeignException.NotFound e) {
            return null;
        } catch (FeignException e) {
            log.error("[UserClient] 외부 유저 서비스 호출 중 오류 - userId: {}, status: {}, message: {}",
                userId, e.status(), e.getMessage());
            throw new ExternalApiException(ExternalApiErrorCode.USER_SERVICE_UNAVAILABLE);
        }
    }
}
