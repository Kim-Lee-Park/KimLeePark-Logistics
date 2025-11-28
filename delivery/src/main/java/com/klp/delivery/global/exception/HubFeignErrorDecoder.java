package com.klp.delivery.global.exception;

import com.klp.delivery.common.exception.ExternalApiErrorCode;
import com.klp.delivery.common.exception.ExternalApiException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HubFeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder errorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();

        return switch (status) {

            // 5xx - 외부 서비스 장애
            case 500, 502, 503, 504 -> {
                log.error("[HubFeignErrorDecoder] 외부 서비스 장애 발생. status={}", status);
                yield new ExternalApiException(ExternalApiErrorCode.HUB_SERVICE_UNAVAILABLE);
            }
            case 400 -> {
                log.warn("[HubFeignErrorDecoder] 잘못된 요청. status=400");
                yield new ExternalApiException(ExternalApiErrorCode.HUB_SERVICE_BAD_REQUEST);
            }
            case 401 -> {
                log.warn("[HubFeignErrorDecoder] 인증 실패. status=401");
                yield new ExternalApiException(ExternalApiErrorCode.HUB_SERVICE_UNAUTHORIZED);
            }
            case 403 -> {
                log.warn("[HubFeignErrorDecoder] 인가 거부. status=403");
                yield new ExternalApiException(ExternalApiErrorCode.HUB_SERVICE_FORBIDDEN);
            }
            case 404 -> {
                log.warn("[HubFeignErrorDecoder] 리소스 없음(404). 기본 decoder 처리 적용");
                yield errorDecoder.decode(methodKey, response);
            }
            default -> {
                if (status >= 400 && status < 500) {
                    log.warn("[HubFeignErrorDecoder] 기타 4xx 오류. status={}", status);
                    yield new ExternalApiException(
                        ExternalApiErrorCode.HUB_SERVICE_BAD_REQUEST);
                }
                yield errorDecoder.decode(methodKey, response);
            }
        };
    }
}

