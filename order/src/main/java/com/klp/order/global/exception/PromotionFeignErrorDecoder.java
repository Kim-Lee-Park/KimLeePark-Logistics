package com.klp.order.global.exception;

import com.klp.order.common.exception.ExternalApiErrorCode;
import com.klp.order.common.exception.ExternalApiException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PromotionFeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder errorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();

        return switch (status) {
            case 500, 502, 503, 504 -> {
                log.error("[PromotionFeignErrorDecoder] 외부 서비스 장애 발생. status={}", status);
                yield new ExternalApiException(ExternalApiErrorCode.PROMOTION_SERVICE_UNAVAILABLE);
            }
            case 400 -> {
                log.warn("[PromotionFeignErrorDecoder] 잘못된 요청. status=400");
                yield new ExternalApiException(ExternalApiErrorCode.PROMOTION_SERVICE_BAD_REQUEST);
            }
            case 401 -> {
                log.warn("[PromotionFeignErrorDecoder] 인증 실패. status=401");
                yield new ExternalApiException(ExternalApiErrorCode.PROMOTION_SERVICE_UNAUTHORIZED);
            }
            case 403 -> {
                log.warn("[PromotionFeignErrorDecoder] 인가 거부. status=403");
                yield new ExternalApiException(ExternalApiErrorCode.PROMOTION_SERVICE_FORBIDDEN);
            }
            case 404 -> {
                log.warn("[PromotionFeignErrorDecoder] 리소스 없음(404). 기본 decoder 처리 적용");
                yield errorDecoder.decode(methodKey, response);
            }
            default -> {
                if (status >= 400 && status < 500) {
                    log.warn("[PromotionFeignErrorDecoder] 기타 4xx 오류. status={}", status);
                    yield new ExternalApiException(
                        ExternalApiErrorCode.PROMOTION_SERVICE_BAD_REQUEST);
                }
                yield errorDecoder.decode(methodKey, response);
            }
        };
    }
}