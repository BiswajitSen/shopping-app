package com.shopapp.order.domain;

public enum OrderStatus {
    // Initial status when order is placed
    PLACED,
    
    // Vendor is preparing the order
    PREPARING,
    
    // Order has been shipped
    SHIPPED,
    
    // Order is out for delivery
    OUT_FOR_DELIVERY,
    
    // Order has been delivered
    DELIVERED,
    
    // Delivery is scheduled for a future date
    DELIVERY_SCHEDULED,
    
    // Order was cancelled
    CANCELLED,
    
    // Legacy statuses (kept for backward compatibility)
    @Deprecated
    CREATED,
    @Deprecated
    CONFIRMED
}
