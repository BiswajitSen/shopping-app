package com.shopapp.shared.events.vendor;

import com.shopapp.shared.events.DomainEvent;
import lombok.Getter;

@Getter
public class VendorRejectedEvent extends DomainEvent {
    
    private final String vendorId;
    private final String userId;
    private final String reason;
    
    public VendorRejectedEvent(String vendorId, String userId, String reason) {
        super();
        this.vendorId = vendorId;
        this.userId = userId;
        this.reason = reason;
    }
    
    @Override
    public String getEventType() {
        return "VENDOR_REJECTED";
    }
}
