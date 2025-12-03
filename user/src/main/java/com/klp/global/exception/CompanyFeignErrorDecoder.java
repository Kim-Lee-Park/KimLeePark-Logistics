package com.klp.global.exception;

import com.klp.user.domain.exception.ExternalApiErrorCode;
import com.klp.user.domain.exception.ExternalApiException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CompanyFeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder errorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();

        return switch (status) {

            // 5xx - 외부 서비스 장애
            case 500, 502, 503, 504 -> {
                log.error("[CompanyFeignErrorDecoder] 업체 서비스 장애 발생. method={}, status={}",
                    methodKey, status);
                yield new ExternalApiException(ExternalApiErrorCode.COMPANY_SERVICE_UNAVAILABLE);
            }

            // 4xx - 클라이언트 오류
            case 400 -> {
                log.warn("[CompanyFeignErrorDecoder] 잘못된 요청. method={}, status=400", methodKey);
                yield new ExternalApiException(ExternalApiErrorCode.COMPANY_SERVICE_BAD_REQUEST);
            }
            case 401 -> {
                log.warn("[CompanyFeignErrorDecoder] 인증 실패. method={}, status=401", methodKey);
                yield new ExternalApiException(ExternalApiErrorCode.COMPANY_SERVICE_UNAUTHORIZED);
            }
            case 403 -> {
                log.warn("[CompanyFeignErrorDecoder] 인가 거부. method={}, status=403", methodKey);
                yield new ExternalApiException(ExternalApiErrorCode.COMPANY_SERVICE_FORBIDDEN);
            }
            case 404 -> {
                log.warn("[CompanyFeignErrorDecoder] 업체 정보 없음. method={}, status=404", methodKey);
                yield new ExternalApiException(ExternalApiErrorCode.COMPANY_NOT_FOUND);
            }

            // 기타 오류
            default -> {
                if (status >= 400 && status < 500) {
                    log.warn("[CompanyFeignErrorDecoder] 기타 4xx 오류. method={}, status={}",
                        methodKey, status);
                    yield new ExternalApiException(ExternalApiErrorCode.COMPANY_SERVICE_BAD_REQUEST);
                }
                log.error("[CompanyFeignErrorDecoder] 예상치 못한 오류. method={}, status={}",
                    methodKey, status);
                yield errorDecoder.decode(methodKey, response);
            }
        };
    }
}
