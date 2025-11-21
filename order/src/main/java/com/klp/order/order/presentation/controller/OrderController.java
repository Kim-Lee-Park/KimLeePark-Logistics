package com.klp.order.order.presentation.controller;

import com.klp.order.order.application.service.OrderService;
import com.klp.order.order.application.service.dto.OrderCreateCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/orders")
@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public String createOrder(@RequestBody OrderCreateCommand command) {
        orderService.createOrder(command);
        return "success";
    }
}
