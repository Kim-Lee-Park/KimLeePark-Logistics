package com.klp.order.common.event.presentation.controller;

import com.klp.order.common.event.application.service.EventStoreService;
import com.klp.order.common.event.application.service.dto.EventStoreDto;
import com.klp.order.common.event.presentation.controller.dto.response.EventStoreListResponse;
import com.klp.order.common.event.presentation.controller.dto.response.EventStoreResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/events")
@RestController
@RequiredArgsConstructor
public class EventStoreController {
    private final EventStoreService eventStoreService;

    @GetMapping
    public ResponseEntity<EventStoreListResponse> findAll() {
        log.info("이벤트 목록 조회");
        List<EventStoreDto> list = eventStoreService.findAll();
        log.info("이벤트 목록 성공");
        return ResponseEntity.ok(EventStoreListResponse.from(list));
    }
}
