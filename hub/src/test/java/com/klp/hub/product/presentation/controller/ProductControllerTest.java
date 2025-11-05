package com.klp.hub.product.presentation.controller;

import com.klp.hub.product.application.ProductReader;
import com.klp.hub.product.presentation.dto.ProductResponse;
import com.klp.hub.product.presentation.dto.ProductsPageRowResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductReader productReader;

    @Test
    @DisplayName("상품 ID 로 상품을 조회할 수 있다")
    void getProductById() throws Exception {
        UUID productId = UUID.randomUUID();
        when(productReader.getProductById(productId))
                .thenReturn(new ProductResponse(productId, UUID.randomUUID(), "업체명", "상품명"));

        mockMvc.perform(get("/v1/products/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.productId").isString())
                .andExpect(jsonPath("$.hubId").isString())
                .andExpect(jsonPath("$.companyName").isString())
                .andExpect(jsonPath("$.productName").isString());
    }

    @Test
    @DisplayName("상품 목록을 페이지네이션으로 조회할 수 있다")
    void getProductsByPageable() throws Exception {
        var row = new ProductsPageRowResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "업체명",
                "상품명"
        );
        var page = new PageImpl<>(List.of(row), PageRequest.of(0, 10), 100);

        when(productReader.getProductsByPageable(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/v1/products")
                .param("page", "0")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products[0].productId").isString())
                .andExpect(jsonPath("$.products[0].hubId").isString())
                .andExpect(jsonPath("$.products[0].companyName").isString())
                .andExpect(jsonPath("$.products[0].productName").isString())
                .andExpect(jsonPath("$.pageable.page").isNumber())
                .andExpect(jsonPath("$.pageable.size").isNumber())
                .andExpect(jsonPath("$.pageable.totalElements").isNumber())
                .andExpect(jsonPath("$.pageable.totalPages").isNumber())
                .andExpect(jsonPath("$.pageable.hasNext").isBoolean())
                .andExpect(jsonPath("$.pageable.isFirst").isBoolean())
                .andExpect(jsonPath("$.pageable.isLast").isBoolean());
    }
}
