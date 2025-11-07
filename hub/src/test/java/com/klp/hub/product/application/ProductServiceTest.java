package com.klp.hub.product.application;

import com.klp.hub.company.application.CompanyService;
import com.klp.hub.company.domain.CompanyType;
import com.klp.hub.company.presentation.dto.CompanyResponse;
import com.klp.hub.inventory.application.InventoryService;
import com.klp.hub.inventory.domain.repository.exception.UniqueConstraintException;
import com.klp.hub.inventory.presentation.dto.InventoryResponse;
import com.klp.hub.product.application.dto.ProductCreateCommand;
import com.klp.hub.product.domain.Product;
import com.klp.hub.product.domain.repository.ProductRepository;
import com.klp.hub.product.presentation.dto.ProductResponse;
import com.klp.hub.product.application.dto.ProductUpdateCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CompanyService companyService;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private ProductService productService;

    private UUID productId = UUID.randomUUID();

    private UUID companyId = UUID.randomUUID();

    private UUID inventoryId = UUID.randomUUID();

    private UUID hubId = UUID.randomUUID();

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
        String oldName = "기존 상품명";
        String newName = "새로운 상품명";
        ProductUpdateCommand command = new ProductUpdateCommand(
                productId,
                newName
        );
        Product product = new Product(companyId, oldName);
        when(productRepository.findById(any(UUID.class)))
                .thenReturn(Optional.of(product));

        productService.update(command);

        assertThat(product.getName()).isEqualTo(newName);
    }

    @Test
    @DisplayName("상품 ID 를 통해 상품을 삭제할 때 상품이 존재하지 않으면 예외가 발생한다")
    void throwDeleteByNullProduct() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productService.delete(productId));
    }

    @Test
    @DisplayName("상품을 생성할때 재고도 같이 생성한다")
    void withCreateInventory() {
        Integer quantity = 10;
        ProductCreateCommand command = new ProductCreateCommand(
                companyId,
                hubId,
                "상품명",
                quantity
        );
        Product product = mock(Product.class);
        CompanyResponse companyResponse = mock(CompanyResponse.class);
        when(companyService.getByCompanyId(companyId)).thenReturn(companyResponse);
        when(companyResponse.id()).thenReturn(companyId);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(product.getId()).thenReturn(productId);

        productService.create(command);

        verify(inventoryService, times(1)).create(productId, hubId, quantity);
    }

    @Test
    @DisplayName("상품 생성시 업체가 존재하지 않는다면 예외가 발생한다")
    void throwNullCompany() {
        ProductCreateCommand command = new ProductCreateCommand(
                companyId,
                hubId,
                "상품명",
                10
        );
        when(companyService.getByCompanyId(companyId)).thenThrow(RuntimeException.class);

        assertThrows(RuntimeException.class, () -> productService.create(command));
    }

    @Test
    @DisplayName("상품 생성시 이미 해당 상품의 재고가 존재하면 예외가 발생한다")
    void throwDuplicatedInventory() {
        Integer quantity = 10;
        ProductCreateCommand command = new ProductCreateCommand(
                companyId,
                hubId,
                "상품명",
                quantity
        );
        Product product = mock(Product.class);
        CompanyResponse companyResponse = mock(CompanyResponse.class);
        when(companyService.getByCompanyId(companyId)).thenReturn(companyResponse);
        when(companyResponse.id()).thenReturn(companyId);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(product.getId()).thenReturn(productId);
        when(inventoryService.create(productId, command.hubId(), quantity)).thenThrow(UniqueConstraintException.class);

        assertThrows(RuntimeException.class, () -> productService.create(command));
    }

    @Test
    @DisplayName("상품 ID를 통해서 상품을 softDelete 할 수 있다")
    void softDelete() {
        Product product = new Product(companyId, "상품명");
        InventoryResponse inventoryResponse = mock(InventoryResponse.class);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(inventoryService.getByProductId(productId)).thenReturn(inventoryResponse);
        when(inventoryResponse.inventoryId()).thenReturn(inventoryId);

        productService.delete(productId);

        assertTrue(product.isDeleted());
        assertThat(product.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("상품 삭제시 관련된 재고도 삭제된다")
    void deletedInventoryWhenProductDeleted() {
        Product product = new Product(companyId, "상품명");
        InventoryResponse inventoryResponse = new InventoryResponse(productId, inventoryId, 0);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(inventoryService.getByProductId(productId)).thenReturn(inventoryResponse);

        productService.delete(productId);

        verify(inventoryService, times(1)).delete(inventoryResponse.inventoryId());
    }
}
