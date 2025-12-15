package com.klp.hub.product.presentation.controller;

import com.klp.hub.product.application.ProductService;
import io.swagger.v3.oas.annotations.Hidden;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/v1/internal/products")
@Hidden
public class ProductInternalController {

    private final ProductService productService;

    @GetMapping("/ids")
    public ResponseEntity<List<UUID>> getAllProductIds() {
        List<UUID> productIds = productService.getAllProductIds();
        return ResponseEntity.ok().body(productIds);
    }
}
