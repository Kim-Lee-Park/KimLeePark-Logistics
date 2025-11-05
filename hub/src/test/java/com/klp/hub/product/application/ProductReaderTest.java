package com.klp.hub.product.application;

import com.klp.hub.product.domain.Product;
import com.klp.hub.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductReaderTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductReader productReader;

    private UUID productId = UUID.randomUUID();

    @Test
    @DisplayName("상품의 ID 로 상품을 조회할 수 있다")
    void getProductById() {
        var product = mock(Product.class);
        when(product.getName()).thenReturn("상품명");
        when(product.getId()).thenReturn(productId);
        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        var response = productReader.getProductById(productId);

        assertThat(response.productId()).isNotNull();
        assertEquals(productId, response.productId());
        assertEquals(response.productName(), product.getName());
    }

    @Test
    @DisplayName("해당 상품이 존재하지 않는다면 예외가 발생한다")
    void throwGetProductById() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productReader.getProductById(productId));
    }
}
