package com.klp.order.infrastructure.client;

import com.klp.common.exception.ExternalApiErrorCode;
import com.klp.common.exception.ExternalApiException;
import com.klp.order.application.service.ProductClient;
import com.klp.order.domain.vo.Product;
import feign.FeignException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductClientImpl implements ProductClient {

    private final InventoryFeignClient inventoryClient;

    @Override
    public Product getProductById(UUID productId) {
        try {
            return inventoryClient.getProductInfo(productId).toVo();
        } catch (FeignException.NotFound e) {
            return null;
        } catch (FeignException e) {
            log.error("[ProductClient] 외부 유저 서비스 호출 중 오류 - productId: {}, status: {}, message: {}",
                productId, e.status(), e.getMessage());
            throw new ExternalApiException(ExternalApiErrorCode.PRODUCT_SERVICE_UNAVAILABLE);
        }

    }
}
