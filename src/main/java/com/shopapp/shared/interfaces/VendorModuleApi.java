package com.shopapp.shared.interfaces;

import java.util.Optional;

/**
 * Contract for the Vendor module - used by other modules to interact with vendor data.
 * This interface ensures loose coupling between modules.
 */
public interface VendorModuleApi {
    
    /**
     * Find a vendor by their user ID
     */
    Optional<VendorDto> findByUserId(String userId);
    
    /**
     * Find a vendor by their vendor ID
     */
    Optional<VendorDto> findById(String vendorId);
    
    /**
     * Check if a user is an approved vendor
     */
    boolean isApprovedVendor(String userId);
    
    /**
     * Get vendor ID by user ID (for approved vendors)
     */
    Optional<String> getVendorIdByUserId(String userId);
    
    /**
     * DTO for vendor data exposed to other modules
     */
    record VendorDto(
            String id,
            String userId,
            String businessName,
            String description,
            String contactEmail,
            String status
    ) {}
}
