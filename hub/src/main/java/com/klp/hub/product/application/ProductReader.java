package com.klp.hub.product.application;

import com.klp.hub.company.application.CompanyReader;
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
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class ProductReader {

    private final ProductRepository productRepository;

    private final CompanyReader companyReader;

    public ProductResponse getProductById(UUID productId) {
        var product = productRepository.findById(productId).orElseThrow(() -> {
            log.error("해당 상품을 찾을 수 없습니다. productId : {}", productId);
            return new RuntimeException();
        });

        var company = companyReader.getByCompanyId(product.getCompanyId());

        return new ProductResponse(
                product.getId(),
                UUID.randomUUID(),
                company.name(),
                product.getName()
        );
    }

    public Page<ProductsPageRowResponse> getProductsByPageable(Pageable pageable) {
        return productRepository.findAllByPageable(pageable);
    }
}
