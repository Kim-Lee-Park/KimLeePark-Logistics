package com.klp.order.application.service;

import com.klp.order.domain.vo.Product;
import java.util.UUID;

public interface ProductClient {

    Product getProductById(UUID productId);
}
