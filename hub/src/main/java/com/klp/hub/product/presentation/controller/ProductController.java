package com.klp.hub.product.presentation.controller;

import com.klp.hub.product.application.ProductReader;
import com.klp.hub.product.presentation.dto.ProductResponse;
import com.klp.hub.product.presentation.dto.ProductsPageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/v1/products")
public class ProductController {

    private final ProductReader productReader;

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable("productId") String productId) {
        log.info("== 단일 상품 조회 productId : {} ==", productId);
        var response = productReader.getProductById(UUID.fromString(productId));
        log.info("== 단일 상품 조회 성공 ==");
        return ResponseEntity.ok().body(response);
    }

    @GetMapping
    public ResponseEntity<ProductsPageResponse> getProductsByPageable(@PageableDefault(size = 10) Pageable pageable) {
        log.info("== 상품 목록 조회 ==");
        var response = productReader.getProductsByPageable(pageable);
        log.info("== 상품 목록 조회 성공 ==");
        return ResponseEntity.ok().body(ProductsPageResponse.from(response));
    }
}
