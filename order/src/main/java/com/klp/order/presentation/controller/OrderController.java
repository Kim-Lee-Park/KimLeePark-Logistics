package com.klp.order.presentation.controller;

import com.klp.order.application.command.CancelOrderCommand;
import com.klp.order.application.command.CreateOrderCommand;
import com.klp.order.application.command.UpdateOrderCommand;
import com.klp.order.application.service.OrderService;
import com.klp.order.domain.entity.order.Order;
import com.klp.order.presentation.dto.order.request.cancel.CancelOrderRequest;
import com.klp.order.presentation.dto.order.request.create.CreateOrderRequest;
import com.klp.order.presentation.dto.order.request.update.ChangeOrderStatusRequest;
import com.klp.order.presentation.dto.order.request.update.UpdateOrderRequest;
import com.klp.order.presentation.dto.order.response.cancel.CancelOrderResponse;
import com.klp.order.presentation.dto.order.response.create.CreateOrderResponse;
import com.klp.order.presentation.dto.order.response.delete.DeleteOrderResponse;
import com.klp.order.presentation.dto.order.response.get.GetOneOrderResponse;
import com.klp.order.presentation.dto.order.response.update.ChangeOrderStatusResponse;
import com.klp.order.presentation.dto.order.response.update.UpdateOrderResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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

    @PatchMapping("/{orderId}")
    public ResponseEntity<UpdateOrderResponse> updateOrder(
        @PathVariable UUID orderId,
        @Valid @RequestBody UpdateOrderRequest request
    ) {
        UpdateOrderCommand command = request.toCommand();
        Order order = orderService.updateOrder(orderId, command);
        UpdateOrderResponse response = UpdateOrderResponse.from(order);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<DeleteOrderResponse> deleteOrder(
        @PathVariable UUID orderId,
        @RequestHeader("X-User-Id") Long deletedBy
    ) {
        Order order = orderService.deleteOrder(orderId, deletedBy);
        DeleteOrderResponse response = DeleteOrderResponse.from(order);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<CancelOrderResponse> cancelOrder(
        @PathVariable UUID orderId,
        @RequestHeader("X-User-Id") Long cancelledBy,
        @Valid @RequestBody CancelOrderRequest request
    ) {
        CancelOrderCommand command = request.toCommand(cancelledBy);
        Order order = orderService.cancelOrder(orderId, command);
        CancelOrderResponse response = CancelOrderResponse.from(order);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ChangeOrderStatusResponse> changeOrderStatus(
        @PathVariable UUID orderId,
        @Valid @RequestBody ChangeOrderStatusRequest request
    ) {
        Order order = orderService.changeOrderStatus(orderId, request.orderStatus());
        ChangeOrderStatusResponse response = ChangeOrderStatusResponse.from(order);

        return ResponseEntity.ok(response);
    }

}
