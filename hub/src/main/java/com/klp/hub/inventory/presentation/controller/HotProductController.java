package com.klp.hub.inventory.presentation.controller;

import com.klp.hub.inventory.application.HotProductService;
import com.klp.hub.inventory.presentation.dto.request.HotProductRegisterRequest;
import com.klp.hub.inventory.presentation.dto.request.HotProductRequest;
import com.klp.hub.inventory.presentation.dto.request.HotProductUpdateQuantityRequest;
import com.klp.hub.inventory.presentation.dto.response.HotProductQuantityResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Hot Product", description = "인기 상품 관리 API")
@RestController
@RequestMapping("/v1/inventories/hot")
@RequiredArgsConstructor
public class HotProductController {

    private final HotProductService hotProductService;

    @Operation(summary = "인기 상품 등록", description = "상품을 인기 상품으로 등록하고 Redis 캐시에 재고를 설정합니다.")
    @PostMapping
    public ResponseEntity<Void> register(@Valid @RequestBody HotProductRegisterRequest request) {
        hotProductService.register(request.productId(), request.hubId(), request.ttlSeconds());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "인기 상품 해제", description = "인기 상품 등록을 해제하고 Redis 캐시를 삭제합니다.")
    @DeleteMapping
    public ResponseEntity<Void> unregister(@Valid @RequestBody HotProductRequest request) {
        hotProductService.unregister(request.productId(), request.hubId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "인기 상품 재고 조회", description = "Redis 캐시에서 인기 상품의 현재 재고를 조회합니다.")
    @GetMapping("/quantity")
    public ResponseEntity<HotProductQuantityResponse> getQuantity(
        @RequestParam UUID productId,
        @RequestParam UUID hubId
    ) {
        int quantity = hotProductService.getQuantity(productId, hubId);
        return ResponseEntity.ok(HotProductQuantityResponse.of(productId, hubId, quantity));
    }

    @Operation(summary = "인기 상품 재동기화", description = "DB의 재고를 Redis 캐시에 동기화합니다. 기존 TTL을 유지합니다.")
    @PostMapping("/resync")
    public ResponseEntity<Void> resync(@Valid @RequestBody HotProductRequest request) {
        hotProductService.resync(request.productId(), request.hubId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "인기 상품 재고 수정", description = "Redis 캐시의 재고를 직접 수정합니다. 기존 TTL을 유지합니다.")
    @PutMapping("/quantity")
    public ResponseEntity<Void> updateQuantity(@Valid @RequestBody HotProductUpdateQuantityRequest request) {
        hotProductService.updateQuantity(request.productId(), request.hubId(), request.quantity());
        return ResponseEntity.ok().build();
    }
}
