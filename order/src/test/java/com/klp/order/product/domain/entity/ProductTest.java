package com.klp.order.product.domain.entity;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductTest {

    @Test
    @DisplayName("상품명이 Null 이면 예외가 발생한다")
    void name_is_null() {
        assertThrows(IllegalArgumentException.class, () -> new Product(null));
    }

    @Test
    @DisplayName("상품명이 비어있다면 예외가 발생한다")
    void name_is_empty() {
        assertThrows(IllegalArgumentException.class, () -> new Product(" "));
    }

    @Test
    @DisplayName("상품이 생성되면 재고는 0 이다")
    void stock_is_zero() {
        Product product = new Product("상품명");

        Integer stock = product.getStock();

        assertThat(stock).isEqualTo(0);
    }

    @Test
    @DisplayName("특정 수량의 재고를 차감할 수 있다")
    void decreaseStock() {
        int stock = 10;
        int decreaseStock = 10;
        Product product = new Product("상품명", stock);

        product.decreaseStock(decreaseStock);

        assertThat(product.getStock()).isEqualTo(stock - decreaseStock);
    }

    @Test
    @DisplayName("재고 차감시 상품의 재고가 부족하면 예외가 발생한다")
    void decreaseStockThrowException() {
        int stock = 10;
        int decreaseStock = 11;
        Product product = new Product("상품명", stock);

        assertThrows(
            IllegalArgumentException.class,
            () -> product.decreaseStock(decreaseStock)
        );
    }

    @Test
    @DisplayName("재고 차감시 음수 수량을 입력할 수 없다")
    void decreaseStockNegativeQuantity() {
        int stock = 10;
        int decreaseStock = -1;
        Product product = new Product("상품명", stock);

        assertThrows(
            IllegalArgumentException.class,
            () -> product.decreaseStock(decreaseStock)
        );
    }
}
