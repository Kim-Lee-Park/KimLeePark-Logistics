package com.klp.hub.product.application;

import com.klp.hub.company.application.CompanyService;
import com.klp.hub.product.domain.repository.ProductRepository;
import com.klp.hub.product.presentation.dto.ProductsPageRowResponse;
import com.klp.hub.product.presentation.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    private final CompanyService companyService;

    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID productId) {
        var product = productRepository.findById(productId).orElseThrow(() -> {
            log.error("해당 상품을 찾을 수 없습니다. productId : {}", productId);
            return new RuntimeException();
        });

        var company = companyService.getByCompanyId(product.getCompanyId());

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
}
