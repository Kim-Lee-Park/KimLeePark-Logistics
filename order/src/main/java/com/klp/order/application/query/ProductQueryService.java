package com.klp.order.application.query;

import com.klp.order.application.cache.ProductCache;
import com.klp.order.domain.vo.Product;
import com.klp.order.global.exception.BusinessException;
import com.klp.order.global.exception.ProductRefErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductQueryService {

    private final ProductCache productCache;

    public Product getProductById(UUID productId) {
        Product product = productCache.getProductById(productId);
        if (product == null) {
            throw new BusinessException(ProductRefErrorCode.PRODUCT_NOT_FOUND);
        }
        return product;
    }
}
