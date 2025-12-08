package com.klp.hub.inventory.presentation.controller;

import com.klp.hub.inventory.application.InventoryFacade;
import com.klp.hub.inventory.application.InventoryService;
import com.klp.hub.inventory.presentation.docs.InventoryControllerDocs;
import com.klp.hub.inventory.presentation.dto.request.InventoryReplenishRequest;
import com.klp.hub.inventory.presentation.dto.response.InventoryReplenishResponse;
import com.klp.hub.inventory.presentation.dto.response.InventoryResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/v1/inventories")
public class InventoryController implements InventoryControllerDocs {

    private final InventoryService inventoryService;

    private final InventoryFacade inventoryFacade;

    @Override
    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> getInventoryByProductId(
        @PathVariable("productId") String productId
    ) {
        log.info("== 단일 상품의 재고 조회 productId : {} ==", productId);
        InventoryResponse response = inventoryService.getByProductId(UUID.fromString(productId));
        log.info("== 단일 상품의 재고 조회 성공 ==");
        return ResponseEntity.ok().body(response);
    }

    @Override
    @PostMapping("/replenish")
    public ResponseEntity<InventoryReplenishResponse> replenish(
        @Valid @RequestBody InventoryReplenishRequest request
    ) {
        log.info("== 재고 증가 멱등키 : {} ==", request.idempotencyKey());
        InventoryReplenishResponse response = inventoryFacade.replenish(request.toCommand());
        log.info("== 재고 증가 성공");
        return ResponseEntity.ok().body(response);
    }
}
