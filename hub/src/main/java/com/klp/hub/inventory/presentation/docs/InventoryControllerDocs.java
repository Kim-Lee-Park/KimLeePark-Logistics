package com.klp.hub.inventory.presentation.docs;

import com.klp.hub.inventory.presentation.dto.request.InventoryReplenishRequest;
import com.klp.hub.inventory.presentation.dto.response.InventoryReplenishResponse;
import com.klp.hub.inventory.presentation.dto.response.InventoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Inventory")
public interface InventoryControllerDocs {

    @Operation(summary = "단일 상품 재고 조회")
    ResponseEntity<InventoryResponse> getInventoryByProductId(
        @Parameter(description = "상품 ID(UUID 문자열)") String productId
    );

    @Operation(summary = "재고 증가")
    ResponseEntity<InventoryReplenishResponse> replenish(
        @RequestBody InventoryReplenishRequest request
    );
}
