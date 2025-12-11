package com.klp.order.infrastructure.client.service;

import com.klp.order.infrastructure.client.PromotionClient;
import com.klp.order.infrastructure.client.dto.promotion.request.PromotionCalculateRequest;
import com.klp.order.infrastructure.client.dto.promotion.response.PromotionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionDiscountService {

    private final PromotionClient promotionClient;

    public PromotionResponse promotionInfo(PromotionCalculateRequest request) {
        log.info("할인 금액 조회 요청");
        try {
            PromotionResponse response = promotionClient.getPromotionInfo(request);
            log.info("할인 금액 조회 성공");
            return response;
        } catch (Exception e) {
            log.error("할인 금액 조회 실패");
            throw e;
        }
    }
}
