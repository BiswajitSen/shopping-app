package com.shopapp.payment.controller;

import com.shopapp.payment.dto.*;
import com.shopapp.payment.service.PaymentService;
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
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment management APIs")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    @Operation(summary = "Initiate payment", description = "Initiate payment for an order")
    public ResponseEntity<ApiResponse<PaymentResponse>> initiatePayment(
            @Valid @RequestBody InitiatePaymentRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        PaymentResponse payment = paymentService.initiatePayment(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment initiated successfully", payment));
    }

    @PostMapping("/process")
    @Operation(summary = "Process payment", description = "Process a pending payment (simulated)")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @Valid @RequestBody ProcessPaymentRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        PaymentResponse payment = paymentService.processPayment(userId, request);
        
        String message = payment.getStatus().name().equals("SUCCESS") 
                ? "Payment processed successfully" 
                : "Payment processing failed";
        
        return ResponseEntity.ok(ApiResponse.success(message, payment));
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get payment", description = "Get payment details by ID")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(@PathVariable String paymentId) {
        String userId = SecurityUtils.getCurrentUserId();
        PaymentResponse payment = paymentService.getPayment(userId, paymentId);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get payment by order", description = "Get payment details by order ID")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrder(@PathVariable String orderId) {
        String userId = SecurityUtils.getCurrentUserId();
        PaymentResponse payment = paymentService.getPaymentByOrderId(userId, orderId);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @GetMapping
    @Operation(summary = "Get my payments", description = "Get all payments for the current user")
    public ResponseEntity<ApiResponse<PagedResponse<PaymentResponse>>> getMyPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        String userId = SecurityUtils.getCurrentUserId();
        Sort sort = sortDir.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<PaymentResponse> payments = paymentService.getUserPayments(userId, pageable);
        PagedResponse<PaymentResponse> response = PagedResponse.of(
                payments.getContent(), page, size, payments.getTotalElements());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
