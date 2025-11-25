package com.klp.delivery.routeplan.infrastructure.client;

import com.klp.delivery.delivery.application.facade.DeliveryFacade;
import com.klp.delivery.routeplan.application.service.DeliveryClientService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeliveryClientServiceImpl implements DeliveryClientService {

    private final DeliveryFacade deliveryFacade;

    @Override
    public boolean hasActiveDeliveries(UUID routePlanId) {
        //TODO: 배송에서 구현되면 수정
        return true;
    }
}
