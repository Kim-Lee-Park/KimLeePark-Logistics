package com.klp.hub.inventory.presentation.docs;

import com.klp.hub.inventory.presentation.dto.request.InventoryReservationRequest;
import com.klp.hub.inventory.presentation.dto.response.InventoryReservationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Inventory Reservation (Internal)")
public interface InventoryReservationInternalControllerDocs {

    @Operation(summary = "재고 예약")
    ResponseEntity<InventoryReservationResponse> reserveInventory(
        @RequestBody InventoryReservationRequest request
    );
}
