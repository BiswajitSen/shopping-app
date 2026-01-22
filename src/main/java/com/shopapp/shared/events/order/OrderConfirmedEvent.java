package com.shopapp.shared.events.order;

import com.shopapp.shared.events.DomainEvent;
import lombok.Getter;

@Getter
public class OrderConfirmedEvent extends DomainEvent {
    
    private final String orderId;
    private final String userId;
    
    public OrderConfirmedEvent(String orderId, String userId) {
        super();
        this.orderId = orderId;
        this.userId = userId;
    }
    
    @Override
    public String getEventType() {
        return "ORDER_CONFIRMED";
    }
}
