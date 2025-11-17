package com.klp.order.common.event.presentation.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.klp.order.common.event.application.service.EventStoreService;
import com.klp.order.common.event.application.service.dto.EventStoreDto;
import com.klp.order.common.event.domain.EventType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(EventStoreController.class)
class EventStoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventStoreService eventStoreService;

    @Test
    @DisplayName("이벤트 목록을 조회할 수 있다")
    void findAll() throws Exception {
        UUID eventStoreId1 = UUID.randomUUID();
        UUID eventStoreId2 = UUID.randomUUID();
        Map<String, Object> payload1 = Map.of("orderId", "uuid", "amount", 100);
        Map<String, Object> payload2 = Map.of("paymentId", "uuid", "amount", 100);
        List<EventStoreDto> eventStoreList = List.of(
            new EventStoreDto(
                eventStoreId1,
                EventType.ORDER_CREATED.name(),
                payload1,
                LocalDateTime.now()
            ),
            new EventStoreDto(
                eventStoreId2,
                EventType.PAYMENT_COMPLETED.name(),
                payload2,
                LocalDateTime.now()
            )
        );

        when(eventStoreService.findAll()).thenReturn(eventStoreList);

        mockMvc.perform(get("/events"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.list").isArray())
            .andExpect(jsonPath("$.list[0].eventStoreId").isString())
            .andExpect(jsonPath("$.list[0].eventType").isString())
            .andExpect(jsonPath("$.list[0].payload.orderId").isString())
            .andExpect(jsonPath("$.list[0].payload.amount").isNumber())
            .andExpect(jsonPath("$.list[1].payload.paymentId").isString())
            .andExpect(jsonPath("$.list[1].payload.amount").isNumber());
    }

    @Test
    @DisplayName("이벤트가 없으면 빈 리스트를 반환한다")
    void returnEmptyList() throws Exception {
        when(eventStoreService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/events"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.list").isEmpty());
    }
}
