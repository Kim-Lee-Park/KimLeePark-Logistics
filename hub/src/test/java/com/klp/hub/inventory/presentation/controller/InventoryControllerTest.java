package com.klp.hub.inventory.presentation.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.hub.global.config.SecurityConfig;
import com.klp.hub.global.exception.GlobalExceptionHandler;
import com.klp.hub.global.filter.AuthorizationFilter;
import com.klp.hub.inventory.application.InventoryFacade;
import com.klp.hub.inventory.application.InventoryService;
import com.klp.hub.inventory.presentation.dto.request.InventoryReplenishRequest;
import com.klp.hub.inventory.presentation.dto.response.InventoryReplenishResponse;
import com.klp.hub.inventory.presentation.dto.response.InventoryResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@Disabled
@WebMvcTest(InventoryController.class)
@Import({SecurityConfig.class, AuthorizationFilter.class, GlobalExceptionHandler.class})
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InventoryService inventoryService;

    @MockitoBean
    private InventoryFacade inventoryFacade;

    @Nested
    class GetInventory {

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
    }

    @Nested
    class Replenish {

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
            when(inventoryFacade.replenish(request.toCommand()))
                .thenReturn(
                    new InventoryReplenishResponse(InventoryReplenishResponse.Status.SUCCESS));

            mockMvc.perform(post("/v1/inventories/replenish")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").isString());
        }

        @Test
        @DisplayName("멱등키가 누락되면 400 Bad Request 를 반환한다")
        void missingIdempotencyKey() throws Exception {
            InventoryReplenishRequest request = new InventoryReplenishRequest(
                " ",
                List.of(
                    new InventoryReplenishRequest.Product(UUID.randomUUID(), UUID.randomUUID(), 10))
            );

            mockMvc.perform(post("/v1/inventories/replenish")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("상품 수량이 1 미만이면 400 Bad Request 를 반환한다")
        void invalidQuantity() throws Exception {
            InventoryReplenishRequest request = new InventoryReplenishRequest(
                "idempotencyKey",
                List.of(
                    new InventoryReplenishRequest.Product(UUID.randomUUID(), UUID.randomUUID(), 0))
            );

            mockMvc.perform(post("/v1/inventories/replenish")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("상품 목록이 비어있으면 400 Bad Request 를 반환한다")
        void emptyProducts() throws Exception {
            InventoryReplenishRequest request = new InventoryReplenishRequest(
                "idempotencyKey",
                List.of()
            );

            mockMvc.perform(post("/v1/inventories/replenish")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("상품 ID 가 누락되면 400 Bad Request 를 반환한다")
        void missingProductId() throws Exception {
            InventoryReplenishRequest request = new InventoryReplenishRequest(
                "idempotencyKey",
                List.of(new InventoryReplenishRequest.Product(null, UUID.randomUUID(), 0))
            );

            mockMvc.perform(post("/v1/inventories/replenish")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("허브 ID 가 누락되면 400 Bad Request 를 반환한다")
        void missingHubId() throws Exception {
            InventoryReplenishRequest request = new InventoryReplenishRequest(
                "idempotencyKey",
                List.of(new InventoryReplenishRequest.Product(UUID.randomUUID(), null, 0))
            );

            mockMvc.perform(post("/v1/inventories/replenish")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }
    }
}
