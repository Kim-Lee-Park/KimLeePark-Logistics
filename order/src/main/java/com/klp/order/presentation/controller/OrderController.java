package com.klp.order.presentation.controller;

import com.klp.order.application.command.CreateOrderCommand;
import com.klp.order.application.service.OrderService;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.presentation.dto.order.request.CreateOrderRequest;
import com.klp.order.presentation.dto.order.response.CreateOrderResponse;
import com.klp.order.presentation.dto.order.response.GetOneOrderResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(
        @Valid @RequestBody CreateOrderRequest request
    ) {
        CreateOrderCommand command = request.toCommand();
        Order order = orderService.createOrder(command);
        CreateOrderResponse response = CreateOrderResponse.from(order);

        URI location = URI.create("/v1/orders/" + response.orderId());

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<GetOneOrderResponse> getOrder(
        @PathVariable UUID orderId
    ) {
        Order order = orderService.findById(orderId);
        GetOneOrderResponse response = GetOneOrderResponse.from(order);

        return ResponseEntity.ok(response);
    }
}
