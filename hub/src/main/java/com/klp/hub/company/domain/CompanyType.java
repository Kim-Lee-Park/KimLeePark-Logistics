package com.klp.hub.company.domain;

public enum CompanyType {
    SUPPLIER("생산"), CUSTOMER("수령");

    private String description;

    CompanyType(String description) {
        this.description = description;
    }
}
