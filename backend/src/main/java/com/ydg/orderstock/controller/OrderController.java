package com.ydg.orderstock.controller;

import com.ydg.orderstock.dto.OrderItemResponse;
import com.ydg.orderstock.dto.OrderResponse;
import com.ydg.orderstock.dto.PaymentRequest;
import com.ydg.orderstock.entity.Order;
import com.ydg.orderstock.security.UserPrincipal;
import com.ydg.orderstock.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(toResponse(orderService.checkout(principal.getUser())));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> list(@AuthenticationPrincipal UserPrincipal principal) {
        List<OrderResponse> responses = orderService.listOrders(principal.getUser()).stream()
            .map(this::toResponse)
            .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderResponse> get(@AuthenticationPrincipal UserPrincipal principal,
                                             @PathVariable("id") Long id) {
        return ResponseEntity.ok(toResponse(orderService.getOrder(principal.getUser(), id)));
    }

    @PostMapping("/orders/{id}/pay")
    public ResponseEntity<OrderResponse> pay(@AuthenticationPrincipal UserPrincipal principal,
                                             @PathVariable("id") Long id,
                                             @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(toResponse(orderService.pay(principal.getUser(), id, request)));
    }

    @PostMapping("/orders/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(@AuthenticationPrincipal UserPrincipal principal,
                                                @PathVariable("id") Long id) {
        return ResponseEntity.ok(toResponse(orderService.cancel(principal.getUser(), id)));
    }

    @PostMapping("/orders/{id}/ship")
    public ResponseEntity<OrderResponse> ship(@AuthenticationPrincipal UserPrincipal principal,
                                              @PathVariable("id") Long id) {
        return ResponseEntity.ok(toResponse(orderService.ship(principal.getUser(), id)));
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
            .map(item -> new OrderItemResponse(item.getProductId(), item.getProductTitle(),
                item.getUnitPrice(), item.getQuantity()))
            .toList();
        return new OrderResponse(order.getId(), order.getStatus(), order.getTotalAmount(),
            order.getCreatedAt(), items);
    }
}
