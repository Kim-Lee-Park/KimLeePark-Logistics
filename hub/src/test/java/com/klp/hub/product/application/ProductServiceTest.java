package com.klp.hub.product.application;

import com.klp.hub.company.application.CompanyService;
import com.klp.hub.company.domain.CompanyType;
import com.klp.hub.company.presentation.dto.CompanyResponse;
import com.klp.hub.product.domain.Product;
import com.klp.hub.product.domain.repository.ProductRepository;
import com.klp.hub.product.presentation.dto.ProductResponse;
import com.klp.hub.product.application.dto.ProductUpdateCommand;
import com.klp.hub.product.domain.Product;
import com.klp.hub.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CompanyService companyService;

    @InjectMocks
    private ProductService productService;

    private UUID productId = UUID.randomUUID();

    private UUID companyId = UUID.randomUUID();

    @Test
    @DisplayName("상품의 ID 로 상품을 조회할 수 있다")
    void getProductById() {
        Product product = mock(Product.class);
        CompanyResponse companyResponse = new CompanyResponse(companyId, CompanyType.SUPPLIER.name(), "업체명", "업체주소");
        when(product.getName()).thenReturn("상품명");
        when(product.getId()).thenReturn(productId);
        when(product.getCompanyId()).thenReturn(companyId);
        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));
        when(companyService.getByCompanyId(companyId)).thenReturn(companyResponse);

        ProductResponse response = productService.getProductById(productId);

        assertThat(response.productId()).isNotNull();
        assertEquals(productId, response.productId());
        assertEquals(response.productName(), product.getName());
    }

    @Test
    @DisplayName("해당 상품이 존재하지 않는다면 예외가 발생한다")
    void throwGetProductById() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productService.getProductById(productId));
    }

    @Test
    @DisplayName("상품명을 변경할 수 있다")
    void updateProduct() {
        var oldName = "기존 상품명";
        var newName = "새로운 상품명";
        var request = new ProductUpdateCommand(
                productId,
                newName
        );
        var product = new Product(companyId, oldName);
        when(product.getId()).thenReturn(productId);
        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        productService.update(request);

        assertThat(product.getName()).isEqualTo(newName);
    }
}
