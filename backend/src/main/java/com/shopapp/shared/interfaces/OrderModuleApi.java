package com.shopapp.shared.interfaces;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Contract for the Order module - used by other modules to interact with order data.
 * This interface ensures loose coupling between modules.
 */
public interface OrderModuleApi {
    
    /**
     * Find an order by ID
     */
    Optional<OrderDto> findById(String orderId);
    
    /**
     * Confirm an order (typically after successful payment)
     */
    void confirmOrder(String orderId);
    
    /**
     * Cancel an order (typically after failed payment)
     */
    void cancelOrder(String orderId, String reason);
    
    /**
     * DTO for order data exposed to other modules
     */
    record OrderDto(
            String id,
            String userId,
            BigDecimal totalAmount,
            String status
    ) {}
}
