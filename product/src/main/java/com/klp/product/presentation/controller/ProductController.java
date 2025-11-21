package com.klp.product.presentation.controller;

import com.klp.product.applicaiton.service.ProductService;
import com.klp.product.applicaiton.service.dto.DeductStockCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/product")
@RestController
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping
    public String deduct(@RequestBody DeductStockCommand command) {
        productService.deductStock(command);
        return "success";
    }
}
