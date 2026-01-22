package com.shopapp.payment.service;

import com.shopapp.payment.domain.Payment;
import com.shopapp.payment.domain.PaymentStatus;
import com.shopapp.payment.dto.*;
import com.shopapp.payment.repository.PaymentRepository;
import com.shopapp.shared.events.DomainEventPublisher;
import com.shopapp.shared.events.payment.PaymentFailedEvent;
import com.shopapp.shared.events.payment.PaymentSuccessEvent;
import com.shopapp.shared.exception.BadRequestException;
import com.shopapp.shared.exception.ResourceNotFoundException;
import com.shopapp.shared.interfaces.OrderModuleApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderModuleApi orderModuleApi;
    private final DomainEventPublisher eventPublisher;
    private final Random random = new Random();

    @Transactional
    public PaymentResponse initiatePayment(String userId, InitiatePaymentRequest request) {
        log.info("Initiating payment for order: {} by user: {}", request.getOrderId(), userId);

        // Check if payment already exists for this order
        if (paymentRepository.existsByOrderId(request.getOrderId())) {
            throw new BadRequestException("Payment already initiated for this order");
        }

        // Get order details
        OrderModuleApi.OrderDto order = orderModuleApi.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", request.getOrderId()));

        // Verify order belongs to user
        if (!order.userId().equals(userId)) {
            throw new BadRequestException("Order does not belong to current user");
        }

        // Verify order is in CREATED status
        if (!"CREATED".equals(order.status())) {
            throw new BadRequestException("Order is not in a valid state for payment");
        }

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .userId(userId)
                .amount(order.totalAmount())
                .status(PaymentStatus.PENDING)
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "CARD")
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment initiated with id: {} for order: {}", savedPayment.getId(), request.getOrderId());

        return toPaymentResponse(savedPayment);
    }

    @Transactional
    public PaymentResponse processPayment(String userId, ProcessPaymentRequest request) {
        log.info("Processing payment: {} for user: {}", request.getPaymentId(), userId);

        Payment payment = paymentRepository.findByIdAndUserId(request.getPaymentId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", request.getPaymentId()));

        if (!payment.isPending()) {
            throw new BadRequestException("Payment has already been processed");
        }

        // Simulate payment processing with 90% success rate
        boolean success = request.isSimulateSuccess() || random.nextDouble() < 0.9;

        if (success) {
            String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            payment.markSuccess(transactionId);
            paymentRepository.save(payment);
            
            log.info("Payment {} succeeded with transaction id: {}", payment.getId(), transactionId);

            // Publish success event
            eventPublisher.publish(new PaymentSuccessEvent(
                    payment.getId(),
                    payment.getOrderId(),
                    payment.getUserId(),
                    payment.getAmount(),
                    transactionId
            ));
        } else {
            String failureReason = "Payment declined by payment provider";
            payment.markFailed(failureReason);
            paymentRepository.save(payment);
            
            log.info("Payment {} failed: {}", payment.getId(), failureReason);

            // Publish failure event
            eventPublisher.publish(new PaymentFailedEvent(
                    payment.getId(),
                    payment.getOrderId(),
                    payment.getUserId(),
                    payment.getAmount(),
                    failureReason
            ));
        }

        return toPaymentResponse(payment);
    }

    public PaymentResponse getPayment(String userId, String paymentId) {
        Payment payment = paymentRepository.findByIdAndUserId(paymentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));
        return toPaymentResponse(payment);
    }

    public PaymentResponse getPaymentByOrderId(String userId, String orderId) {
        Payment payment = paymentRepository.findByOrderIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId));
        return toPaymentResponse(payment);
    }

    public Page<PaymentResponse> getUserPayments(String userId, Pageable pageable) {
        return paymentRepository.findByUserId(userId, pageable)
                .map(this::toPaymentResponse);
    }

    private PaymentResponse toPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .failureReason(payment.getFailureReason())
                .paymentMethod(payment.getPaymentMethod())
                .createdAt(payment.getCreatedAt())
                .processedAt(payment.getProcessedAt())
                .build();
    }
}
