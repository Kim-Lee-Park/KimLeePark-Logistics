package com.klp.delivery.common.exception;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DriverFeignClientErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder errorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();

        return switch (status) {

            case 500, 502, 503, 504 -> {
                log.error("[DriverFeignErrorDecoder] 외부 서비스 장애 발생. status={}", status);
                yield new ExternalApiException(ExternalApiErrorCode.DRIVER_SERVICE_UNAVAILABLE);
            }
            case 400 -> {
                log.warn("[DriverFeignErrorDecoder] 잘못된 요청. status=400");
                yield new ExternalApiException(ExternalApiErrorCode.DRIVER_SERVICE_BAD_REQUEST);
            }
            case 401 -> {
                log.warn("[DriverFeignErrorDecoder] 인증 실패. status=401");
                yield new ExternalApiException(ExternalApiErrorCode.DRIVER_SERVICE_UNAUTHORIZED);
            }
            case 403 -> {
                log.warn("[DriverFeignErrorDecoder] 인가 거부. status=403");
                yield new ExternalApiException(ExternalApiErrorCode.DRIVER_SERVICE__FORBIDDEN);
            }
            case 404 -> {
                log.warn("[DriverFeignErrorDecoder] 리소스 없음(404). 기본 decoder 처리 적용");
                yield errorDecoder.decode(methodKey, response);
            }
            default -> {
                if (status >= 400 && status < 500) {
                    log.warn("[DriverFeignErrorDecoder] 기타 4xx 오류. status={}", status);
                    yield new ExternalApiException(
                        ExternalApiErrorCode.DRIVER_SERVICE_BAD_REQUEST);
                }
                yield errorDecoder.decode(methodKey, response);
            }
        };
    }

}
