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
        if (orderId == null || orderId.trim().isEmpty()) {
            return Optional.empty();
        }

        return orderRepository.findById(orderId)
                .map(this::toOrderDto);
    }

    @Override
    @Transactional
    public void confirmOrder(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new BadRequestException("Order ID is required");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.isPlaced()) {
            throw new BadRequestException("Only placed orders can be confirmed");
        }

        order.confirm();
        orderRepository.save(order);
        log.info("Order {} confirmed", orderId);

        eventPublisher.publish(new OrderConfirmedEvent(orderId, order.getUserId()));
    }

    @Override
    @Transactional
    public void cancelOrder(String orderId, String reason) {
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new BadRequestException("Order ID is required");
        }

        if (reason == null) {
            reason = "No reason provided";
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.canBeCancelled()) {
            throw new BadRequestException("Order cannot be cancelled in current status");
        }

        // Restore stock for cancelled items
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                if (item != null && item.getProductId() != null) {
                    productModuleApi.restoreStock(item.getProductId(), item.getQuantity());
                }
            }
        }

        order.cancel(reason);
        orderRepository.save(order);
        log.info("Order {} cancelled with reason: {}", orderId, reason);
    }

    // ===== Order Operations =====

    @Transactional
    public OrderResponse createOrder(String userId, CreateOrderRequest request) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new BadRequestException("User ID is required");
        }

        if (request == null) {
            throw new BadRequestException("Order request is required");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Order must contain at least one item");
        }

        log.info("Creating order for user: {}", userId);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        // Validate products and calculate totals
        for (OrderItemRequest itemRequest : request.getItems()) {
            if (itemRequest == null) {
                throw new BadRequestException("Order item cannot be null");
            }

            if (itemRequest.getProductId() == null || itemRequest.getProductId().trim().isEmpty()) {
                throw new BadRequestException("Product ID is required for order item");
            }

            if (itemRequest.getQuantity() == null || itemRequest.getQuantity() <= 0) {
                throw new BadRequestException("Quantity must be greater than 0 for order item");
            }
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

            // Get first product image or null
            String productImage = (product.images() != null && !product.images().isEmpty()) 
                    ? product.images().get(0) 
                    : null;
            
            OrderItem orderItem = OrderItem.create(
                    product.id(),
                    product.name(),
                    productImage,
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
                .status(OrderStatus.PLACED)
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
        if (userId == null || userId.trim().isEmpty()) {
            throw new BadRequestException("User ID is required");
        }

        if (orderId == null || orderId.trim().isEmpty()) {
            throw new BadRequestException("Order ID is required");
        }

        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return toOrderResponse(order);
    }

    public Page<OrderResponse> getUserOrders(String userId, Pageable pageable) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new BadRequestException("User ID is required");
        }

        if (pageable == null) {
            throw new BadRequestException("Pageable parameter is required");
        }

        return orderRepository.findByUserId(userId, pageable)
                .map(this::toOrderResponse);
    }

    @Transactional
    public OrderResponse cancelUserOrder(String userId, String orderId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new BadRequestException("User ID is required");
        }

        if (orderId == null || orderId.trim().isEmpty()) {
            throw new BadRequestException("Order ID is required");
        }

        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.canBeCancelled()) {
            throw new BadRequestException("Order cannot be cancelled");
        }

        cancelOrder(orderId, "Cancelled by user");

        // Refresh the order
        order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return toOrderResponse(order);
    }

    // ===== Vendor Order Operations =====

    /**
     * Get all orders containing items from a specific vendor
     */
    public Page<OrderResponse> getVendorOrders(String vendorId, Pageable pageable) {
        if (vendorId == null || vendorId.trim().isEmpty()) {
            throw new BadRequestException("Vendor ID is required");
        }
        
        log.info("Fetching orders for vendor: {}", vendorId);
        return orderRepository.findByVendorId(vendorId, pageable)
                .map(this::toOrderResponse);
    }

    /**
     * Get orders for a vendor filtered by status
     */
    public Page<OrderResponse> getVendorOrdersByStatus(String vendorId, OrderStatus status, Pageable pageable) {
        if (vendorId == null || vendorId.trim().isEmpty()) {
            throw new BadRequestException("Vendor ID is required");
        }
        
        log.info("Fetching orders for vendor: {} with status: {}", vendorId, status);
        return orderRepository.findByVendorIdAndStatus(vendorId, status, pageable)
                .map(this::toOrderResponse);
    }

    /**
     * Get a specific order for a vendor
     */
    public OrderResponse getVendorOrder(String vendorId, String orderId) {
        if (vendorId == null || vendorId.trim().isEmpty()) {
            throw new BadRequestException("Vendor ID is required");
        }
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new BadRequestException("Order ID is required");
        }
        
        Order order = orderRepository.findByIdAndVendorId(orderId, vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return toOrderResponse(order);
    }

    /**
     * Update order status by vendor
     */
    @Transactional
    public OrderResponse updateOrderStatus(String vendorId, String orderId, UpdateOrderStatusRequest request) {
        if (vendorId == null || vendorId.trim().isEmpty()) {
            throw new BadRequestException("Vendor ID is required");
        }
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new BadRequestException("Order ID is required");
        }
        if (request == null || request.getStatus() == null) {
            throw new BadRequestException("Status is required");
        }

        Order order = orderRepository.findByIdAndVendorId(orderId, vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.canBeUpdatedByVendor()) {
            throw new BadRequestException("Order cannot be updated in current status: " + order.getStatus());
        }

        OrderStatus newStatus = request.getStatus();
        
        // Validate status transition
        validateStatusTransition(order.getStatus(), newStatus);

        // Handle DELIVERY_SCHEDULED specially
        if (newStatus == OrderStatus.DELIVERY_SCHEDULED) {
            if (request.getEstimatedDeliveryDate() == null) {
                throw new BadRequestException("Estimated delivery date is required for DELIVERY_SCHEDULED status");
            }
            order.scheduleDelivery(request.getEstimatedDeliveryDate(), request.getNote());
        } else {
            order.updateStatus(newStatus, request.getNote());
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Order {} status updated to {} by vendor {}", orderId, newStatus, vendorId);

        return toOrderResponse(savedOrder);
    }

    /**
     * Validate that the status transition is allowed
     */
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Define valid transitions
        boolean validTransition = switch (currentStatus) {
            case PLACED, CREATED -> newStatus == OrderStatus.PREPARING || 
                                    newStatus == OrderStatus.CANCELLED;
            // CONFIRMED is a legacy status that acts like PREPARING
            case CONFIRMED, PREPARING -> newStatus == OrderStatus.SHIPPED || 
                             newStatus == OrderStatus.DELIVERY_SCHEDULED ||
                             newStatus == OrderStatus.CANCELLED;
            case DELIVERY_SCHEDULED -> newStatus == OrderStatus.SHIPPED ||
                                       newStatus == OrderStatus.OUT_FOR_DELIVERY;
            case SHIPPED -> newStatus == OrderStatus.OUT_FOR_DELIVERY ||
                           newStatus == OrderStatus.DELIVERY_SCHEDULED;
            case OUT_FOR_DELIVERY -> newStatus == OrderStatus.DELIVERED ||
                                     newStatus == OrderStatus.DELIVERY_SCHEDULED;
            default -> false;
        };

        if (!validTransition) {
            throw new BadRequestException(
                    String.format("Cannot transition from %s to %s", currentStatus, newStatus));
        }
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
                        .productImage(item.getProductImage())
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
                .statusNote(order.getStatusNote())
                .estimatedDeliveryDate(order.getEstimatedDeliveryDate())
                .createdAt(order.getCreatedAt())
                .confirmedAt(order.getConfirmedAt())
                .cancelledAt(order.getCancelledAt())
                .shippedAt(order.getShippedAt())
                .deliveredAt(order.getDeliveredAt())
                .build();
    }

    private ShippingAddress toShippingAddress(ShippingAddressRequest request) {
        if (request == null) {
            throw new BadRequestException("Shipping address is required");
        }

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
