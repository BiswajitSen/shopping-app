package com.shopapp.order.controller;

import com.shopapp.order.dto.CreateOrderRequest;
import com.shopapp.order.dto.OrderResponse;
import com.shopapp.order.service.OrderService;
import com.shopapp.shared.dto.ApiResponse;
import com.shopapp.shared.dto.PagedResponse;
import com.shopapp.shared.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management APIs")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create order", description = "Create a new order")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        OrderResponse order = orderService.createOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", order));
    }

    @GetMapping
    @Operation(summary = "Get my orders", description = "Get all orders for the current user")
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse>>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        String userId = SecurityUtils.getCurrentUserId();
        Sort sort = sortDir.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<OrderResponse> orders = orderService.getUserOrders(userId, pageable);
        PagedResponse<OrderResponse> response = PagedResponse.of(
                orders.getContent(), page, size, orders.getTotalElements());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID", description = "Get order details by ID")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable String orderId) {
        String userId = SecurityUtils.getCurrentUserId();
        OrderResponse order = orderService.getOrder(userId, orderId);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order", description = "Cancel an order (only if status is CREATED)")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable String orderId) {
        String userId = SecurityUtils.getCurrentUserId();
        OrderResponse order = orderService.cancelUserOrder(userId, orderId);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", order));
    }
}
