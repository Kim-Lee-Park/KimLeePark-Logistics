package com.klp.hub.product.infrastructure.repository;

import com.klp.hub.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<Product, UUID> {
}
