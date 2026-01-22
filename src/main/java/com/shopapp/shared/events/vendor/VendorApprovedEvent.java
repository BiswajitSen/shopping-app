package com.shopapp.shared.events.vendor;

import com.shopapp.shared.events.DomainEvent;
import lombok.Getter;

@Getter
public class VendorApprovedEvent extends DomainEvent {
    
    private final String vendorId;
    private final String userId;
    
    public VendorApprovedEvent(String vendorId, String userId) {
        super();
        this.vendorId = vendorId;
        this.userId = userId;
    }
    
    @Override
    public String getEventType() {
        return "VENDOR_APPROVED";
    }
}
