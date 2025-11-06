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

    @Test
    @DisplayName("변경할 상품명이 Null인 경우 예외가 발생한다")
    void throwNullProductName() {
        var product = createProductByName("기존 상품명");

        assertThrows(IllegalArgumentException.class, () -> product.updateName(null));
    }

    @Test
    @DisplayName("변경할 상품명이 빈 값인 경우 예외가 발생한다")
    void throwBlankProductName() {
        var product = createProductByName("기존 상품명");

        assertThrows(IllegalArgumentException.class, () -> product.updateName(" "));
    }

    @Test
    @DisplayName("상품명을 변경할 수 있다")
    void updateProductName() {
        var name = "새로운 상품명";
        var product = createProductByName("기존 상품명");

        product.updateName(name);

        assertEquals(name, product.getName());
    }

    private Product createProductByName(String name) {
        return new Product(UUID.randomUUID(), name);
    }

    private Product createProductByCompanyId(UUID companyId) {
        return new Product(companyId, "상품명");
    }
}
