package com.klp.order.infrastructure.client;

import com.klp.common.exception.ExternalApiErrorCode;
import com.klp.common.exception.ExternalApiException;
import com.klp.order.application.service.PromotionClient;
import com.klp.order.infrastructure.client.dto.promotion.request.PromotionCalculateRequest;
import com.klp.order.infrastructure.client.dto.promotion.response.PromotionResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PromotionClientImpl implements PromotionClient {

    private final PromotionFeignClient promotionFeignClient;

    @Override
    public PromotionResponse getPromotionInfo(PromotionCalculateRequest request) {
        try {
            return promotionFeignClient.getPromotionInfo(request);
        } catch (FeignException.NotFound e) {
            log.warn("[PromotionClient] 프로모션 정보를 찾을 수 없음 - request: {}", request);
            return null;  // 또는 기본값 반환
        } catch (FeignException.BadRequest e) {
            log.error("[PromotionClient] 잘못된 요청 - request: {}, message: {}",
                request, e.getMessage());
            throw new ExternalApiException(ExternalApiErrorCode.PROMOTION_SERVICE_BAD_REQUEST);
        } catch (FeignException.Unauthorized e) {
            log.error("[PromotionClient] 인증 실패 - status: {}, message: {}",
                e.status(), e.getMessage());
            throw new ExternalApiException(ExternalApiErrorCode.PROMOTION_SERVICE_UNAUTHORIZED);
        } catch (FeignException.Forbidden e) {
            log.error("[PromotionClient] 권한 없음 - status: {}, message: {}",
                e.status(), e.getMessage());
            throw new ExternalApiException(ExternalApiErrorCode.PROMOTION_SERVICE_FORBIDDEN);
        } catch (FeignException e) {
            log.error(
                "[PromotionClient] 외부 프로모션 서비스 호출 중 오류 - request: {}, status: {}, message: {}",
                request, e.status(), e.getMessage());
            throw new ExternalApiException(ExternalApiErrorCode.PROMOTION_SERVICE_UNAVAILABLE);
        }
    }
}