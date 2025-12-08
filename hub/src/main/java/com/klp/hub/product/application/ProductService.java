package com.klp.hub.product.application;

import com.klp.hub.company.application.CompanyService;
import com.klp.hub.company.presentation.dto.response.CompanyResponse;
import com.klp.hub.global.exception.BusinessException;
import com.klp.hub.inventory.application.InventoryService;
import com.klp.hub.inventory.presentation.dto.InventoryResponse;
import com.klp.hub.product.application.dto.ProductCreateCommand;
import com.klp.hub.product.application.dto.ProductUpdateCommand;
import com.klp.hub.product.application.event.ProductInfoChangedEvent;
import com.klp.hub.product.domain.Product;
import com.klp.hub.product.domain.repository.ProductRepository;
import com.klp.hub.product.exception.ProductErrorCode;
import com.klp.hub.product.presentation.dto.ProductResponse;
import com.klp.hub.product.presentation.dto.ProductUpdateResponse;
import com.klp.hub.product.presentation.dto.ProductsPageRowResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    private final CompanyService companyService;

    private final InventoryService inventoryService;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID productId) {
        Product product = getById(productId);
        InventoryResponse inventory = inventoryService.getByProductId(productId);
        CompanyResponse company = companyService.getByCompanyId(product.getCompanyId());

        return new ProductResponse(
            product.getId(),
            inventory.hubId(),
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
            new Product(companyResponse.companyId(), command.name())
        );

        inventoryService.create(savedProduct.getId(), command.hubId(), command.quantity());
        return savedProduct.getId();
    }

    @Transactional
    public ProductUpdateResponse update(ProductUpdateCommand command) {
        Product product = getById(command.productId());

        product.updateName(command.name());

        applicationEventPublisher.publishEvent(new ProductInfoChangedEvent(product.getId()));

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
            return new BusinessException(ProductErrorCode.NOT_FOUND_PRODUCT);
        });
        return product;
    }
}
