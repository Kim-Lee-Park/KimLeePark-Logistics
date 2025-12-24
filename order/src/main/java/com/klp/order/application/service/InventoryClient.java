package com.klp.order.application.service;

import com.klp.order.infrastructure.client.dto.inventory.request.InventoryReservationRequest;
import com.klp.order.infrastructure.client.dto.inventory.response.InventoryReservationResponse;
import org.springframework.web.bind.annotation.RequestBody;

public interface InventoryClient {

    InventoryReservationResponse reserveProduct(@RequestBody InventoryReservationRequest request);

}
