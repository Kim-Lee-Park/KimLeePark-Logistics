package com.klp.delivery.delivery.application.service;

import com.klp.delivery.delivery.application.command.HubInfoCommand;
import com.klp.delivery.delivery.exception.DeliveryErrorCode;
import com.klp.delivery.global.exception.BusinessException;
import com.klp.delivery.routeplan.application.service.HubClientService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubService {

    private final HubClientService hubClientService;

    public HubInfoCommand findHubInfo(UUID hubId) {
        try {
            return HubInfoCommand.of(hubClientService.getHubById(hubId));
        } catch (Exception e) {
            log.error("허브 조회 실패: {}", e.getMessage(), e);
            throw new BusinessException(DeliveryErrorCode.EXTERNAL_API_ERROR, "업체 조회에 실패했습니다.", e);
        }
    }

}
