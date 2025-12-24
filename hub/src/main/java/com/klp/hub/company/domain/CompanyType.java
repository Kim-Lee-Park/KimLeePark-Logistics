package com.klp.hub.company.domain;

import com.klp.hub.company.exception.CompanyErrorCode;
import com.klp.hub.global.exception.BusinessException;

public enum CompanyType {
    SUPPLIER("생산"), CUSTOMER("수령");

    private String description;

    CompanyType(String description) {
        this.description = description;
    }

    public static CompanyType from(String type) {
        try {
            return CompanyType.valueOf(type);
        } catch (Exception e) {
            throw new BusinessException(CompanyErrorCode.UNSUPPORTED_COMPANY_TYPE);
        }
    }
}
