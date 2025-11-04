package com.klp.hub.product.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    @DisplayName("상품명은 Null일 수 없다")
    void nullProductName() {
        assertThrows(IllegalArgumentException.class, () -> new Product(null));
    }

    @Test
    @DisplayName("상품명은 필수이다")
    void blankProductName() {
        assertThrows(IllegalArgumentException.class, () -> new Product(" "));
    }
}
