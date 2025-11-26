package com.klp.hub.hub.infrastructure.client;

import com.klp.hub.hub.application.service.OrderClientService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderClientServiceImpl implements OrderClientService {

    private final OrderFeignClient orderFeignClient;

    @Override
    public boolean hasProgressingOrders(UUID hubId) {
        return orderFeignClient.hasProgressingOrder(hubId);
    }
}
