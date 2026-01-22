package com.shopapp.shared.events.product;

import com.shopapp.shared.events.DomainEvent;
import lombok.Getter;

@Getter
public class ProductApprovedEvent extends DomainEvent {
    
    private final String productId;
    private final String vendorId;
    
    public ProductApprovedEvent(String productId, String vendorId) {
        super();
        this.productId = productId;
        this.vendorId = vendorId;
    }
    
    @Override
    public String getEventType() {
        return "PRODUCT_APPROVED";
    }
}
