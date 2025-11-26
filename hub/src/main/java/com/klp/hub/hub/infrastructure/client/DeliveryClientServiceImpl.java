package com.klp.hub.hub.infrastructure.client;

import com.klp.hub.hub.application.service.DeliveryClientService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliveryClientServiceImpl implements DeliveryClientService {

    private final DeliveryFeignClient deliveryFeignClient;

    @Override
    public void deleteRoutePlansByHubId(UUID hubId) {
        deliveryFeignClient.deleteRoutePlansByHubId(hubId);
    }
}
