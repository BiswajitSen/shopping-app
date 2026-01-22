package com.shopapp.order.controller;

import com.shopapp.order.domain.OrderStatus;
import com.shopapp.order.dto.OrderResponse;
import com.shopapp.order.dto.UpdateOrderStatusRequest;
import com.shopapp.order.service.OrderService;
import com.shopapp.shared.dto.ApiResponse;
import com.shopapp.shared.dto.PagedResponse;
import com.shopapp.shared.interfaces.VendorModuleApi;
import com.shopapp.shared.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendor/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('VENDOR')")
@Tag(name = "Vendor Orders", description = "Vendor order management APIs")
public class VendorOrderController {

    private final OrderService orderService;
    private final VendorModuleApi vendorModuleApi;

    @GetMapping
    @Operation(summary = "Get vendor orders", description = "Get all orders containing items from this vendor")
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse>>> getVendorOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) OrderStatus status) {
        
        String userId = SecurityUtils.getCurrentUserId();
        String vendorId = getVendorIdForUser(userId);
        
        Sort sort = sortDir.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<OrderResponse> orders;
        if (status != null) {
            orders = orderService.getVendorOrdersByStatus(vendorId, status, pageable);
        } else {
            orders = orderService.getVendorOrders(vendorId, pageable);
        }
        
        PagedResponse<OrderResponse> response = PagedResponse.of(
                orders.getContent(), page, size, orders.getTotalElements());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get vendor order by ID", description = "Get order details for a vendor's order")
    public ResponseEntity<ApiResponse<OrderResponse>> getVendorOrder(@PathVariable String orderId) {
        String userId = SecurityUtils.getCurrentUserId();
        String vendorId = getVendorIdForUser(userId);
        
        OrderResponse order = orderService.getVendorOrder(vendorId, orderId);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PatchMapping("/{orderId}/status")
    @Operation(summary = "Update order status", description = "Update the status of an order")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable String orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        
        String userId = SecurityUtils.getCurrentUserId();
        String vendorId = getVendorIdForUser(userId);
        
        OrderResponse order = orderService.updateOrderStatus(vendorId, orderId, request);
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", order));
    }

    @GetMapping("/statuses")
    @Operation(summary = "Get available statuses", description = "Get list of available order statuses for vendors")
    public ResponseEntity<ApiResponse<OrderStatus[]>> getAvailableStatuses() {
        // Return only the statuses that vendors can set
        OrderStatus[] vendorStatuses = {
            OrderStatus.PREPARING,
            OrderStatus.SHIPPED,
            OrderStatus.OUT_FOR_DELIVERY,
            OrderStatus.DELIVERED,
            OrderStatus.DELIVERY_SCHEDULED,
            OrderStatus.CANCELLED
        };
        return ResponseEntity.ok(ApiResponse.success(vendorStatuses));
    }

    private String getVendorIdForUser(String userId) {
        return vendorModuleApi.findByUserId(userId)
                .map(VendorModuleApi.VendorDto::id)
                .orElseThrow(() -> new IllegalStateException("User is not a vendor"));
    }
}
