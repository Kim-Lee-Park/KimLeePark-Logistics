package com.klp.hub.product.infrastructure.repository;

import com.klp.hub.company.domain.QCompany;
import com.klp.hub.product.domain.Product;
import com.klp.hub.product.domain.QProduct;
import com.klp.hub.product.domain.repository.ProductRepository;
import com.klp.hub.product.presentation.dto.ProductsPageRowResponse;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {
    private final ProductJpaRepository productJpaRepository;
    private final JPAQueryFactory queryFactory;

    private QProduct qProduct = QProduct.product;
    private QCompany qCompany = QCompany.company;

    @Override
    public Optional<Product> findById(UUID id) {
        return productJpaRepository.findById(id);
    }

    @Override
    public Page<ProductsPageRowResponse> findAllByPageable(Pageable pageable) {
        var query = queryFactory
                .select(getProductListRowProjection())
                .from(qProduct)
                .join(qCompany)
                .on(qProduct.companyId.eq(qCompany.id))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        var total = queryFactory
                .select(qProduct.count())
                .from(qProduct)
                .join(qCompany)
                .on(qProduct.companyId.eq(qCompany.id))
                .fetchOne();

        return new PageImpl<>(query, pageable, total);
    }

    private ConstructorExpression<ProductsPageRowResponse> getProductListRowProjection() {
        return Projections.constructor(
                ProductsPageRowResponse.class,
                qProduct.id.as("productId"),
                qProduct.id.as("hubId"),
                qCompany.name.as("companyName"),
                qProduct.name.as("productName")
        );
    }
}
