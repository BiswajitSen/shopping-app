package com.shopapp.order.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
public class Order {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    private BigDecimal totalAmount;

    @Indexed
    @Builder.Default
    private OrderStatus status = OrderStatus.PLACED;

    private ShippingAddress shippingAddress;

    private String cancellationReason;
    
    // Estimated delivery date set by vendor
    private LocalDate estimatedDeliveryDate;
    
    // Status update notes from vendor
    private String statusNote;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;

    // Status transition methods
    public void updateStatus(OrderStatus newStatus, String note) {
        this.status = newStatus;
        this.statusNote = note;
        
        switch (newStatus) {
            case SHIPPED -> this.shippedAt = LocalDateTime.now();
            case DELIVERED -> this.deliveredAt = LocalDateTime.now();
            case CANCELLED -> this.cancelledAt = LocalDateTime.now();
            default -> {}
        }
    }
    
    public void scheduleDelivery(LocalDate deliveryDate, String note) {
        this.status = OrderStatus.DELIVERY_SCHEDULED;
        this.estimatedDeliveryDate = deliveryDate;
        this.statusNote = note;
    }

    public void confirm() {
        this.status = OrderStatus.PREPARING;
        this.confirmedAt = LocalDateTime.now();
    }

    public void cancel(String reason) {
        this.status = OrderStatus.CANCELLED;
        this.cancellationReason = reason;
        this.cancelledAt = LocalDateTime.now();
    }

    public boolean isPlaced() {
        return status == OrderStatus.PLACED || status == OrderStatus.CREATED;
    }

    public boolean isConfirmed() {
        return status == OrderStatus.CONFIRMED || status == OrderStatus.PREPARING;
    }

    public boolean isCancelled() {
        return status == OrderStatus.CANCELLED;
    }

    public boolean isDelivered() {
        return status == OrderStatus.DELIVERED;
    }

    public boolean canBeCancelled() {
        // Can only cancel if not yet shipped
        return Set.of(OrderStatus.PLACED, OrderStatus.CREATED, OrderStatus.PREPARING)
                .contains(status);
    }
    
    public boolean canBeUpdatedByVendor() {
        // Vendor can update status until delivered or cancelled
        return !Set.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED).contains(status);
    }
}
