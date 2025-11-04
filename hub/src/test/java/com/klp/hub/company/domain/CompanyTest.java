package com.klp.hub.company.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompanyTest {

    @Test
    @DisplayName("업체 종류는 Null 일 수 없다")
    void nullCompanyType() {
        assertThrows(IllegalArgumentException.class, () -> createCompanyByType(null));
    }

    @Test
    @DisplayName("업체명은 Null일 수 없다")
    void nullCompanyName() {
        assertThrows(IllegalArgumentException.class, () -> createCompanyByName(null));
    }

    @Test
    @DisplayName("업체명은 필수이다")
    void blankCompanyName() {
        assertThrows(IllegalArgumentException.class, () -> createCompanyByName(" "));
    }

    @Test
    @DisplayName("업체주소은 Null일 수 없다")
    void nullCompanyAddress() {
        assertThrows(IllegalArgumentException.class, () -> createCompanyByAddress(null));
    }

    @Test
    @DisplayName("업체주소는 필수이다")
    void blankCompanyAddress() {
        assertThrows(IllegalArgumentException.class, () -> createCompanyByAddress(" "));
    }

    private Company createCompanyByType(CompanyType type) {
        return new Company(type, "name", "address");
    }

    private Company createCompanyByName(String name) {
        return new Company(CompanyType.SUPPLIER, name, "address");
    }

    private Company createCompanyByAddress(String address) {
        return new Company(CompanyType.SUPPLIER, "상품명", address);
    }
}
