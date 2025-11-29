package com.klp.delivery.delivery.application.service;

import com.klp.common.exception.BusinessException;
import com.klp.delivery.delivery.application.command.DriverCommand;
import com.klp.delivery.delivery.exception.DeliveryErrorCode;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverClientService driverClientService;


    public List<DriverCommand> findArrivalHubDrivers(UUID hubId) {
        try {
            return DriverCommand.from(driverClientService.findArrivalHubDrivers(hubId));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("배송 담당자들 조회 실패: {}", e.getMessage(), e);
            throw new BusinessException(DeliveryErrorCode.EXTERNAL_API_ERROR, "배송 담당자 조회에 실패했습니다.");
        }
    }

    public DriverCommand findDriverAtArrivalHub(long vendorDriverId) {
        try {
            return DriverCommand.of(driverClientService.findDriverAtArrivalHub(vendorDriverId));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("배송 담당자 조회 실패: {}", e.getMessage(), e);
            throw new BusinessException(DeliveryErrorCode.EXTERNAL_API_ERROR, "배송 담당자 조회에 실패했습니다.");
        }
    }

    public List<DriverCommand> findLogisticsDrivers() {
        try {
            return DriverCommand.from(driverClientService.findLogisticsDrivers());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("물류 배송 담당자들 조회 실패: {}", e.getMessage(), e);
            throw new BusinessException(DeliveryErrorCode.EXTERNAL_API_ERROR, "물류 배송 담당자 조회에 실패했습니다.");
        }
    }
}
