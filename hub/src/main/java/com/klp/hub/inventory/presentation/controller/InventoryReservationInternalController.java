package com.klp.hub.inventory.presentation.controller;

import com.klp.hub.inventory.application.InventoryFacade;
import com.klp.hub.inventory.presentation.dto.request.InventoryReservationRequest;
import com.klp.hub.inventory.presentation.dto.response.InventoryReservationResponse;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/inventories")
@RequiredArgsConstructor
@Hidden
public class InventoryReservationInternalController {

    private final InventoryFacade inventoryFacade;

    @PostMapping("/reserve")
    public ResponseEntity<InventoryReservationResponse> reserveInventory(
        @Valid @RequestBody InventoryReservationRequest request
    ) {
        InventoryReservationResponse response = inventoryFacade.reserve(request.toCommand());
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/confirm/{orderId}")
    public ResponseEntity<Void> confirmReservation(@PathVariable UUID orderId) {
        inventoryFacade.confirm(orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/release/{orderId}")
    public ResponseEntity<Void> releaseReservation(@PathVariable UUID orderId) {
        inventoryFacade.release(orderId);
        return ResponseEntity.ok().build();
    }
}
