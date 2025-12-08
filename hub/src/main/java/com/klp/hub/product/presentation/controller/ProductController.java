package com.klp.hub.product.presentation.controller;

import com.klp.hub.product.application.ProductService;
import com.klp.hub.product.presentation.docs.ProductControllerDocs;
import com.klp.hub.product.presentation.dto.ProductCreateRequest;
import com.klp.hub.product.presentation.dto.ProductCreateResponse;
import com.klp.hub.product.presentation.dto.ProductDeleteResponse;
import com.klp.hub.product.presentation.dto.ProductResponse;
import com.klp.hub.product.presentation.dto.ProductUpdateRequest;
import com.klp.hub.product.presentation.dto.ProductUpdateResponse;
import com.klp.hub.product.presentation.dto.ProductsPageResponse;
import com.klp.hub.product.presentation.dto.ProductsPageRowResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/v1/products")
public class ProductController implements ProductControllerDocs {

    private final ProductService productService;

    @Override
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable("productId") String productId) {
        log.info("== 단일 상품 조회 productId : {} ==", productId);
        ProductResponse response = productService.getProductById(UUID.fromString(productId));
        log.info("== 단일 상품 조회 성공 ==");
        return ResponseEntity.ok().body(response);
    }

    @Override
    @GetMapping
    public ResponseEntity<ProductsPageResponse> getProductsByPageable(@PageableDefault(size = 10) Pageable pageable) {
        log.info("== 상품 목록 조회 ==");
        Page<ProductsPageRowResponse> response = productService.getProductsByPageable(pageable);
        log.info("== 상품 목록 조회 성공 ==");
        return ResponseEntity.ok().body(ProductsPageResponse.from(response));
    }

    @Override
    @PostMapping
    public ResponseEntity<ProductCreateResponse> create(@Valid @RequestBody ProductCreateRequest request) {
        log.info("== 상품 생성 ==");
        UUID productId = productService.create(request.toCommand());
        log.info("== 상품 생성 성공 ==");
        return ResponseEntity.ok().body(new ProductCreateResponse(productId));
    }

    @Override
    @PatchMapping("/{productId}")
    public ResponseEntity<ProductUpdateResponse> updateProduct(
            @PathVariable("productId") String productId,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        log.info("== 상품 변경 ==");
        ProductUpdateResponse response = productService.update(request.toCommand(productId));
        log.info("== 상품 변경 성공 ==");
        return ResponseEntity.ok().body(response);
    }

    @Override
    @DeleteMapping("/{productId}")
    public ResponseEntity<ProductDeleteResponse> deleteById(@PathVariable("productId") String productId) {
        log.info("== 상품 삭제 ==");
        UUID deletedProductId = productService.delete(UUID.fromString(productId));
        log.info("== 상품 삭제 완료 ==");
        return ResponseEntity.ok().body(new ProductDeleteResponse(deletedProductId));
    }
}
