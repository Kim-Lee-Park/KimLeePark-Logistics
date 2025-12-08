package com.klp.hub.inventory.presentation.controller;

import com.klp.hub.inventory.application.InventoryFacade;
import com.klp.hub.inventory.presentation.dto.request.InventoryReservationRequest;
import com.klp.hub.inventory.presentation.dto.response.InventoryReservationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/inventories")
@RequiredArgsConstructor
public class InventoryReservationInternalController {

    private final InventoryFacade inventoryFacade;

    @PostMapping("/reserve")
    public ResponseEntity<InventoryReservationResponse> reserveInventory(
        @Valid @RequestBody InventoryReservationRequest request
    ) {
        InventoryReservationResponse response = inventoryFacade.reserve(request.toCommand());
        return ResponseEntity.ok().body(response);
    }
}
