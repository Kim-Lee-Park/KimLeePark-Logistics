package com.klp.delivery.delivery.application.service;

import com.klp.common.exception.BusinessException;
import com.klp.delivery.delivery.application.command.CompanyCommand;
import com.klp.delivery.delivery.exception.DeliveryErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyClientService companyClientService;

    public CompanyCommand findCompany(String customerId) {
        try {
            return CompanyCommand.of(companyClientService.findCompany(customerId));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("업체 조회 실패: {}", e.getMessage(), e);
            throw new BusinessException(DeliveryErrorCode.EXTERNAL_API_ERROR, "업체 조회에 실패했습니다.");
        }
    }

}
