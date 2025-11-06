package com.klp.hub.product.presentation.controller;

import com.klp.hub.product.application.ProductService;
import com.klp.hub.product.presentation.dto.ProductDeleteResponse;
import com.klp.hub.product.presentation.dto.ProductResponse;
import com.klp.hub.product.presentation.dto.ProductsPageResponse;
import com.klp.hub.product.presentation.dto.ProductsPageRowResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/v1/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable("productId") String productId) {
        log.info("== 단일 상품 조회 productId : {} ==", productId);
        ProductResponse response = productService.getProductById(UUID.fromString(productId));
        log.info("== 단일 상품 조회 성공 ==");
        return ResponseEntity.ok().body(response);
    }

    @GetMapping
    public ResponseEntity<ProductsPageResponse> getProductsByPageable(@PageableDefault(size = 10) Pageable pageable) {
        log.info("== 상품 목록 조회 ==");
        Page<ProductsPageRowResponse> response = productService.getProductsByPageable(pageable);
        log.info("== 상품 목록 조회 성공 ==");
        return ResponseEntity.ok().body(ProductsPageResponse.from(response));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ProductDeleteResponse> deleteById(@PathVariable("productId") String productId) {
        log.info("== 상품 삭제 ==");
        UUID deletedProductId = productService.delete(UUID.fromString(productId));
        log.info("== 상품 삭제 완료 ==");
        return ResponseEntity.ok().body(new ProductDeleteResponse(deletedProductId));
    }
}
