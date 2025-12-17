package com.klp.order.infrastructure.client;

import com.klp.common.exception.ExternalApiErrorCode;
import com.klp.common.exception.ExternalApiException;
import com.klp.order.application.service.InventoryClient;
import com.klp.order.infrastructure.client.dto.inventory.request.InventoryReservationRequest;
import com.klp.order.infrastructure.client.dto.inventory.response.InventoryReservationResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryClientImpl implements InventoryClient {

    private final InventoryFeignClient inventoryFeignClient;

    @Override
    public InventoryReservationResponse reserveProduct(InventoryReservationRequest request) {
        try {
            return inventoryFeignClient.reserveProduct(request);
        } catch (FeignException.NotFound e) {
            return null;
        } catch (FeignException.BadRequest e) {
            log.error("[InventoryClient] 잘못된 요청 - orderId: {}, message: {}",
                request.orderId(), e.getMessage());
            throw new ExternalApiException(ExternalApiErrorCode.INVENTORY_SERVICE_BAD_REQUEST);
        } catch (FeignException.Unauthorized e) {
            log.error("[InventoryClient] 인증 실패 - status: {}, message: {}",
                e.status(), e.getMessage());
            throw new ExternalApiException(ExternalApiErrorCode.INVENTORY_SERVICE_UNAUTHORIZED);
        } catch (FeignException.Forbidden e) {
            log.error("[InventoryClient] 권한 없음 - status: {}, message: {}",
                e.status(), e.getMessage());
            throw new ExternalApiException(ExternalApiErrorCode.INVENTORY_SERVICE_FORBIDDEN);
        } catch (FeignException e) {
            log.error("[InventoryClient] 재고 선점 호출 중 오류 - orderId: {}, status: {}, message: {}",
                request.orderId(), e.status(), e.getMessage());
            throw new ExternalApiException(ExternalApiErrorCode.INVENTORY_SERVICE_UNAVAILABLE);
        }
    }

}
