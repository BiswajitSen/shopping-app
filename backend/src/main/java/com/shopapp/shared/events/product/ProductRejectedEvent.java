package com.shopapp.shared.events.product;

import com.shopapp.shared.events.DomainEvent;
import lombok.Getter;

@Getter
public class ProductRejectedEvent extends DomainEvent {
    
    private final String productId;
    private final String vendorId;
    private final String reason;
    
    public ProductRejectedEvent(String productId, String vendorId, String reason) {
        super();
        this.productId = productId;
        this.vendorId = vendorId;
        this.reason = reason;
    }
    
    @Override
    public String getEventType() {
        return "PRODUCT_REJECTED";
    }
}
