package com.klp.authservice.auth.infrastructure.external;

import com.klp.authservice.auth.exception.AuthErrorCode;
import com.klp.common.exception.BusinessException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserFeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder errorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();

        return switch (status) {
            case 500 -> {
                log.error("유저 서비스에서 오류가 발생했습니다");
                yield new BusinessException(AuthErrorCode.USER_SERVICE_ERROR);
            }
            case 502, 503, 504 -> {
                log.error("유저 서비스를 일시적으로 사용할 수 없습니다");
                yield new BusinessException(AuthErrorCode.USER_SERVICE_UNAVAILABLE);
            }
            default -> {
                if (status >= 400 && status < 500) {
                    yield new BusinessException(AuthErrorCode.USER_SERVICE_BAD_REQUEST);
                }
                yield errorDecoder.decode(methodKey, response);
            }
        };
    }
}
