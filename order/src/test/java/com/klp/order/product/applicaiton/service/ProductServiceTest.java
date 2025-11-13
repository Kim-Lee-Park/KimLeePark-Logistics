package com.klp.order.product.applicaiton.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.klp.order.product.applicaiton.service.dto.DeductStockCommand;
import com.klp.order.product.domain.entity.Product;
import com.klp.order.product.infrastructure.repository.ProductRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("상품의 재고 차감을 수행한다")
    void tryDecreaseStock() {
        UUID productId = UUID.randomUUID();
        Integer quantity = 10;
        DeductStockCommand command = new DeductStockCommand(
            List.of(new DeductStockCommand.Product(productId, quantity))
        );
        Product product = mock(Product.class);
        when(productRepository.findById(productId))
            .thenReturn(Optional.of(product));

        productService.deductStock(command);

        verify(product, times(1)).decreaseStock(quantity);
    }

    @Test
    @DisplayName("재고 차감시 상품이 존재하지 않는다면 예외가 발생한다")
    void productIsNull() {
        UUID productId = UUID.randomUUID();
        Integer quantity = 10;
        DeductStockCommand command = new DeductStockCommand(
            List.of(new DeductStockCommand.Product(productId, quantity))
        );
        when(productRepository.findById(productId))
            .thenReturn(Optional.empty());

        assertThrows(
            RuntimeException.class,
            () -> productService.deductStock(command)
        );
    }
}
