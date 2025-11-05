package com.klp.hub.product.domain.repository;

import com.klp.hub.product.domain.Product;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Optional<Product> findById(UUID id);
}
