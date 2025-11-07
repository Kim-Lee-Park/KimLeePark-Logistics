package com.klp.hub.inventory.presentation.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.klp.hub.inventory.application.InventoryService;
import com.klp.hub.inventory.presentation.dto.InventoryResponse;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
}
