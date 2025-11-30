package com.klp.order.order.presentation.controller;

import com.klp.order.order.application.service.OrderService;
import com.klp.order.order.application.service.dto.OrderCreateCommand;
import com.klp.order.order.application.service.dto.OrderResponse;
import com.klp.order.order.presentation.controller.dto.OrderCreateResponse;
import lombok.RequiredArgsConstructor;
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
    public OrderCreateResponse createOrder(@RequestBody OrderCreateCommand command) {
        OrderResponse response = orderService.createOrder(command);
        return new OrderCreateResponse(response.orderId(), "v2");
    }
}
