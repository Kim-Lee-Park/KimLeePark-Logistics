package com.klp.product.applicaiton.service;

import com.klp.product.applicaiton.service.dto.DeductStockCommand;
import com.klp.product.domain.entity.Product;
import com.klp.product.infrastructure.repository.ProductRepository;
import com.klp.product.presentation.controller.dto.ProductCreateRequest;
import com.klp.product.presentation.controller.dto.ProductListResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public UUID create(ProductCreateRequest request) {
        Product savedProduct = productRepository.save(new Product(
            request.name(),
            request.stock()
        ));
        return savedProduct.getProductId();
    }

    @Transactional
    public UUID deductStock(DeductStockCommand command) {
        log.info("상품의 재고 차감 수행");
        UUID productId = null;
        for (DeductStockCommand.Product deductCommandProduct : command.products()) {
            Product product = getById(deductCommandProduct.productId());
            product.decreaseStock(deductCommandProduct.quantity());
            productId = product.getProductId();
        }
        log.info("상품의 재고 차감 완료");
        return productId;
    }

    @Transactional
    public List<ProductListResponse> getAll() {
        List<Product> products = productRepository.findAll();

        return products.stream()
            .map(product -> new ProductListResponse(product.getProductId(), product.getName()))
            .toList();
    }

    private Product getById(UUID productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> {
                log.info("상품을 찾을 수 없습니다.");
                return new RuntimeException("상품을 찾을 수 없습니다");
            });
    }
}
