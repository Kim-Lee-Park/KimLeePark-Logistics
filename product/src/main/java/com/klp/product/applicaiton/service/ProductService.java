package com.klp.product.applicaiton.service;

import com.klp.product.applicaiton.service.dto.DeductStockCommand;
import com.klp.product.domain.entity.Product;
import com.klp.product.infrastructure.repository.ProductRepository;
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
    public void deductStock(DeductStockCommand command) {
        log.info("상품의 재고 차감 수행");
        for (DeductStockCommand.Product deductCommandProduct : command.products()) {
            Product product = getById(deductCommandProduct.productId());
            product.decreaseStock(deductCommandProduct.quantity());
        }
        log.info("상품의 재고 차감 완료");
    }

    private Product getById(UUID productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> {
                log.info("상품을 찾을 수 없습니다.");
                return new RuntimeException("상품을 찾을 수 없습니다");
            });
    }
}
