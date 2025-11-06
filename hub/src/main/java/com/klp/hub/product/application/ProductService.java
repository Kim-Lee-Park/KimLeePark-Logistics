package com.klp.hub.product.application;

import com.klp.hub.company.application.CompanyService;
import com.klp.hub.company.presentation.dto.CompanyResponse;
import com.klp.hub.inventory.application.InventoryService;
import com.klp.hub.inventory.domain.repository.exception.UniqueConstraintException;
import com.klp.hub.inventory.presentation.dto.InventoryResponse;
import com.klp.hub.product.application.dto.ProductCreateCommand;
import com.klp.hub.product.domain.Product;
import com.klp.hub.product.domain.repository.ProductRepository;
import com.klp.hub.product.presentation.dto.ProductUpdateResponse;
import com.klp.hub.product.presentation.dto.ProductsPageRowResponse;
import com.klp.hub.product.presentation.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import com.klp.hub.product.application.dto.ProductUpdateCommand;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    private final CompanyService companyService;

    private final InventoryService inventoryService;

    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID productId) {
        Product product = getById(productId);
        CompanyResponse company = companyService.getByCompanyId(product.getCompanyId());

        return new ProductResponse(
                product.getId(),
                UUID.randomUUID(),
                company.name(),
                product.getName()
        );
    }

    @Transactional(readOnly = true)
    public Page<ProductsPageRowResponse> getProductsByPageable(Pageable pageable) {
        return productRepository.findAllByPageable(pageable);
    }

    @Transactional
    public UUID create(ProductCreateCommand command) {
        CompanyResponse companyResponse = companyService.getByCompanyId(command.companyId());
        Product savedProduct = productRepository.save(
                new Product(companyResponse.id(), command.name())
        );

        try {
            inventoryService.create(savedProduct.getId(), command.hubId(), command.quantity());
            return savedProduct.getId();
        } catch (UniqueConstraintException exception) {
            log.error("이미 해당 재고가 존재합니다.");
            // FIXME: 도메인 예외 교체 필요
            throw new RuntimeException(exception.getMessage());
        }
    }

    @Transactional
    public ProductUpdateResponse update(ProductUpdateCommand command) {
        Product product = getById(command.productId());

        product.updateName(command.name());

        return new ProductUpdateResponse(
                product.getId(),
                product.getName()
        );
    }

    @Transactional
    public UUID delete(UUID productId) {
        Product product = getById(productId);

        product.delete(1L);

        InventoryResponse response = inventoryService.getByProductId(productId);
        inventoryService.delete(response.inventoryId());
        return product.getId();
    }

    private Product getById(UUID productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> {
            log.error("해당 상품을 찾을 수 없습니다. productId : {}", productId);
            return new RuntimeException();
        });
        return product;
    }
}
