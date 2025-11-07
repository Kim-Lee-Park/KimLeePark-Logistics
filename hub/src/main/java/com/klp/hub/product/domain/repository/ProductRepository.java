package com.klp.hub.product.domain.repository;

import com.klp.hub.product.domain.Product;
import com.klp.hub.product.presentation.dto.ProductsPageRowResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Optional<Product> findById(UUID id);

    Page<ProductsPageRowResponse> findAllByPageable(Pageable pageable);

    Product save(Product product);
}
