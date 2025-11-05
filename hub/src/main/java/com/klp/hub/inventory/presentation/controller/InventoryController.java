package com.klp.hub.inventory.presentation.controller;

import com.klp.hub.inventory.application.InventoryReader;
import com.klp.hub.inventory.presentation.dto.InventoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/v1/inventories")
public class InventoryController {

    private final InventoryReader inventoryReader;

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> getInventoryByProductId(@PathVariable("productId") String productId) {
        log.info("== 단일 상품의 재고 조회 productId : {} ==", productId);
        var response = inventoryReader.getByProductId(UUID.fromString(productId));
        log.info("== 단일 상품의 재고 조회 성공 ==");
        return ResponseEntity.ok().body(response);
    }
}
