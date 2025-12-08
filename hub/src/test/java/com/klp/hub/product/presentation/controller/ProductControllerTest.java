package com.klp.hub.product.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.klp.hub.global.config.SecurityConfig;
import com.klp.hub.global.exception.GlobalExceptionHandler;
import com.klp.hub.global.filter.AuthorizationFilter;
import com.klp.hub.product.application.ProductService;
import com.klp.hub.product.presentation.dto.ProductCreateRequest;
import com.klp.hub.product.presentation.dto.ProductResponse;
import com.klp.hub.product.presentation.dto.ProductUpdateRequest;
import com.klp.hub.product.presentation.dto.ProductUpdateResponse;
import com.klp.hub.product.presentation.dto.ProductsPageRowResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@Disabled
@WebMvcTest(ProductController.class)
@Import({SecurityConfig.class, AuthorizationFilter.class, GlobalExceptionHandler.class})
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @Nested
    class GetProduct {

        @Test
        @DisplayName("상품 ID 로 상품을 조회할 수 있다")
        void getProductById() throws Exception {
            UUID productId = UUID.randomUUID();
            when(productService.getProductById(productId))
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
            ProductsPageRowResponse row = new ProductsPageRowResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "업체명",
                "상품명"
            );
            PageImpl<ProductsPageRowResponse> page = new PageImpl<>(List.of(row),
                PageRequest.of(0, 10),
                100);

            when(productService.getProductsByPageable(any(Pageable.class))).thenReturn(page);

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

    @Nested
    class Create {

        @Test
        @DisplayName("상품을 생성할 수 있다")
        void createProduct() throws Exception {
            UUID productId = UUID.randomUUID();
            UUID companyId = UUID.randomUUID();
            UUID hubId = UUID.randomUUID();
            ProductCreateRequest request = new ProductCreateRequest(
                companyId,
                hubId,
                "상품명",
                10
            );
            when(productService.create(any())).thenReturn(productId);

            mockMvc.perform(post("/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.productId").isString());
        }

        @Test
        @DisplayName("업체 ID 가 누락되면 400 Bad Request 를 반환한다")
        void missingCompanyId() throws Exception {
            UUID productId = UUID.randomUUID();
            UUID companyId = null;
            UUID hubId = UUID.randomUUID();
            ProductCreateRequest request = new ProductCreateRequest(
                companyId,
                hubId,
                "상품명",
                10
            );
            when(productService.create(any())).thenReturn(productId);

            mockMvc.perform(post("/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("허브 ID 가 누락되면 400 Bad Request 를 반환한다")
        void missingHubId() throws Exception {
            UUID productId = UUID.randomUUID();
            UUID companyId = UUID.randomUUID();
            UUID hubId = null;
            ProductCreateRequest request = new ProductCreateRequest(
                companyId,
                hubId,
                "상품명",
                10
            );
            when(productService.create(any())).thenReturn(productId);

            mockMvc.perform(post("/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("상품명이 누락되면 400 Bad Request 를 반환한다")
        void missingProductName() throws Exception {
            UUID productId = UUID.randomUUID();
            UUID companyId = UUID.randomUUID();
            UUID hubId = UUID.randomUUID();
            ProductCreateRequest request = new ProductCreateRequest(
                companyId,
                hubId,
                null,
                10
            );
            when(productService.create(any())).thenReturn(productId);

            mockMvc.perform(post("/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("상품명이 빈값이면 400 Bad Request 를 반환한다")
        void emptyProductName() throws Exception {
            UUID productId = UUID.randomUUID();
            UUID companyId = UUID.randomUUID();
            UUID hubId = UUID.randomUUID();
            ProductCreateRequest request = new ProductCreateRequest(
                companyId,
                hubId,
                " ",
                10
            );
            when(productService.create(any())).thenReturn(productId);

            mockMvc.perform(post("/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("재고수량이 음수라면 400 Bad Request 를 반환한다")
        void negativeInventory() throws Exception {
            UUID productId = UUID.randomUUID();
            UUID companyId = UUID.randomUUID();
            UUID hubId = UUID.randomUUID();
            ProductCreateRequest request = new ProductCreateRequest(
                companyId,
                hubId,
                "상품명",
                -1
            );
            when(productService.create(any())).thenReturn(productId);

            mockMvc.perform(post("/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class Update {

        @Test
        @DisplayName("상품을 변경할 수 있다")
        void updateProduct() throws Exception {
            UUID productId = UUID.randomUUID();
            String name = "상품명";
            ProductUpdateRequest request = new ProductUpdateRequest(
                name
            );
            ProductUpdateResponse response = new ProductUpdateResponse(
                productId,
                name
            );
            when(productService.update(any())).thenReturn(response);

            mockMvc.perform(patch("/v1/products/{productId}", productId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.productId").isString());
        }

        @Test
        @DisplayName("상품명이 누락되면 400 Bad Request 를 반환한다")
        void missingProductName() throws Exception {
            UUID productId = UUID.randomUUID();
            String name = null;
            ProductUpdateRequest request = new ProductUpdateRequest(
                name
            );

            mockMvc.perform(patch("/v1/products/{productId}", productId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("상품명이 빈값이면 400 Bad Request 를 반환한다")
        void emptyProductName() throws Exception {
            UUID productId = UUID.randomUUID();
            String name = " ";
            ProductUpdateRequest request = new ProductUpdateRequest(
                name
            );

            mockMvc.perform(patch("/v1/products/{productId}", productId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }
    }

    @Test
    @DisplayName("상품 ID 로 상품을 삭제할 수 있다")
    void softDeleteById() throws Exception {
        UUID productId = UUID.randomUUID();
        when(productService.delete(productId))
            .thenReturn(productId);

        mockMvc.perform(delete("/v1/products/{productId}", productId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.productId").isString());
    }
}
