package com.klp.order.order.application.service;

import com.klp.order.order.domain.vo.Product;
import java.util.UUID;

public interface ProductClient {

    Product getProductById(UUID productId);
}
