package com.digitalart.order.presentation;

import com.digitalart.order.application.OrderService;
import com.digitalart.order.application.dto.CreateOrderRequest;
import com.digitalart.order.application.dto.OrderDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        OrderDto order = orderService.createOrder(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderDto>> getMyOrders(Authentication authentication) {
        String email = authentication.getName();
        List<OrderDto> orders = orderService.getMyOrders(email);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication.getName();
        OrderDto order = orderService.getOrderById(id, email);
        return ResponseEntity.ok(order);
    }
}
