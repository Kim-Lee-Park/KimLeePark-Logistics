package com.klp.hub.inventory.presentation.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.common.exception.GlobalExceptionHandler;
import com.klp.common.security.config.SecurityConfig;
import com.klp.common.security.filter.AuthorizationFilter;
import com.klp.hub.inventory.application.InventoryService;
import com.klp.hub.inventory.presentation.dto.InventoryDeductRequest;
import com.klp.hub.inventory.presentation.dto.InventoryDeductRequest.Product;
import com.klp.hub.inventory.presentation.dto.InventoryDeductResponse;
import com.klp.hub.inventory.presentation.dto.InventoryDeductResponse.Status;
import com.klp.hub.inventory.presentation.dto.InventoryReplenishRequest;
import com.klp.hub.inventory.presentation.dto.InventoryReplenishResponse;
import com.klp.hub.inventory.presentation.dto.InventoryResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InventoryController.class)
@Import({SecurityConfig.class, AuthorizationFilter.class, GlobalExceptionHandler.class})
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InventoryService inventoryService;

    @Test
    @DisplayName("단일 상품에 대한 재고를 조회할 수 있다")
    void getInventoryByProductId() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID inventoryId = UUID.randomUUID();
        UUID hubId = UUID.randomUUID();
        when(inventoryService.getByProductId(productId))
            .thenReturn(new InventoryResponse(productId, inventoryId, hubId, 10));

        mockMvc.perform(get("/v1/inventories/{productId}", productId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.productId").isString())
            .andExpect(jsonPath("$.inventoryId").isString())
            .andExpect(jsonPath("$.hubId").isString())
            .andExpect(jsonPath("$.quantity").isNumber());
    }

    @Test
    @DisplayName("재고를 차감시킬 수 있다")
    void deduct() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID hubId = UUID.randomUUID();
        Integer quantity = 10;
        String idempotencyKey = "idempotencyKey";
        InventoryDeductRequest request = new InventoryDeductRequest(
            idempotencyKey,
            List.of(new Product(productId, hubId, quantity))
        );
        when(inventoryService.deduct(request.toCommand()))
            .thenReturn(new InventoryDeductResponse(Status.SUCCESS));

        mockMvc.perform(post("/v1/inventories/deduct")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").isString());
    }

    @Test
    @DisplayName("재고를 증가시킬 수 있다")
    void replenish() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID hubId = UUID.randomUUID();
        Integer quantity = 10;
        String idempotencyKey = "idempotencyKey";
        InventoryReplenishRequest request = new InventoryReplenishRequest(
            idempotencyKey,
            List.of(new InventoryReplenishRequest.Product(productId, hubId, quantity))
        );
        when(inventoryService.replenish(request.toCommand()))
            .thenReturn(new InventoryReplenishResponse(InventoryReplenishResponse.Status.SUCCESS));

        mockMvc.perform(post("/v1/inventories/replenish")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").isString());
    }
}
