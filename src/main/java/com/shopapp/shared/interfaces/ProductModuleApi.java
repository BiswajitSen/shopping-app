package com.shopapp.shared.interfaces;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Contract for the Product module - used by other modules to interact with product data.
 * This interface ensures loose coupling between modules.
 */
public interface ProductModuleApi {
    
    /**
     * Find a product by ID
     */
    Optional<ProductDto> findById(String productId);
    
    /**
     * Check if a product exists and is approved
     */
    boolean isApprovedProduct(String productId);
    
    /**
     * Decrement product stock
     * @return true if stock was successfully decremented, false if insufficient stock
     */
    boolean decrementStock(String productId, int quantity);
    
    /**
     * Restore product stock (e.g., when order is cancelled)
     */
    void restoreStock(String productId, int quantity);
    
    /**
     * DTO for product data exposed to other modules
     */
    record ProductDto(
            String id,
            String name,
            String category,
            BigDecimal price,
            int stock,
            String vendorId,
            String status
    ) {}
}
