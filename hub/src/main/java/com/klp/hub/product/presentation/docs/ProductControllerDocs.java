package com.klp.hub.product.presentation.docs;

import com.klp.hub.product.presentation.dto.ProductCreateRequest;
import com.klp.hub.product.presentation.dto.ProductCreateResponse;
import com.klp.hub.product.presentation.dto.ProductDeleteResponse;
import com.klp.hub.product.presentation.dto.ProductResponse;
import com.klp.hub.product.presentation.dto.ProductUpdateRequest;
import com.klp.hub.product.presentation.dto.ProductUpdateResponse;
import com.klp.hub.product.presentation.dto.ProductsPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Product API", description = "상품 관리 API")
public interface ProductControllerDocs {

    @Operation(summary = "단일 상품 조회", description = "상품 ID로 상품을 조회합니다.")
    ResponseEntity<ProductResponse> getProductById(
        @Parameter(description = "상품 ID(UUID 문자열)") String productId
    );

    @Operation(summary = "상품 목록 조회 (페이지)", description = "페이지네이션으로 상품 목록을 조회합니다.")
    ResponseEntity<ProductsPageResponse> getProductsByPageable(Pageable pageable);

    @Operation(summary = "상품 생성", description = "상품을 생성합니다.")
    ResponseEntity<ProductCreateResponse> create(@RequestBody ProductCreateRequest request);

    @Operation(summary = "상품 변경", description = "상품 정보를 수정합니다.")
    ResponseEntity<ProductUpdateResponse> updateProduct(
        @Parameter(description = "상품 ID(UUID 문자열)") String productId,
        @RequestBody ProductUpdateRequest request
    );

    @Operation(summary = "상품 삭제", description = "상품을 삭제합니다.")
    ResponseEntity<ProductDeleteResponse> deleteById(
        @Parameter(description = "상품 ID(UUID 문자열)") String productId
    );
}
