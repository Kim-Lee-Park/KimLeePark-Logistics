package com.klp.delivery.delivery.application.util;

import com.klp.delivery.delivery.application.command.DriverCommand;
import com.klp.delivery.delivery.exception.DeliveryErrorCode;
import com.klp.delivery.global.exception.BusinessException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DriverSelector {

    private DriverSelector() {
        throw new AssertionError("Utility class 인스턴스화 할 수 없음");
    }

    public static DriverCommand pickRandomDriver(List<DriverCommand> drivers) {
        if (drivers == null || drivers.isEmpty()) {
            throw new BusinessException(DeliveryErrorCode.DRIVER_NOT_FOUND, "담당자 조회 결과가 없습니다.");
        }
        int index = ThreadLocalRandom.current().nextInt(drivers.size());

        log.info("배송 담당자 driver={}", drivers.get(index));
        return drivers.get(index);
    }
}

