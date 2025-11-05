package com.klp.hub.product.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    @DisplayName("상품명은 Null일 수 없다")
    void nullProductName() {
        assertThrows(IllegalArgumentException.class, () -> createProductByName(null));
    }

    @Test
    @DisplayName("상품명은 필수이다")
    void blankProductName() {
        assertThrows(IllegalArgumentException.class, () -> createProductByName(" "));
    }

    @Test
    @DisplayName("업체 ID는 Null일 수 없다")
    void nullCompanyId() {
        assertThrows(IllegalArgumentException.class, () -> createProductByCompanyId(null));
    }

    private Product createProductByName(String name) {
        return new Product(UUID.randomUUID(), name);
    }

    private Product createProductByCompanyId(UUID companyId) {
        return new Product(companyId, "상품명");
    }
}
