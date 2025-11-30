package com.klp.product.presentation.controller;

import com.klp.product.applicaiton.service.ProductService;
import com.klp.product.applicaiton.service.dto.DeductStockCommand;
import com.klp.product.presentation.controller.dto.ProductCreateRequest;
import com.klp.product.presentation.controller.dto.ProductListResponse;
import com.klp.product.presentation.controller.dto.ProductResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/product")
@RestController
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping("/deduct")
    public ProductResponse deduct(@RequestBody DeductStockCommand command) {
        UUID productId = productService.deductStock(command);
        return new ProductResponse(productId, "v2");
    }

    @PostMapping
    public UUID create(@RequestBody ProductCreateRequest request) {
        UUID productId = productService.create(request);
        return productId;
    }

    @GetMapping
    public List<ProductListResponse> getAll() {
        List<ProductListResponse> response = productService.getAll();
        return response;
    }
}
