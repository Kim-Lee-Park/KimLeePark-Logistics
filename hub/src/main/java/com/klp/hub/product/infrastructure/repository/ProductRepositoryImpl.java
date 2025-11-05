package com.klp.hub.product.infrastructure.repository;

import com.klp.hub.product.domain.Product;
import com.klp.hub.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {
    private final ProductJpaRepository productJpaRepository;

    @Override
    public Optional<Product> findById(UUID id) {
        return productJpaRepository.findById(id);
    }
}
