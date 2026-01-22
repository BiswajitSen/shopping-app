package com.shopapp.order.service;

import com.shopapp.order.domain.Order;
import com.shopapp.order.domain.OrderItem;
import com.shopapp.order.domain.OrderStatus;
import com.shopapp.order.domain.ShippingAddress;
import com.shopapp.order.dto.*;
import com.shopapp.order.repository.OrderRepository;
import com.shopapp.shared.events.DomainEventPublisher;
import com.shopapp.shared.events.order.OrderConfirmedEvent;
import com.shopapp.shared.events.order.OrderCreatedEvent;
import com.shopapp.shared.events.payment.PaymentFailedEvent;
import com.shopapp.shared.events.payment.PaymentSuccessEvent;
import com.shopapp.shared.exception.BadRequestException;
import com.shopapp.shared.exception.ForbiddenException;
import com.shopapp.shared.exception.ResourceNotFoundException;
import com.shopapp.shared.interfaces.OrderModuleApi;
import com.shopapp.shared.interfaces.ProductModuleApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService implements OrderModuleApi {

    private final OrderRepository orderRepository;
    private final ProductModuleApi productModuleApi;
    private final DomainEventPublisher eventPublisher;

    // ===== OrderModuleApi Implementation =====

    @Override
    public Optional<OrderDto> findById(String orderId) {
        return orderRepository.findById(orderId)
                .map(this::toOrderDto);
    }

    @Override
    @Transactional
    public void confirmOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.isCreated()) {
            throw new BadRequestException("Only created orders can be confirmed");
        }

        order.confirm();
        orderRepository.save(order);
        log.info("Order {} confirmed", orderId);

        eventPublisher.publish(new OrderConfirmedEvent(orderId, order.getUserId()));
    }

    @Override
    @Transactional
    public void cancelOrder(String orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.canBeCancelled()) {
            throw new BadRequestException("Order cannot be cancelled in current status");
        }

        // Restore stock for cancelled items
        for (OrderItem item : order.getItems()) {
            productModuleApi.restoreStock(item.getProductId(), item.getQuantity());
        }

        order.cancel(reason);
        orderRepository.save(order);
        log.info("Order {} cancelled with reason: {}", orderId, reason);
    }

    // ===== Order Operations =====

    @Transactional
    public OrderResponse createOrder(String userId, CreateOrderRequest request) {
        log.info("Creating order for user: {}", userId);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // Validate products and calculate totals
        for (OrderItemRequest itemRequest : request.getItems()) {
            ProductModuleApi.ProductDto product = productModuleApi.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new BadRequestException(
                            "Product not found: " + itemRequest.getProductId()));

            if (!"APPROVED".equals(product.status())) {
                throw new BadRequestException("Product is not available: " + product.name());
            }

            // Try to decrement stock
            boolean stockDeducted = productModuleApi.decrementStock(
                    product.id(), itemRequest.getQuantity());
            
            if (!stockDeducted) {
                throw new BadRequestException(
                        "Insufficient stock for product: " + product.name());
            }

            OrderItem orderItem = OrderItem.create(
                    product.id(),
                    product.name(),
                    product.vendorId(),
                    itemRequest.getQuantity(),
                    product.price()
            );
            orderItems.add(orderItem);
            totalAmount = totalAmount.add(orderItem.getSubtotal());
        }

        ShippingAddress shippingAddress = toShippingAddress(request.getShippingAddress());

        Order order = Order.builder()
                .userId(userId)
                .items(orderItems)
                .totalAmount(totalAmount)
                .status(OrderStatus.CREATED)
                .shippingAddress(shippingAddress)
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("Order created with id: {} for user: {}", savedOrder.getId(), userId);

        // Publish order created event
        eventPublisher.publish(new OrderCreatedEvent(
                savedOrder.getId(), userId, totalAmount));

        return toOrderResponse(savedOrder);
    }

    public OrderResponse getOrder(String userId, String orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return toOrderResponse(order);
    }

    public Page<OrderResponse> getUserOrders(String userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable)
                .map(this::toOrderResponse);
    }

    @Transactional
    public OrderResponse cancelUserOrder(String userId, String orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.canBeCancelled()) {
            throw new BadRequestException("Order cannot be cancelled");
        }

        cancelOrder(orderId, "Cancelled by user");
        
        // Refresh the order
        order = orderRepository.findById(orderId).get();
        return toOrderResponse(order);
    }

    // ===== Event Handlers =====

    @EventListener
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        log.info("Handling PaymentSuccessEvent for order: {}", event.getOrderId());
        confirmOrder(event.getOrderId());
    }

    @EventListener
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("Handling PaymentFailedEvent for order: {}", event.getOrderId());
        cancelOrder(event.getOrderId(), "Payment failed: " + event.getFailureReason());
    }

    // ===== Helper Methods =====

    private OrderDto toOrderDto(Order order) {
        return new OrderDto(
                order.getId(),
                order.getUserId(),
                order.getTotalAmount(),
                order.getStatus().name()
        );
    }

    private OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .vendorId(item.getVendorId())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .items(itemResponses)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .cancellationReason(order.getCancellationReason())
                .createdAt(order.getCreatedAt())
                .confirmedAt(order.getConfirmedAt())
                .cancelledAt(order.getCancelledAt())
                .build();
    }

    private ShippingAddress toShippingAddress(ShippingAddressRequest request) {
        return ShippingAddress.builder()
                .fullName(request.getFullName())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .phoneNumber(request.getPhoneNumber())
                .build();
    }
}
